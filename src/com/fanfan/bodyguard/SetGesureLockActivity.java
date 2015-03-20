package com.fanfan.bodyguard;

import java.util.List;

import com.fanfan.utils.CLog;
import com.fanfan.utils.LockPatternUtils;
import com.fanfan.view.LockPatternView;
import com.fanfan.view.LockPatternView.Cell;
import com.fanfan.view.LockPatternView.DisplayMode;
import com.fanfan.view.LockPatternView.OnPatternListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

public class SetGesureLockActivity extends BaseActivity implements
		OnPatternListener {
	private LockPatternView lockView;
	private TextView tv_setLock;
	private LockPatternUtils patternUtils;

	private enum SetLockType {
		MODIFY, FIRSTSET
	}

	private SetLockType lockType;

	private enum TimesType {
		OLDCLOCK, FIRSTCLOCK, SECONDLOCK;
	}

	private TimesType currentTimestype;
	private String firstLock;
	private Handler mHandler;
	private static final int ORDER_CLEAR_PATTERN = 0xE0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setlock_activity);

		intiData();
		intiView();
	}

	private void intiData() {
		patternUtils = new LockPatternUtils(this);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ORDER_CLEAR_PATTERN:
					lockView.clearPattern();
					break;

				default:
					break;
				}
			}
		};
	}

	private void intiView() {
		lockView = (LockPatternView) findViewById(R.id.setlock_patternview);
		tv_setLock = (TextView) findViewById(R.id.tv_setlockhint);
		adjustViewWidth(lockView, 90);
		if (patternUtils.isSetGesureLock()) {
			// 已经设置过了，进行修改
			tv_setLock.setText(getResources().getString(R.string.lock_oldpwd));
			lockType = SetLockType.MODIFY;
			currentTimestype = TimesType.OLDCLOCK;
		} else {
			// 还没有设置
			tv_setLock.setText(getResources()
					.getString(R.string.lock_setnewpwd));
			lockType = SetLockType.FIRSTSET;
			currentTimestype = TimesType.FIRSTCLOCK;
		}
		lockView.setOnPatternListener(this);
	}

	@Override
	public void onPatternStart() {
		switch (currentTimestype) {
		case OLDCLOCK:
			tv_setLock.setText(getResources().getString(R.string.lock_oldpwd));
			break;
		case FIRSTCLOCK:
			tv_setLock.setText(getResources()
					.getString(R.string.lock_setnewpwd));
			break;
		case SECONDLOCK:
			tv_setLock.setText(getResources().getString(
					R.string.lock_setnewpwdtoo));
			break;
		default:
			break;
		}
		tv_setLock.setTextColor(getResources()
				.getColor(R.drawable.fanfan_white));
	}

	@Override
	public void onPatternCleared() {

	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {

	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		if (!patternUtils.isValid(pattern)) {
			tv_setLock.setText(getResources()
					.getString(R.string.lock_fourpoint));
			lockView.setDisplayMode(DisplayMode.Wrong);
			mHandler.sendEmptyMessageDelayed(ORDER_CLEAR_PATTERN, 500);
			return;
		}
		if (lockType == SetLockType.MODIFY) {
			int result = patternUtils.checkPattern(pattern);
			// 修改
			switch (currentTimestype) {
			case OLDCLOCK:
				judgeOldLock(result);
				break;
			case FIRSTCLOCK:
				firstLock = LockPatternUtils.patternToString(pattern);
				lockView.clearPattern();
				tv_setLock.setText(getResources().getString(
						R.string.lock_setnewpwdtoo));
				currentTimestype = TimesType.SECONDLOCK;
				break;
			case SECONDLOCK:
				judgeFristLock(firstLock,
						LockPatternUtils.patternToString(pattern), pattern);
				break;
			default:
				break;
			}
		} else if (lockType == SetLockType.FIRSTSET) {
			// 设置
			switch (currentTimestype) {
			case FIRSTCLOCK:
				firstLock = LockPatternUtils.patternToString(pattern);
				lockView.clearPattern();
				tv_setLock.setText(getResources().getString(
						R.string.lock_setnewpwdtoo));
				currentTimestype = TimesType.SECONDLOCK;
				break;
			case SECONDLOCK:
				judgeFristLock(firstLock,
						LockPatternUtils.patternToString(pattern), pattern);
				break;
			default:
				break;
			}
		}
		mHandler.sendEmptyMessageDelayed(ORDER_CLEAR_PATTERN, 500);
	}

	private void judgeFristLock(String first, String second, List<Cell> pattern) {
		if (TextUtils.isEmpty(first) || TextUtils.isEmpty(second)) {
			CLog.i("info", "lock is null first:" + first + " ,second:" + second);
			return;
		}
		if (first.equals(second)) {
			// 两次一致
			patternUtils.saveLockPattern(pattern);
			setResult(1);
			SetGesureLockActivity.this.finish();
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
		} else {
			tv_setLock.setText(getResources()
					.getString(R.string.lock_pwdnotfit));
			tv_setLock.setTextColor(getResources().getColor(R.color.red));
			lockView.setDisplayMode(DisplayMode.Wrong);
			currentTimestype = TimesType.SECONDLOCK;
		}
	}

	private void judgeOldLock(int result) {
		switch (result) {
		case LockPatternUtils.PWDERROR:
			tv_setLock
					.setText(getResources().getString(R.string.lock_pwderror));
			tv_setLock.setTextColor(getResources().getColor(R.color.red));
			lockView.setDisplayMode(DisplayMode.Wrong);
			currentTimestype = TimesType.OLDCLOCK;
			break;
		case LockPatternUtils.PWDCORRECT:
			patternUtils.clearLock();
			tv_setLock.setText(getResources()
					.getString(R.string.lock_setnewpwd));
			tv_setLock.setTextColor(getResources().getColor(
					R.drawable.fanfan_white));
			currentTimestype = TimesType.FIRSTCLOCK;
			break;
		case LockPatternUtils.NOPWD:
			tv_setLock.setText(getResources()
					.getString(R.string.lock_setnewpwd));
			patternUtils.clearLock();
			currentTimestype = TimesType.FIRSTCLOCK;
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 用户按下返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!patternUtils.isSetGesureLock()) {
				openHintDialog();
			} else {
				setResult(3);
				onBackPressed();
				overridePendingTransition(R.anim.push_right_in,
						R.anim.push_right_out);
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void openHintDialog() {
		new AlertDialog.Builder(this)
				.setMessage(getResources().getString(R.string.lock_sethint))
				.setNegativeButton("继续", null)
				.setPositiveButton("放弃", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(0);
						SetGesureLockActivity.this.finish();
						overridePendingTransition(R.anim.push_right_in,
								R.anim.push_right_out);
					}
				}).show();

	}
}
