package com.reginald.tasklogger;

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
import android.util.Log;

import com.reginald.tasklogger.reflect.Reflect;

import static com.reginald.tasklogger.TaskLoggerService.TaskActivity;

public class TaskLoggerApplication extends Application {

    public static final String TASK_TAG = "$tasklogger$";
    public static final String DEBUG_TAG = "$debug$Application";

    static Context getContextImpl(Context context) {
        Context nextContext;
        while ((context instanceof ContextWrapper) &&
                (nextContext = ((ContextWrapper) context).getBaseContext()) != null) {
            context = nextContext;
        }
        return context;
    }

    public static String getSystemInfo(Context context) {
        String currentProcName = "unknown";
        int pid = android.os.Process.myPid();
        long tid = Thread.currentThread().getId();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                currentProcName = processInfo.processName;
                break;
            }
        }
        return " in process " + currentProcName + "(" + pid + ") thread " + tid;
    }



    private void replaceIntrumentation(Context contextImpl) {

        Reflect contextImplRef = Reflect.on(contextImpl);
        Reflect activityThreadRef = contextImplRef.field("mMainThread");
        Reflect instrumentationRef = activityThreadRef.field("mInstrumentation");
        TaskLoggerInstrumentation newInstrumentation = new TaskLoggerInstrumentation((Instrumentation)instrumentationRef.get());
        activityThreadRef.set("mInstrumentation", newInstrumentation);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(DEBUG_TAG, " TaskLoggerApplication attachBaseContext() context base = " + base + getSystemInfo(this));

        Context contextImpl = getContextImpl(this);
        replaceIntrumentation(contextImpl);
    }

    void connectService(TaskActivity taskActivity, int flag) {
        Intent intent = new Intent(this, TaskLoggerService.class);
        intent.putExtra("TaskActivity", taskActivity);
        intent.putExtra("flag", flag);
        startService(intent);
    }

    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, " TaskLoggerApplication onCreate() " + getSystemInfo(this));
        super.onCreate();

    }

    @Override
    public void onTerminate() {
        Log.d(DEBUG_TAG, " TaskLoggerApplication onTerminate() " + getSystemInfo(this));
        super.onTerminate();
    }
    
    @Override
    public void onLowMemory() {
        Log.d(DEBUG_TAG, " TaskLoggerApplication onLowMemory() " + getSystemInfo(this));
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(DEBUG_TAG, " TaskLoggerApplication onTrimMemory() level = " + level + getSystemInfo(this));
        super.onTrimMemory(level);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        launchActivity(this, intent);
    }

    public void launchActivity(Context context, Intent intent) {
        ResolveInfo info = context.getPackageManager().resolveActivity(intent, 0);
        connectService(new TaskActivity(context.toString(), info.activityInfo, intent),TaskLoggerService.MSG_LAUNCH_ACTIVITY);
    }

    public class TaskLoggerInstrumentation extends Instrumentation {

        Instrumentation base;
        Reflect instrumentRef;

        public TaskLoggerInstrumentation(Instrumentation base) {
            this.base = base;
            instrumentRef = Reflect.on(base);
        }

        /**
         * @Override
         */
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {

            Log.i(DEBUG_TAG, "execStartActivity()");
            launchActivity(who, intent);
            return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode, options).get();
        }

        /**
         * @Override
         */
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode) {
            Log.i(DEBUG_TAG, "execStartActivity()");
            launchActivity(who, intent);
            return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode).get();

        }


        @Override
        public void callActivityOnCreate(Activity activity, Bundle bundle) {

            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_CREATE);
            super.callActivityOnCreate(activity, bundle);
        }

        @Override
        public void callActivityOnNewIntent(Activity activity, Intent intent) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_NEW_INTENT);
            super.callActivityOnNewIntent(activity, intent);
        }

        @Override
        public void callActivityOnRestart(Activity activity) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_RESTART);
            super.callActivityOnRestart(activity);
        }

        @Override
        public void callActivityOnStart(Activity activity) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_START);
            super.callActivityOnStart(activity);
        }

        @Override
        public void callActivityOnResume(Activity activity) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_RESUME);
            super.callActivityOnResume(activity);
        }

        @Override
        public void callActivityOnPause(Activity activity) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_PAUSE);
            super.callActivityOnPause(activity);
        }

        @Override
        public void callActivityOnStop(Activity activity) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_STOP);
            super.callActivityOnStop(activity);
        }

        @Override
        public void callActivityOnDestroy(Activity activity) {
            connectService(new TaskActivity(activity), TaskLoggerService.MSG_ON_ACTIVITY_DESTROY);
            super.callActivityOnDestroy(activity);
        }

    }
}
