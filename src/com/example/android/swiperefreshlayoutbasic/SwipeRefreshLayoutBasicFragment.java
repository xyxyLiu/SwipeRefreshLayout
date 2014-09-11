/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.swiperefreshlayoutbasic;

import com.example.android.common.dummydata.Cheeses;
import com.example.android.common.logger.Log;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.common.view.CustomSwipeRefreshLayout;

import java.util.List;

/**
 * A basic sample that shows how to use {@link android.support.v4.widget.SwipeRefreshLayout} to add
 * the 'swipe-to-refresh' gesture to a layout. In this sample, SwipeRefreshLayout contains a
 * scrollable {@link android.widget.ListView} as its only child.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 */
public class SwipeRefreshLayoutBasicFragment extends Fragment {

    private static final String LOG_TAG = SwipeRefreshLayoutBasicFragment.class.getSimpleName();

    private static final int LIST_ITEM_COUNT = 20;

    /**
     * The {@link android.support.v4.widget.SwipeRefreshLayout} that detects swipe gestures and
     * triggers callbacks in the app.
     */
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * The {@link android.widget.ListView} that displays the content that should be refreshed.
     */
    private ListView mListView;

    /**
     * The {@link android.widget.ListAdapter} used to populate the {@link android.widget.ListView}
     * defined in the previous statement.
     */
    private ArrayAdapter<String> mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    // BEGIN_INCLUDE (inflate_view)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	    Log.i("lxy","Fragment.onCreateView()");
        View view = inflater.inflate(R.layout.fragment_sample, container, false);

        // Retrieve the SwipeRefreshLayout and ListView instances
        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
	    mSwipeRefreshLayout.enableTopProgressBar(true);


        // BEGIN_INCLUDE (change_colors)
        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
        mSwipeRefreshLayout.setColorScheme(
                R.color.swiperefresh_color_1, R.color.swiperefresh_color_2,
                R.color.swiperefresh_color_3, R.color.swiperefresh_color_4);
        // END_INCLUDE (change_colors)

        // Retrieve the ListView
        mListView = (ListView) view.findViewById(android.R.id.list);

	    /*
	    ViewPager mViewPager = (ViewPager)view.findViewById(R.id.viewpager);
	    PagerTitleStrip mPagerTitleStrip = (PagerTitleStrip)view.findViewById(R.id.pagertitle);


	    mListAdapter = new ArrayAdapter<String>(
			    getActivity(),
			    android.R.layout.simple_list_item_1,
			    android.R.id.text1,
			    Cheeses.randomList(LIST_ITEM_COUNT));

	    // Set the adapter between the ListView and its backing data.
	    //mListView.setAdapter(mListAdapter);

	    //将要分页显示的View装入数组中
	    LayoutInflater mLi = LayoutInflater.from(getActivity().getApplicationContext());
	    ListView view1 = (ListView)mLi.inflate(R.layout.onelistview, null);
	    ListView view2 = (ListView)mLi.inflate(R.layout.onelistview, null);
	    ListView view3 =(ListView) mLi.inflate(R.layout.onelistview, null);
	    view1.setAdapter(mListAdapter);
	    view2.setAdapter(mListAdapter);
	    view3.setAdapter(mListAdapter);

	    //每个页面的Title数据
	    final ArrayList<View> views = new ArrayList<View>();
	    views.add(view1);
	    views.add(view2);
	    views.add(view3);


	    final ArrayList<String> titles = new ArrayList<String>();
	    titles.add("tab1");
	    titles.add("tab2");
	    titles.add("tab3");

	    //填充ViewPager的数据适配器
	    PagerAdapter mPagerAdapter = new PagerAdapter() {

		    @Override
		    public boolean isViewFromObject(View arg0, Object arg1) {
			    return arg0 == arg1;
		    }

		    @Override
		    public int getCount() {
			    return views.size();
		    }

		    @Override
		    public void destroyItem(View container, int position, Object object) {
			    ((ViewPager)container).removeView(views.get(position));
		    }

		    @Override
		    public CharSequence getPageTitle(int position) {
			    return titles.get(position);
		    }

		    @Override
		    public Object instantiateItem(View container, int position) {
			    ((ViewPager)container).addView(views.get(position));
			    return views.get(position);
		    }
	    };

	    mViewPager.setAdapter(mPagerAdapter);
*/
        return view;
    }
    // END_INCLUDE (inflate_view)

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
         * uses the system-defined simple_list_item_1 layout that contains one TextView.
         */
        mListAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                Cheeses.randomList(LIST_ITEM_COUNT));

        // Set the adapter between the ListView and its backing data.
        mListView.setAdapter(mListAdapter);






        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });
        // END_INCLUDE (setup_refreshlistener)
    }
    // END_INCLUDE (setup_views)

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    // BEGIN_INCLUDE (setup_refresh_menu_listener)
    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Log.i(LOG_TAG, "Refresh menu item selected");

                // We make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }

                // Start our refresh background task
                initiateRefresh();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (initiate_refresh)
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        new DummyBackgroundTask().execute();
    }
    // END_INCLUDE (initiate_refresh)

    // BEGIN_INCLUDE (refresh_complete)
    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<String> result) {
        Log.i(LOG_TAG, "onRefreshComplete");

        // Remove all items from the ListAdapter, and then replace them with the new items
        mListAdapter.clear();
        for (String cheese : result) {
            mListAdapter.add(cheese);

        }
	    Log.i(LOG_TAG, "mListAdapter refreshed");
        // Stop the refreshing indicator
        mSwipeRefreshLayout.setRefreshing(false);
    }
    // END_INCLUDE (refresh_complete)

    /**
     * Dummy {@link AsyncTask} which simulates a long running task to fetch new cheeses.
     */
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

            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result);
        }

    }
}
