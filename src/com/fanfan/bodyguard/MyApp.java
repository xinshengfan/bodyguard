package com.fanfan.bodyguard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;

import com.baidu.location.LocationClient;
import com.fanfan.backgroundService.ListenService;
import com.fanfan.data.FileManager;
import com.fanfan.data.MyContacts;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.HandlerException;
import com.fanfan.utils.SharePreferUtils;

public class MyApp extends Application {
	public static MyApp instance;
	private ArrayList<Activity> activities;
	private ArrayList<MyContacts> myContacts;
	public static final String MUSIC_FILE_NAME = "bodyguardMusic.mp3";
	@SuppressWarnings("deprecation")
	private KeyguardLock kl;
	private boolean isFront;// �ж�MainActivity�Ƿ�����ǰ��
	// private boolean isHadDealMessage;// �Ƿ��Ѿ���������Ϣ
	private String currentAddress;
	private String lastAddress;
	// �ٶȵ�ͼ
	public LocationClient mLocationClient;
	public MyLocationListener myLocationListener;
	private SharePreferUtils preferUtils;
	private FileManager fileManager;
	public static final String FILE_NAME = "warning.mp3";
	private boolean isSettingRing;// �Ƿ���������������Թ���һ�����

	public boolean isSettingRing() {
		return isSettingRing;
	}

	public void setSettingRing(boolean isSettingRing) {
		this.isSettingRing = isSettingRing;
	}

	public String getLastAddress() {
		return lastAddress;
	}

	public void setLastAddress(String lastAddress) {
		this.lastAddress = lastAddress;
	}

	public String getCurrentAddress() {
		return currentAddress;
	}

	public void setCurrentAddress(String currentAddress) {
		this.currentAddress = currentAddress;
	}

	public boolean isFront() {
		return isFront;
	}

	public void setFront(boolean isFront) {
		this.isFront = isFront;
	}

	public ArrayList<MyContacts> getMyContacts() {
		return myContacts;
	}

	public void setMyContacts(ArrayList<MyContacts> myContacts) {
		this.myContacts = myContacts;
	}

	public void addActivty(Activity activity) {
		activities.add(activity);
	}

	public void exitAll() {
		for (Activity activity : activities) {
			activity.finish();
		}
		// System.exit(0);
	}

	@Override
	public void onTerminate() {
		CLog.i("info", "App onTerminate����Listenservice");
		Intent startService = new Intent(this, ListenService.class);
		startService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(startService);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		CLog.isDebug = true;
		activities = new ArrayList<Activity>();
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("mylock");
		mLocationClient = new LocationClient(this.getApplicationContext());
		myLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(myLocationListener);
		fileManager = new FileManager(this);
		fileManager.copyFileToSD(fileManager.getRecordPath(this), FILE_NAME);
		// ȫ���쳣��Ϣ��س�ʼ��
		// HandlerException handlerException = HandlerException.getInstance();
		// handlerException.initVariable(this);
		intiPrefer();
	}

	/**
	 * ��ʼ��һЩƫ������
	 */
	private void intiPrefer() {
		preferUtils = new SharePreferUtils(this);
		if (preferUtils.isFirstUse()) {
			// ��һ�ΰ�װ����һЩ��ʼ������
			preferUtils.setBooleanPrefer(G.KEY_PLAY_RING, true);
			// Ĭ������
			preferUtils.setStringPrefer("keyRingName", "������");
			preferUtils.setStringPrefer("keyRingPath",
					fileManager.getRecordPath(this) + FILE_NAME);
			preferUtils.setStringPrefer("keySaveRingType", "valuemediaring");

			preferUtils.setBooleanPrefer(G.KEY_PEACE_ONTIME, true);
			// Ĭ�Ϸ�4��ʱ�䣬���ϣ����磬���磬���ϸ�һ��,ÿ�ζ���ÿ�춼��
			Set<String> times = new HashSet<String>();
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.set(Calendar.HOUR_OF_DAY, 8);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);// KEY_SAVE_ALARM_TIMES
			times.add(String.valueOf(calendar.getTimeInMillis()));
			preferUtils.setIntPrefer(
					String.valueOf(calendar.getTimeInMillis()),
					G.CYCLE_EVERY_DAY);
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			times.add(String.valueOf(calendar.getTimeInMillis()));
			preferUtils.setIntPrefer(
					String.valueOf(calendar.getTimeInMillis()),
					G.CYCLE_EVERY_DAY);
			calendar.set(Calendar.HOUR_OF_DAY, 18);
			times.add(String.valueOf(calendar.getTimeInMillis()));
			preferUtils.setIntPrefer(
					String.valueOf(calendar.getTimeInMillis()),
					G.CYCLE_EVERY_DAY);
			calendar.set(Calendar.HOUR_OF_DAY, 22);
			times.add(String.valueOf(calendar.getTimeInMillis()));
			preferUtils.setIntPrefer(
					String.valueOf(calendar.getTimeInMillis()),
					G.CYCLE_EVERY_DAY);
			preferUtils.setStringSetPrefer(G.KEY_SAVE_ALARM_TIMES, times);

			// ����¼��
			preferUtils.setBooleanPrefer(G.KEY_USE_RECORD, true);
			if (preferUtils.getIntPrefer(G.KEY_RECORD_LENGTH) == 0) {
				preferUtils.setIntPrefer(G.KEY_RECORD_LENGTH, 15);
			}
			// ����ʱ�����趨Ϊ5����
			preferUtils.setIntPrefer(G.KEY_TIME_INTERVAL, 5);
		}

	}

	/**
	 * �ͷż�����
	 */
	@SuppressWarnings("deprecation")
	public void releaseLock() {
		kl.disableKeyguard();
	}

	@SuppressWarnings("deprecation")
	public void enableLock() {
		kl.reenableKeyguard();
	}

	public static MyApp getInstance() {
		if (null == instance) {
			instance = new MyApp();
		}
		return instance;
	}

}
