package com.reginald.swiperefresh;

/**
 * Created by tony.lxy on 2014/9/9.
 */

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;


public final class CustomSwipeRefreshHeadview extends ViewGroup {

    // Default progress animation colors are grays.
    private final static int COLOR1 = 0xB3000000;
    private final static int COLOR2 = 0x80000000;
    private final static int COLOR3 = 0x4d000000;
    private final static int COLOR4 = 0x1a000000;

    // The duration of the animation cycle.
    private static final int ANIMATION_DURATION_MS = 2000;

    // The duration of the animation to clear the bar.
    private static final int FINISH_ANIMATION_DURATION_MS = 1000;

    // Interpolator for varying the speed of the animation.
    private static final Interpolator INTERPOLATOR = CustomInterpolator.getInstance();

    private final Paint mPaint = new Paint();
    private final RectF mClipRect = new RectF();
    private float mTriggerPercentage;
    private long mStartTime;
    private long mFinishTime;
    private boolean mRunning;



    // Colors used when rendering the animation,
    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;
    //private View mParent;

    private View mHeadLayout;

    private Rect mBounds = new Rect();
    private float triggerDistance;
    State currentState = new State(State.STATE_NORMAL,0f);

    public CustomSwipeRefreshHeadview(Context context) {
        super(context);
        mColor1 = COLOR1;
        mColor2 = COLOR2;
        mColor3 = COLOR3;
        mColor4 = COLOR4;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Never called here
        //mHeadLayout.layout(l,t,r,b);
    }

    public void setTriggerDistance(float distance){
        triggerDistance = distance;
    }

    public void setDefaultHeadLayout() {
        setHeadLayout(new DefaultCustomHeadViewLayout(getContext()));
    }

    public void setRefreshState(int state) {
        currentState = new State(state,(float)mBounds.height()/triggerDistance);
        ((CustomSwipeRefreshHeadLayout) mHeadLayout).onStateChange(currentState);
    }

    public CustomSwipeRefreshHeadview setHeadLayout(View layout) {

        if (!(layout instanceof CustomSwipeRefreshHeadLayout)) {
            throw new IllegalStateException(
                    "ViewGroup must implements CustomSwipeRefreshHeadLayout interface!");
        }

        removeAllViews();
        mHeadLayout = layout;
        //mHeadLayout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(mHeadLayout);
        return this;
    }


    @Override
    public void onDraw(Canvas canvas) {
        final int width = mBounds.width();
        final int height = mBounds.height();
        int restoreCount = canvas.save();
        canvas.clipRect(mBounds);

        Rect mainRect = new Rect();
        mainRect.set(0, 0, width, height);

        mHeadLayout.setBackgroundColor(Color.TRANSPARENT);

        //Measure the view at the exact dimensions (otherwise the view won't center correctly)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(mainRect.width(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(mainRect.height(), View.MeasureSpec.EXACTLY);
        mHeadLayout.measure(widthSpec, heightSpec);
        //Lay the view out at the rect width and height
        mHeadLayout.layout(0, 0, mainRect.width(), mainRect.height());
        canvas.translate(mainRect.left, mainRect.top);
        mHeadLayout.draw(canvas);

        canvas.restoreToCount(restoreCount);
    }

    public void updateHeight(int height, int distanceToTriggerSync, boolean changeHeightOnly) {
        mBounds.bottom = height;

        if(changeHeightOnly) {
            setRefreshState(currentState.refreshState);
        }else {
            if (mBounds.bottom > distanceToTriggerSync) {
                setRefreshState(State.STATE_READY);
            } else {
                setRefreshState(State.STATE_NORMAL);
            }
        }

        invalidateView();
    }

    protected void invalidateView() {
        if (getParent() != null && getParent() instanceof View)
            ((View) getParent()).postInvalidate();

    }

    /**
     * Set the drawing bounds of this SwipeProgressBar.
     */
    void setBounds(int left, int top, int right, int bottom) {
        mBounds.left = left;
        mBounds.top = top;
        mBounds.right = right;
        mBounds.bottom = bottom;
    }

    public interface CustomSwipeRefreshHeadLayout {
        void onStateChange(State state);
    }

    public class State{
        //states
        public final static int STATE_NORMAL = 0;
        public final static int STATE_READY = 1;
        public final static int STATE_REFRESHING = 2;
        public final static int STATE_COMPLETE = 3;

        private int refreshState;
        private float percent;
        public State(int refreshState, float percent){
            this.refreshState = refreshState;
            this.percent = percent;
        }
        public int getRefreshState(){
            return refreshState;
        }
        public float getPercent(){
            return percent;
        }
        public String toString(){
            return "[refreshState = " + refreshState + ", percent = " + percent + "]";
        }
    }

}
