package com.reginald.swiperefresh.sample;


        import android.app.Activity;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
        import com.reginald.swiperefresh.sample.dummydata.Cheeses;

        import java.util.List;

public class ButtonFragment extends Fragment{
    Button myButton;
    View mRootView;
    static final String TAG = "ButtonFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "ButtonFragment.onCreateView()" + this);
        mRootView = inflater.inflate(R.layout.guide_1, container, false);//关联布局文件

        myButton = (Button)mRootView.findViewById(R.id.mybutton);//根据rootView找到button
        //设置按键监听事件
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(ButtonFragment.this.getActivity(), "button is click!", Toast.LENGTH_SHORT).show();
            }
        });

        setHasOptionsMenu(true);
        return mRootView;
    }


    @Override
    public void onPause(){
        Log.d(TAG, "ButtonFragment.onPause()" + this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle bundle){
        Log.d(TAG, "ButtonFragment.onCreate()" + this);
        super.onCreate(bundle);
    }

    @Override
    public void onStart(){
        Log.d(TAG, "ButtonFragment.onStart()" + this);
        super.onStart();
    }


    public void onResume(){
        Log.d(TAG, "ButtonFragment.onResume()" + this);
            setupView(mRootView);
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity){
        Log.d(TAG, "ButtonFragment.onAttach()" + this);
        super.onAttach(activity);
    }


    @Override
    public void onStop(){
        Log.d(TAG, "ButtonFragment.onStop()" + this);
        super.onStop();
    }

    @Override
    public void onDetach(){
        Log.d(TAG, "ButtonFragment.onDetach()" + this);
        super.onDetach();
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "ButtonFragment.onDestory()" + this);
        super.onDestroy();
    }

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


    protected void setupView(View rootView) {
        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) rootView.findViewById(R.id.swipelayout);
        mListView = (ListView)rootView.findViewById(R.id.listview);

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
                getActivity(),
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
         //inflater.inflate(R.menu.main, menu);
    }



    @Override
    public void onPrepareOptionsMenu(Menu menu) {
//        CommonUtils.logTaskInfo(getActivity());

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
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

}

