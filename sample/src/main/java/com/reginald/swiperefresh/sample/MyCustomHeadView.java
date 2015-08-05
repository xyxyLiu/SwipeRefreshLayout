package com.reginald.swiperefresh.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout.State;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

public class MyCustomHeadView extends LinearLayout implements CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout {

    private static final boolean DEBUG = BuildConfig.ENABLE_DEBUG;

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
    public void onStateChange(State state) {
        if (DEBUG)
            Log.d("csrh", "onStateChange state = " + state);
        int stateCode = state.getRefreshState();
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

                if (mState != CustomSwipeRefreshLayout.State.STATE_NORMAL) {
                    mImageView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mMainTextView.setText("  pull to refresh    ");
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_READY:
                if (mState != CustomSwipeRefreshLayout.State.STATE_READY) {
                    mImageView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    setImageRotation(180);
                    mMainTextView.setText("release to refresh");
                    mMainTextView.setTextColor(Color.RED);
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
                if (mState != CustomSwipeRefreshLayout.State.STATE_REFRESHING) {
                    mImageView.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mMainTextView.setText("    refreshing  ...    ");
                    mMainTextView.setTextColor(Color.RED);
                }
                break;

            case CustomSwipeRefreshLayout.State.STATE_COMPLETE:
                if (mState != CustomSwipeRefreshLayout.State.STATE_COMPLETE) {
                    setImageRotation(0);
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
                    }else {
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

    Matrix matrix = new Matrix();
    private void setImageRotation(float rotation) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mImageView.setRotation(rotation);
        } else {
            // 计算旋转的中心点
            Drawable imageDrawable = mImageView.getDrawable();
            if (null != imageDrawable) {
                int rotationPivotX = Math.round(imageDrawable.getIntrinsicWidth() / 2f);
                int rotationPivotY = Math.round(imageDrawable.getIntrinsicHeight() / 2f);
                matrix.setRotate(rotation, rotationPivotX, rotationPivotY);
                mImageView.setImageMatrix(matrix);
            }
        }
    }


}
