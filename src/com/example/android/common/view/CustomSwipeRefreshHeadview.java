package com.example.android.common.view;

/**
 * Created by tony.lxy on 2014/9/9.
 */
import android.content.Context;
import android.graphics.*;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.android.common.logger.Log;
import com.example.android.swiperefreshlayoutbasic.R;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		Log.i("lxy", "CustomSwipeRefreshHeadview(View parent)");
		mColor1 = COLOR1;
		mColor2 = COLOR2;
		mColor3 = COLOR3;
		mColor4 = COLOR4;

		setDefaultHeadLayout();
	}

	public CustomSwipeRefreshHeadview(Context context, ViewGroup layout) {
		super(context);
		Log.i("lxy", "CustomSwipeRefreshHeadview(View parent)");
		mColor1 = COLOR1;
		mColor2 = COLOR2;
		mColor3 = COLOR3;
		mColor4 = COLOR4;

		setHeadLayout(layout);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.v("lxy", "CustomSwipeRefreshHeadview.onLayout");
		// Never called here
		//mHeadLayout.layout(l,t,r,b);
	}

	public void setDefaultHeadLayout() {
		setHeadLayout(new CustomHeadViewLayout(getContext()));
	}

	public void setRefreshState(int state) {
		if (mHeadLayout instanceof EtaoSwipeRefreshHeadLayout) {
			((EtaoSwipeRefreshHeadLayout) mHeadLayout).setState(state);
		}
	}

	public void tryToUpdateLastUpdateTime() {
		if (mHeadLayout instanceof EtaoSwipeRefreshHeadLayout) {
			((EtaoSwipeRefreshHeadLayout) mHeadLayout).updateData();
		}
	}

	public CustomSwipeRefreshHeadview setHeadLayout(ViewGroup layout) {

		if(!(layout instanceof EtaoSwipeRefreshHeadLayout)) {
			throw new IllegalStateException(
					"ViewGroup must implements EtaoSwipeRefreshHeadLayout interface!");
		}

		mHeadLayout = layout;
		mHeadLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		addView(mHeadLayout);
		return this;
	}

	/**
	 * Set the four colors used in the progress animation. The first color will
	 * also be the color of the bar that grows in response to a user swipe
	 * gesture.
	 *
	 * @param color1 Integer representation of a color.
	 * @param color2 Integer representation of a color.
	 * @param color3 Integer representation of a color.
	 * @param color4 Integer representation of a color.
	 */
	void setColorScheme(int color1, int color2, int color3, int color4) {
		mColor1 = color1;
		mColor2 = color2;
		mColor3 = color3;
		mColor4 = color4;
	}

	/**
	 * @return Return whether the progress animation is currently running.
	 */
	boolean isRunning() {
		return mRunning || mFinishTime > 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//Log.i("lxy","headview.onDraw()");
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

		//Log.v("lxy", "mMainTextView.width = " + mMainTextView.getWidth() + ",mMainTextView.height = " + mMainTextView.getHeight());
		canvas.restoreToCount(restoreCount);
	}

	public void updateHeight(int height, int distanceToTriggerSync, boolean changeHeightOnly) {

		mBounds.bottom = height;
		//Log.i("lxy","mBounds.bottom = " + mBounds.bottom);
		if (changeHeightOnly) {
			invalidateView();
			return;
		}

		if (mBounds.bottom > distanceToTriggerSync) {
			//Log.i("lxy","change to STATE_READY");
			setRefreshState(STATE_READY);
		} else {
			//Log.i("lxy","change to STATE_NORMAL");
			setRefreshState(STATE_NORMAL);
		}

		invalidateView();
	}

	private void invalidateView() {
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

	public interface EtaoSwipeRefreshHeadLayout {
		void setState(int state);

		void updateData();

		String fetchData();
	}

	class CustomHeadViewLayout extends LinearLayout implements EtaoSwipeRefreshHeadLayout {

		private LinearLayout mContainer;

		private TextView mMainTextView;
		private TextView mSubTextView;
		private ImageView mImageView;
		private ProgressBar mProgressBar;

		private Animation mRotateUpAnim;
		private Animation mRotateDownAnim;
		private final int ROTATE_ANIM_DURATION = 180;

		private int mState = -1;

		public CustomHeadViewLayout(Context context) {
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
			if (state == STATE_REFRESHING) {    // 显示进度
				mImageView.clearAnimation();
				mImageView.setVisibility(View.INVISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
			} else {    // 显示箭头图片
				mImageView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
			}

			switch (state) {
				case STATE_NORMAL:
					if (mState == STATE_READY) {
						mImageView.startAnimation(mRotateDownAnim);
					}
					if (mState == STATE_REFRESHING) {
						mImageView.clearAnimation();
					}
					mMainTextView.setText("下拉刷新");
					break;
				case STATE_READY:

					if (mState != STATE_READY) {
						mImageView.clearAnimation();
						mImageView.startAnimation(mRotateUpAnim);
						mMainTextView.setText("释放刷新");
					}
					break;
				case STATE_REFRESHING:
					mMainTextView.setText("正在更新");

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
			//if (!TextUtils.isEmpty(mDataTag)) {
			if (true) {
				String time = fetchData();
				if (!TextUtils.isEmpty(time)) {
					mSubTextView.setVisibility(VISIBLE);
					mSubTextView.setText(time);
				} else {
					mSubTextView.setVisibility(GONE);
				}
			} else {
				mSubTextView.setVisibility(GONE);
			}
		}

		public String fetchData() {
			return "上次更新于" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		}

	}

}
