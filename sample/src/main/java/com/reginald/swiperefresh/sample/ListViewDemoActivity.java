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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
import com.reginald.swiperefresh.sample.dummydata.Cheeses;

import java.util.List;

/**
 * Created by tony.lxy on 2014/9/11.
 */

/**
 * One Sample activity that shows the features of CustomSwipeRefreshLayout
 */
public class ListViewDemoActivity extends Activity {

    public static final String TAG = "MainActivity";

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
        setContentView(R.layout.listview_demo_layout);
        setupViews();
    }

    protected void setupViews(){
        setupCustomSwipeRefreshLayout();

        mListView = (ListView) findViewById(R.id.listview);
        mListAdapter = new ArrayAdapter<String>(
                this,
                R.layout.demo_list_item,
                R.id.item_text,
                Cheeses.randomList(LIST_ITEM_COUNT));

        mListView.setAdapter(mListAdapter);
    }

    protected void setupCustomSwipeRefreshLayout() {
        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swipelayout);
        // Set your a custom HeadView. use default HeadView if not provided
        mCustomSwipeRefreshLayout.setCustomHeadview(new MyCustomHeadView(this));

        // YOU CAN MAKE CONFIGURATION USING THE FOLLOWING CODE
        // Set refresh mode to swipe mode(CustomSwipeRefreshLayout.REFRESH_MODE_PULL for pull-to-refresh mode)
        //mCustomSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);

        // Enable the top progress bar
        //mCustomSwipeRefreshLayout.enableTopProgressBar(true);

        // Keep the refreshing head movable(true stands for fixed) on the top
        //mCustomSwipeRefreshLayout.enableTopRefreshingHead(false);

        // Timeout to return to original state when the swipe motion stay in the same position
        //mCustomSwipeRefreshLayout.setmReturnToOriginalTimeout(200);

        // Timeout to show the refresh complete information on the refreshing head.
        //mCustomSwipeRefreshLayout.setmRefreshCompleteTimeout(1000);

        // Set progress bar colors( Or use setProgressBarColorRes(int colorRes1,int colorRes2,int colorRes3,int colorRes4) for color resources)
        //mCustomSwipeRefreshLayout.setProgressBarColor(
        //        0x77ff6600, 0x99ffee33,
        //        0x66ee5522, 0xddffcc11);

        // Set the height of Progress bar
        //mCustomSwipeRefreshLayout.setProgressBarHeight(3);

        // Set the resistance factor
        //mCustomSwipeRefreshLayout.setResistanceFactor(0.5f);

        // Set the trigger distance.
        // (pull -> release distance for PULL mode or swipe refresh distance for SWIPE mode)
        //mCustomSwipeRefreshLayout.setTriggerDistance(120);

        // set refresh checker to check whether to trigger refresh
//        mCustomSwipeRefreshLayout.setRefreshCheckHandler(new CustomSwipeRefreshLayout.RefreshCheckHandler() {
//            @Override
//            public boolean canRefresh() {
//                // i.e. return false when nothing can be refreshed
//            }
//        });

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
        mCustomSwipeRefreshLayout.refreshComplete();
    }

    private void initiateRefresh() {
        new DummyBackgroundTask().execute(0);
    }

    private class DummyBackgroundTask extends AsyncTask<Integer, Void, List<String>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds
        int viewId;
        @Override
        protected List<String> doInBackground(Integer... params) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {

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

        //if (mCustomSwipeRefreshLayout.getRefreshMode() == CustomSwipeRefreshLayout.REFRESH_MODE_PULL) {
            menu.add(1, 3, 0, "fixed refresh head");
            menu.add(1, 4, 0, "movable refresh head");
            if (mCustomSwipeRefreshLayout.isEnableTopRefreshingHead()) {
                menu.getItem(2).setEnabled(false);
                menu.getItem(3).setEnabled(true);
            } else {
                menu.getItem(2).setEnabled(true);
                menu.getItem(3).setEnabled(false);
            }
        //}
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
        Toast.makeText(ListViewDemoActivity.this, text, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

}
