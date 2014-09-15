package com.example.android.common.view;

import android.content.Context;
import android.text.TextUtils;
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
import com.example.android.common.logger.Log;
import com.example.android.swiperefreshlayoutbasic.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liu on 2014/9/15.
 */
public class CustomDefaultHeadViewLayout extends LinearLayout implements CustomSwipeRefreshHeadview.EtaoSwipeRefreshHeadLayout {

    private LinearLayout mContainer;

    private TextView mMainTextView;
    private TextView mSubTextView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;

    private int mState = -1;

    public CustomDefaultHeadViewLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        setupLayout();
    }

    public void setupLayout() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.etao_default_swiperefresh_layout, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);
        mImageView = (ImageView) findViewById(R.id.xlistview_header_arrow);
        mMainTextView = (TextView) findViewById(R.id.xlistview_header_hint_textview);
        mSubTextView = (TextView) findViewById(R.id.xlistview_header_time);
        mProgressBar = (ProgressBar) findViewById(R.id.xlistview_header_progressbar);

        setupAnimation();

    }

    public void setupAnimation() {

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation.AnimationListener mRotateUpAnimListener = new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
                Log.i("lxy", "start mRotateUpAnim");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i("lxy", "end mRotateUpAnim");
            }

            @Override public void onAnimationRepeat(Animation animation) {
                Log.i("lxy", "repeat mRotateUpAnim");
            }

        };
        mRotateUpAnim.setAnimationListener(mRotateUpAnimListener);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    public void setState(int state) {
        //tryToUpdateLastUpdateTime();
        if (state == mState) {
            return;
        }
        Log.i("lxy", "state = " + state);
        if (state == CustomSwipeRefreshHeadview.STATE_REFRESHING) {    // 显示进度
            mImageView.clearAnimation();
            mImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {    // 显示箭头图片
            mImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (state) {
            case CustomSwipeRefreshHeadview.STATE_NORMAL:
                if (mState == CustomSwipeRefreshHeadview.STATE_READY) {
                    mImageView.startAnimation(mRotateDownAnim);
                }
                if (mState == CustomSwipeRefreshHeadview.STATE_REFRESHING) {
                    mImageView.clearAnimation();
                }
                mMainTextView.setText(R.string.csr_text_state_normal);
                break;
            case CustomSwipeRefreshHeadview.STATE_READY:

                if (mState != CustomSwipeRefreshHeadview.STATE_READY) {
                    mImageView.clearAnimation();
                    mImageView.startAnimation(mRotateUpAnim);
                    mMainTextView.setText(R.string.csr_text_state_ready);
                }
                break;
            case CustomSwipeRefreshHeadview.STATE_REFRESHING:
                mMainTextView.setText(R.string.csr_text_state_refresh);
                break;

            case CustomSwipeRefreshHeadview.STATE_COMPLETE:
                mMainTextView.setText(R.string.csr_text_state_complete);
                break;
            default:
        }

        mState = state;
    }

    public void setLastUpdateTime(String time) {
        mSubTextView.setVisibility(VISIBLE);
        mSubTextView.setText(time);
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