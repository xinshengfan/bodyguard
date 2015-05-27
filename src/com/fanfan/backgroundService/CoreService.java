package com.fanfan.backgroundService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fanfan.bodyguard.MyApp;
import com.fanfan.utils.CLog;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class CoreService extends Service {
	private Timer mTimer;
	private Handler mHandler;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// CLog.i("info", " ,��ǰapp�Ƿ��ں�̨��"
				//		+ isBackground(CoreService.this));
				if (isHome() && isBackground(CoreService.this)) {
					mHandler.sendEmptyMessage(0);
				}
			}
		}, 0, 1000);
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				startBackgroudActivity();
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_REDELIVER_INTENT;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		mHandler.sendEmptyMessage(0);
	}

	@Override
	public void onDestroy() {
	}

	/**
	 * �жϵ�ǰAPP�Ƿ��Ѿ��˵���̨
	 * 
	 * @param context
	 * @return���Ѿ��˵���̨����true�����򷵻�false
	 */
	public static boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public void startBackgroudActivity() {
		if (isBackground(MyApp.getInstance().getApplicationContext())) {
			sendBroadcast(new Intent("com.fanfan.action.startBackground"));
		}
	}

	/**
	 * �жϵ�ǰ�����Ƿ�������
	 * 
	 * @return �Ƿ���true
	 */
	private boolean isHome() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return getHomes().contains(rti.get(0).topActivity.getPackageName());
	}

	/**
	 * ������������Ӧ�õ�Ӧ�ð�����; 1����ȻҪ�жϵ�ǰ���棬�Ǿ�Ҫ�жϵ�ǰ��RunningTasks�еĵ�һ����</br>
	 * 
	 * @return ���ذ������а������ַ����б�
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}
}
