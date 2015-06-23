package com.reginald.swiperefresh.sample.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by baidu on 15/6/9.
 */
public class CommonUtils {
    public static final String TAG = "TaskTest";


    public static class TaskLoggerService extends Service implements Handler.Callback{
        public static final boolean IS_IPC = true;
        Map<Integer, List<TaskActivity>> taskLists;

        private static TaskLoggerService INSTANCE;
        private int foregroundTaskId;
        private Handler mRemoteHandler;
        private Messenger mMessenger;

        public static final int MSG_ON_ACTIVITY_CREATE = 1;
        public static final int MSG_ON_ACTIVITY_DESTROY = 2;
        public static final int MSG_ON_ACTIVITY_NEW_INTENT = 3;
        public static final int MSG_ON_ACTIVITY_RESTART = 4;
        public static final int MSG_ON_ACTIVITY_RESUME = 5;
        public static final int MSG_TASKS_PRINT = 6;

        @Override
        public void onCreate(){
            Log.d(TAG, "TaskLoggerService.onCreate()");
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId){
            Log.d(TAG, "TaskLoggerService.onStartCommand()");
            Message msg = intent.getParcelableExtra("message");
            Log.d(TAG,"message = " + msg);
            handleMessage(msg);

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            Log.d(TAG, "TaskLoggerService.onBind()");
            return getBinder();
        }

            @Override
            public boolean handleMessage(Message msg) {
                Log.d(TAG, "handleMessage:" + msg.what + " in process " + android.os.Process.myPid() + " thread " +  Thread.currentThread().getId());

                switch (msg.what) {
                    case MSG_ON_ACTIVITY_CREATE: {
                        Bundle bundle = msg.getData();
                        bundle.setClassLoader(TaskActivity.class.getClassLoader());
                        TaskActivity taskActivity = (TaskActivity)  bundle.getParcelable("TaskActivity");
                        List<TaskActivity> list = taskLists.get(taskActivity.taskId);
                        if (list == null){
                            list = new ArrayList<TaskActivity>();
                            taskLists.put(taskActivity.taskId,list);
                            //taskName = taskActivity.taskName;
                        }else {
                            taskActivity.taskName = list.get(0).taskName;
                        }
                        foregroundTaskId = taskActivity.taskId;
                        list.add(taskActivity);
                        break;
                    }
                    case MSG_ON_ACTIVITY_DESTROY: {
                        Bundle bundle = msg.getData();
                        bundle.setClassLoader(TaskActivity.class.getClassLoader());
                        TaskActivity taskActivity = (TaskActivity)  bundle.getParcelable("TaskActivity");
                        String activity = taskActivity.activity;
                        int taskId = taskActivity.taskId;

                        List<TaskActivity> list = taskLists.get(taskId);
                        int index = list.indexOf(new TaskActivity(taskId,activity,null));
                        if (index != -1){
                            list.remove(index);
                            if (list.size() == 0)
                                taskLists.remove(taskId);
                        }

                        if (taskLists.size() == 0){
                            foregroundTaskId = 0;
                        }

                        print();
                        break;
                    }
                    case MSG_ON_ACTIVITY_NEW_INTENT:
                    case MSG_ON_ACTIVITY_RESTART:
                    case MSG_ON_ACTIVITY_RESUME:{
                        //foregroundTaskId = msg.arg1;
                        Bundle bundle = msg.getData();
                        bundle.setClassLoader(TaskActivity.class.getClassLoader());
                        TaskActivity taskActivity = (TaskActivity)  bundle.getParcelable("TaskActivity");
                        foregroundTaskId = taskActivity.taskId;
                        print();
                        break;
                    }
                    case MSG_TASKS_PRINT: {
                        print();
                        break;
                    }
                    default:
                        break;
                }

                return true;
            }

        public static synchronized TaskLoggerService getInstance(){
            if (INSTANCE == null)
                INSTANCE = new TaskLoggerService();

            return INSTANCE;
        }

        public TaskLoggerService(){
            Log.d(TAG," new TaskLoggerService()");
            taskLists = new HashMap<>();
            mRemoteHandler = new Handler(this);
            mMessenger = new Messenger(mRemoteHandler);
        }

        public IBinder getBinder(){
            return mMessenger.getBinder();
        }

