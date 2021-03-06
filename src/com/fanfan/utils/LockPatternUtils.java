package com.fanfan.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.fanfan.view.LockPatternView;

public class LockPatternUtils {

	// private static final String TAG = "LockPatternUtils";
	private final static String KEY_LOCK_PWD = "lock_pwd";

	private static Context mContext;

	private static SharePreferUtils preferUtils;

	public static final int PWDERROR = 0;
	public static final int NOPWD = -1;
	public static final int PWDCORRECT = 1;

	public LockPatternUtils(Context context) {
		mContext = context;
		preferUtils = new SharePreferUtils(context);
		// mContentResolver = context.getContentResolver();
	}

	/**
	 * Deserialize a pattern.
	 * 
	 * @param string
	 *            The pattern serialized with {@link #patternToString}
	 * @return The pattern.
	 */
	public static List<LockPatternView.Cell> stringToPattern(String string) {
		List<LockPatternView.Cell> result = new ArrayList<LockPatternView.Cell>();

		final byte[] bytes = string.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			result.add(LockPatternView.Cell.of(b / 3, b % 3));
		}
		return result;
	}

	/**
	 * Serialize a pattern.
	 * 
	 * @param pattern
	 *            The pattern.
	 * @return The pattern in string form.
	 */
	public static String patternToString(List<LockPatternView.Cell> pattern) {
		if (pattern == null) {
			return "";
		}
		final int patternSize = pattern.size();

		byte[] res = new byte[patternSize];
		for (int i = 0; i < patternSize; i++) {
			LockPatternView.Cell cell = pattern.get(i);
			res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
		}
		return Arrays.toString(res);
	}

	public void saveLockPattern(List<LockPatternView.Cell> pattern) {
		preferUtils.setStringPrefer(KEY_LOCK_PWD, patternToString(pattern));
	}

	public String getLockPaternString() {
		return preferUtils.getStringPrefer(KEY_LOCK_PWD);
	}

	/**
	 * 判断输入的图案是否有效
	 * 
	 * @param pattern
	 * @return
	 */
	public boolean isValid(List<LockPatternView.Cell> pattern) {
		return pattern.size() >= 4;
	}

	/**
	 * 
	 * @return true 设置过了，false 还没有设置
	 */
	public boolean isSetGesureLock() {
		return !TextUtils.isEmpty(getLockPaternString());
	}

	public int checkPattern(List<LockPatternView.Cell> pattern) {
		String stored = getLockPaternString();
		if (!stored.isEmpty()) {
			return stored.equals(patternToString(pattern)) ? PWDCORRECT
					: PWDERROR;
		}
		return NOPWD;
	}

	public void clearLock() {
		saveLockPattern(null);
	}

}
