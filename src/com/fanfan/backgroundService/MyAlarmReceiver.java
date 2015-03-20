package com.fanfan.backgroundService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import com.fanfan.bodyguard.MyApp;
import com.fanfan.bodyguard.R;
import com.fanfan.utils.AlarmMessageUtils;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.SharePreferUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class MyAlarmReceiver extends BroadcastReceiver {
	private static final SharePreferUtils preferUtils = new SharePreferUtils(
			MyApp.getInstance().getApplicationContext());
	private static final AlarmMessageUtils alarmMessageUtils = new AlarmMessageUtils(
			MyApp.getInstance().getApplicationContext());

	@Override
	public void onReceive(Context context, Intent intent) {
		// ������Ϣ���ڴ˴��ж��Զ����ʱ��
		String action = intent.getAction();
		CLog.i("info", "���յ�������Ϣ�㲥:" + intent);
		if (G.ACTION_ALARM_SEND.equals(action)) {

			String time = intent.getStringExtra(G.KEY_ALARM_TIME);
			long time_seconds = Long.parseLong(time);
			if (time_seconds<System.currentTimeMillis()-10*1000) {
				CLog.i("info", "ʱ���ѹ�");
				return;
			}
			Date date = new Date(time_seconds);
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			CLog.i("info", "����ʱ�䣺" + format.format(date));

			int curent_type = preferUtils.getIntPrefer(time);
			if (curent_type == G.CYCLE_SELF) {
				// �Զ�����ʱ��
				Set<String> self_date = preferUtils.getStringSetPrefer(time
						+ "self");
				String current_day = getWeekDay(time);
				if (self_date.contains(current_day)) {
					sendSafetyMessage();
				}
			} else {
				sendSafetyMessage();
			}
		} else if (G.ACTION_APP_REMOVED.equals(action)) {
			CLog.i("info", "receiver ������service");
			Intent startService = new Intent(MyApp.getInstance()
					.getApplicationContext(), ListenService.class);
			startService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MyApp.getInstance().getApplicationContext()
					.startService(startService);
		}
	}

	private void sendSafetyMessage() {
		CLog.i("info", "��ʱ�����¿�ʼ������Ϣ");
		String peace_msg = preferUtils.getStringPrefer(G.KEY_SAVE_PEACE);
		if (TextUtils.isEmpty(peace_msg)) {
			peace_msg = String.format(MyApp.getInstance().getResources()
					.getString(R.string.peace_message), new Object[] { MyApp
					.getInstance().getCurrentAddress() });
		}
		alarmMessageUtils.sendNomalMessage(peace_msg);
	}

	/**
	 * �õ���ǰΪ�ܼ�
	 * 
	 * @param time
	 * @return
	 */
	private String getWeekDay(String time) {
		Date date = new Date(Long.parseLong(time));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : calendar
				.get(Calendar.DAY_OF_WEEK) - 1;
		switch (day) {
		case 1:
			return G.Monday;
		case 2:
			return G.Tuesday;
		case 3:
			return G.Wednesday;
		case 4:
			return G.Thursday;
		case 5:
			return G.Friday;
		case 6:
			return G.Saturday;
		case 7:
			return G.Sunday;
		default:
			break;
		}
		return null;
	}
}
