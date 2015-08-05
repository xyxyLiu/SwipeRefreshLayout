package com.reginald.swiperefresh;
/**
 * Created by tony.lxy on 2014/9/5.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;

/**
 * The CustomSwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. And refreshComplete()
 * should be called whenever the refreshing is complete. The CustomSwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * Two refresh mode are supported:
 * <li>swipe mode: android.support.v4.widget.SwipeRefreshLayout style with custom refresh head </li>
 * <li>pull mode: pull-to-refresh style with progress bar and custom refresh head</li>
 * <p/>
 */
public class CustomSwipeRefreshLayout extends ViewGroup {

    public static final boolean DEBUG = BuildConfig.ENABLE_DEBUG;
    public static final String TAG = "csrl";

    public static final int REFRESH_MODE_SWIPE = 1;
    public static final int REFRESH_MODE_PULL = 2;

    // time out for no movements during swipe action
    private static final int RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 500;

    // time out for showing refresh complete info
    private static final int REFRESH_COMPLETE_POSITION_TIMEOUT = 1000;

    // acceleration of progress bar
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5f;

    // deceleration of progress bar
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    // height of progress bar
    private static final int PROGRESS_BAR_HEIGHT = 4;

    // maximum swipe distance( percent of parent container)
    private static final float MAX_SWIPE_DISTANCE_FACTOR = .5f;

    // swipe distance to trigger refreshing
    private static final int SWIPE_REFRESH_TRIGGER_DISTANCE = 100;

