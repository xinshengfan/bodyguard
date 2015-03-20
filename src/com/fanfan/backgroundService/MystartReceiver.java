package com.fanfan.backgroundService;

import java.util.Timer;
import java.util.TimerTask;

import com.fanfan.bodyguard.MyApp;
import com.fanfan.utils.CLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MystartReceiver extends BroadcastReceiver {
	private static long last_time;
	private static Timer timer = new Timer();
	
	static{
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (System.currentTimeMillis()-last_time>2000) {
					//服务已死，重启
					CLog.i("info", "服务已死，重启");
					Intent service = new Intent(MyApp.getInstance(), ListenService.class);
					service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					MyApp.getInstance().startService(service);
				}
			}
		}, 0, 1500);
	}
	

	@Override
	public void onReceive(Context context, Intent intent) {
		String action =intent.getAction();
		if ("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.USER_PRESENT".equals(action)) {
			Intent service = new Intent(context, ListenService.class);
			service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(service);
		}else if ("com.fanfan.action.ServiceSend".equals(action)) {
			last_time = System.currentTimeMillis();
		}
	}

}
