package com.reginald.swiperefresh.sample.tasklogger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by baidu on 15/6/9.
 */
public class TaskLoggerService extends Service implements Handler.Callback {

    public static final String TASK_TAG = "$tasklogger$";
    public static final String DEBUG_TAG = "$debug$Service";

    public static final int MSG_ON_ACTIVITY_CREATE = 1;
    public static final int MSG_ON_ACTIVITY_NEW_INTENT = 2;
    public static final int MSG_ON_ACTIVITY_RESTART = 3;
    public static final int MSG_ON_ACTIVITY_START = 4;
    public static final int MSG_ON_ACTIVITY_RESUME = 5;
    public static final int MSG_ON_ACTIVITY_PAUSE = 6;
    public static final int MSG_ON_ACTIVITY_STOP = 7;
    public static final int MSG_ON_ACTIVITY_DESTROY = 8;
    public static final int MSG_LAUNCH_ACTIVITY = 9;

    public static final SparseArray<String> STATE_MAP = new SparseArray<String>();
    static {
        STATE_MAP.put(MSG_ON_ACTIVITY_CREATE,"onCreate()");
        STATE_MAP.put(MSG_ON_ACTIVITY_NEW_INTENT,"onNewIntent()");
        STATE_MAP.put(MSG_ON_ACTIVITY_RESTART,"onRestart()");
        STATE_MAP.put(MSG_ON_ACTIVITY_START,"onStart()");
        STATE_MAP.put(MSG_ON_ACTIVITY_RESUME,"onResume()");
        STATE_MAP.put(MSG_ON_ACTIVITY_PAUSE,"onPause()");
        STATE_MAP.put(MSG_ON_ACTIVITY_STOP,"onStop()");
        STATE_MAP.put(MSG_ON_ACTIVITY_DESTROY,"onDestroy()");
    }

    private Map<Integer, List<TaskActivity>> taskLists;
    private int foregroundTaskId;
    private volatile Looper mServiceLooper;
    private volatile Handler mServiceHandler;

