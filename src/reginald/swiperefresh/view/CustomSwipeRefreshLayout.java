package reginald.swiperefresh.view;
/**
 * Created by tony.lxy on 2014/9/5.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.view.ViewCompat;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.annotation.TargetApi;

/**
 * The CustomSwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. And onRefreshingComplete()
 * should be called whenever the refreshing is complete. The CustomSwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * Two refresh mode are supported:
 * <li>swipe mode: android.support.v4.widget.SwipeRefreshLayout style with custom refresh head </li>
 * <li>pull mode: pull-to-refresh style with progress bar and custom refresh head</li>
 * <p/>
 */
public class CustomSwipeRefreshLayout extends ViewGroup {

    public static final int REFRESH_MODE_SWIPE = 1;
    public static final int REFRESH_MODE_PULL = 2;
    // 不滑动时返回原状态的时间限制
    private static final int RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 200;

    // 不滑动时返回原状态的时间限制
    private static final int REFRESH_COMPLETE_POSITION_TIMEOUT = 600;

    // 顶部滑动条动画加速度
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5f;

    // 顶部滑动条动画减速度
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    //顶部进度条高度
    private static final float PROGRESS_BAR_HEIGHT = 4;

    //最大触发滑动长度（父容器高度百分比）
    private static final float MAX_SWIPE_DISTANCE_FACTOR = .5f;

    //下拉至释放的触发滑动长度
    private static final int REFRESH_TRIGGER_DISTANCE = 80;

    //下拉阻尼系数
    private static final float SWIPE_DOMP_FACTOR = .5f;

    //顶部进度条view
    private CustomSwipeProgressBar mTopProgressBar;

    //刷新头部view
    private CustomSwipeRefreshHeadview mHeadview;

    //是否加载顶部进度条
    boolean enableTopProgressBar = false;
    boolean enableTopRefreshingHead = true;
    int refresshMode = REFRESH_MODE_SWIPE;
    //
    private View mTarget = null; //the content that gets pulled down

    private int mOriginalOffsetTop;
    private int mOriginalOffsetBottom;
    private OnRefreshListener mListener;
    private MotionEvent mDownEvent;
    private int mFrom;
    private boolean mRefreshing = false;
    private int mTouchSlop;
    private float mDistanceToTriggerSync = -1;
    private float mPrevY;
    private int mMediumAnimationDuration;
    private float mFromPercentage = 0;
    private float mCurrPercentage = 0;
    private int mProgressBarHeight;
    private int mCurrentTargetOffsetTop = 0;


    private int mReturnToOriginalTimeout = RETURN_TO_ORIGINAL_POSITION_TIMEOUT;
    private int mRefreshCompleteTimeout = REFRESH_COMPLETE_POSITION_TIMEOUT;

    //是否返回至原始状态
    private boolean mReturningToStart;
    private final DecelerateInterpolator mDecelerateInterpolator;

