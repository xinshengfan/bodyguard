package com.fanfan.backgroundService;

import com.fanfan.bodyguard.R;
import com.fanfan.utils.CLog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class WallpaperActivity extends Activity {
	private InnerRecevier innerRecevier;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		innerRecevier = new InnerRecevier();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		filter.addAction("com.fanfan.action.finishActivity");
		registerReceiver(innerRecevier, filter);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.gc();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (innerRecevier != null) {
			unregisterReceiver(innerRecevier);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_HOME:
			this.moveTaskToBack(true);
			return true;
		default:
			break;
		}

		return false;
	}

	class InnerRecevier extends BroadcastReceiver {
		final String SYSTEM_DIALOG_REASON_KEY = "reason";
		final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
		final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reason != null) {
					if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
						WallpaperActivity.this.onRestart();
					} else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
						WallpaperActivity.this.onRestart();
					} else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
					}
				}
			} else if ("com.fanfan.action.finishActivity".equals(action)) {
				WallpaperActivity.this.finish();
			}
		}
	}
}
