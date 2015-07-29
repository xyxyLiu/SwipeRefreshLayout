package com.reginald.swiperefresh.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.reginald.swiperefresh.CustomSwipeRefreshHeadview;
import com.reginald.swiperefresh.CustomSwipeRefreshHeadview.State;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyCustomHeadViewLayout extends LinearLayout implements CustomSwipeRefreshHeadview.CustomSwipeRefreshHeadLayout {

    private static final boolean DEBUG = BuildConfig.ENABLE_DEBUG;
    private ViewGroup mContainer;

    private TextView mMainTextView;
    private TextView mSubTextView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private int mState = -1;

    public MyCustomHeadViewLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        setupLayout();
    }

    private void setupLayout() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.my_swiperefresh_head_layout, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);
        mImageView = (ImageView) findViewById(com.reginald.swiperefresh.R.id.default_header_arrow);
        mMainTextView = (TextView) findViewById(com.reginald.swiperefresh.R.id.default_header_textview);
        mSubTextView = (TextView) findViewById(com.reginald.swiperefresh.R.id.default_header_time);
        mProgressBar = (ProgressBar) findViewById(com.reginald.swiperefresh.R.id.default_header_progressbar);
    }

    @SuppressLint("NewApi")
    @Override
    public void onStateChange(CustomSwipeRefreshHeadview.State state) {
        if (DEBUG)
            Log.d("csrh", "onStateChange state = " + state);
        int stateCode = state.getRefreshState();
        float percent = state.getPercent();

        switch (stateCode) {
            case State.STATE_NORMAL:
                if (percent > 0.5f) {
                    mImageView.setRotation((percent - 0.5f) * 180 / 0.5f);
                    mMainTextView.setTextColor(Color.argb(0xff,(int)((percent - 0.5f) * 255 / 0.5f),0,0));
                } else {
                    mImageView.setRotation(0);
                    mMainTextView.setTextColor(Color.BLACK);
                }

                if (mState != State.STATE_NORMAL) {
                    // show arrow
                    mImageView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mMainTextView.setText("  pull to refresh    ");
                }
                break;
            case State.STATE_READY:
                if (mState != State.STATE_READY) {
                    // show arrow
                    mImageView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mImageView.setRotation(180);
                    mMainTextView.setText("release to refresh");
                    mMainTextView.setTextColor(Color.RED);
                }
                break;
            case State.STATE_REFRESHING:
                if (mState != State.STATE_REFRESHING) {
                    // show progress
                    mImageView.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mMainTextView.setText("    refreshing  ...    ");
                    mMainTextView.setTextColor(Color.RED);
                }
                break;

            case State.STATE_COMPLETE:
                if (mState != State.STATE_COMPLETE){
                    mImageView.setRotation(0);
                    mImageView.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Integer colorFrom = Color.RED;
                    Integer colorTo = Color.BLACK;
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            mMainTextView.setTextColor((Integer) animator.getAnimatedValue());
                        }

                    });
                    colorAnimation.setDuration(1000);
                    colorAnimation.start();
                }

                mMainTextView.setText("  refresh  complete  ");
                break;
            default:
        }

        mState = stateCode;
    }

}
