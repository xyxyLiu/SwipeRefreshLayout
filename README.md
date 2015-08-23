CustomSwipeRefreshLayout
========================
CustomSwipeRefreshLayout is a modified version of android.support.v4.widget.SwipeRefreshLayout, which supports custom refresh headview which contains the images, texts, animations(A default refresh headview is provided). You can add you own view in CustomSwipeRefreshLayout.
Note that CustomSwipeRefreshLayout can only contain one child View.

### Features
* Highly customizable(pull/swipe mode, different refresh header settings, even create your own refresh header)
* Easy to integrate in your Android project (see usage)
* support most of commonly used views (ListView, RecyclerView, ScrollView, HorizontalScrollView, WebView, ViewPager, ...)
* Works for any Android project targeting Android 2.3 (API level 9) and up


### Demo
[Demo apk](https://github.com/xyxyLiu/SwipeRefreshLayout/releases/download/1.1/sample.apk)

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

#### Listview in a LinearLayout
<img src="https://raw.githubusercontent.com/xyxyLiu/SwipeRefreshLayout/master/website/listview_with_custom_header.gif" width="240" alt="Screenshot"/>

#### ScrollView and WebView in a LinearLayout
<img src="https://raw.githubusercontent.com/xyxyLiu/SwipeRefreshLayout/master/website/scrollview_and_webview.gif" width="240" alt="Screenshot"/>

#### ListView and RecyclerView in a ViewPager
<img src="https://raw.githubusercontent.com/xyxyLiu/SwipeRefreshLayout/master/website/viewpager_with_listview_and_recyclerview.gif" width="240" alt="Screenshot"/>


### Usage
#### Import
Import CustomSwipeRefreshLayout project in your project with gradle:
```
compile 'com.reginald.swiperefresh:library:1.1.1'
```

or Download [here](https://github.com/xyxyLiu/SwipeRefreshLayout/releases/download/1.1/library-1.1.1.aar)


#### Xml config
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
        swiperefresh:return_to_top_duration="500"
        swiperefresh:return_to_header_duration="500"
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
* **time_out_refresh_complete:** integer, time in milliseconds, default is 500ms

   Duration of the animation from the top of the content view to parent top.(e.g. when refresh complete)
* **time_out_return_to_top:** integer, time in milliseconds, default is 500ms

   Duration of the animation from the top of the content view to the height of header.(e.g. when content view is released)

* **top_progress_bar_color_1|2|3|4:** color, defaults are 4 grey colors.

   4 colors of the top progress bar.

#### Java code config
You can also config CustomSwipeRefreshLayout using Java code (OPTIONAL)
````java
        // Set a custom HeadView. use default HeadView if not provided
        mCustomSwipeRefreshLayout.setCustomHeadview(new MyCustomHeadView(this));
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
        // Duration of the animation from the top of the content view to parent top.(e.g. when refresh complete)
        mCustomSwipeRefreshLayout.setReturnToTopDuration(500);
        // Duration of the animation from the top of the content view to the height of header.(e.g. when content view is released)
        mCustomSwipeRefreshLayout.setReturnToHeaderDuration(500);
        // Set progress bar colors
        mSwipeRefreshLayout.setProgressBarColor(color1, color2,color3, color4);
        // Set the height of Progress bar, in dp. Default is 4dp
        mCustomSwipeRefreshLayout.setProgressBarHeight(4);
        // Set the resistance factor. Default is 0.5f
        mCustomSwipeRefreshLayout.setResistanceFactor(0.5f);
        // Set the trigger distance. in dp. Default is 100dp
        // (pull -> release distance for PULL mode or swipe refresh distance for SWIPE mode)
        mCustomSwipeRefreshLayout.setTriggerDistance(100);
```

#### Handle refresh event
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

        //set RefreshCheckHandler (OPTIONAL)
        mCustomSwipeRefreshLayout.setRefreshCheckHandler(new CustomSwipeRefreshLayout.RefreshCheckHandler() {
            @Override
            public boolean canRefresh() {
                // return false when you don't want to trigger refresh
                // e.g. return false when network is disabled.
            }
        });

        // to tell the CustomSwipeRefreshLayout when your refreshing process is complete
        // e.g. when received data from server
        mSwipeRefreshLayout.refreshComplete();
```

#### Create custom head refresh view (OPTIONAL)
Your can Create your custom refresh view by implementing CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout. If you don't provide a head view, the CustomSwipeRefreshLayout will use the default one.
Please See [DefaultCustomHeadView](https://github.com/xyxyLiu/SwipeRefreshLayout/blob/master/library/src/main/java/com/reginald/swiperefresh/DefaultCustomHeadView.java) or [MyCustomHeadView](https://github.com/xyxyLiu/SwipeRefreshLayout/tree/master/sample/src/main/java/com/reginald/swiperefresh/sample/MyCustomHeadView.java) to know how to make one head refresh view.
``` java
     /**
     * Classes that must be implemented by for custom headview
     *
     * @see com.reginald.swiperefresh.CustomSwipeRefreshLayout.State
     * @see DefaultCustomHeadViewLayout a default headview if no custom headview provided
     */
    public interface CustomSwipeRefreshHeadLayout {
        void onStateChange(State currentState, State lastState);
    }
```

#### Handle scroll event (OPTIONAL)
Check whether the views in CustomSwipeRefreshLayout can comsume the scroll event.
Note that CustomSwipeRefreshLayout will also check the scoll event for views inside the CustomSwipeRefreshLayout. So you don't have to handle scroll event by yourself unless you use custom views or other views(e.g. RecyclerView below API 14) that can be scrolled up/left/right.
``` java
        // to handle scrolling up event
        mCustomSwipeRefreshLayout.setScrollUpHandler(new CustomSwipeRefreshLayout.ScrollUpHandler() {
            @Override
            public boolean canScrollUp(View view) {
                // e.g. check whether the scroll up event can be consumed by the RecyclerView
                if (view == mRecyclerView){
                    return ((GridLayoutManager)mLayoutManager).findFirstCompletelyVisibleItemPosition() != 0;
                }
                return false;
            }
        });

        // to handle scrolling left of right event
        mCustomSwipeRefreshLayout.setScrollLeftOrRightHandler(new CustomSwipeRefreshLayout.ScrollLeftOrRightHandler() {
            @Override
            public boolean canScrollLeftOrRight(View view, int direction) {
                // e.g. check whether the scroll left or right event can be consumed by your Custom View
                if (view == myCustomView){
                  return myCustomView.canScrollHorizontal(direction);
                }
                return false;
            }
        });
```
