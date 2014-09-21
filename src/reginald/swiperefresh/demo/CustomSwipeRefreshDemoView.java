package reginald.swiperefresh.demo;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import reginald.swiperefresh.R;
import reginald.swiperefresh.demo.dummydata.Cheeses;
import reginald.swiperefresh.view.CustomSwipeRefreshLayout;

import java.util.List;

/**
 * Created by tony.lxy on 2014/9/11.
 */

/**
 * One Sample view that shows the features of CustomSwipeRefreshLayout
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

    protected void setupView() {
        mSwipeRefreshLayout = new CustomSwipeRefreshLayout(mContext);
        // OPTIONAL:  Set refresh mode to swipe mode(CustomSwipeRefreshLayout.REFRESH_MODE_PULL for pull-to-refresh mode)
        mSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);
        // OPTIONAL:  Enable the top progress bar
        mSwipeRefreshLayout.enableTopProgressBar(true);
        // OPTIONAL:  keep the refreshing head movable(true stands for fixed) on the top
        mSwipeRefreshLayout.enableTopRefreshingHead(false);
        // OPTIONAL:  Timeout to return to original state when the swipe motion stay in the same position
        mSwipeRefreshLayout.setmReturnToOriginalTimeout(200);
        // OPTIONAL:  Timeout to show the refresh complete information on the refreshing head.
        mSwipeRefreshLayout.setmRefreshCompleteTimeout(1000);
        // OPTIONAL:  Set progress bar colors( Or use setProgressBarColorRes(int colorRes1,int colorRes2,int colorRes3,int colorRes4))
        mSwipeRefreshLayout.setProgressBarColor(
                0x33669900, 0x99ccff00,
                0x3399cc00, 0xaadddd00);

        // Create one listview as the only content view in the CustomSwipeRefreshLayout
        mListView = new ListView(mContext);
        mListView.setCacheColorHint(Color.TRANSPARENT);

        mListAdapter = new ArrayAdapter<String>(
                mContext,
                R.layout.demo_list_item,
                R.id.item_text,
                Cheeses.randomList(LIST_ITEM_COUNT));

        mListView.setAdapter(mListAdapter);

        // set onRefresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
                initiateRefresh();
            }
        });


        // add listview to CustomSwipeRefreshLayout. YOU SHOULD ONLY ADD ONE CHILD IN CUSTOMSWIPEREFRESHLAYOUT
        mSwipeRefreshLayout.setContent(mListView);
        addView(mSwipeRefreshLayout);
        setOrientation(LinearLayout.VERTICAL);
    }

    public CustomSwipeRefreshLayout getSwipeRefreshLayout()
    {
        return mSwipeRefreshLayout;
    }

    private void initiateRefresh() {
        new DummyBackgroundTask().execute();
    }

    private void onRefreshComplete(List<String> result) {
        mListAdapter.clear();
        for (String cheese : result) {
            mListAdapter.add(cheese);
        }
        // return to the first item
        mListView.setSelection(0);
        // to notify CustomSwipeRefreshLayout that the refreshing is completed
        mSwipeRefreshLayout.onRefreshingComplete();
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


}
