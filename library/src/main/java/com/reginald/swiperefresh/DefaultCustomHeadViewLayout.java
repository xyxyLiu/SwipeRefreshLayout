package com.reginald.swiperefresh;

import android.content.Context;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.reginald.swiperefresh.CustomSwipeRefreshHeadview.State;


/**
 * Created by liu on 2014/9/15.
 */

/**
 * The DefaultCustomHeadViewLayout is a refresh head view provided as default.
 * You can also make your own head view layout which must implement
 * CustomSwipeRefreshHeadview.CustomSwipeRefreshHeadLayout interface.
 */
public class DefaultCustomHeadViewLayout extends LinearLayout implements CustomSwipeRefreshHeadview.CustomSwipeRefreshHeadLayout {

    private LinearLayout mContainer;

    private TextView mMainTextView;
    private TextView mSubTextView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;

    private int mState = -1;
    private Animation.AnimationListener animationListener;

    public DefaultCustomHeadViewLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        setupLayout();
    }

    public void setupLayout() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.default_swiperefresh_head_layout, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);
        mImageView = (ImageView) findViewById(R.id.default_header_arrow);
        mMainTextView = (TextView) findViewById(R.id.default_header_textview);
        mSubTextView = (TextView) findViewById(R.id.default_header_time);
        mProgressBar = (ProgressBar) findViewById(R.id.default_header_progressbar);

        setupAnimation();

    }

    public void setupAnimation() {

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation.AnimationListener mRotateUpAnimListener = animationListener;
        mRotateUpAnim.setAnimationListener(mRotateUpAnimListener);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    @Override
    public void onStateChange(CustomSwipeRefreshHeadview.State state) {
        Log.d("csrh", "onStateChange state = " + state);
        int stateCode = state.getRefreshState();
        if (stateCode == mState) {
            return;
        }
        //Log.i("csr", "state = " + state);
        if (stateCode == CustomSwipeRefreshHeadview.State.STATE_COMPLETE) {
            mImageView.clearAnimation();
            mImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        } else if (stateCode == State.STATE_REFRESHING) {
            // show progress
            mImageView.clearAnimation();
            mImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            // show arrow
            mImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (stateCode) {
            case State.STATE_NORMAL:
                if (mState == State.STATE_READY) {
                    mImageView.startAnimation(mRotateDownAnim);
                }
                if (mState == State.STATE_REFRESHING) {
                    mImageView.clearAnimation();
                }
                mMainTextView.setText(R.string.csr_text_state_normal);
                break;
            case State.STATE_READY:

                if (mState != State.STATE_READY) {
                    mImageView.clearAnimation();
                    mImageView.startAnimation(mRotateUpAnim);
                    mMainTextView.setText(R.string.csr_text_state_ready);
                }
                break;
            case State.STATE_REFRESHING:
                mMainTextView.setText(R.string.csr_text_state_refresh);
                updateData();
                break;

            case State.STATE_COMPLETE:
                mMainTextView.setText(R.string.csr_text_state_complete);
                updateData();
                break;
            default:
        }

        mState = stateCode;
    }

    public void updateData() {

        String time = fetchData();
        if (time != null) {
            mSubTextView.setVisibility(VISIBLE);
            mSubTextView.setText(time);
        } else {
            mSubTextView.setVisibility(GONE);
        }

    }

    public String fetchData() {
        return getResources().getString(R.string.csr_text_last_refresh) + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}