    public TaskLoggerService() {
        Log.d(DEBUG_TAG, " new TaskLoggerService()");
    }

    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "TaskLoggerService.onCreate()");
        super.onCreate();
        taskLists = new HashMap<>();
        HandlerThread thread = new HandlerThread("TaskLoggerService");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new Handler(mServiceLooper,this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mServiceLooper.quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "TaskLoggerService.onStartCommand()");
        TaskActivity activity = intent.getParcelableExtra("TaskActivity");
        if (activity != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.what = intent.getIntExtra("flag", -1);
            msg.obj  = activity;
            mServiceHandler.sendMessage(msg);
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG_TAG, "TaskLoggerService.onBind()");
        return null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(DEBUG_TAG, "handleMessage:" + msg.what + " in process " + android.os.Process.myPid() + " thread " + Thread.currentThread().getId());

        if (msg == null || msg.obj == null)
            return false;

        checkTaskLists();

        TaskActivity taskActivity = (TaskActivity)msg.obj;
        showActivityState(taskActivity, msg.what);


        switch (msg.what) {
            case MSG_ON_ACTIVITY_CREATE: {
                List<TaskActivity> list = taskLists.get(taskActivity.taskId);
                if (list == null) {
                    list = new ArrayList<TaskActivity>();
                    taskLists.put(taskActivity.taskId, list);
                    //taskName = taskActivity.taskName;
                } else {
                    taskActivity.taskName = list.get(0).taskName;
                }
                foregroundTaskId = taskActivity.taskId;
                list.add(taskActivity);
                break;
            }
            case MSG_ON_ACTIVITY_DESTROY: {
                String activity = taskActivity.activity;
                int taskId = taskActivity.taskId;

                List<TaskActivity> list = taskLists.get(taskId);
                int index = list.indexOf(new TaskActivity(taskId, activity, null));
                if (index != -1) {
                    list.remove(index);
                    if (list.size() == 0)
                        taskLists.remove(taskId);
                }

                if (taskLists.size() == 0) {
                    foregroundTaskId = 0;
                }

                print();
                break;
            }
            case MSG_ON_ACTIVITY_NEW_INTENT:
            case MSG_ON_ACTIVITY_RESTART:
            case MSG_ON_ACTIVITY_RESUME: {
                foregroundTaskId = taskActivity.taskId;
                print();
                break;
            }
            case MSG_LAUNCH_ACTIVITY:{
                launchActivity(taskActivity);
                break;
            }

            default:
                break;
        }

        return true;
    }


    public void showActivityState(TaskActivity taskActivity, int state){
        if (STATE_MAP.get(state) != null){
            Log.d(TASK_TAG, taskActivity.activity + " " + STATE_MAP.get(state) +
                    " in process " + taskActivity.processName + "(" + taskActivity.pid + ")");
        }
    }

    public void launchActivity(TaskActivity taskActivity) {
        Intent intent = taskActivity.intent;
        ActivityInfo activityInfo = taskActivity.activityInfo;

        String activityName = activityInfo.name;
        String launchMode = getLaunchMode(activityInfo.launchMode);
        String intentFlags = getFlags(intent.getFlags());
        String taskAffinity = activityInfo.taskAffinity;
        String targetActivity = activityInfo.targetActivity;

        Log.d(TASK_TAG, String.format("startActivity %s  ->  %s%s", taskActivity.invoker, activityName, (targetActivity == null ? "":"(" + targetActivity + ")")));
        Log.d(TASK_TAG, String.format("launch mode = %s ,intentFlags = %s ,taskAffinity = %s ,process = %s", launchMode, intentFlags, taskAffinity, activityInfo.processName));
    }




    private void checkTaskLists(){
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskList = activityManager.getRunningTasks(100);

        for (int taskId: taskLists.keySet()){
            boolean taskIsRunning = false;
            for (ActivityManager.RunningTaskInfo task : runningTaskList) {
                if (taskId == task.id) {
                    Log.d(DEBUG_TAG,"checkTaskLists(): task " + taskId + " is running. base = " + task.baseActivity + " top = " + task.topActivity + " size = " + task.numActivities);
                    taskIsRunning = true;
                    break;
                }
            }
            if (!taskIsRunning) {
                Log.w(DEBUG_TAG,"checkTaskLists(): task " + taskId + " is not running! REMOVED from taskLists");
                taskLists.remove(taskId);
            }


        }
    }

    private void print() {

        Log.d(TASK_TAG, "$$$$$$$$$$$$$ task info: ");

        StringBuilder msg = new StringBuilder("");
        for (HashMap.Entry<Integer, List<TaskActivity>> entry : taskLists.entrySet()) {
            int taskId = entry.getKey();
            List<TaskActivity> list = entry.getValue();
            TaskActivity baseActivity = list.get(0);
            String taskName = baseActivity.taskName;
            if (foregroundTaskId == taskId) {
                Log.d(TASK_TAG, "###############  * task " + taskName + "(" + taskId + ")  size = " + list.size());
                msg.append(String.format("## * task %3s  \n", taskName + "(" + taskId + ")"));
            } else {
                Log.d(TASK_TAG, "###############    task " + taskName + "(" + taskId + ") size = " + list.size());
                msg.append(String.format("##   task %3s  \n", taskName + "(" + taskId + ")"));
            }


            ListIterator<TaskActivity> reverseIterator = list.listIterator(list.size());
            while (reverseIterator.hasPrevious()) {
                TaskActivity ta = reverseIterator.previous();
                msg.append(ta.text() + "\n");
                Log.d(TASK_TAG, ta.text());
                msg.append("  ^  \n");
                Log.d(TASK_TAG, "           ^             ");
            }
        }

        Log.d(TASK_TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    public static class TaskActivity implements Parcelable {
        public final static Creator<TaskActivity> CREATOR = new Creator<TaskActivity>() {
            @Override
            public TaskActivity createFromParcel(Parcel source) {
                return new TaskActivity(source);
            }

            @Override
            public TaskActivity[] newArray(int size) {
                return new TaskActivity[size];
            }
        };
        public String activity;
        public int taskId;
        public String taskName;
        public ActivityInfo activityInfo;
        public Intent intent;

        public int pid;
        public String processName;

        // only used for launch activity
        public String invoker;


        public TaskActivity(Activity a) {
            activity = TaskActivity.contextToString(a);
            taskId = a.getTaskId();
            intent = a.getIntent();
            ResolveInfo info = a.getPackageManager().resolveActivity(intent, 0);
            activityInfo = info.activityInfo;
            taskName = activityInfo.taskAffinity;

            pid = android.os.Process.myPid();
            processName = getProcessName(pid,a);
        }

        public TaskActivity(int taskId, String activity, String name) {
            this.taskId = taskId;
            this.activity = activity;
            this.taskName = name;
        }

        public TaskActivity(String src, ActivityInfo activityInfo, Intent intent) {
            this.activityInfo = activityInfo;
            this.invoker = src;
            this.intent = intent;
        }

        public TaskActivity(Parcel parcel) {
            activity = parcel.readString();
            taskId = parcel.readInt();
            taskName = parcel.readString();
            activityInfo = parcel.readParcelable(ActivityInfo.class.getClassLoader());
            invoker = parcel.readString();
            intent = parcel.readParcelable(Intent.class.getClassLoader());

            pid = parcel.readInt();
            processName = parcel.readString();
        }

        public static String contextToString(Context a) {
            return a.getClass().getName() + " (" + Integer.toHexString(a.hashCode()) + ")";
        }

        public String toString() {
            return "[ activity = " + activity + " , taskId = " + taskId + " , taskName = " + taskName + " ]";
        }

        public String text(){
            return  String.format(" [ %s  launchMode= %s, flags= %s, options= %s process= %s(%s) ]",
                    activity, getLaunchMode(activityInfo.launchMode), getFlags(intent.getFlags()), getOptions(activityInfo.flags), processName, pid);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (((Object) this).getClass() != obj.getClass()) return false;
            TaskActivity other = (TaskActivity) obj;

            if (this.activity.equals(other.activity) && this.taskId == other.taskId)
                return true;
            else
                return false;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(activity);
            dest.writeInt(taskId);
            dest.writeString(taskName);
            dest.writeParcelable(activityInfo, flags);
            dest.writeString(invoker);
            dest.writeParcelable(intent, flags);
            dest.writeInt(pid);
            dest.writeString(processName);
        }
    }






    public static String getFlags(int flags) {
        String res = "";
        if ((flags & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            res += "NEW_TASK ";
            flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
        }
        if ((flags & Intent.FLAG_ACTIVITY_MULTIPLE_TASK) != 0) {
            res += "MULTIPLE_TASK ";
            flags &= ~Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        }
        if ((flags & Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0) {
            res += "SINGLE_TOP ";
            flags &= ~Intent.FLAG_ACTIVITY_SINGLE_TOP;
        }
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0) {
            res += "CLEAR_TOP ";
            flags &= ~Intent.FLAG_ACTIVITY_CLEAR_TOP;
        }
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0) {
            res += "CLEAR_TASK ";
            flags &= ~Intent.FLAG_ACTIVITY_CLEAR_TASK;
        }

        if (flags != 0){
            res += Integer.toHexString(flags);
        }

        return res;
    }

    public static String getOptions(int options) {
        String res = "";
        if ((options & ActivityInfo.FLAG_FINISH_ON_TASK_LAUNCH) != 0) {
            res += "finishOnTaskLaunch ";
            options &= ~ActivityInfo.FLAG_FINISH_ON_TASK_LAUNCH;
        }
        if ((options & ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH) != 0) {
            res += "clearTaskOnLaunch ";
            options &= ~ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH;
        }
        if ((options & ActivityInfo.FLAG_ALWAYS_RETAIN_TASK_STATE) != 0) {
            res += "alwaysRetainTaskState ";
            options &= ~ActivityInfo.FLAG_ALWAYS_RETAIN_TASK_STATE;
        }
        if ((options & ActivityInfo.FLAG_ALLOW_TASK_REPARENTING) != 0) {
            res += "allowTaskReparenting ";
            options &= ~ActivityInfo.FLAG_ALLOW_TASK_REPARENTING;
        }
        if ((options & ActivityInfo.FLAG_NO_HISTORY) != 0) {
            res += "noHistory ";
            options &= ~ActivityInfo.FLAG_NO_HISTORY;
        }
        if ((options & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
            res += "hardwareAccelerated ";
            options &= ~ActivityInfo.FLAG_HARDWARE_ACCELERATED;
        }

        if (options != 0){
            res += Integer.toHexString(options);
        }

        return res;
    }

    public static String getLaunchMode(int launchMode) {
        String res = "";
        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
            res = "standard";
        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {
            res = "single top";
        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {
            res = "single task";
        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            res = "single instance";
        }

        return res;

    }

    public static String getProcessName(int pid, Context context) {
        String currentProcName = null;
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                currentProcName = processInfo.processName;
                break;
            }
        }
        return currentProcName;
    }

}
