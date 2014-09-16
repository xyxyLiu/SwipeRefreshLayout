/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package reginald.swiperefresh.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p/>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity.onCreate()");
        super.onCreate(savedInstanceState);
        //show demo view
        setContentView(new CustomSwipeRefreshDemoView(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "MainActivity.onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "MainActivity.onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "MainActivity.onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "MainActivity.onStop()");
        super.onStop();
    }

}
