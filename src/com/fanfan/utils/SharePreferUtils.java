package com.fanfan.utils;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferUtils {
	private SharedPreferences pf;
	private static final String KEY_FIRST_TIME = "keyfirsttime";
	private static final String KEY_FIRST_MAINACTIVITY = "keyfirstMainActivity";
	private static final String KEY_FIRST_SETTINGACTIVITY = "keyfirstSettingActivity";

	public SharePreferUtils(Context context) {
		// pf = PreferenceManager.getDefaultSharedPreferences(context);
		pf = context.getSharedPreferences(G.SHAREPREFER_NAME,
				Context.MODE_PRIVATE);
	}

	/**
	 * 判断该程序是否是第一次使用,该方法只能使用一次；
	 * 
	 * @return　是返回true,否则返回false
	 */
	public boolean isFirstUse() {
		boolean firstTime = pf.getBoolean(KEY_FIRST_TIME, true);
		if (firstTime) {
			pf.edit().putBoolean(KEY_FIRST_TIME, false).commit();
		}
		return firstTime;
	}

	/**
	 * 判断MainActivity序是否是第一次使用,该方法只能使用一次；
	 * 
	 * @return　是返回true,否则返回false
	 */
	public boolean isFirstMainActivity() {
		boolean firstTime = pf.getBoolean(KEY_FIRST_MAINACTIVITY, true);
		if (firstTime) {
			pf.edit().putBoolean(KEY_FIRST_MAINACTIVITY, false).commit();
		}
		return firstTime;
	}

	/**
	 * 判断SettingActivity是否是第一次使用,该方法只能使用一次；
	 * 
	 * @return　是返回true,否则返回false
	 */
	public boolean isFirstSettingActivity() {
		boolean firstTime = pf.getBoolean(KEY_FIRST_SETTINGACTIVITY, true);
		if (firstTime) {
			pf.edit().putBoolean(KEY_FIRST_SETTINGACTIVITY, false).commit();
		}
		return firstTime;
	}

	public void setBooleanPrefer(String Key, boolean isSeclect) {
		pf.edit().putBoolean(Key, isSeclect).commit();
	}

	public boolean getBooleanPrefer(String key) {
		return pf.getBoolean(key, false);
	}

	public void setIntPrefer(String key, int choice) {
		pf.edit().putInt(key, choice).commit();
	}

	public int getIntPrefer(String key) {
		return pf.getInt(key, 0);
	}

	public void setStringPrefer(String key, String value) {
		pf.edit().putString(key, value).commit();
	}

	public String getStringPrefer(String key) {
		return pf.getString(key, "");
	}

	public void setStringSetPrefer(String key, Set<String> values) {
		pf.edit().putStringSet(key, values).commit();
	}

	public Set<String> getStringSetPrefer(String key) {
		return pf.getStringSet(key, new HashSet<String>());
	}

	public void setLongPrefer(String key, long value) {
		pf.edit().putLong(key, value).commit();
	}

	public long getLongPrefer(String key) {
		return pf.getLong(key, 0);
	}

	public void deleteKey(String key) {
		pf.edit().remove(key).commit();
	}
}
