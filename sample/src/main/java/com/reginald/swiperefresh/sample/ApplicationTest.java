package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


import com.reginald.swiperefresh.sample.reflect.Reflect;
import com.reginald.swiperefresh.sample.utils.CommonUtils;

import java.lang.reflect.Field;

/**
 * Created by baidu on 15/6/16.
 */
public class ApplicationTest extends Application {

    public static final String LOG_TAG = "TaskTest(Application)";

    static Context getContextImpl(Context context) {
        Context nextContext;
        while ((context instanceof ContextWrapper) &&
                (nextContext=((ContextWrapper)context).getBaseContext()) != null) {
            context = nextContext;
        }
        return context;
    }

    private void replaceIntrumentation(Context contextImpl){
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



        InstrumentTest newInstrumentation = new InstrumentTest(instrumentation);

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
        Log.d(LOG_TAG," replaceIntrumentation ok! ");

    }


    public class InstrumentTest extends Instrumentation {

        Instrumentation base;
        Reflect instrumentRef;

        public InstrumentTest(Instrumentation base) {
            this.base = base;
            instrumentRef = Reflect.on(base);
        }

        /**
         * @Override
         */
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {

            Log.i(LOG_TAG, "execStartActivity()");
            launchActivity(who, intent);
            return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode, options).get();
        }

        /**
         * @Override
         */
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode) {
            Log.i(LOG_TAG, "execStartActivity()");
            launchActivity(who, intent);
            return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode).get();

        }


        @Override
        public void callActivityOnCreate(Activity activity, Bundle bundle) {
            Log.d(LOG_TAG, activity + "callActivityOnCreate()" + getSystemInfo(ApplicationTest.this));
            connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_CREATE);
            super.callActivityOnCreate(activity, bundle);
        }

        @Override
        public void callActivityOnNewIntent(Activity activity, Intent intent) {
            Log.d(LOG_TAG, activity + "callActivityOnNewIntent()" + getSystemInfo(ApplicationTest.this));
            connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_NEW_INTENT);
            super.callActivityOnNewIntent(activity, intent);
        }

        @Override
        public void callActivityOnRestart(Activity activity){
            Log.d(LOG_TAG, activity + "callActivityOnRestart()" + getSystemInfo(ApplicationTest.this));
            connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_RESTART);
            super.callActivityOnRestart(activity);
        }

        @Override
        public void callActivityOnStart(Activity activity){
            Log.d(LOG_TAG, activity + "callActivityOnStart()" + getSystemInfo(ApplicationTest.this));
            super.callActivityOnStart(activity);
        }

        @Override
        public void callActivityOnResume(Activity activity){
            Log.d(LOG_TAG, activity + "callActivityOnResume()" + getSystemInfo(ApplicationTest.this));
            connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_RESUME);
            super.callActivityOnResume(activity);
        }

        @Override
        public void callActivityOnPause(Activity activity){
            Log.d(LOG_TAG, activity + "callActivityOnPause()" + getSystemInfo(ApplicationTest.this));
            super.callActivityOnPause(activity);
        }

        @Override
        public void callActivityOnStop(Activity activity){
            Log.d(LOG_TAG, activity + "callActivityOnStop()" + getSystemInfo(ApplicationTest.this));
            super.callActivityOnStop(activity);
        }

        @Override
        public void callActivityOnDestroy(Activity activity)  {
            Log.d(LOG_TAG, activity + "callActivityOnDestroy()" + getSystemInfo(ApplicationTest.this));
            connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_DESTROY);
            super.callActivityOnDestroy(activity);
        }

    }


    public static String getSystemInfo(Context context){
        String currentProcName = "unknown";
        int pid = android.os.Process.myPid();
        long tid = Thread.currentThread().getId();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
        Log.d(LOG_TAG, " ApplicationTest attachBaseContext() context base = " + base + getSystemInfo(this));

        Context contextImpl = getContextImpl(this);
        replaceIntrumentation(contextImpl);
       // registerActivityLifecycleCallbacks(this);
    }

