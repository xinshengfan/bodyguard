package com.fanfan.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.fanfan.bodyguard.R;

public class SlipSwitchView extends View implements OnTouchListener {

	private Bitmap switch_on_Bkg, switch_off_Bkg, slip_Btn;
	private Rect on_Rect, off_Rect;

	private boolean isSlipping = false;
	private boolean isSwitchOn = false;

	private float previousX, currentX;

	private OnSwitchListener onSwitchListener;
	private boolean isSwitchListenerOn = false;
	private int mGreenColor = Color.rgb(108, 181, 40);
	private int mWhiteColor = Color.rgb(255, 255, 255);
	private String mLeftlabelText;
	private String mRightlabelText;
	private float mMargin = 0.0f;
	private boolean mCanMoveable = true;
	private boolean mAnimEnable = true;

	public SlipSwitchView(Context context) {
		super(context);
		init();
	}

	public SlipSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.SlipSwitchView);
		mAnimEnable = (boolean) array.getBoolean(
				R.styleable.SlipSwitchView_anim, true);
	}

	private void init() {
		setOnTouchListener(this);
		switch_on_Bkg = BitmapFactory.decodeResource(getResources(),
				R.drawable.switch_on_bg);
		switch_off_Bkg = BitmapFactory.decodeResource(getResources(),
				R.drawable.switch_off_bg);
		slip_Btn = BitmapFactory.decodeResource(getResources(),
				R.drawable.switch_ball);
		if (switch_on_Bkg != null && slip_Btn != null) {
			on_Rect = new Rect(switch_off_Bkg.getWidth() - slip_Btn.getWidth(),
					0, switch_off_Bkg.getWidth(), slip_Btn.getHeight());
			off_Rect = new Rect(0, 0, slip_Btn.getWidth(), slip_Btn.getHeight());
		}
	}

	public void setMargin(float margin) {
		this.mMargin = margin;
	}

	public void setImageResource(int switchOnBkg, int switchOffBkg, int slipBtn) {
		switch_on_Bkg = BitmapFactory.decodeResource(getResources(),
				switchOnBkg);
		switch_off_Bkg = BitmapFactory.decodeResource(getResources(),
				switchOffBkg);
		slip_Btn = BitmapFactory.decodeResource(getResources(), slipBtn);

		on_Rect = new Rect(switch_off_Bkg.getWidth() - slip_Btn.getWidth(), 0,
				switch_off_Bkg.getWidth(), slip_Btn.getHeight());
		off_Rect = new Rect(0, 0, slip_Btn.getWidth(), slip_Btn.getHeight());
	}

	public void setLabelText(String leftLabel, String rightLabel) {
		mLeftlabelText = leftLabel;
		mRightlabelText = rightLabel;
	}

	public void setSwitchState(boolean switchState) {
		isSwitchOn = switchState;
	}

	public void setSlipButtonCanMoveAble(boolean moveable) {
		mCanMoveable = moveable;
	}

	public boolean getSwitchState() {
		return isSwitchOn;
	}

	protected void updateSwitchState(boolean switchState) {
		isSwitchOn = switchState;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (switch_on_Bkg == null || slip_Btn == null) {
			return;
		}
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		int leftTextColor;
		int rightTextColor;
		float left_SlipBtn;
		int mMaxDis = switch_on_Bkg.getWidth() - slip_Btn.getWidth();
		float margin = 0;

		Log.v("----currentX----", String.valueOf(currentX));

		if (isSlipping) {
			if (currentX < (switch_on_Bkg.getWidth() / 2)) {
				canvas.drawBitmap(switch_off_Bkg, matrix, paint);
				margin = mMargin;
			} else {
				canvas.drawBitmap(switch_on_Bkg, matrix, paint);
				margin = -mMargin;
			}

			if (currentX > switch_on_Bkg.getWidth()) {
				left_SlipBtn = switch_on_Bkg.getWidth() - slip_Btn.getWidth();
			} else {
				left_SlipBtn = currentX - slip_Btn.getWidth() / 2;
			}
		} else {
			if (isSwitchOn) {
				left_SlipBtn = on_Rect.left;
				margin = -mMargin;
				canvas.drawBitmap(switch_on_Bkg, matrix, paint);

			} else {

				left_SlipBtn = off_Rect.left;
				margin = mMargin;
				canvas.drawBitmap(switch_off_Bkg, matrix, paint);
			}
		}

		if (left_SlipBtn < 0) {
			left_SlipBtn = 0;
		} else if (left_SlipBtn > switch_on_Bkg.getWidth()
				- slip_Btn.getWidth()) {
			left_SlipBtn = switch_on_Bkg.getWidth() - slip_Btn.getWidth();
		}
		int red = (int) ((Color.red(mWhiteColor) - Color.red(mGreenColor))
				* left_SlipBtn * 1.0f / mMaxDis);
		int green = (int) ((Color.green(mWhiteColor) - Color.green(mGreenColor))
				* left_SlipBtn * 1.0f / mMaxDis);
		int blue = (int) ((Color.blue(mWhiteColor) - Color.blue(mGreenColor))
				* left_SlipBtn * 1.0f / mMaxDis);
		float rate = getResources().getDisplayMetrics().widthPixels / 480.0f;
		rightTextColor = Color.rgb(Color.red(mWhiteColor) - red,
				Color.green(mWhiteColor) - green, Color.blue(mWhiteColor)
						- blue);
		leftTextColor = Color.rgb(Color.red(mGreenColor) + red,
				Color.green(mGreenColor) + green, Color.blue(mGreenColor)
						+ blue);
		canvas.drawBitmap(slip_Btn, left_SlipBtn + margin,
				(this.getHeight() - slip_Btn.getHeight()) / 2, paint);

		paint.setTextSize(20 * rate);
		FontMetrics fm = null;
		fm = paint.getFontMetrics();
		float fFontHeight = (float) Math.ceil(fm.descent - fm.ascent);
		int width = 0;
		if (mLeftlabelText != null) {
			width = (int) paint.measureText(mLeftlabelText);
			paint.setColor(leftTextColor);
			canvas.drawText(mLeftlabelText, (slip_Btn.getWidth() - width) / 2
					+ Math.abs(margin), (this.getHeight() - fFontHeight / 2),
					paint);
		}
		if (mRightlabelText != null) {
			width = (int) paint.measureText(mRightlabelText);
			paint.setColor(rightTextColor);
			canvas.drawText(
					mRightlabelText,
					this.getWidth() - (slip_Btn.getWidth() + width) / 2
							- Math.abs(margin),
					(this.getHeight() - fFontHeight / 2), paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		if (switch_on_Bkg != null)
			setMeasuredDimension(switch_on_Bkg.getWidth(),
					switch_on_Bkg.getHeight());
		else {
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (mAnimEnable) {
				currentX = event.getX();
				if (!mCanMoveable) {
					isSlipping = false;
					boolean previousSwitchState = isSwitchOn;

					if (event.getX() >= (switch_on_Bkg.getWidth() / 2)) {
						isSwitchOn = true;
					} else {
						isSwitchOn = false;
					}

					if (isSwitchListenerOn
							&& (previousSwitchState != isSwitchOn)) {
						onSwitchListener.onSwitched(this, isSwitchOn);
					}
				}
			}
			break;

		case MotionEvent.ACTION_DOWN:
			if (mAnimEnable) {
				if (event.getX() > switch_on_Bkg.getWidth()
						|| event.getY() > switch_on_Bkg.getHeight()) {
					return false;
				}

				isSlipping = true;
				previousX = event.getX();
				currentX = previousX;
			}
			break;

		case MotionEvent.ACTION_UP:
			isSlipping = false;
			boolean previousSwitchState = isSwitchOn;

			if (event.getX() >= (switch_on_Bkg.getWidth() / 2)) {
				isSwitchOn = true;
			} else {
				isSwitchOn = false;
			}

			if (isSwitchListenerOn && (previousSwitchState != isSwitchOn)) {
				onSwitchListener.onSwitched(this, isSwitchOn);
			}
			break;

		default:
			break;
		}

		invalidate();
		return true;
	}

	public void setOnSwitchListener(OnSwitchListener listener) {
		onSwitchListener = listener;
		if (onSwitchListener != null) {
			isSwitchListenerOn = true;
		} else {
			isSwitchListenerOn = false;
		}
	}

	public interface OnSwitchListener {
		abstract void onSwitched(View view, boolean isSwitchOn);
	}

}
