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
 * The SwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. The SwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * the listener is responsible for correctly determining when to actually
 * initiate a refresh of its content. If the listener determines there should
 * not be a refresh, it must call setRefreshing(false) to cancel any visual
 * indication of a refresh. If an activity wishes to show just the progress
 * animation, it should call setRefreshing(true). To disable the gesture and progress
 * animation, call setEnabled(false) on the view.
 * <p/>
 * <p> This layout should be made the parent of the view that will be refreshed as a
 * result of the gesture and can only support one direct child. This view will
 * also be made the target of the gesture and will be forced to match both the
 * width and the height supplied in this layout. The SwipeRefreshLayout does not
 * provide accessibility events; instead, a menu item must be provided to allow
 * refresh of the content wherever this gesture is used.</p>
 */
public class CustomSwipeRefreshLayout extends ViewGroup {

    // 不滑动时返回原状态的时间限制
    private static final long RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 200;

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
    private int mCurrentTargetOffsetTop;


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

    private void animateOffsetToTrigerPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToTrigerPosition.reset();
        mAnimateToTrigerPosition.setDuration(mMediumAnimationDuration);
        mAnimateToTrigerPosition.setAnimationListener(listener);
        mAnimateToTrigerPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToTrigerPosition);
    }

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
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
        Log.i("lxy", "CustomSwipeRefreshLayout(Context context)");
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("lxy", "CustomSwipeRefreshLayout(Context context, AttributeSet attrs)");
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

    public void enableTopProgressBar(boolean isEnable) {
        if (enableTopProgressBar == isEnable)
            return;

        enableTopProgressBar = isEnable;
        requestLayout();
    }

    public void enableTopRefreshingHead(boolean isEnable) {
        if (enableTopRefreshingHead == isEnable)
            return;

        enableTopRefreshingHead = isEnable;
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
    public void setRefreshing(boolean refreshing) {
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
                mReturnToTrigerPosition.run();

            } else {
                if (enableTopProgressBar) {
                    mTopProgressBar.stop();
                } else {
                    postInvalidate();
                }
                mHeadview.setRefreshState(CustomSwipeRefreshHeadview.STATE_COMPLETE);
                mReturnToStartPosition.run();
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
    public void setColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        //ensureTarget();
        final Resources res = getResources();
        final int color1 = res.getColor(colorRes1);
        final int color2 = res.getColor(colorRes2);
        final int color3 = res.getColor(colorRes3);
        final int color4 = res.getColor(colorRes4);
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

        // Log.i("lxy", " mTarget == null = " + ( mTarget == null?"yes":"no"));
        if (mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException(
                        "SwipeRefreshLayout can host only one direct child");
            }
            mTarget = getChildAt(1);
            mOriginalOffsetTop = mTarget.getTop() + getPaddingTop();
            mOriginalOffsetBottom = getChildAt(1).getHeight();
            Log.i("lxy", "mOriginalOffsetBottom = " + mOriginalOffsetBottom);
        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                //Log.i("lxy" , "parent.height = " + ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR + ", REFRESH_TRIGGER_DISTANCE * metrics.density = " + REFRESH_TRIGGER_DISTANCE * metrics.density);
                mDistanceToTriggerSync = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        //Log.i("lxy","draw(Canvas canvas)");
        super.draw(canvas);

        if (enableTopProgressBar) {
            mTopProgressBar.draw(canvas);
        }
        // refreshing while progressbar is disabled. redraw the headview until refreshing complete
        else if (isRefreshing()) {
            postInvalidate();
        }

        mHeadview.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.v("lxy", "CustomSwipeRefreshLayout.onLayout()");
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (enableTopProgressBar) {
            mTopProgressBar.setBounds(0, 0, width, mProgressBarHeight);
        } else {
            mTopProgressBar.setBounds(0, 0, 0, 0);
        }
        mHeadview.setBounds(0, 0, width, 0);

        if (getChildCount() == 0) {
            return;
        }
        final View child = getChildAt(1);
        final int childLeft = getPaddingLeft();
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);


        Log.i("lxy", "child.layout(" + childLeft + "," + childTop + ", " + childLeft + childWidth + ", " + childTop + childHeight + ");");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("SwipeRefreshLayout can host only one child content view");
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

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(1)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        boolean handled = false;
        if (mReturningToStart && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (isEnabled() && !mReturningToStart && !canChildScrollUp()) {
            handled = onTouchEvent(ev);
        }
        return !handled ? super.onInterceptTouchEvent(ev) : handled;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    private boolean toRefreshFlag = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.v("lxy","onTouchEvent(MotionEvent event)");
        final int action = event.getAction();
        boolean handled = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCurrPercentage = 0;
                mDownEvent = MotionEvent.obtain(event);
                mPrevY = mDownEvent.getY();
                toRefreshFlag = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDownEvent != null && !mReturningToStart) {
                    final float eventY = event.getY();
                    float yDiff = eventY - mDownEvent.getY();

                    if (yDiff > mTouchSlop || yDiff < -mTouchSlop) {

                        float offsetTop = (yDiff - mTouchSlop) * SWIPE_DOMP_FACTOR;

                        if (!enableTopRefreshingHead) {
                            if (isRefreshing()) {
                                // scroll down while refreshing
                                if ((yDiff - mTouchSlop) < 0) {
                                    // when refresh head is disappear
                                    if (mTarget.getTop() <= 0) {
                                        mPrevY = event.getY();
                                        handled = false;
                                        updateContentOffsetTop(mOriginalOffsetTop, true);
                                        break;
                                    }
                                }
                                // scroll up while refreshing
                                else {
                                    // when refresh head is disappear
                                    if (mTarget.getTop() >= mDistanceToTriggerSync) {
                                        mPrevY = event.getY();
                                        handled = true;
                                        updateContentOffsetTop((int) mDistanceToTriggerSync, true);
                                        break;
                                    }
                                    //setTargetOffsetTop((int)((eventY - mPrevY)),true);
                                }

                                setTargetOffsetTop((int) ((eventY - mPrevY)), true);
                                mPrevY = event.getY();
                                handled = true;
                                break;
                            }
                        } else {
                            if (isRefreshing() && offsetTop > 0) {
                                mPrevY = event.getY();
                                handled = false;
                                break;
                            }
                        }

                        if (yDiff < -mTouchSlop) {
                            handled = false;
                            break;
                        }
                        // User velocity passed min velocity; trigger a refresh
                        if (offsetTop > mDistanceToTriggerSync) {
                            // User movement passed distance; trigger a refresh
                            toRefreshFlag = true;
                            //handled = true;
                            removeCallbacks(mCancel);
                        } else {
                            toRefreshFlag = false;
                            // Just track the user's movement

                            setTriggerPercentage(
                                    mAccelerateInterpolator.getInterpolation(
                                            offsetTop / mDistanceToTriggerSync));

                            if (mPrevY > eventY && (mTarget.getTop() < 1)) {
                                // If the user puts the view back at the top, we
                                // don't need to. This shouldn't be considered
                                // cancelling the gesture as the user can restart from the top.
                                removeCallbacks(mCancel);
                            } else {
                                updatePositionTimeout();
                            }

                        }
                        mPrevY = event.getY();
                        handled = true;
                        //Log.i("lxy","offsetTop = " + offsetTop);
                        updateContentOffsetTop((int) (offsetTop), false);

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (toRefreshFlag) {
                    startRefresh();
                    toRefreshFlag = false;
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
        mHeadview.tryToUpdateLastUpdateTime();
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
        //Log.i("lxy","mTarget.getBottom() = " +  mTarget.getBottom() + ", mTarget.getBottom() = " + (mTarget.getBottom() - offset ));
        mTarget.offsetTopAndBottom(offset);
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
        postDelayed(mCancel, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
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
