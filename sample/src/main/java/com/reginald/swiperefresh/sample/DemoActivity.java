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
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
import com.reginald.swiperefresh.sample.dummydata.Cheeses;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tony.lxy on 2014/9/11.
 */

/**
 * One Sample activity that shows the features of CustomSwipeRefreshLayout
 */
public class DemoActivity extends Activity {

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

    ArrayList<View> viewPagerViews = new ArrayList<>();
    RecyclerView mRecyclerView;
    MyAdapter mRecyclerViewAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show demo view
        setContentView(R.layout.main_layout);
        setupView();
    }

    protected void setupView() {
        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swipelayout);
        mCustomSwipeRefreshLayout.setCustomHeadview(new MyCustomHeadViewLayout(this));

        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        setupViewPagerViews();
        content.addView(mListView);
//        mViewPager = (ViewPager) findViewById(R.id.viewpager);
//        mViewPager.setAdapter(new ViewPagerAdapter(viewPagerViews));



//        // YOU CAN MAKE CONFIGURATION USING THE FOLLOWING CODE
//        // Set refresh mode to swipe mode(CustomSwipeRefreshLayout.REFRESH_MODE_PULL for pull-to-refresh mode)
//        mCustomSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);
//        // Enable the top progress bar
//        mCustomSwipeRefreshLayout.enableTopProgressBar(true);
//        // Keep the refreshing head movable(true stands for fixed) on the top
//        mCustomSwipeRefreshLayout.enableTopRefreshingHead(false);
//        // Timeout to return to original state when the swipe motion stay in the same position
//        mCustomSwipeRefreshLayout.setmReturnToOriginalTimeout(200);
//        // Timeout to show the refresh complete information on the refreshing head.
//        mCustomSwipeRefreshLayout.setmRefreshCompleteTimeout(1000);
//        // Set progress bar colors( Or use setProgressBarColorRes(int colorRes1,int colorRes2,int colorRes3,int colorRes4) for color resources)
//        mCustomSwipeRefreshLayout.setProgressBarColor(
//                0x77ff6600, 0x99ffee33,
//                0x66ee5522, 0xddffcc11);

        // set onRefresh listener
        mCustomSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
                initiateRefresh();
            }
        });
    }

    private void setupViewPagerViews(){
        mRecyclerView = new RecyclerView(this);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this,2);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            Drawable mDivider;

            {
                final TypedArray a = DemoActivity.this.obtainStyledAttributes(new int[]{android.R.attr.listDivider});
                mDivider = a.getDrawable(0);
                a.recycle();
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                final int left = parent.getPaddingLeft();
                final int right = parent.getWidth() - parent.getPaddingRight();

                final int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = parent.getChildAt(i);
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                            .getLayoutParams();
                    final int top = child.getBottom() + params.bottomMargin;
                    final int bottom = top + mDivider.getIntrinsicHeight();
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }
        });

        // specify an adapter (see also next example)
        mRecyclerViewAdapter = new MyAdapter(Cheeses.randomList(LIST_ITEM_COUNT));
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mListView = new MyListView(this);
        mListAdapter = new ArrayAdapter<String>(
                this,
                R.layout.demo_list_item,
                R.id.item_text,
                Cheeses.randomList(LIST_ITEM_COUNT));

        mListView.setAdapter(mListAdapter);


        viewPagerViews.add(mListView);
        viewPagerViews.add(mRecyclerView);
    }

    static class MyListView extends ListView{
        public MyListView(Context context){
            super(context);
        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent event){
            Log.d(TAG,"ListView.dispatchTouchEvent() " + event);
            return super.dispatchTouchEvent(event);
        }
    }

    public static class ViewPagerAdapter extends PagerAdapter {
        private List<View> list;

        public ViewPagerAdapter(List<View> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(View view, int position) {
            ViewPager pViewPager = ((ViewPager) view);
            pViewPager.addView(list.get(position));
            return list.get(position);
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public ViewHolder(TextView v) {
                super(v);
                mTextView = v;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(List<String> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                int viewType) {
            // create a new view
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.demo_list_item, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(mDataset.get(position));

        }

        public void updateData(List<String> newData){
            mDataset.clear();
            mDataset.addAll(newData);
            notifyDataSetChanged();
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    private void onRefreshComplete(int viewId, List<String> result) {

        if (viewId == 1){
            mRecyclerViewAdapter.updateData(result);
            mRecyclerView.scrollToPosition(0);
        } else if(viewId == 0) {
            mListAdapter.clear();
            for (String cheese : result) {
                mListAdapter.add(cheese);
            }
            // return to the first item
            mListView.setSelection(0);
        }

        // to notify CustomSwipeRefreshLayout that the refreshing is completed
        mCustomSwipeRefreshLayout.refreshComplete();
    }

    private void initiateRefresh() {
        new DummyBackgroundTask().execute(0);
//        new DummyBackgroundTask().execute(mViewPager.getCurrentItem());
    }

    private class DummyBackgroundTask extends AsyncTask<Integer, Void, List<String>> {

        static final int TASK_DURATION = 3 * 1000; // 3 seconds
        int viewId;
        @Override
        protected List<String> doInBackground(Integer... params) {
            // Sleep for a small amount of time to simulate a background-task
            try {
                viewId = params[0];
                Log.d(TAG,"viewId = " + viewId);
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
            onRefreshComplete(viewId,result);
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
