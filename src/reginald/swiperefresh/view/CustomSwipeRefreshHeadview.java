package reginald.swiperefresh.view;

/**
 * Created by tony.lxy on 2014/9/9.
 */

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Custom progress bar that shows a cycle of colors as widening circles that
 * overdraw each other. When finished, the bar is cleared from the inside out as
 * the main cycle continues. Before running, this can also indicate how close
 * the user is to triggering something (e.g. how far they need to pull down to
 * trigger a refresh).
 */
final class CustomSwipeRefreshHeadview extends ViewGroup {

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

    //states
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    public final static int STATE_COMPLETE = 3;

    // Colors used when rendering the animation,
    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;
    //private View mParent;

    private ViewGroup mHeadLayout;

    private Rect mBounds = new Rect();

    public CustomSwipeRefreshHeadview(Context context) {
        super(context);
        //Log.i("csr", "CustomSwipeRefreshHeadview(View parent)");
        mColor1 = COLOR1;
        mColor2 = COLOR2;
        mColor3 = COLOR3;
        mColor4 = COLOR4;

        setDefaultHeadLayout();
    }

    public CustomSwipeRefreshHeadview(Context context, ViewGroup layout) {
        super(context);
        //Log.i("csr", "CustomSwipeRefreshHeadview(View parent)");
        mColor1 = COLOR1;
        mColor2 = COLOR2;
        mColor3 = COLOR3;
        mColor4 = COLOR4;

        setHeadLayout(layout);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //Log.v("lxy", "CustomSwipeRefreshHeadview.onLayout");
        // Never called here
        //mHeadLayout.layout(l,t,r,b);
    }

    public void setDefaultHeadLayout() {
        setHeadLayout(new DefaultCustomHeadViewLayout(getContext()));
    }

    public void setRefreshState(int state) {
        if (mHeadLayout instanceof CustomSwipeRefreshHeadLayout) {
            ((CustomSwipeRefreshHeadLayout) mHeadLayout).setState(state);
        }
    }

    public void updateData() {
        if (mHeadLayout instanceof CustomSwipeRefreshHeadLayout) {
            ((CustomSwipeRefreshHeadLayout) mHeadLayout).updateData();
        }
    }

    public CustomSwipeRefreshHeadview setHeadLayout(ViewGroup layout) {

        if (!(layout instanceof CustomSwipeRefreshHeadLayout)) {
            throw new IllegalStateException(
                    "ViewGroup must implements CustomSwipeRefreshHeadLayout interface!");
        }

        mHeadLayout = layout;
        mHeadLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mHeadLayout);
        return this;
    }


    @Override
    public void onDraw(Canvas canvas) {
        final int width = mBounds.width();
        final int height = mBounds.height();
        int restoreCount = canvas.save();
        canvas.clipRect(mBounds);

        Rect mainTextRect = new Rect();
        mainTextRect.set(0, 0, width, height);

        mHeadLayout.setBackgroundColor(Color.TRANSPARENT);

        //Measure the view at the exact dimensions (otherwise the view won't center correctly)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(mainTextRect.width(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(mainTextRect.height(), View.MeasureSpec.EXACTLY);
        mHeadLayout.measure(widthSpec, heightSpec);
        //Lay the view out at the rect width and height
        mHeadLayout.layout(0, 0, mainTextRect.width(), mainTextRect.height());
        canvas.translate(mainTextRect.left, mainTextRect.top);
        mHeadLayout.draw(canvas);

        canvas.restoreToCount(restoreCount);
    }

    public void updateHeight(int height, int distanceToTriggerSync, boolean changeHeightOnly) {
        mBounds.bottom = height;

        if (changeHeightOnly) {
            invalidateView();
            return;
        }

        if (mBounds.bottom > distanceToTriggerSync) {
            setRefreshState(STATE_READY);
        } else {
            setRefreshState(STATE_NORMAL);
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
        void setState(int state);

        void updateData();
    }


}