        public Messenger getClientMessenger(){
            return new Messenger(mMessenger.getBinder());
        }

        public static void onActivityDestroy(Messenger messenger, Activity a){
            String activity = TaskActivity.contextToString(a);
            int taskId = a.getTaskId();
            TaskActivity taskActivity = new TaskActivity(taskId, activity, null);
            try {
                Message message = Message.obtain(null, TaskLoggerService.MSG_ON_ACTIVITY_DESTROY);
                Bundle bundle = new Bundle();
                bundle.putParcelable("TaskActivity",taskActivity);
                message.setData(bundle);
                messenger.send(message);
            }catch (RemoteException e){
                Log.e(TAG,"remote exception in onActivityDestroy()");
                e.printStackTrace();
            }


        }

        public static void onActivityRestart(Messenger messenger, Activity a){
            try {
                messenger.send(Message.obtain(null, TaskLoggerService.MSG_ON_ACTIVITY_RESTART, a.getTaskId(), 0));
            }catch (RemoteException e){
                Log.e(TAG,"remote exception in onActivityRestart()");
                e.printStackTrace();
            }
        }

        public static void onActivityResume(Messenger messenger, Activity a){
            try {
                messenger.send(Message.obtain(null, TaskLoggerService.MSG_ON_ACTIVITY_RESUME, a.getTaskId(), 0));
            }catch (RemoteException e){
                Log.e(TAG,"remote exception in onActivityRestart()");
                e.printStackTrace();
            }
        }

        public static void onActivityNewIntent(Messenger messenger, Activity a){
            try {
                messenger.send(Message.obtain(null, TaskLoggerService.MSG_ON_ACTIVITY_NEW_INTENT, a.getTaskId(), 0));
            }catch (RemoteException e){
                Log.e(TAG,"remote exception in onActivityNewIntent()");
                e.printStackTrace();
            }
        }

        public static void onActivityCreate(Messenger messenger, Activity a){
            Log.d(TAG, "TaskLoggerService.onActivityCreate(): binder " + messenger.getBinder());
            String activity = TaskActivity.contextToString(a);
            int taskId = a.getTaskId();
            ResolveInfo info = a.getPackageManager().resolveActivity(a.getIntent(), 0);
            String taskName = info.activityInfo.taskAffinity;
            TaskActivity taskActivity = new TaskActivity(taskId, activity, taskName);
            try {
                Message message = Message.obtain(null, TaskLoggerService.MSG_ON_ACTIVITY_CREATE);
                Bundle bundle = new Bundle();
                bundle.putParcelable("TaskActivity", taskActivity);
                message.setData(bundle);
                messenger.send(message);
            }catch (RemoteException e){
                Log.e(TAG,"remote exception in onActivityCreate()");
                e.printStackTrace();
            }
        }

