CustomSwipeRefreshLayout
========================


swipe/release/refresh the CustomSwipeRefreshLayout:
![Screenshot](https://github.com/xyxyLiu/CustomSwipeRefreshLayout/blob/master/website/CSF_DEMO.png)


CustomSwipeRefreshLayout is a modified version of android.support.v4.widget.SwipeRefreshLayout, which supports custom refresh headview which contains the images, texts, animations(A default refresh headview is provided). You can add you own view in CustomSwipeRefreshLayout. 
Note that CustomSwipeRefreshLayout can only contain one child View.  

### Features
* Highly customizable(pull/swipe mode, different refresh header settings, even create your own refresh header)
* Easy to integrate in your Android project (see usage)
* Works for any Android project targeting Android 2.3 (API level 9) and up

### Usage 


#### Prelimenary:
* You can import reginald.swiperefresh.view directly to your project. Note that android.support.v4 jar is required.
* Add \res\layout\default_swiperefresh_head_layout.xml and \res\values\strings.xml to 
your corresponding folders if you use the internal default refresh head layout              reginald.swiperefresh.view.DefaultCustomHeadViewLayout. (You can also implement your own custom refresh head by implementing CustomSwipeRefreshHeadview.CustomSwipeRefreshHeadLayout interface.)
     
     AAR format package will be released later... 
     
#### Create CustomSwipeRefreshLayout
``` java
        // STEP 1: instantiate one SwipeRefreshLayout.
        mSwipeRefreshLayout = new CustomSwipeRefreshLayout(context);
        
        // STEP 2(OPTIONAL): settings
        mSwipeRefreshLayout = new CustomSwipeRefreshLayout(mContext);
        // OPTIONAL:  Set refresh mode to swipe mode
                      (CustomSwipeRefreshLayout.REFRESH_MODE_PULL for pull-to-refresh mode)
        mSwipeRefreshLayout.setRefreshMode(CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE);
        // OPTIONAL:  Enable the top progress bar
        mSwipeRefreshLayout.enableTopProgressBar(true);
        // OPTIONAL:  keep the refreshing head movable(true stands for fixed) on the top
        mSwipeRefreshLayout.enableTopRefreshingHead(false);
        // OPTIONAL:  Timeout to return to original state when the swipe motion stay in the same position
        mSwipeRefreshLayout.setmReturnToOriginalTimeout(200);
        // OPTIONAL:  Timeout to show the refresh complete information on the refreshing head.
        mSwipeRefreshLayout.setmRefreshCompleteTimeout(1000);
        // OPTIONAL:  Set progress bar colors
        mSwipeRefreshLayout.setProgressBarColor(color1, color2,color3, color4);

        // STEP3: add your content view to CustomSwipeRefresh
        mSwipeRefreshLayout.setContent(yourContentView);
```

#### Handle refresh event:
``` java
        //set onRefresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
            }
        });


        // use mSwipeRefreshLayout.onRefreshingComplete()
        // to tell the CustomSwipeRefreshLayout when your refreshing process is complete


```


### Demo
See the demo in reginald.swiperefresh.demo:
you can use different setting as following in the menu:
* pull/swipe mode mode 
* fixed/movable refreshing head mode 

<br>
![Screenshot](https://github.com/xyxyLiu/CustomSwipeRefreshLayout/blob/master/website/demoScreenShot.png)