    private final AccelerateInterpolator mAccelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            if (mFrom != mOriginalOffsetTop) {
                targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTop(offset, true);
        }
    };


    private final Animation mAnimateToTrigerPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            if (mFrom > mDistanceToTriggerSync) {
                targetTop = (mFrom + (int) ((mDistanceToTriggerSync - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTop(offset, true);
        }
    };


    private final Animation mAnimateStayComplete = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            // DO NOTHING
        }
    };

    private void animateStayComplete(AnimationListener listener) {
        mAnimateStayComplete.reset();
        mAnimateStayComplete.setDuration(mRefreshCompleteTimeout);
        mAnimateStayComplete.setAnimationListener(listener);
        //mAnimateStayComplete.setInterpolator(mDecelerateInterpolator);
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

    private final Runnable mStayRefreshCompletePosition = new Runnable() {

        @Override
        public void run() {
            animateStayComplete(mStayCompleteListener);
        }

    };

    private final Runnable mReturnToTrigerPosition = new Runnable() {

        @Override
        public void run() {
            animateOffsetToTrigerPosition(mCurrentTargetOffsetTop + getPaddingTop(),
                    null);
        }

    };


    private Animation mShrinkTrigger = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            float percent = mFromPercentage + ((0 - mFromPercentage) * interpolatedTime);
            mTopProgressBar.setTriggerPercentage(percent);
        }
    };

    private final AnimationListener mStayCompleteListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mReturnToStartPosition.run();
            mRefreshing = false;
        }
    };

    private final AnimationListener mReturnToStartPositionListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // Once the target content has returned to its start position, reset
            // the target offset to 0
            mCurrentTargetOffsetTop = 0;
        }
    };

    private final AnimationListener mShrinkAnimationListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrPercentage = 0;
        }
    };

    private final Runnable mReturnToStartPosition = new Runnable() {

        @Override
        public void run() {
            //Log.i("csr", "run mReturnToStartPosition");
            mReturningToStart = true;
            animateOffsetToStartPosition(mCurrentTargetOffsetTop + getPaddingTop(),
                    mReturnToStartPositionListener);
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
            animateOffsetToStartPosition(mCurrentTargetOffsetTop + getPaddingTop(),
                    mReturnToStartPositionListener);
        }

    };

    /**
     * Simple constructor to use when creating a CustomSwipeRefreshLayout from code.
     *
     * @param context
     */
    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
        Log.i("csr", "CustomSwipeRefreshLayout(Context context)");
    }

    /**
     * Constructor that is called when inflating CustomSwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("csr", "CustomSwipeRefreshLayout(Context context, AttributeSet attrs)");
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mTopProgressBar = new CustomSwipeProgressBar(this);
        mHeadview = new CustomSwipeRefreshHeadview(context);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mProgressBarHeight = (int) (metrics.density * PROGRESS_BAR_HEIGHT);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mAccelerateInterpolator = new AccelerateInterpolator(ACCELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();

        // child index of mHeadview = 0; child index of mTarget = 1;
        addView(mHeadview);
    }

    public void setRefreshMode(int mode) {
        switch (mode) {
            case REFRESH_MODE_PULL:
                refresshMode = REFRESH_MODE_PULL;
                mHeadview.setRefreshState(CustomSwipeRefreshHeadview.STATE_NORMAL);
                break;
            case REFRESH_MODE_SWIPE:
                refresshMode = REFRESH_MODE_SWIPE;
                enableTopRefreshingHead(false);
                mHeadview.setRefreshState(CustomSwipeRefreshHeadview.STATE_NORMAL);
                break;
            default:
                throw new IllegalStateException(
                        "refresh mode " + mode + " is node supported in CustomSwipeRefreshLayout");

        }
    }

    public int getRefreshMode() {
        return refresshMode;
    }

    public void enableTopProgressBar(boolean isEnable) {
        if (enableTopProgressBar == isEnable)
            return;

        enableTopProgressBar = isEnable;
        requestLayout();
    }

    public void enableTopRefreshingHead(boolean isEnable) {
        enableTopRefreshingHead = isEnable;
    }

    public boolean isEnableTopRefreshingHead() {
        return enableTopRefreshingHead;
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
                } else {
                    postInvalidate();
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
                } else {
                    postInvalidate();
                }
                if (refresshMode == REFRESH_MODE_PULL) {
                    mRefreshing = true;
                    removeCallbacks(mReturnToStartPosition);
                    removeCallbacks(mCancel);
                    mHeadview.setRefreshState(CustomSwipeRefreshHeadview.STATE_COMPLETE);
                    mStayRefreshCompletePosition.run();
                } else if (refresshMode == REFRESH_MODE_SWIPE) {
                    mRefreshing = false;
                    mReturnToStartPosition.run();
                }

            }
        }
    }


    public void onRefreshingComplete() {
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
        //ensureTarget();
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

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid out yet.
        if (mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException(
                        "CustomSwipeRefreshLayout can host only one direct child");
            }
            mTarget = getChildAt(1);
            mOriginalOffsetTop = mTarget.getTop() + getPaddingTop();
            mOriginalOffsetBottom = getChildAt(1).getHeight();
        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDistanceToTriggerSync = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (enableTopProgressBar) {
            mTopProgressBar.draw(canvas);
        }

        mHeadview.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (enableTopProgressBar) {
            mTopProgressBar.setBounds(0, 0, width, mProgressBarHeight);
        } else {
            mTopProgressBar.setBounds(0, 0, 0, 0);
        }
        mHeadview.setBounds(0, 0, width, mCurrentTargetOffsetTop);

        if (getChildCount() == 0) {
            return;
        }
        final View child = getChildAt(1);
        final int childLeft = getPaddingLeft();
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host only one child content view");
        }
        if (getChildCount() > 0) {
            getChildAt(1).measure(
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                            MeasureSpec.EXACTLY));
        }


    }


    public void setContent(View content) {
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host only one child content view");
        }
        addView(content);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        boolean ret;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                ret = absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                ret = mTarget.getScrollY() > 0;
            }
        } else {
            ret = ViewCompat.canScrollVertically(mTarget, -1);
        }

        return ret;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        // to be further modified here ...

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
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
            mToRefreshFlag = false;
            mCheckValidMotionFlag = true;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float yDiff = curY - mDownEvent.getY();
            if (yDiff < 0)
                yDiff = -yDiff;

            if (yDiff < mTouchSlop) {
                mPrevY = curY;
                return false;
            }
        }

        if (isEnabled()) {
            if (!mReturningToStart && !canChildScrollUp()) {
                handled = onTouchEvent(ev);
            } else {
                // keep updating last Y position when the event is not intercepted!
                mPrevY = ev.getY();
            }
        }

        return !handled ? super.onInterceptTouchEvent(ev) : handled;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    private boolean mToRefreshFlag = false;
    private boolean mCheckValidMotionFlag = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        boolean handled = false;

        switch (action) {

            case MotionEvent.ACTION_MOVE:
                if (mDownEvent != null && !mReturningToStart) {
                    final float eventY = event.getY();
                    float yDiff = eventY - mDownEvent.getY();
                    int curTargetTop = mTarget.getTop();
                    mCurrentTargetOffsetTop = curTargetTop;
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
                                if (curTargetTop <= 0) {
                                    mPrevY = event.getY();
                                    handled = false;
                                    updateContentOffsetTop(mOriginalOffsetTop, true);
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
                                    updateContentOffsetTop((int) mDistanceToTriggerSync, true);
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
                        if(enableTopProgressBar)
                            mTopProgressBar.setTriggerPercentage(1f);

                        removeCallbacks(mCancel);
                        if (refresshMode == REFRESH_MODE_SWIPE) {
                            mToRefreshFlag = false;
                            startRefresh();
                            handled = true;
                            break;
                        } else if (refresshMode == REFRESH_MODE_PULL) {
                            mToRefreshFlag = true;
                        }
                    }
                    // curTargetTop is not bigger than trigger
                    else {
                        mToRefreshFlag = false;
                        // Just track the user's movement

                        setTriggerPercentage(
                                mAccelerateInterpolator.getInterpolation(
                                        curTargetTop / mDistanceToTriggerSync));

                        if (!isScrollUp && (curTargetTop < 1)) {
                            removeCallbacks(mCancel);
                            mPrevY = event.getY();
                            handled = false;
                            // clear the progressBar
                            mTopProgressBar.setTriggerPercentage(0f);
                            break;
                        } else {
                            updatePositionTimeout();
                        }

                    }

                    handled = true;
                    if (curTargetTop > 0 && !isRefreshing())
                        setTargetOffsetTop((int) ((eventY - mPrevY) * SWIPE_DOMP_FACTOR), false);
                    else
                        setTargetOffsetTop((int) ((eventY - mPrevY)), true);
                    mPrevY = event.getY();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mToRefreshFlag && refresshMode == REFRESH_MODE_PULL) {
                    startRefresh();
                    mToRefreshFlag = false;
                    handled = true;
                    break;
                }

            case MotionEvent.ACTION_CANCEL:
                if (mDownEvent != null) {
                    mDownEvent.recycle();
                    mDownEvent = null;
                }
                break;
        }
        return handled;
    }

    private void startRefresh() {
        removeCallbacks(mCancel);
        mHeadview.setRefreshState(CustomSwipeRefreshHeadview.STATE_REFRESHING);
        setRefreshing(true);
        mListener.onRefresh();
    }

    private void updateContentOffsetTop(int targetTop, boolean changeHeightOnly) {
        final int currentTop = mTarget.getTop();

        if (targetTop < 0) {
            targetTop = 0;
        }

        setTargetOffsetTop(targetTop - currentTop, changeHeightOnly);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setTargetOffsetTop(int offset, boolean changeHeightOnly) {


        // check whether the mTarget.getTop() is going to be smaller than 0
        if(mCurrentTargetOffsetTop + offset >= 0)
            mTarget.offsetTopAndBottom(offset);
        else
            updateContentOffsetTop(0,changeHeightOnly);

        mCurrentTargetOffsetTop = mTarget.getTop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mTarget.setBottom(mOriginalOffsetBottom);
        else {
            //LayoutParams lp = mTarget.getLayoutParams();
            //lp.height = mOriginalOffsetBottom - mOriginalOffsetTop - mCurrentTargetOffsetTop;
            //mTarget.setLayoutParams(lp);
        }
        mHeadview.updateHeight(mTarget.getTop(), (int) mDistanceToTriggerSync, changeHeightOnly);
    }

    private void updatePositionTimeout() {
        removeCallbacks(mCancel);
        postDelayed(mCancel, mReturnToOriginalTimeout);
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

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        public void onRefresh();
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