        public static void printTasks(Messenger messenger){
            try {
                messenger.send(Message.obtain(null, MSG_TASKS_PRINT));
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }


        public void print(){

            Log.d(TAG, "$$$$$$$$$$$$$ task info: ");

            StringBuilder msg = new StringBuilder("");
            for(HashMap.Entry<Integer,List<TaskActivity>> entry : taskLists.entrySet()) {
                int taskId = entry.getKey();
                List<TaskActivity> list = entry.getValue();
                TaskActivity baseActivity = list.get(0);
                String taskName = baseActivity.taskName;
                if (foregroundTaskId == taskId) {
                    Log.d(TAG, "###############  * task " + taskName + "(" + taskId + ")  size = " + list.size());
                    msg.append(String.format("## * task %3s  \n", taskName + "(" + taskId + ")"));
                } else {
                    Log.d(TAG, "###############    task " + taskName + "(" + taskId + ") size = " + list.size());
                    msg.append(String.format("##   task %3s  \n", taskName + "(" + taskId + ")"));
                }


                ListIterator<TaskActivity> reverseIterator = list.listIterator(list.size());
                while(reverseIterator.hasPrevious()){
                    TaskActivity ta = reverseIterator.previous();
                    msg.append(ta.activity + "\n");
                    Log.d(TAG, ta.activity);
                    msg.append("  ^  \n");
                    Log.d(TAG, "           ^             ");
                }
               // msg.delete(msg.length()-4 , msg.length());
               // Log.d(LOG_TAG, msg.toString());
            }

            Log.d(TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        }




        // NO IPC：
        // 使用非IPC手段：通过在Intent中加入TaskLogger对象实现行不通。
        // 如果在一个 activity A 在 进程1 中启动 在 进程2 中的 activity B, 其activity回调时序如下：
        // A.onPause -> B.onCreate(接收TaskLogger, 会使TaskLogger中的task队列变化) -> B.onStart -> B.onResume -> A.onStop ..... -> A.onDestroy(返回TaskLogger，会使TaskLogger中的task队列变化)
        // 由于B.onCreate 与 A.onDestroy 中的TaskLogger是不同的，A.onDestroy在B.onCreate前面才可保证正确状态！

        public synchronized void onActivityDestroy(Activity a){
            String activity = TaskActivity.contextToString(a);
            int taskId = a.getTaskId();
            Log.d(TAG,"onActivityDestroy: of " + activity);
            List<TaskActivity> list = taskLists.get(taskId);
            int index = list.indexOf(new TaskActivity(taskId,activity,null));
            if (index != -1){
                Log.d(TAG,"onActivityDestroy: REMOVE activity " + activity + " in task " + taskId);
                list.remove(index);
                if (list.size() == 0) {
                    Log.d(TAG,"onActivityDestroy: REMOVE task " + taskId);
                    taskLists.remove(taskId);
                }
            }

            if (taskLists.size() == 0){
                foregroundTaskId = 0;
            }

        }

        public synchronized void onActivityRestart(Activity a){
            foregroundTaskId = a.getTaskId();
            print();
        }

        public synchronized void onActivityNewIntent(Activity a){
            foregroundTaskId = a.getTaskId();
            print();
        }

        public synchronized void onActivityCreate(Activity a){
            String activity = TaskActivity.contextToString(a);
            int taskId = a.getTaskId();
            foregroundTaskId = a.getTaskId();
            List<TaskActivity> list = taskLists.get(taskId);
            String taskName;
            if (list == null){
                list = new ArrayList<TaskActivity>();
                taskLists.put(taskId,list);

                ResolveInfo info = a.getPackageManager().resolveActivity(a.getIntent(), 0);
                taskName = info.activityInfo.taskAffinity;
            }else {
                taskName = list.get(0).taskName;
            }
            list.add(new TaskActivity(taskId, activity, taskName));
        }

    }




    public static class TaskActivity implements Parcelable{
        public String activity;
        public int taskId;
        public String taskName;

        public TaskActivity(Activity a){
            activity = TaskActivity.contextToString(a);
            taskId = a.getTaskId();
            ResolveInfo info = a.getPackageManager().resolveActivity(a.getIntent(), 0);
            taskName = info.activityInfo.taskAffinity;
        }

        public TaskActivity(int taskId, String activity, String name){
            this.taskId =taskId;
            this.activity = activity;
            this.taskName = name;
        }

        public String toString(){
            return "[ activity = " + activity + " , taskId = " + taskId + " , taskName = " + taskName + " ]";
        }

        public TaskActivity(Parcel parcel){
            activity = parcel.readString();
            taskId = parcel.readInt();
            taskName = parcel.readString();
        }

        public static String contextToString(Context a){
            return a.getClass().getSimpleName() + " (" + Integer.toHexString(a.hashCode()) + ")";
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj) return true;
            if (obj == null) return false;
            if (((Object)this).getClass() != obj.getClass()) return false;
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
        }

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
    }


    public static String getFlags(int flags){
        String res = "";
        if ((flags & Intent.FLAG_ACTIVITY_NEW_TASK) != 0){
            res += "NEW_TASK ";
        }
        if ((flags & Intent.FLAG_ACTIVITY_MULTIPLE_TASK) != 0){
            res += "MULTIPLE_TASK ";
        }
        if ((flags & Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0){
            res += "SINGLE_TOP ";
        }
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0){
            res += "CLEAR_TOP ";
        }
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0){
            res += "CLEAR_TASK ";
        }

        return res;
    }

    public static String getLaunchMode(int launchMode){
        String res = "";
        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE){
            res = "standard";
        }else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP){
            res = "single top";
        }else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK){
            res = "single task";
        }else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE){
            res = "single instance";
        }

        return res;
    }

}
