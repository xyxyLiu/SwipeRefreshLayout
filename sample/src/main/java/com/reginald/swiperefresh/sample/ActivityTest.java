package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;


/**
 * Created by baidu on 15/6/10.
 */
public class ActivityTest extends Activity{

    public String TAG = "";
    //CommonUtils.TaskLogger taskLogger;
    TextView taskInfoOld;
    TextView taskInfoNew;
    TextView userAction;
    AlertDialog menuDialog;
    View menuView;

    public static int intentFlagsAB;
    public static int intentFlagsBA;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, this + " onCreate() ");
//        taskLogger = CommonUtils.TaskLogger.getInstance(this);
//        taskLogger.onActivityCreate(this);
        //printOnTaskLogger();

        // 创建AlertDialog
        menuDialog = new AlertDialog.Builder(this).create();
        menuView = View.inflate(this, R.layout.menu, null);
        menuDialog.setView(menuView);

        Log.d(TAG,"intentFlagsAB = " + intentFlagsAB + ", intentFlagsBA = " + intentFlagsBA);
    }

    public void setTaskInfoView(int id_old, int id_new, int id_action){
        taskInfoOld = (TextView) findViewById(id_old);
        taskInfoNew = (TextView) findViewById(id_new);
        userAction = (TextView) findViewById(id_action);
        userAction.setMovementMethod(new ScrollingMovementMethod());
        taskInfoOld.setMovementMethod(new ScrollingMovementMethod());
        taskInfoNew.setMovementMethod(new ScrollingMovementMethod());
//        taskLogger.setViews(taskInfoOld, taskInfoNew, userAction);
    }

    public void setTaskInfoView(){
        setTaskInfoView(R.id.task_info_text_old, R.id.task_info_text_new, R.id.user_action_text);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, this + " onStart() ");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, this + "onRestart() ");
//        taskLogger.onActivityRestart(this);
        //printOnTaskLogger();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, this + "onResume() ");
        setTaskInfoView();
//        printOnTaskLogger();
        //CommonUtils.logTaskInfo(this);
    }

    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Log.d(TAG, this + "onNewIntent ");
//        taskLogger.onActivityNewIntent(this);
        //printOnTaskLogger();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, this + "onPause() ");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, this + "onStop() ");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, this + "onDestroy() ");
//        taskLogger.onActivityDestroy(this);
//        printOnTaskLogger();
    }

//    public void printOnTaskLogger(){
//        taskLogger.print();
//    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
//        taskLogger.setLastUserActionInfo("back from " + CommonUtils.TaskActivity.activityToString(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menuDialog != null) {
            final CheckBox a1 = (CheckBox) menuView.findViewById(R.id.new_task_checkbox_a);
            final CheckBox a2 = (CheckBox) menuView.findViewById(R.id.multi_task_checkbox_a);
            final CheckBox a3 = (CheckBox) menuView.findViewById(R.id.clear_top_checkbox_a);
            final CheckBox a4 = (CheckBox) menuView.findViewById(R.id.clear_task_checkbox_a);
            final CheckBox a5 = (CheckBox) menuView.findViewById(R.id.single_top_checkbox_a);

            final CheckBox b1 = (CheckBox) menuView.findViewById(R.id.new_task_checkbox_b);
            final CheckBox b2 = (CheckBox) menuView.findViewById(R.id.multi_task_checkbox_b);
            final CheckBox b3 = (CheckBox) menuView.findViewById(R.id.clear_top_checkbox_b);
            final CheckBox b4 = (CheckBox) menuView.findViewById(R.id.clear_task_checkbox_b);
            final CheckBox b5 = (CheckBox) menuView.findViewById(R.id.single_top_checkbox_b);

            Log.d("$$ ActivityA", "onShow: static block in A: intentFlagsAB = " + intentFlagsAB + " ,intentFlagsBA = " + intentFlagsBA);
            refreshCheckBoxes(intentFlagsAB, a1, a2, a3, a4, a5);
            refreshCheckBoxes(intentFlagsBA, b1, b2, b3, b4, b5);

            menuDialog.show();


        menuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                intentFlagsAB = refreshflags(a1.isChecked(), a2.isChecked(), a3.isChecked(), a4.isChecked(), a5.isChecked());
                intentFlagsBA = refreshflags(b1.isChecked(), b2.isChecked(), b3.isChecked(), b4.isChecked(), b5.isChecked());
            }
        });
        }

        return false;// 返回为true 则显示系统menu
    }

    public int refreshflags(boolean isNewTask, boolean isMultiTask, boolean isClearTop, boolean isClearTask, boolean isSingleTop){
        int flags = 0;
        if (isNewTask)
            flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
        if (isMultiTask)
            flags |= Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (isClearTop)
            flags |= Intent.FLAG_ACTIVITY_CLEAR_TOP;
        if (isClearTask)
            flags |= Intent.FLAG_ACTIVITY_CLEAR_TASK;
        if (isSingleTop)
            flags |= Intent.FLAG_ACTIVITY_SINGLE_TOP;

        return flags;
    }

    public void refreshCheckBoxes(int flags, CheckBox cb1, CheckBox cb2, CheckBox cb3, CheckBox cb4, CheckBox cb5) {
        Log.d(TAG,"flags = " + flags);

        cb1.setChecked(false);
        cb2.setChecked(false);
        cb3.setChecked(false);
        cb4.setChecked(false);
        cb5.setChecked(false);

        if ((flags & Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
            cb1.setChecked(true);
        if ((flags & Intent.FLAG_ACTIVITY_MULTIPLE_TASK) != 0)
            cb2.setChecked(true);
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0)
            cb3.setChecked(true);
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0)
            cb4.setChecked(true);
        if ((flags & Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0)
            cb5.setChecked(true);
    }

}
