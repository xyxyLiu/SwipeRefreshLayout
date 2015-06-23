/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
import com.reginald.swiperefresh.sample.dummydata.Cheeses;
import com.reginald.swiperefresh.sample.utils.CommonUtils;

import java.util.List;

/**
 * Created by tony.lxy on 2014/9/11.
 */

/**
 * One Sample activity that shows the features of CustomSwipeRefreshLayout
 */
public class DemoActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "$$ MainActivity";

    private static final int LIST_ITEM_COUNT = 20;

    /**
     * The CustomSwipeRefreshLayout that detects swipe gestures and
     * triggers callbacks in the app.
     */
    private CustomSwipeRefreshLayout mCustomSwipeRefreshLayout;

    /**
     * The {@link ListView} that displays the content that should be refreshed.
     */
    private ListView mListView;

    /**
     * The {@link android.widget.ListAdapter} used to populate the {@link ListView}
     * defined in the previous statement.
     */
    private ArrayAdapter<String> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show demo view
        setContentView(R.layout.activity_main);
        setupView();

    }
    @Override
    protected void onResume(){
        super.onResume();
//        CommonUtils.logTaskInfo(this);
        setTitle("DemoActivity (task " + getTaskId() + ")");
    }

    protected void setupView() {
        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swipelayout);
        mListView = (ListView)findViewById(R.id.listview);
        Button testBtn0 = (Button) findViewById(R.id.test_viewpager_btn0);
        testBtn0.setOnClickListener(this);
        Button testBtn1 = (Button) findViewById(R.id.test_viewpager_btn1);
        testBtn1.setOnClickListener(this);
        Button testBtn2 = (Button) findViewById(R.id.test_viewpager_btn2);
        testBtn2.setOnClickListener(this);

        // OPTIONAL:  Set refresh mode to swipe mode(CustomSwipeRefreshLayout.REFRESH_MODE_PULL for pull-to-refresh mode)
        mCustomSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);
        // OPTIONAL:  Enable the top progress bar
        mCustomSwipeRefreshLayout.enableTopProgressBar(true);
        // OPTIONAL:  keep the refreshing head movable(true stands for fixed) on the top
        mCustomSwipeRefreshLayout.enableTopRefreshingHead(false);
        // OPTIONAL:  Timeout to return to original state when the swipe motion stay in the same position
        mCustomSwipeRefreshLayout.setmReturnToOriginalTimeout(200);
        // OPTIONAL:  Timeout to show the refresh complete information on the refreshing head.
        mCustomSwipeRefreshLayout.setmRefreshCompleteTimeout(1000);
        // OPTIONAL:  Set progress bar colors( Or use setProgressBarColorRes(int colorRes1,int colorRes2,int colorRes3,int colorRes4))
        mCustomSwipeRefreshLayout.setProgressBarColor(
                0x77ff6600, 0x99ffee33,
                0x66ee5522, 0xddffcc11);

        mListAdapter = new ArrayAdapter<String>(
                this,
                R.layout.demo_list_item,
                R.id.item_text,
                Cheeses.randomList(LIST_ITEM_COUNT));

        mListView.setAdapter(mListAdapter);

        // set onRefresh listener
        mCustomSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
                initiateRefresh();
            }
        });
    }

    private void onRefreshComplete(List<String> result) {
        mListAdapter.clear();
        for (String cheese : result) {
            mListAdapter.add(cheese);
        }
        // return to the first item
        mListView.setSelection(0);
        // to notify CustomSwipeRefreshLayout that the refreshing is completed
        mCustomSwipeRefreshLayout.onRefreshingComplete();
    }

    private void initiateRefresh() {
        new DummyBackgroundTask().execute();
    }

    private class DummyBackgroundTask extends AsyncTask<Void, Void, List<String>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds

        @Override
        protected List<String> doInBackground(Void... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                Thread.sleep(TASK_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Return a new random list of cheeses
            return Cheeses.randomList(LIST_ITEM_COUNT);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            // Tell the view that the refresh has completed
            onRefreshComplete(result);
        }

    }


    @Override
    public void onClick(View view){
        if (view.getId() == R.id.test_viewpager_btn0){
            //Intent intent = new Intent("com.reginald.swiperefresh.action.testviewpager");
            Intent intent = new Intent(this,DemoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn1){
            //Intent intent = new Intent("com.reginald.swiperefresh.action.testviewpager");
            Intent intent = new Intent(this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn2){
            ComponentName componentName = new ComponentName(this,ActivityA.class);//"com.reginald.swiperefresh.sample.MainActivityAlias");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        CommonUtils.logTaskInfo(this);
        menu.clear();
        menu.add(0, 1, 0, "swipe mode");
        menu.add(0, 2, 0, "pull mode");
        if (mCustomSwipeRefreshLayout.getRefreshMode() == CustomSwipeRefreshLayout.REFRESH_MODE_PULL) {
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(true);
        }

        if (mCustomSwipeRefreshLayout.getRefreshMode() == CustomSwipeRefreshLayout.REFRESH_MODE_PULL) {
            menu.add(1, 3, 0, "fixed refresh head");
            menu.add(1, 4, 0, "movable refresh head");
            if (mCustomSwipeRefreshLayout.isEnableTopRefreshingHead()) {
                menu.getItem(2).setEnabled(false);
                menu.getItem(3).setEnabled(true);
            } else {
                menu.getItem(2).setEnabled(true);
                menu.getItem(3).setEnabled(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String text = "";
        switch (item.getItemId()) {
            case 1:
                mCustomSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);
                text = "swipe refresh mode";
                break;
            case 2:
                mCustomSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_PULL);
                text = "pull refresh mode";
                break;
            case 3:
                mCustomSwipeRefreshLayout.enableTopRefreshingHead(true);
                text = "fixed refreshing head";
                break;
            case 4:
                mCustomSwipeRefreshLayout.enableTopRefreshingHead(false);
                text = "movable refreshing head";
                break;
        }
        Toast.makeText(DemoActivity.this, text, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }




}
