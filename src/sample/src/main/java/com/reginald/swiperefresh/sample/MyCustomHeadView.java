package com.reginald.swiperefresh.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout.State;

public class MyCustomHeadView extends LinearLayout implements CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout {

    private static final boolean DEBUG = false;

    private static final SparseArray<String> STATE_MAP = new SparseArray<>();
    private ViewGroup mContainer;
    private TextView mMainTextView;
    private TextView mSubTextView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private int mState = -1;

    {
        STATE_MAP.put(0, "STATE_NORMAL");
        STATE_MAP.put(1, "STATE_READY");
        STATE_MAP.put(2, "STATE_REFRESHING");
        STATE_MAP.put(3, "STATE_COMPLETE");
    }

    public MyCustomHeadView(Context context) {
        super(context);
        setupLayout();
    }

    private void setupLayout() {
        ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.my_swiperefresh_head_layout, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);
        mImageView = (ImageView) findViewById(com.reginald.swiperefresh.R.id.default_header_arrow);
        mMainTextView = (TextView) findViewById(com.reginald.swiperefresh.R.id.default_header_textview);
        mSubTextView = (TextView) findViewById(com.reginald.swiperefresh.R.id.default_header_time);
        mProgressBar = (ProgressBar) findViewById(com.reginald.swiperefresh.R.id.default_header_progressbar);
    }

    @Override
    public void onStateChange(State state, State lastState) {
        if (DEBUG)
            Log.d("csrh", "onStateChange state = " + state + ", lastState = " + lastState);
        int stateCode = state.getRefreshState();
        int lastStateCode = lastState.getRefreshState();
        float percent = state.getPercent();

        switch (stateCode) {
            case CustomSwipeRefreshLayout.State.STATE_NORMAL:
                if (percent > 0.5f) {
                    setImageRotation((percent - 0.5f) * 180 / 0.5f);
                    mMainTextView.setTextColor(Color.argb(0xff, (int) ((percent - 0.5f) * 255 / 0.5f), 0, 0));
                } else {
                    setImageRotation(0);
                    mMainTextView.setTextColor(Color.BLACK);
                }

                if (stateCode != lastStateCode) {
                    mImageView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mMainTextView.setText("  pull to refresh    ");
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_READY:
                if (stateCode != lastStateCode) {
                    mImageView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    setImageRotation(180);
                    mMainTextView.setText("release to refresh");
                    mMainTextView.setTextColor(Color.RED);
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
                if (stateCode != lastStateCode) {
                    mImageView.clearAnimation();
                    mImageView.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mMainTextView.setText("    refreshing  ...    ");
                    mMainTextView.setTextColor(Color.RED);
                }
                break;

            case CustomSwipeRefreshLayout.State.STATE_COMPLETE:
                if (stateCode != lastStateCode) {
                    mImageView.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
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
                    } else {
                        mMainTextView.setTextColor(Color.BLACK);
                    }
                }
                mMainTextView.setText("  refresh  complete  ");
                break;
            default:
        }
        mSubTextView.setText(String.format("state: %s, percent: %1.4f", STATE_MAP.get(stateCode), percent));
        mState = stateCode;
    }


    private void setImageRotation(float rotation) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mImageView.setRotation(rotation);
        } else {
            if (mImageView.getTag() == null){
                mImageView.setTag(0f);
            }
            mImageView.clearAnimation();
            Float lastDegree = (Float)mImageView.getTag();
            RotateAnimation rotate = new RotateAnimation(lastDegree, rotation,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mImageView.setTag(rotation);
            rotate.setFillAfter(true);
            mImageView.startAnimation(rotate);
        }
    }

}
