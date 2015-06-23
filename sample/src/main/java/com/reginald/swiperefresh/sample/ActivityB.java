package com.reginald.swiperefresh.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class ActivityB extends ActivityTest implements View.OnClickListener{

    String TAG;
    {
        TAG = "$$ ActivityB";
    }


    static {
        Log.d("$$ ActivityB", "static block in B: intentFlagsAB = " + intentFlagsAB + " ,intentFlagsBA = " + intentFlagsBA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);

        Button testBtn0 = (Button) findViewById(R.id.test_viewpager_btn0);
        testBtn0.setOnClickListener(this);
        Button testBtn1 = (Button) findViewById(R.id.test_viewpager_btn1);
        testBtn1.setOnClickListener(this);
        Button testBtn2 = (Button) findViewById(R.id.test_viewpager_btn2);
        testBtn2.setOnClickListener(this);
        Button testBtn3 = (Button) findViewById(R.id.test_viewpager_btn3);
        testBtn3.setOnClickListener(this);
        testBtn0.setText("return to DemoActivity");
        testBtn1.setText("to ActivityA");
        testBtn2.setText("to itself");
    }




    @Override
    public void onClick(View view){
        if (view.getId() == R.id.test_viewpager_btn0){
            //Intent intent = new Intent("com.reginald.swiperefresh.action.testviewpager");
            Intent intent = new Intent(this,DemoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn1){
            // B -> A
            Intent intent = new Intent(this, ActivityA.class);
            intent.setFlags(intentFlagsBA);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn2){
            // B -> B
            Intent intent = new Intent(this, ActivityB.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn3){
            //CommonUtils.logTaskInfo(this);
//            printOnTaskLogger();
        }
    }
    

}