//    public void waitForConnectingService(long timeoutMs) {
//
//        if (taskLoggerMessenger != null)
//            return;
//
//        if (OsUtils.getProcessName(this, android.os.Process.myPid()).equals("com.reginald.swiperefresh.sample:taskLogger"))
//            return;
//
//        connectServiceIfNeeded();
//
//        long timeElapsed = 0;
//        while (true) {
//            Log.d(LOG_TAG, "checking status: " + taskLoggerMessenger);
//            if (taskLoggerMessenger != null) {
//                Log.d(LOG_TAG, "service connected!");
//                return;
//            }
//            if (timeoutMs >= 0 && timeElapsed >= timeoutMs) {
//                Log.d(LOG_TAG, "service connection timeout!");
//                return;
//            }
//            connectServiceIfNeeded();
//            SystemClock.sleep(100);
//            timeElapsed += 100;
//        }
//
//    }
//
//    private void connectServiceIfNeeded() {
//        if (taskLoggerMessenger != null) {
//            return; // service is running
//        }
//
//        Intent intent = new Intent(this, CommonUtils.TaskLoggerService.class);
//        Log.d(LOG_TAG, "connect service...");
//        if (!bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
//            Log.e(LOG_TAG, "cannot connect");
//        }
//    }
//
//    ServiceConnection connection = new ServiceConnection() {
//        private boolean mConnectLost = false;
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.d(LOG_TAG, "service disconnected: " + name + ", connLost: " + mConnectLost + getSystemInfo());
//            if (mConnectLost) {
//                return;
//            }
//            mConnectLost = true;
//            taskLoggerMessenger = null;
//            ApplicationTest.this.unbindService(this);
//        }
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.d(LOG_TAG, "service connected: " + name + ", binder: " + service
//                    + ", connLost: " + mConnectLost);
//            Log.d(LOG_TAG, "onServiceConnected()" + getSystemInfo());
//            if (!mConnectLost) {
//                taskLoggerMessenger = new Messenger(service);
//            }
//        }
//    };


     void connectService(Activity activity, int flag){
        Intent intent = new Intent(this, CommonUtils.TaskLoggerService.class);
        CommonUtils.TaskActivity taskActivity = new CommonUtils.TaskActivity(activity);
        Message msg = Message.obtain();
        msg.what = flag;
        Bundle bundle = new Bundle();
        bundle.putParcelable("TaskActivity",taskActivity);
        msg.setData(bundle);
        intent.putExtra("message", msg);
        startService(intent);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, " ApplicationTest onCreate() " + getSystemInfo(this));
        super.onCreate();

    }

    @Override
    public void onTerminate(){
        Log.d(LOG_TAG, " ApplicationTest onTerminate() " + getSystemInfo(this));
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Log.d(LOG_TAG, " ApplicationTest onLowMemory() " + getSystemInfo(this));
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(LOG_TAG, " ApplicationTest onTrimMemory() " + getSystemInfo(this));
        super.onTrimMemory(level);
    }


//
//    @Override
//    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
//        Log.d(LOG_TAG, activity + "onActivityCreated() " + getSystemInfo());
//        connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_CREATE);
//    }
//
//    @Override
//    public void onActivityStarted(Activity activity) {
//        Log.d(LOG_TAG, activity + "onActivityStarted()" + getSystemInfo());
//    }
//
//    @Override
//    public void onActivityResumed(Activity activity) {
//        Log.d(LOG_TAG, activity + "onActivityResumed()" + getSystemInfo());
//        connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_RESUME);
//    }
//
//    @Override
//    public void onActivityPaused(Activity activity) {
//        Log.d(LOG_TAG, activity + "onActivityPaused()" + getSystemInfo());
//    }
//
//    @Override
//    public void onActivityStopped(Activity activity) {
//        Log.d(LOG_TAG, activity + "onActivityStopped()" + getSystemInfo());
//    }
//
//    @Override
//    public void onActivitySaveInstanceState(Activity activity, Bundle outState){
//            Log.d(LOG_TAG, activity + "onActivitySaveInstanceState()" + getSystemInfo());
//    }
//
//    @Override
//    public void onActivityDestroyed(Activity activity) {
//        Log.d(LOG_TAG, activity + "onActivityDestroyed()" + getSystemInfo());
//        connectService(activity, CommonUtils.TaskLoggerService.MSG_ON_ACTIVITY_DESTROY);
//    }



    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        launchActivity(this, intent);
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
