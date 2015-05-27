package com.fanfan.backgroundService;

import com.fanfan.bodyguard.MyApp;
import com.fanfan.utils.CLog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LoadService extends Service {
	private MyStartReceiver receiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		receiver = new MyStartReceiver();
		IntentFilter filter = new IntentFilter(
				"com.fanfan.action.startBackground");
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_REDELIVER_INTENT;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	class MyStartReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			MyApp.getInstance().exitAll();
			Intent staIntent = new Intent(MyApp.getInstance()
					.getApplicationContext(), WallpaperActivity.class);
			staIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// 必须添加，否则会出现在前台
			context.startActivity(staIntent);
		}
	}

 }
