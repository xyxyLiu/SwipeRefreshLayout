package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

/**
 * Created by lxy on 15/8/6.
 */
public class BaseDemoActivity extends Activity{

    /**
     * The CustomSwipeRefreshLayout that detects swipe gestures and
     * triggers callbacks in the app.
     */
    protected CustomSwipeRefreshLayout mCustomSwipeRefreshLayout;

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

        menu.add(1, 3, 0, "fixed refresh head");
        menu.add(1, 4, 0, "movable refresh head");
        if (mCustomSwipeRefreshLayout.isKeepTopRefreshingHead()) {
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(true);
        } else {
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(false);
        }

        return super.onPrepareOptionsMenu(menu);
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
                mCustomSwipeRefreshLayout.setKeepTopRefreshingHead(true);
                text = "fixed refreshing head";
                break;
            case 4:
                mCustomSwipeRefreshLayout.setKeepTopRefreshingHead(false);
                text = "movable refreshing head";
                break;
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
}
