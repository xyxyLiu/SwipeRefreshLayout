package com.example.android.swiperefreshlayoutbasic;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.example.android.common.dummydata.Cheeses;
import com.example.android.common.view.CustomSwipeRefreshLayout;

import java.util.List;
/**
 * Created by tony.lxy on 2014/9/11.
 */
public class CustomSwipeRefreshDemoView extends LinearLayout {

	private Context mContext;
	private static final int LIST_ITEM_COUNT = 20;

	/**
	 * The CustomSwipeRefreshLayout that detects swipe gestures and
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

	public CustomSwipeRefreshDemoView(Context context) {
		super(context);
		mContext = context;
		setupView();
	}

	protected void setupView()
	{
		mSwipeRefreshLayout = new CustomSwipeRefreshLayout(mContext);
		mSwipeRefreshLayout.enableTopProgressBar(true);

		// Retrieve the ListView
		mListView = new ListView(mContext);
		mListView.setBackgroundColor(Color.GRAY);
		/**
		 * Create an ArrayAdapter to contain the data for the ListView. Each item in the ListView
		 * uses the system-defined simple_list_item_1 layout that contains one TextView.
		 */
		mListAdapter = new ArrayAdapter<String>(
				mContext,
				android.R.layout.simple_list_item_1,
				android.R.id.text1,
				Cheeses.randomList(LIST_ITEM_COUNT));

		mListView.setAdapter(mListAdapter);

		mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				initiateRefresh();
			}
		});
		mSwipeRefreshLayout.addView(mListView,1);

		mSwipeRefreshLayout.setColorScheme(
				R.color.swiperefresh_color_1, R.color.swiperefresh_color_2,
				R.color.swiperefresh_color_3, R.color.swiperefresh_color_4);


		addView(mSwipeRefreshLayout);
		setOrientation(LinearLayout.VERTICAL);
	}


	private void initiateRefresh() {
		new DummyBackgroundTask().execute();
	}

	private void onRefreshComplete(List<String> result) {
		// Remove all items from the ListAdapter, and then replace them with the new items
		mListAdapter.clear();
		for (String cheese : result) {
			mListAdapter.add(cheese);
		}
		// Stop the refreshing indicator
		mSwipeRefreshLayout.setRefreshing(false);
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

			// Tell the Fragment that the refresh has completed
			onRefreshComplete(result);
		}

	}


}
