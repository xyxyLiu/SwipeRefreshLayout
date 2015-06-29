CustomSwipeRefreshLayout
========================

### Demo
CustomSwipeRefreshLayout has different refresh mode as following:
* pull/swipe mode mode 
* fixed/movable refreshing head mode

<table cellspacing="0" cellpadding="0" margin="0" style='border-collapse: collapse;'>
<tr><td align="center">swipe mode</td><td align="center">pull mode<br>(fixed/movable refreshing head mode)</td></tr>
<tr><td>
   <img src="https://raw.githubusercontent.com/xyxyLiu/SwipeRefreshLayout/master/website/swipe_mode.gif" width="240" alt="Screenshot"/>
   </td>
   <td>
   <img src="https://raw.githubusercontent.com/xyxyLiu/SwipeRefreshLayout/master/website/pull_mode.gif" width="240" alt="Screenshot"/>
   </td>
</tr>
</table>


CustomSwipeRefreshLayout is a modified version of android.support.v4.widget.SwipeRefreshLayout, which supports custom refresh headview which contains the images, texts, animations(A default refresh headview is provided). You can add you own view in CustomSwipeRefreshLayout. 
Note that CustomSwipeRefreshLayout can only contain one child View.  

### Features
* Highly customizable(pull/swipe mode, different refresh header settings, even create your own refresh header)
* Easy to integrate in your Android project (see usage)
* Works for any Android project targeting Android 2.3 (API level 9) and up

### Usage
Import CustomSwipeRefreshLayout project in your project with gradle
- [swiperefreshlayout-1.0.aar](https://github.com/xyxyLiu/SwipeRefreshLayout/releases/download/1.0/swiperefreshlayout-1.0.aar)


````xml
 <com.reginald.swiperefresh.CustomSwipeRefreshLayout
        xmlns:swiperefresh="http://schemas.android.com/apk/res-auto"
        android:id="@+id/swipelayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        swiperefresh:refresh_mode="pull_mode"
        swiperefresh:keep_refresh_head="true"
        swiperefresh:enable_top_progress_bar="true"
        swiperefresh:time_out_refresh_complete="2000"
        swiperefresh:time_out_return_to_top="1000"
        swiperefresh:top_progress_bar_color_1="@color/common_red"
        swiperefresh:top_progress_bar_color_2="#ee5522"
        swiperefresh:top_progress_bar_color_3="#ffa600"
        swiperefresh:top_progress_bar_color_4="@color/common_yellow">

    <!-- Attention: you can add ONLY one view in CustomSwipeRefreshLayout either in xml or java code -->

        <ListView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:cacheColorHint="#00000000"
            android:id="@+id/listview">
        </ListView>

    </com.reginald.swiperefresh.CustomSwipeRefreshLayout>
````
* **refresh_mode:** swipe mode / pull mode, default is swipe mode

   Refresh mode.
* **keep_refresh_head:** boolean, default is false

   Whether to keep head when refresh.
* **enable_top_progress_bar:** boolean, default is true

   Whether to show the top progress bar.
* **time_out_refresh_complete:** integer, time in milliseconds, default is 1000ms

   Timeout for keeping head when refresh complete.
* **time_out_return_to_top:** integer, time in milliseconds, default is 500ms

   Timeout to return to original state when the swipe motion stay in the same position.
* **top_progress_bar_color_1|2|3|4:** color, defaults are 4 grey colors.

   4 colors of the top progress bar.

You can also config CustomSwipeRefreshLayout using Java code
````java
        // Set refresh mode to swipe mode
        // (CustomSwipeRefreshLayout.REFRESH_MODE_PULL or CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE)
        mSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);
        // Enable the top progress bar
        mSwipeRefreshLayout.enableTopProgressBar(true);
        // Keep the refreshing head movable(true stands for fixed) on the top
        mSwipeRefreshLayout.enableTopRefreshingHead(false);
        // Timeout to return to original state when the swipe motion stay in the same position
        mSwipeRefreshLayout.setmReturnToOriginalTimeout(200);
        // Timeout to show the refresh complete information on the refreshing head.
        mSwipeRefreshLayout.setmRefreshCompleteTimeout(1000);
        // Set progress bar colors
        mSwipeRefreshLayout.setProgressBarColor(color1, color2,color3, color4);
```

Handle refresh event
``` java
        CustomSwipeRefreshLayout mSwipeRefreshLayout;


        //set onRefresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
                // e.g. to request data from server
            }
        });

        // use CustomSwipeRefreshLayout.onRefreshingComplete()
        // to tell the CustomSwipeRefreshLayout when your refreshing process is complete
        // e.g. when received data from server
```