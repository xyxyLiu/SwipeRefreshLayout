CustomSwipeRefreshLayout
========================


swipe/release/refresh the CustomSwipeRefreshLayout:
![Screenshot](https://github.com/xyxyLiu/CustomSwipeRefreshLayout/blob/master/website/CSF_DEMO.png)


CustomSwipeRefreshLayout is a modified version of android.support.v4.widget.SwipeRefreshLayout, which supports custom refresh headviews with custom animations. You can add almost any View in CustomSwipeRefreshLayout, NOT ONLY Listview. Note that CustomSwipeRefreshLayout can only contain one child View.  You can make your own custom headview which contains the refreshing infomation like images, texts, animations....(A default headview is provided) and content View.

### Usage
``` java
        // STEP 1: instantiate one SwipeRefreshLayout.
        mSwipeRefreshLayout = new CustomSwipeRefreshLayout(context);
        
        // STEP 2(OPTIONAL): settings
        // OPTIONAL:  Enable the top progress bar
        mSwipeRefreshLayout.enableTopProgressBar(true);
        // OPTIONAL:  Keeping the refreshing head on the top
        mSwipeRefreshLayout.enableTopRefreshingHead(false);
        // OPTIONAL:  Timeout to return to original state when the swipe motion stay in the same position
        mSwipeRefreshLayout.setmReturnToOriginalTimeout(200);
        // OPTIONAL:  Timeout to show the refresh complete information on the refreshing head.
        mSwipeRefreshLayout.setmRefreshCompleteTimeout(2000);
        // OPTIONAL:  Set progress bar colors
        mSwipeRefreshLayout.setProgressBarColor(color1, color2,color3, color4);

        // STEP3: set onRefresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
            }
        });

        // STEP4: add your content view to CustomSwipeRefresh
        mSwipeRefreshLayout.setContent(mListView);
```

### For more information
See the demo in reginald.swiperefresh.demo.
