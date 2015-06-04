CustomSwipeRefreshLayout
========================

# Demo
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


#### Prelimenary:
* You can import reginald.swiperefresh.view directly to your project. Note that android.support.v4 jar is required.
* Add \res\layout\default_swiperefresh_head_layout.xml and \res\values\strings.xml to 
your corresponding folders if you use the internal default refresh head layout              com.reginald.swiperefresh.view.DefaultCustomHeadViewLayout. (You can also implement your own custom refresh head by implementing CustomSwipeRefreshHeadview.CustomSwipeRefreshHeadLayout interface.)

#### Create CustomSwipeRefreshLayout
STEP 1: apply CustomSwipeRefreshLayout in your layout XML
````xml
   <com.reginald.swiperefresh.CustomSwipeRefreshLayout
     android:layout_width="match_parent"
     android:layout_height="wrap_content"
     android:id="@+id/swipelayout">
     <ListView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/listview">
     </ListView>
   </com.reginald.swiperefresh.CustomSwipeRefreshLayout>
````  
STEP 2: make settings in your Java code (OPTIONAL)
  ````java      
        // OPTIONAL:  Set refresh mode to swipe mode
                      (CustomSwipeRefreshLayout.REFRESH_MODE_PULL or CustomSwipeRefreshLayout.REFRESH_MODE_SWIPE)
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
```
STEP 3: Handle refresh event
``` java
        //set onRefresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
                // e.g. to request data from server
            }
        });

        // use mSwipeRefreshLayout.onRefreshingComplete()
        // to tell the CustomSwipeRefreshLayout when your refreshing process is complete
        // e.g. when received data from server
```

