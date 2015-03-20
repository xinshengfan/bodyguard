package com.fanfan.utils;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

public class GPSUtils {
	/**
	 * �ж�GPS�Ƿ�����GPS����AGPS����һ������Ϊ�ǿ�����
	 * 
	 * @param context
	 * @return true ��ʾ����
	 */
	public static final boolean isOPen(final Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		// ͨ��GPS���Ƕ�λ����λ������Ծ�ȷ���֣�ͨ��24�����Ƕ�λ��������Ϳտ��ĵط���λ׼ȷ���ٶȿ죩
		boolean gps = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// ͨ��WLAN���ƶ�����(3G/2G)ȷ����λ�ã�Ҳ����AGPS������GPS��λ����Ҫ���������ڻ��ڸ������Ⱥ��ï�ܵ����ֵȣ��ܼ��ĵط���λ��
		// boolean network = locationManager
		// .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		// if (gps || network) {
		// return true;
		// }
		// return false;
		return gps;
	}

	/**
	 * ǿ�ư��û���GPS
	 * 
	 * @param context
	 */
	public static final void openGPS(Context context) {
		Intent GPSIntent = new Intent();
		GPSIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
		GPSIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		Settings.Secure.setLocationProviderEnabled(
				context.getContentResolver(), LocationManager.GPS_PROVIDER,
				true);
	}

	public static void closeGPS(Context context) {
		Intent GPSIntent = new Intent();
		GPSIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
		GPSIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		Settings.Secure.setLocationProviderEnabled(
				context.getContentResolver(), LocationManager.GPS_PROVIDER,
				false);
	}
}
