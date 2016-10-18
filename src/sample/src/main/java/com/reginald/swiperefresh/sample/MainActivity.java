
package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by tony.lxy on 2014/9/11.
 */

/**
 * One Sample activity that shows the features of CustomSwipeRefreshLayout
 */
public class MainActivity extends Activity {

    private Button but1;
    private Button but2;
    private Button but3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show demo view
        setContentView(R.layout.main_activity_layout);
        setupViews();
    }

    private void setupViews() {
        but1 = (Button) findViewById(R.id.but1);
        but2 = (Button) findViewById(R.id.but2);
        but3 = (Button) findViewById(R.id.but3);
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListViewDemoActivity.class));
            }
        });
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScrollViewDemoActivity.class));
            }
        });
        but3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ViewPagerDemoActivity.class));
            }
        });
    }


}
