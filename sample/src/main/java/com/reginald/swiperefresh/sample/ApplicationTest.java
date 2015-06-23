package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;


import com.reginald.swiperefresh.sample.reflect.Reflect;
import com.reginald.swiperefresh.sample.utils.CommonUtils;
import com.reginald.swiperefresh.sample.utils.OsUtils;

import java.lang.reflect.Field;

/**
 * Created by baidu on 15/6/16.
 */
public class ApplicationTest extends Application implements Application.ActivityLifecycleCallbacks {

    public static final String LOG_TAG = "TaskTest(Application)";
    Messenger taskLoggerMessenger;
    CommonUtils.TaskLoggerService taskLoggerService;

    static Context getContextImpl(Context context) {
        Context nextContext;
        while ((context instanceof ContextWrapper) &&
                (nextContext=((ContextWrapper)context).getBaseContext()) != null) {
            context = nextContext;
        }
        return context;
    }

    private void replaceIntrument(Context contextImpl){
        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.app.ContextImpl");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Field activityThreadField = null;
        try {
            activityThreadField = clazz.getDeclaredField("mMainThread");
            activityThreadField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        Object activityThread = null;
        try {
            activityThread = activityThreadField.get(contextImpl);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }



        try {
            clazz = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Field instrumentField = null;
        try {
            instrumentField = clazz.getDeclaredField("mInstrumentation");
            instrumentField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        Instrumentation instrumentation = null;
        try {
            instrumentation = (Instrumentation)instrumentField.get(activityThread);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        Log.d(LOG_TAG, "instrumentation = " + instrumentation);



        InstrumentTest newInstrumentation = new InstrumentTest(instrumentation, taskLoggerMessenger);

        try {
            instrumentField.set(activityThread,newInstrumentation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        instrumentation = null;
        try {
            instrumentation = (Instrumentation)instrumentField.get(activityThread);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }


//        Log.d(LOG_TAG," SHOW ALL METHODS IN  Instrumentation start --------------------------- ");
//        Method[] methods = Instrumentation.class.getDeclaredMethods();
//        for (Method method: methods){
//            Reflect.showMethod(method);
//        }
//        Log.d(LOG_TAG," SHOW ALL METHODS IN  Instrumentation:  end --------------------------- ");

        Log.d(LOG_TAG, "newInstrumentation = " + instrumentation);
        Log.d(LOG_TAG," replaceIntrument ok! ");

    }


    public static class InstrumentTest extends Instrumentation {

        Instrumentation base;
        Reflect instrumentRef;
        Messenger taskLoggerMessenger;
        public InstrumentTest(Instrumentation base, Messenger taskLoggerMessenger){
            this.base = base;
            instrumentRef = Reflect.on(base);
            this.taskLoggerMessenger = taskLoggerMessenger;
        }

        @Override
        public String toString(){
            Log.d(LOG_TAG, "InstrumentTest.toString()");
            return super.toString();
        }

        /**@Override*/
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {

            Log.i(LOG_TAG, "execStartActivity()");
            launchActivity(who,intent, taskLoggerMessenger);
            //String className = componentName.getClassName();
            //Log.i(LOG_TAG, "Jump to " + className);
            return instrumentRef.call("execStartActivity",who,contextThread,token,target,intent,requestCode,options).get();
        }

        /**@Override*/
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode) {
            Log.i(LOG_TAG, "execStartActivity()");
            launchActivity(who,intent, taskLoggerMessenger);
            //String className = componentName.getClassName();
            //Log.i(LOG_TAG, "Jump to " + className);
            return instrumentRef.call("execStartActivity",who,contextThread,token,target,intent,requestCode).get();

        }

    }






    public static void checkInstrInActivity(Activity activity){

        Field instrumentField = null;
        try {
            instrumentField = Activity.class.getDeclaredField("mInstrumentation");
            instrumentField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        Instrumentation instrumentation = null;
        try {
            instrumentation = (Instrumentation)instrumentField.get(activity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }


        Log.d(LOG_TAG, "checkInstrInActivity() instrumentation = " + instrumentation.toString());
    }

    public String getSystemInfo(){
        String currentProcName = "unknown";
        int pid = android.os.Process.myPid();
        long tid = Thread.currentThread().getId();
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses())
        {
            if (processInfo.pid == pid)
            {
                currentProcName = processInfo.processName;
                break;
            }
        }
        return " in process " + currentProcName + "("  + pid + ") thread " + tid;
    }

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        Log.d(LOG_TAG, " ApplicationTest attachBaseContext() context base = " + base + getSystemInfo());

        Context contextImpl = getContextImpl(this);
        replaceIntrument(contextImpl);
        registerActivityLifecycleCallbacks(this);
    }

    public void waitForConnectingService(long timeoutMs) {

        if (taskLoggerMessenger != null)
            return;

        if (OsUtils.getProcessName(this, android.os.Process.myPid()).equals("com.reginald.swiperefresh.sample:taskLogger"))
            return;

        connectServiceIfNeeded();

        long timeElapsed = 0;
        while (true) {
            Log.d(LOG_TAG, "checking status: " + taskLoggerMessenger);
            if (taskLoggerMessenger != null) {
                Log.d(LOG_TAG, "service connected!");
                return;
            }
            if (timeoutMs >= 0 && timeElapsed >= timeoutMs) {
                Log.d(LOG_TAG, "service connection timeout!");
                return;
            }
            connectServiceIfNeeded();
            SystemClock.sleep(100);
            timeElapsed += 100;
        }

    }

    private void connectServiceIfNeeded() {
        if (taskLoggerMessenger != null) {
            return; // service is running
        }

        Intent intent = new Intent(this, CommonUtils.TaskLoggerService.class);



        Log.d(LOG_TAG, "connect service...");
        if (!bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            Log.e(LOG_TAG, "cannot connect");
        }
    }

    ServiceConnection connection = new ServiceConnection() {
        private boolean mConnectLost = false;

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "service disconnected: " + name + ", connLost: " + mConnectLost + getSystemInfo());
            if (mConnectLost) {
                return;
            }
            mConnectLost = true;
            taskLoggerMessenger = null;
            ApplicationTest.this.unbindService(this);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "service connected: " + name + ", binder: " + service
                    + ", connLost: " + mConnectLost);
            Log.d(LOG_TAG, "onServiceConnected()" + getSystemInfo());
            if (!mConnectLost) {
                taskLoggerMessenger = new Messenger(service);
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, " ApplicationTest onCreate() " + getSystemInfo());
        super.onCreate();

    }

    @Override
    public void onTerminate(){
        Log.d(LOG_TAG, " ApplicationTest onTerminate() " + getSystemInfo());
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Log.d(LOG_TAG, " ApplicationTest onLowMemory() " + getSystemInfo());
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(LOG_TAG, " ApplicationTest onTrimMemory() " + getSystemInfo());
        super.onTrimMemory(level);
    }



    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        Log.d(LOG_TAG, activity + "onActivityCreated() " + getSystemInfo());

        if (taskLoggerMessenger != null)
            CommonUtils.TaskLoggerService.onActivityCreate(taskLoggerMessenger, activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(LOG_TAG, activity + "onActivityStarted()" + getSystemInfo());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(LOG_TAG, activity + "onActivityResumed()" + getSystemInfo());
        if (taskLoggerMessenger != null)
            CommonUtils.TaskLoggerService.onActivityResume(taskLoggerMessenger, activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(LOG_TAG, activity + "onActivityPaused()" + getSystemInfo());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(LOG_TAG, activity + "onActivityStopped()" + getSystemInfo());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState){
            Log.d(LOG_TAG, activity + "onActivitySaveInstanceState()" + getSystemInfo());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(LOG_TAG, activity + "onActivityDestroyed()" + getSystemInfo());
        CommonUtils.TaskLoggerService.onActivityDestroy(taskLoggerMessenger, activity);
    }



    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        launchActivity(this, intent, taskLoggerMessenger);
    }

    public static void launchActivity(Context context, Intent intent, Messenger taskLoggerMessenger){
        intent.putExtra("m", taskLoggerMessenger);
        launchActivity(context,intent);
    }

    public static void launchActivity(Context context, Intent intent){

            ResolveInfo info =  context.getPackageManager().resolveActivity(intent, 0);
            String activityName = info.activityInfo.name;
            String launchMode = CommonUtils.getLaunchMode(info.activityInfo.launchMode);
            String flags = CommonUtils.getFlags(intent.getFlags());
            String taskAffinity = info.activityInfo.taskAffinity;
            Log.d(LOG_TAG, "startActivity " + context.getClass().getSimpleName() + " -> " + activityName);
            Log.d(LOG_TAG,"launch mode = " + launchMode + " ,flags = " + flags + " ,taskAffinity = " + taskAffinity + " ,process = " + info.activityInfo.processName);

    }
}
