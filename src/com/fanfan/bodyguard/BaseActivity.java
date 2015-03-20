package com.fanfan.bodyguard;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		MyApp.getInstance().addActivty(this);
	}

	/**
	 * 按照宽调整百分比，宽高相同
	 * 
	 * @param patternview2
	 * @param i
	 */
	protected void adjustViewWidth(View v, int width) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v
				.getLayoutParams();
		int screenwidth = dms.widthPixels;
		// int screenheight = dms.heightPixels;
		params.width = (int) (screenwidth * (width / 100F));
		params.height = params.width;
		v.setLayoutParams(params);
	}

	/**
	 * 根据屏幕高调整控件大小，宽高相同
	 * 
	 * @param v
	 * @param height
	 *            高的百分比
	 */
	protected void adjustViewHeight(View v, float height) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v
				.getLayoutParams();
		// int screenwidth = dms.widthPixels;
		int screenheight = dms.heightPixels;
		params.height = (int) (screenheight * (height / 100F));
		params.width = params.height;
		v.setLayoutParams(params);
	}

	/**
	 * 根据屏幕大小调整控件大小
	 * 
	 * @param v
	 * @param height
	 *            高百分比
	 * @param width
	 *            宽百分比
	 */
	protected void adjustView(View v, float height, float width) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v
				.getLayoutParams();
		int screenwidth = dms.widthPixels;
		int screenheight = dms.heightPixels;
		params.height = (int) (screenheight * (height / 100F));
		params.width = (int) (screenwidth * (width / 100F));
		v.setLayoutParams(params);
	}

	/**
	 * 按照宽调整百分比，宽高相同
	 * 
	 * @param patternview2
	 * @param i
	 */
	protected void adjustViewWidth(LinearLayout v, int width) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v
				.getLayoutParams();
		int screenwidth = dms.widthPixels;
		// int screenheight = dms.heightPixels;
		params.width = (int) (screenwidth * (width / 100F));
		params.height = params.width;
		v.setLayoutParams(params);
	}

	/**
	 * 根据屏幕高调整控件大小，宽高相同
	 * 
	 * @param v
	 * @param height
	 *            高的百分比
	 */
	protected void adjustViewHeight(LinearLayout v, float height) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v
				.getLayoutParams();
		// int screenwidth = dms.widthPixels;
		int screenheight = dms.heightPixels;
		params.height = (int) (screenheight * (height / 100F));
		// params.width = params.height;
		v.setLayoutParams(params);
	}

	/**
	 * 根据屏幕大小调整控件大小
	 * 
	 * @param v
	 * @param height
	 *            高百分比
	 * @param width
	 *            宽百分比
	 */
	protected void adjustView(LinearLayout v, float height, float width) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v
				.getLayoutParams();
		int screenwidth = dms.widthPixels;
		int screenheight = dms.heightPixels;
		params.height = (int) (screenheight * (height / 100F));
		params.width = (int) (screenwidth * (width / 100F));
		v.setLayoutParams(params);
	}

}