    // swipe resistance factor
    private static final float RESISTANCE_FACTOR = .5f;

    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };


    private final DecelerateInterpolator mDecelerateInterpolator;
    private final AccelerateInterpolator mAccelerateInterpolator;
    private final Animation mAnimateStayComplete = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            // DO NOTHING
        }
    };
    boolean enableTopProgressBar = true;
    boolean enableTopRefreshingHead = true;
    int refresshMode = REFRESH_MODE_SWIPE;
    State currentState = new State(State.STATE_NORMAL, 0);
    private RefreshCheckHandler mRefreshCheckHandler;
    private int mReturnToOriginalTimeout = RETURN_TO_ORIGINAL_POSITION_TIMEOUT;
    private int mRefreshCompleteTimeout = REFRESH_COMPLETE_POSITION_TIMEOUT;
    private float mResistanceFactor = RESISTANCE_FACTOR;
    private int mTriggerDistance = SWIPE_REFRESH_TRIGGER_DISTANCE;
    private int mProgressBarHeight = PROGRESS_BAR_HEIGHT;
    private int mConvertedProgressBarHeight;
    private CustomSwipeProgressBar mTopProgressBar;
    private View mHeadview;
    private boolean hasHeadview;
    //the content that gets pulled down
    private View mTarget = null;
    private int mTargetOriginalTop;
    private int mOriginalOffsetBottom;
    private OnRefreshListener mListener;
    private MotionEvent mDownEvent;
    private int mFrom;
    private boolean mRefreshing = false;
    private int mTouchSlop;
    private int mDistanceToTriggerSync = -1;
    private float mPrevY;
    private int mMediumAnimationDuration;
    private final Runnable mReturnToTrigerPosition = new Runnable() {

        @Override
        public void run() {
            animateOffsetToTrigerPosition(mTarget.getTop(),
                    null);
        }

    };
    private float mFromPercentage = 0;
    private float mCurrPercentage = 0;
    private final AnimationListener mShrinkAnimationListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrPercentage = 0;
        }
    };
    private boolean enableHorizontalScroll = true;
    private boolean isHorizontalScroll;
    private boolean checkHorizontalMove;
    private boolean mCheckValidMotionFlag = true;
    private int mCurrentTargetOffsetTop = 0;
    private final AnimationListener mReturnToStartPositionListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // Once the target content has returned to its start position, reset
            // the target offset to 0
            mCurrentTargetOffsetTop = 0;
        }
    };
    private boolean mReturningToStart;
    private final Runnable mReturnToStartPosition = new Runnable() {

        @Override
        public void run() {
            mReturningToStart = true;
            animateOffsetToStartPosition(mTarget.getTop(),
                    mReturnToStartPositionListener);
        }

    };
    private final AnimationListener mStayCompleteListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mReturnToStartPosition.run();
            mRefreshing = false;
        }
    };
    private final Runnable mStayRefreshCompletePosition = new Runnable() {

        @Override
        public void run() {
            animateStayComplete(mStayCompleteListener);
        }

    };
    private Animation mShrinkTrigger = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            float percent = mFromPercentage + ((0 - mFromPercentage) * interpolatedTime);
            mTopProgressBar.setTriggerPercentage(percent);
        }
    };
    // Cancel the refresh gesture and animate everything back to its original state.
    private final Runnable mCancel = new Runnable() {
        @Override
        public void run() {
            mReturningToStart = true;
            // Timeout fired since the user last moved their finger; animate the
            // trigger to 0 and put the target back at its original position
            if (mTopProgressBar != null && enableTopProgressBar) {
                mFromPercentage = mCurrPercentage;
                mShrinkTrigger.setDuration(mMediumAnimationDuration);
                mShrinkTrigger.setAnimationListener(mShrinkAnimationListener);
                mShrinkTrigger.reset();
                mShrinkTrigger.setInterpolator(mDecelerateInterpolator);
                startAnimation(mShrinkTrigger);
            }
            animateOffsetToStartPosition(mTarget.getTop(),
                    mReturnToStartPositionListener);
        }
    };
    private int mTriggerOffset = 0;
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = mTargetOriginalTop;
            if (mFrom != mTargetOriginalTop) {
                targetTop = (mFrom + (int) ((mTargetOriginalTop - mFrom) * interpolatedTime));
            }
            if (DEBUG)
                Log.d(TAG, "mAnimateToStartPosition(): targetTop = " + targetTop + ", mTarget.getTop() = " + mTarget.getTop());
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            if (DEBUG)
                Log.d(TAG, "mAnimateToStartPosition(): offset = " + offset);
            setTargetOffsetTop(offset, true);
        }
    };
    private final Animation mAnimateToTrigerPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = mDistanceToTriggerSync;
            if (mFrom > mDistanceToTriggerSync) {
                targetTop = (mFrom + (int) ((mDistanceToTriggerSync - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            if (DEBUG)
                Log.d(TAG, "mAnimateToTrigerPosition(): offset = " + offset);
            setTargetOffsetTop(offset, true);
        }
    };

    /**
     * Simple constructor to use when creating a CustomSwipeRefreshLayout from code.
     *
     * @param context
     */
    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating CustomSwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mTopProgressBar = new CustomSwipeProgressBar(this);
        setProgressBarHeight(PROGRESS_BAR_HEIGHT);

        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mAccelerateInterpolator = new AccelerateInterpolator(ACCELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSwipeRefreshLayout);
        if (a != null) {
            refresshMode = a.getInteger(R.styleable.CustomSwipeRefreshLayout_refresh_mode, REFRESH_MODE_SWIPE);
            setRefreshMode(refresshMode);
            boolean progressBarEnabled = a.getBoolean(R.styleable.CustomSwipeRefreshLayout_enable_top_progress_bar, true);
            mReturnToOriginalTimeout = a.getInteger(R.styleable.CustomSwipeRefreshLayout_time_out_return_to_top, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
            mRefreshCompleteTimeout = a.getInteger(R.styleable.CustomSwipeRefreshLayout_time_out_refresh_complete, REFRESH_COMPLETE_POSITION_TIMEOUT);
            enableTopRefreshingHead = a.getBoolean(R.styleable.CustomSwipeRefreshLayout_keep_refresh_head, false);
            int color1 = a.getColor(R.styleable.CustomSwipeRefreshLayout_top_progress_bar_color_1, 0);
            int color2 = a.getColor(R.styleable.CustomSwipeRefreshLayout_top_progress_bar_color_2, 0);
            int color3 = a.getColor(R.styleable.CustomSwipeRefreshLayout_top_progress_bar_color_3, 0);
            int color4 = a.getColor(R.styleable.CustomSwipeRefreshLayout_top_progress_bar_color_4, 0);
            setProgressBarColor(color1, color2, color3, color4);
            setEnableTopProgressBar(progressBarEnabled);
            a.recycle();
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    private static boolean canViewScrollUp(View view, MotionEvent event) {
        boolean ret;

        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                ret = absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                ret = view.getScrollY() > 0 || canChildrenScroolUp(view, event);
            }
        } else {
            ret = ViewCompat.canScrollVertically(view, -1) || canChildrenScroolUp(view, event);
        }
        if (DEBUG)
            Log.d(TAG, "canViewScrollUp " + view.getClass().getName() + " " + ret);
        return ret;
    }

    private static boolean canChildrenScroolUp(View view, MotionEvent event) {
        if (view instanceof ViewGroup) {
            final ViewGroup viewgroup = (ViewGroup) view;
            int count = viewgroup.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = viewgroup.getChildAt(i);
                Rect bounds = new Rect();
                child.getHitRect(bounds);
                if (bounds.contains((int) event.getX(), (int) event.getY())) {
                    return canViewScrollUp(child, event);
                }
            }
        }

        return false;
    }

    /**
     * @param direction Negative to check scrolling left, positive to check scrolling right.
     * @return Whether it is possible for the child view of this layout to
     * scroll left or right. Override this if the child view is a custom view.
     */
    private static boolean canViewScrollHorizontally(View view, MotionEvent event, int direction) {
        boolean ret;
        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof ViewPager) {
                ret = ((ViewPager) view).canScrollHorizontally(direction);
            } else {
                ret = view.getScrollX() * direction > 0;
            }
        } else {
            ret = ViewCompat.canScrollHorizontally(view, direction);
        }

        ret = ret || canChildrenScroolHorizontally(view, event, direction);
        if (DEBUG)
            Log.d(TAG, "canViewScrollHorizontally " + view.getClass().getName() + " " + ret);
        return ret;
    }

    private static boolean canChildrenScroolHorizontally(View view, MotionEvent event, int direction) {
        if (view instanceof ViewGroup) {
            final ViewGroup viewgroup = (ViewGroup) view;
            int count = viewgroup.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = viewgroup.getChildAt(i);
                Rect bounds = new Rect();
                child.getHitRect(bounds);
                if (DEBUG)
                    Log.d(TAG, "child: " + child.getClass().getName() + " " + bounds.toShortString());
                if (bounds.contains((int) event.getX(), (int) event.getY())) {
                    if (DEBUG)
                        Log.d(TAG, "in child " + child.getClass().getName());
                    return canViewScrollHorizontally(child, event, direction);
                }
            }
        }
        return false;
    }

    private void animateStayComplete(AnimationListener listener) {
        mAnimateStayComplete.reset();
        mAnimateStayComplete.setDuration(mRefreshCompleteTimeout);
        mAnimateStayComplete.setAnimationListener(listener);
        mTarget.startAnimation(mAnimateStayComplete);
    }

    private void animateOffsetToTrigerPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToTrigerPosition.reset();
        mAnimateToTrigerPosition.setDuration(mMediumAnimationDuration);
        mAnimateToTrigerPosition.setAnimationListener(listener);
        mAnimateToTrigerPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToTrigerPosition);
    }

    public void setCustomHeadview(View customHeadview) {
        if (mHeadview != null) {
            if (mHeadview == customHeadview)
                return;
            removeView(mHeadview);
        }
        mHeadview = customHeadview;
        addView(mHeadview, new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        hasHeadview = true;
    }

    public int getRefreshMode() {
        return refresshMode;
    }

    public void setRefreshMode(int mode) {
        switch (mode) {
            case REFRESH_MODE_PULL:
                refresshMode = REFRESH_MODE_PULL;
                //setRefreshState(State.STATE_NORMAL);
                break;
            case REFRESH_MODE_SWIPE:
                refresshMode = REFRESH_MODE_SWIPE;
                //enableTopRefreshingHead(false);
                //setRefreshState(State.STATE_NORMAL);
                break;
            default:
                throw new IllegalStateException(
                        "refresh mode " + mode + " is NOT supported in CustomSwipeRefreshLayout");

        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(mCancel);
        removeCallbacks(mReturnToStartPosition);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mReturnToStartPosition);
        removeCallbacks(mCancel);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(mMediumAnimationDuration);
        mAnimateToStartPosition.setAnimationListener(listener);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToStartPosition);
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    private void setTriggerPercentage(float percent) {
        if (percent == 0f) {
            // No-op. A null trigger means it's uninitialized, and setting it to zero-percent
            // means we're trying to reset state, so there's nothing to reset in this case.
            mCurrPercentage = 0;
            return;
        }
        mCurrPercentage = percent;
        if (enableTopProgressBar) {
            mTopProgressBar.setTriggerPercentage(percent);
        }
    }

    // for headview
    private void setRefreshState(int state) {
        currentState.update(state, (float) mCurrentTargetOffsetTop / mTriggerOffset);
        ((CustomSwipeRefreshHeadLayout) mHeadview).onStateChange(currentState);
    }

    private void updateHeadViewState(int offset, boolean changeHeightOnly) {
        if (DEBUG)
            Log.d(TAG, String.format("@@ updateHeadViewState() : state=%d offset=%d mCurrentTargetOffsetTop=%d ",
                    currentState.getRefreshState(), offset, mCurrentTargetOffsetTop));
        if (changeHeightOnly) {
            setRefreshState(currentState.getRefreshState());
        } else {
            if (mTarget.getTop() > mDistanceToTriggerSync) {
                setRefreshState(State.STATE_READY);
            } else {
                setRefreshState(State.STATE_NORMAL);
            }
        }
    }

    public void refreshComplete() {
        setRefreshing(false);
    }

    /**
     * Set the four colors used in the progress animation. The first color will
     * also be the color of the bar that grows in response to a user swipe
     * gesture.
     *
     * @param colorRes1 Color resource.
     * @param colorRes2 Color resource.
     * @param colorRes3 Color resource.
     * @param colorRes4 Color resource.
     */
    public void setProgressBarColorRes(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        final Resources res = getResources();
        final int color1 = res.getColor(colorRes1);
        final int color2 = res.getColor(colorRes2);
        final int color3 = res.getColor(colorRes3);
        final int color4 = res.getColor(colorRes4);
        mTopProgressBar.setColorScheme(color1, color2, color3, color4);
    }

    public void setProgressBarColor(int color1, int color2, int color3, int color4) {
        mTopProgressBar.setColorScheme(color1, color2, color3, color4);
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    protected void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            ensureTarget();
            mCurrPercentage = 0;
            mRefreshing = refreshing;
            if (mRefreshing) {
                if (enableTopProgressBar) {
                    mTopProgressBar.start();
                }
                if (refresshMode == REFRESH_MODE_PULL) {
                    mReturnToTrigerPosition.run();
                } else if (refresshMode == REFRESH_MODE_SWIPE) {
                    mReturnToStartPosition.run();
                }

            } else {
                // keep refreshing state for refresh complete
                if (enableTopProgressBar) {
                    mTopProgressBar.stop();
                }
                if (refresshMode == REFRESH_MODE_PULL) {
                    mRefreshing = true;
                    removeCallbacks(mReturnToStartPosition);
                    removeCallbacks(mCancel);
                    mStayRefreshCompletePosition.run();
                } else if (refresshMode == REFRESH_MODE_SWIPE) {
                    mRefreshing = false;
                    mReturnToStartPosition.run();
                }
                setRefreshState(State.STATE_COMPLETE);
            }
        }
    }

    private View getContentView() {
        return getChildAt(0) == mHeadview ? getChildAt(1) : getChildAt(0);
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid out yet.
        if (mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException(
                        "CustomSwipeRefreshLayout can host ONLY one direct child");
            }
            mTarget = getContentView();
            MarginLayoutParams lp = (MarginLayoutParams) mTarget.getLayoutParams();
            mTargetOriginalTop = mTarget.getTop();
            mOriginalOffsetBottom = mTargetOriginalTop + mTarget.getHeight();
            Log.d(TAG, "!!" + String.format("mTarget.getTop()=%d ,getPaddingTop()=%d, lp.topMargin=%d", mTarget.getTop(), getPaddingTop(), lp.topMargin));

            if (DEBUG) {
                Log.d(TAG, "mTargetOriginalTop = " + mTargetOriginalTop + ", mOriginalOffsetBottom = " + mOriginalOffsetBottom);
            }
        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mTriggerOffset = (int) (mTriggerDistance * metrics.density);
                mDistanceToTriggerSync = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        mTriggerOffset + mTargetOriginalTop);

            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (enableTopProgressBar) {
            mTopProgressBar.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        if (enableTopProgressBar) {
            mTopProgressBar.setBounds(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + width, getPaddingTop() + mConvertedProgressBarHeight);
        } else {
            mTopProgressBar.setBounds(0, 0, 0, 0);
        }
        if (DEBUG)
            Log.d(TAG, String.format("mTopProgressBar[%d,%d,%d,%d]", getPaddingLeft(), getPaddingLeft(), getPaddingLeft() + width, getPaddingTop() + mConvertedProgressBarHeight));


        if (getChildCount() == 0) {
            return;
        }
        MarginLayoutParams lp = (MarginLayoutParams) mHeadview.getLayoutParams();
        final int headViewLeft = getPaddingLeft() + lp.leftMargin;
        final int headViewTop = mCurrentTargetOffsetTop - mHeadview.getMeasuredHeight() + getPaddingTop() + lp.topMargin;
        final int headViewRight = headViewLeft + mHeadview.getMeasuredWidth();
        final int headViewBottom = headViewTop + mHeadview.getMeasuredHeight();
        mHeadview.layout(headViewLeft, headViewTop, headViewRight, headViewBottom);
        if (DEBUG)
            Log.d(TAG, String.format("@@ onLayout() : mHeadview [%d,%d,%d,%d] ", headViewLeft, headViewTop, headViewRight, headViewBottom));

        final View content = getContentView();
        lp = (MarginLayoutParams) content.getLayoutParams();
        final int childLeft = getPaddingLeft() + lp.leftMargin;
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop() + lp.topMargin;
        final int childRight = childLeft + content.getMeasuredWidth();
        final int childBottom = childTop + content.getMeasuredHeight();
        content.layout(childLeft, childTop, childRight, childBottom);
        if (DEBUG)
            Log.d(TAG, String.format("@@ onLayout() %d : content [%d,%d,%d,%d] ", getChildAt(0) == mHeadview ? 1 : 0, childLeft, childTop, childRight, childBottom));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!hasHeadview) {
            setCustomHeadview(new DefaultCustomHeadViewLayout(getContext()));
        }

        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host one child content view.");
        }

        measureChildWithMargins(mHeadview, widthMeasureSpec, 0, heightMeasureSpec, 0);

        final View content = getContentView();
        if (getChildCount() > 0) {
//            measureChildWithMargins(content, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) content.getLayoutParams();
            content.measure(
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - lp.leftMargin - lp.rightMargin,
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - lp.topMargin - lp.bottomMargin,
                            MeasureSpec.EXACTLY));
        }

        if (DEBUG) {
            Log.d(TAG, String.format("onMeasure(): swiperefreshlayout: width=%d, height=%d", getMeasuredWidth(), getMeasuredHeight()));
            Log.d(TAG, String.format("onMeasure(): headview: width=%d, height=%d", mHeadview.getMeasuredWidth(), mHeadview.getMeasuredHeight()));
            Log.d(TAG, String.format("onMeasure(): content: width=%d, height=%d", content.getMeasuredWidth(), content.getMeasuredHeight()));
        }
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host ONLY one child content view");
        }
        super.addView(child, index, params);
    }

    private boolean checkCanDoRefresh() {
        if (mRefreshCheckHandler != null) {
            return mRefreshCheckHandler.canRefresh();
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (DEBUG)
            Log.d(TAG, "dispatchTouchEvent() start ");
        boolean ret = super.dispatchTouchEvent(event);
        if (DEBUG)
            Log.d(TAG, "super.dispatchTouchEvent()" + ret);
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            ret = true;
        if (DEBUG)
            Log.d(TAG, "dispatchTouchEvent() " + ret);
        return ret;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG)
            Log.d(TAG, "onInterceptTouchEvent() start " + ev);
        ensureTarget();
        boolean handled = false;
        float curY = ev.getY();


        if (mReturningToStart && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        // record the first event:
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mCurrPercentage = 0;
            mDownEvent = MotionEvent.obtain(ev);
            mPrevY = mDownEvent.getY();
            mCheckValidMotionFlag = true;
            checkHorizontalMove = true;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float yDiff = curY - mDownEvent.getY();
            if (yDiff < 0)
                yDiff = -yDiff;

            if (enableHorizontalScroll) {

                MotionEvent event = MotionEvent.obtain(ev);
                int horizontalScrollDirection = ev.getX() > mDownEvent.getX() ? -1 : 1;
                float xDiff = Math.abs(ev.getX() - mDownEvent.getX());

                if (DEBUG) {
                    Log.d(TAG, "xDiff = " + xDiff + ", mTouchSlop= " + mTouchSlop + ", yDiff = " + yDiff + ", canViewScrollHorizontally(mTarget, event, 0) = " + canViewScrollHorizontally(mTarget, MotionEvent.obtain(event), horizontalScrollDirection)
                            + ", checkHorizontalMove = " + checkHorizontalMove + ", horizontalScrollDirection = " + horizontalScrollDirection);
                }
                if (isHorizontalScroll) {
                    if (DEBUG)
                        Log.d(TAG, "onInterceptTouchEvent(): in horizontal scroll");
                    mPrevY = curY;

                    checkHorizontalMove = false;
                    return false;
                } else if (xDiff <= mTouchSlop) {
                    checkHorizontalMove = true;
                    //return false;
                } else if (canViewScrollHorizontally(mTarget, event, horizontalScrollDirection) && checkHorizontalMove && xDiff > 2 * yDiff) {

                    if (DEBUG)
                        Log.d(TAG, "onInterceptTouchEvent(): start horizontal scroll");
                    mPrevY = curY;
                    isHorizontalScroll = true;

                    checkHorizontalMove = false;
                    return false;
                } else {
                    checkHorizontalMove = false;
                }
            }

            if (yDiff < mTouchSlop) {
                mPrevY = curY;
                return false;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (enableHorizontalScroll && isHorizontalScroll) {
                if (DEBUG)
                    Log.d(TAG, "onInterceptTouchEvent(): finish horizontal scroll");
                isHorizontalScroll = false;
                return false;
            }
        }

        if (isEnabled()) {
            MotionEvent event = MotionEvent.obtain(ev);
            if (!mReturningToStart && !canViewScrollUp(mTarget, event)) {
                handled = onTouchEvent(ev);
                if (DEBUG)
                    Log.d(TAG, "onInterceptTouchEvent(): handled = onTouchEvent(event);" + handled);
            } else {
                // keep updating last Y position when the event is not intercepted!
                mPrevY = ev.getY();
            }
        }
        boolean ret = !handled ? super.onInterceptTouchEvent(ev) : handled;
        if (DEBUG)
            Log.d(TAG, "onInterceptTouchEvent() " + ret);
        return ret;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (DEBUG)
            Log.d(TAG, "onTouchEvent() start");
        final int action = event.getAction();
        boolean handled = false;
        int curTargetTop = mTarget.getTop();
        mCurrentTargetOffsetTop = curTargetTop - mTargetOriginalTop;
        if (DEBUG) {
            Log.d(TAG, "onTouchEvent(): curTargetTop = " + curTargetTop + ", mCurrentTargetOffsetTop = " + mCurrentTargetOffsetTop);
        }
        switch (action) {

            case MotionEvent.ACTION_MOVE:
                if (mDownEvent != null && !mReturningToStart) {
                    final float eventY = event.getY();
                    float yDiff = eventY - mDownEvent.getY();


                    boolean isScrollUp = eventY - mPrevY > 0;

                    // if yDiff is large enough to be counted as one move event
                    if (mCheckValidMotionFlag && (yDiff > mTouchSlop || yDiff < -mTouchSlop)) {
                        mCheckValidMotionFlag = false;
                    }
                    // if refresh head moving with the mTarget is enabled
                    if (!enableTopRefreshingHead) {
                        // when it is refreshing
                        if (isRefreshing()) {
                            // scroll down
                            if (!isScrollUp) {
                                // when the top of mTarget reach the parent top
                                if (curTargetTop <= mTargetOriginalTop) {
                                    mPrevY = event.getY();
                                    handled = false;
                                    updateContentOffsetTop(mTargetOriginalTop, true);
                                    //mStopInterceptFlag = true;
                                    break;
                                }
                            }
                            // scroll up
                            else {
                                // when refresh head is entirely visible
                                if (curTargetTop >= mDistanceToTriggerSync) {
                                    mPrevY = event.getY();
                                    handled = true;
                                    updateContentOffsetTop(mDistanceToTriggerSync, true);
                                    break;
                                }
                            }

                            setTargetOffsetTop((int) ((eventY - mPrevY)), true);
                            mPrevY = event.getY();
                            handled = true;
                            break;
                        }
                    }
                    // keep refresh head above mTarget when refreshing
                    else {
                        if (isRefreshing()) {
                            mPrevY = event.getY();
                            handled = false;
                            break;
                        }
                    }

                    // curTargetTop is bigger than trigger
                    if (curTargetTop >= mDistanceToTriggerSync) {
                        // User movement passed distance; trigger a refresh
                        if (enableTopProgressBar)
                            mTopProgressBar.setTriggerPercentage(1f);

                        removeCallbacks(mCancel);
                        if (refresshMode == REFRESH_MODE_SWIPE) {
                            startRefresh();
                            handled = true;
                            break;
                        }
                    }
                    // curTargetTop is not bigger than trigger
                    else {
                        // Just track the user's movement
                        setTriggerPercentage(
                                mAccelerateInterpolator.getInterpolation(
                                        (float) mCurrentTargetOffsetTop / mTriggerOffset));

                        if (!isScrollUp && (curTargetTop < mTargetOriginalTop + 1)) {
                            removeCallbacks(mCancel);
                            mPrevY = event.getY();
                            handled = false;
                            // clear the progressBar
                            mTopProgressBar.setTriggerPercentage(0f);
                            break;
                        } else {
                            updatePositionTimeout(true);
                        }

                    }

                    handled = true;
                    if (curTargetTop >= mTargetOriginalTop && !isRefreshing())
                        setTargetOffsetTop((int) ((eventY - mPrevY) * mResistanceFactor), false);
                    else
                        setTargetOffsetTop((int) ((eventY - mPrevY)), true);
                    mPrevY = event.getY();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mRefreshing)
                    break;

                if (mCurrentTargetOffsetTop >= mTriggerOffset &&
                        refresshMode == REFRESH_MODE_PULL) {
                    startRefresh();
                    handled = true;
                } else {
                    updatePositionTimeout(false);
                    handled = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mDownEvent != null) {
                    mDownEvent.recycle();
                    mDownEvent = null;
                }
                break;
        }

        if (DEBUG)
            Log.d(TAG, "onTouchEvent() " + handled);
        return handled;
    }

    private void startRefresh() {
        if (!checkCanDoRefresh()) {
            updatePositionTimeout(false);
            return;
        }
        removeCallbacks(mCancel);
        setRefreshState(State.STATE_REFRESHING);
        setRefreshing(true);
        if (mListener != null)
            mListener.onRefresh();
    }

    private void updateContentOffsetTop(int targetTop, boolean changeHeightOnly) {
        final int currentTop = mTarget.getTop();
        if (targetTop < mTargetOriginalTop) {
            targetTop = mTargetOriginalTop;
        }
        setTargetOffsetTop(targetTop - currentTop, changeHeightOnly);
    }


    private void setTargetOffsetTop(int offset, boolean changeHeightOnly) {
        if (DEBUG)
            Log.d(TAG, "setTargetOffsetTop() offset = " + offset);
        if (offset == 0)
            return;
        // check whether the mTarget total top offset is going to be smaller than 0
        if (mCurrentTargetOffsetTop + offset >= 0) {
            mTarget.offsetTopAndBottom(offset);
            mHeadview.offsetTopAndBottom(offset);
            mCurrentTargetOffsetTop += offset;
            invalidate();
        } else {
            updateContentOffsetTop(mTargetOriginalTop, changeHeightOnly);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//            mTarget.setBottom(mOriginalOffsetBottom);
//        else {
//            //LayoutParams lp = mTarget.getLayoutParams();
//            //lp.height = mOriginalOffsetBottom - mTargetOriginalTop - mCurrentTargetOffsetTop;
//            //mTarget.setLayoutParams(lp);
//        }
        updateHeadViewState(offset, changeHeightOnly);

    }

    private void updatePositionTimeout(boolean isDelayed) {
        removeCallbacks(mCancel);
        if (isDelayed && mReturnToOriginalTimeout <= 0)
            return;
        postDelayed(mCancel, isDelayed ? mReturnToOriginalTimeout : 0);
    }


    public void setEnableHorizontalScroll(boolean isEnable) {
        enableHorizontalScroll = isEnable;
    }

    public void setEnableTopProgressBar(boolean isEnable) {
        enableTopProgressBar = isEnable;
        requestLayout();
    }

    public void enableTopRefreshingHead(boolean isEnable) {
        enableTopRefreshingHead = isEnable;
    }

    public boolean isEnableTopRefreshingHead() {
        return enableTopRefreshingHead;
    }

    public void setmReturnToOriginalTimeout(int mReturnToOriginalTimeout) {
        this.mReturnToOriginalTimeout = mReturnToOriginalTimeout;
    }

    public int getmRefreshCompleteTimeout() {
        return mRefreshCompleteTimeout;
    }

    public void setmRefreshCompleteTimeout(int mRefreshCompleteTimeout) {
        this.mRefreshCompleteTimeout = mRefreshCompleteTimeout;
    }

    public void setRefreshCheckHandler(RefreshCheckHandler handler) {
        mRefreshCheckHandler = handler;
    }

    public float getResistanceFactor() {
        return mResistanceFactor;
    }

    public void setResistanceFactor(float factor) {
        mResistanceFactor = factor;
    }

    public int getProgressBarHeight() {
        return mProgressBarHeight;
    }

    public void setProgressBarHeight(int height) {
        mProgressBarHeight = height;
        mConvertedProgressBarHeight =
                (int) (getResources().getDisplayMetrics().density * mProgressBarHeight);

    }

    public int getTriggerDistance() {
        return mTriggerDistance;
    }

    public void setTriggerDistance(int distance) {
        if (distance < 0)
            distance = 0;
        mTriggerDistance = distance;
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface RefreshCheckHandler {
        boolean canRefresh();
    }

    public interface CustomSwipeRefreshHeadLayout {
        void onStateChange(State state);
    }

    public static class State {
        public final static int STATE_NORMAL = 0;
        public final static int STATE_READY = 1;
        public final static int STATE_REFRESHING = 2;
        public final static int STATE_COMPLETE = 3;

        private int refreshState;
        private float percent;

        public State(int refreshState, float percent) {
            this.refreshState = refreshState;
            this.percent = percent;
        }

        void update(int refreshState, float percent) {
            this.refreshState = refreshState;
            this.percent = percent;
        }

        public int getRefreshState() {
            return refreshState;
        }

        public float getPercent() {
            return percent;
        }

        public String toString() {
            return "[refreshState = " + refreshState + ", percent = " + percent + "]";
        }
    }

    /**
     * Simple AnimationListener to avoid having to implement unneeded methods in
     * AnimationListeners.
     */
    private class BaseAnimationListener implements AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
