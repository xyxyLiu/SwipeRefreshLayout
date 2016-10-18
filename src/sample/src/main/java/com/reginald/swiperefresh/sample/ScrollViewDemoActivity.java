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

import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
import com.reginald.swiperefresh.sample.dummydata.Cheeses;

import java.util.List;

/**
 * Created by tony.lxy on 2014/9/11.
 */

/**
 * One Sample activity that shows the features of CustomSwipeRefreshLayout
 */
public class ScrollViewDemoActivity extends BaseDemoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show demo view
        setContentView(R.layout.scrollview_demo_layout);
        setupViews();
    }

    protected void setupViews() {
        setupCustomSwipeRefreshLayout();
        WebView webView = (WebView) findViewById(R.id.webview);
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 0; i < 20; i++)
            html.append("It's a WebView   It's a WebView   It's a WebView ");
        html.append("</body></html>");
        webView.loadData(html.toString(), "text/html", null);
    }

    protected void setupCustomSwipeRefreshLayout() {
        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swipelayout);
        // If no headview provided, use the default headview.
        //mCustomSwipeRefreshLayout.setCustomHeadview(new MyCustomHeadViewLayout(this));

        // YOU CAN MAKE CONFIGURATION USING THE FOLLOWING CODE
        // Set refresh mode to swipe mode(CustomSwipeRefreshLayout.REFRESH_MODE_PULL for pull-to-refresh mode)
        //mCustomSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);

        // Enable the top progress bar
        //mCustomSwipeRefreshLayout.enableTopProgressBar(true);

        // Keep the refreshing head movable(true stands for fixed) on the top
        //mCustomSwipeRefreshLayout.setKeepTopRefreshingHead(true);

        // Timeout to return to original state when the swipe motion stay in the same position
        //mCustomSwipeRefreshLayout.setmReturnToOriginalTimeout(1000);

        // Timeout to show the refresh complete information on the refreshing head.
        //mCustomSwipeRefreshLayout.setmRefreshCompleteTimeout(3000);

        // Duration of the animation from the top of the content view to parent top.(e.g. when refresh complete)
        //mCustomSwipeRefreshLayout.setReturnToTopDuration(500);

        // Duration of the animation from the top of the content view to the height of header.(e.g. when content view is released)
        //mCustomSwipeRefreshLayout.setReturnToHeaderDuration(800);

        // Set progress bar colors( Or use setProgressBarColorRes(int colorRes1,int colorRes2,int colorRes3,int colorRes4) for color resources)
        //mCustomSwipeRefreshLayout.setProgressBarColor(
        //        0x77ff6600, 0x99ffee33,
        //        0x66ee5522, 0xddffcc11);

        // Set the height of Progress bar, in dp.
        //mCustomSwipeRefreshLayout.setProgressBarHeight(2);

        // Set the resistance factor
        //mCustomSwipeRefreshLayout.setResistanceFactor(0.7f);

        // Set the trigger distance. in dp.
        // (pull -> release distance for PULL mode or swipe refresh distance for SWIPE mode)
        //mCustomSwipeRefreshLayout.setTriggerDistance(160);

        // set refresh checker to check whether to trigger refresh
//        mCustomSwipeRefreshLayout.setRefreshCheckHandler(new CustomSwipeRefreshLayout.RefreshCheckHandler() {
//            @Override
//            public boolean canRefresh() {
//                // return false when you don't want to trigger refresh
//                // e.g. return false when network is disabled.
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

        // to notify CustomSwipeRefreshLayout that the refreshing is completed
        mCustomSwipeRefreshLayout.refreshComplete();
    }

    private void initiateRefresh() {
        new DummyBackgroundTask().execute(0);
    }

    public class DummyBackgroundTask extends AsyncTask<Integer, Void, List<String>> {

        public static final int TASK_DURATION = 3 * 1000; // 3 seconds
        public static final int LIST_ITEM_COUNT = 20;

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

}
