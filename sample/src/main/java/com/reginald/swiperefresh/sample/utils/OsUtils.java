package com.reginald.swiperefresh.sample.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;


import java.io.File;
import java.util.List;


public class OsUtils {
    private static final String TAG = "OsUtils";


    /**
     * Check if a process is still alive.
     */
    public static boolean isProcessAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public static boolean waitNonChildProcess(int pid, long timeoutMs) {
        long expireTime = System.currentTimeMillis() + timeoutMs;
        while (true) {
            Log.d(TAG, "checking pid state: " + pid);
            File procFile = new File("/proc/" + pid);
            if (!procFile.exists()) {
                return true;
            }
            if (System.currentTimeMillis() >= expireTime) {
                return false;
            }
            try {
                // Sleep 100 milliseconds
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    /**
     * TODO tyc
     * Sleep for a while, without dealing with the exception.
     * @param time The time to sleep, in milliseconds.
     */
    public static void sleep(long time) {
        SystemClock.sleep(time);
    }

    /**
     * For debug purpose
     */
    public static String getCallstack() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Thread name: ").append(Thread.currentThread().getName()).append('\n');
        for (StackTraceElement element : stack) {
            strBuilder.append("\tat ").append(element.toString()).append('\n');
        }
        return strBuilder.toString();
    }

    /**
     * @return null may be returned if the specified process not found
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
