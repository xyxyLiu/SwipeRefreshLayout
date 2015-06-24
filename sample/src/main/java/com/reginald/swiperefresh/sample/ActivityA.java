package com.reginald.swiperefresh.sample;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class ActivityA extends ActivityTest implements View.OnClickListener{

    {
        TAG = "$$ ActivityA";
    }

    static {
        Log.d("$$ ActivityA","static block in A: intentFlagsAB = " + intentFlagsAB + " ,intentFlagsBA = " + intentFlagsBA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);

        Button testBtn0 = (Button) findViewById(R.id.test_viewpager_btn0);
        testBtn0.setOnClickListener(this);
        Button testBtn1 = (Button) findViewById(R.id.test_viewpager_btn1);
        testBtn1.setOnClickListener(this);
        Button testBtn2 = (Button) findViewById(R.id.test_viewpager_btn2);
        testBtn2.setOnClickListener(this);
        Button testBtn3 = (Button) findViewById(R.id.test_viewpager_btn3);
        testBtn3.setOnClickListener(this);
        testBtn0.setText("return to DemoActivity");
        testBtn1.setText("to ActivityB");
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
            // A -> B
            Intent intent = new Intent(this, ActivityB.class);
            intent.setFlags(intentFlagsAB);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn2){
            // A -> A
            Intent intent = new Intent(this, ActivityA.class);
            startActivity(intent);
        }else if (view.getId() == R.id.test_viewpager_btn3){
            //CommonUtils.logTaskInfo(this);
        }
    }

    @Override
    public void startActivity(Intent intent){
        ComponentName componentName = intent.getComponent();
        intent.getFlags();
        if (componentName != null){
            Log.d(TAG,"startActivity " + this.getClass().getSimpleName() + " -> " + componentName.getClassName());
        }
        super.startActivity(intent);
    }


}
