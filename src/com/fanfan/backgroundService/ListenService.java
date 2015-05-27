package com.fanfan.backgroundService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.fanfan.bodyguard.MainActivity;
import com.fanfan.bodyguard.MyApp;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.MessageUtils;
import com.fanfan.utils.SharePreferUtils;

/**
 * ����˼·������һ��0.01s�������ļ���Ȼ��ÿ��ȥ����һ�Σ��ڲ����ڼ��ȡ�������¼���������
 * 
 * @author FANFAN
 * 
 */
@SuppressLint("NewApi")
public class ListenService extends Service {
	private final int ORDER_START_PLAY = 0xA0;
	private final int ORDER_STOP_PLAY = 0xA1;
	private final Timer timer = new Timer();
	private final ArrayList<Long> times = new ArrayList<Long>();
	private final long TIMING_TIME = 2500L;// �趨����ʱ��
	private WakeLock mWakeLock;
	private boolean isScreenOn;
	private SharePreferUtils preferUtils;
	public static final String ACTION_HAND_SOS = "com.fan.action_handleSOS";
	private MessageUtils messageUtils;

	class VolumeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			CLog.i("info", "���յĹ㲥��" + action);
			if ("android.media.VOLUME_CHANGED_ACTION".equals(action)
					|| "android.media.MASTER_VOLUME_CHANGED_ACTION"
							.equals(action)) {
				// ���ж��Ƿ�������������
				if (MyApp.getInstance().isSettingRing()) {
					return;
				}
				/** �ڴ˴�����ʱ�䣬�ڲ������¼��������뼴�ж�ΪӦ���¼���������ʾΪ���룬������Ϊ������ֻ��ȡ��2.01���ʱ�� ***/
				times.add(System.currentTimeMillis());
				// �յ���һ�������¼����ȵ�����Ļ
				if (!isScreenOn) {
					// CLog.i("fan", "������Ļ");
					mWakeLock.acquire();
					// MyApp.getInstance().releaseLock();
				}

				// Log.i("fan", "times:" + times);
				if (times.size() > 0
						&& (times.get(times.size() - 1) - times.get(0)) > TIMING_TIME) {
					// ��⵽��ʱ�䳬�����룬����ж�Ϊsos
					removeAllOrder();
					if (player.isPlaying()) {
						player.stop();
					}
					MyApp.getInstance().releaseLock();
					// startRecord();
					if (!MyApp.getInstance().isFront()) {
						// ������ǰ��������Activity
						sendBroadcast(new Intent(
								"com.fanfan.action.finishActivity"));
						Intent start_intent = new Intent(ListenService.this,
								MainActivity.class);
						start_intent.putExtra(G.KEY_SEND_IMMETIATELLY, true);
						start_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(start_intent);
					}
					sendBroadcast(new Intent(ACTION_HAND_SOS));
					// }
				}

				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						// Log.i("fan", "�ӳ����뽫ʱ�����");
						times.clear();
						if (mWakeLock.isHeld()) {
							mWakeLock.release();
						}
					}
				}, 3000L);
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				// CLog.i("info", "����ʱ��ʼ��ѯʱ������,ÿ��0.9������һ�Σ�������Ļ��");
				isScreenOn = false;
				play();
				MyApp.getInstance().enableLock();
			} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
				// CLog.i("info", "����ʱ������ѯ���񣬲�����");
				isScreenOn = true;

			} else if (G.ACTION_NEED_INITALARM.equals(action)) {
				initAlarmManager();
			} else if (MessageUtils.ACTION_MESSAGE_SENT.equals(action)) {
				// ��Ϣ�ѷ���
				if (-1 == getResultCode()) {
					String phoneaddress = intent
							.getStringExtra(MessageUtils.KEY_SENT_MESSAGE_NUMBER);
					String content = intent
							.getStringExtra(MessageUtils.KEY_SENT_MESSAGE_CONTENT);
					messageUtils.saveMessageToDB(phoneaddress, content);
				}
				sendBroadcast(new Intent(G.ACTION_MESSAGE_HADSENT));
			} else if ("com.fanfan.action.startBackground".equals(action)) {
				// MyApp.getInstance().exitAll();
				// Intent staIntent = new Intent(MyApp.getInstance()
				// .getApplicationContext(), WallpaperActivity.class);
				// staIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// // ������ӣ�����������ǰ̨
				// context.startActivity(staIntent);
			}

		}
	}

	private VolumeReceiver receiver;
	private MediaPlayer player;
	private Handler mHandler;
	private AlarmManager am;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		intiData();
		initReceiver();
		/**
		 * ��һ����ʱ��ÿ��1.5�뷢��һ���㲥�����չ㲥ʱÿ��1.5ȥ���գ���û�н��յ����ж�Ϊ����������������
		 */
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendBroadcast(new Intent(G.ACTION_SERVICE_SEND));
			}
		}, 0, 1500);
	}

	@SuppressLint("NewApi")
	private void intiData() {
		player = new MediaPlayer();
		player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		player.setLooping(true);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, "fan");
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ORDER_START_PLAY:
					play();
					break;
				case ORDER_STOP_PLAY:
					pause();
					break;
				default:
					break;
				}
			}
		};
		preferUtils = new SharePreferUtils(this);
		messageUtils = new MessageUtils(this);
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
	}

	private void removeAllOrder() {
		mHandler.removeMessages(ORDER_START_PLAY);
		mHandler.removeMessages(ORDER_STOP_PLAY);
	}

	@SuppressLint("NewApi")
	private void play() {
		// �Ƿ�����ͣ״̬
		try {
			player.reset();
			AssetFileDescriptor fileDescriptor = getAssets().openFd(
					MyApp.MUSIC_FILE_NAME);
			player.setDataSource(fileDescriptor.getFileDescriptor(),
					fileDescriptor.getStartOffset(), fileDescriptor.getLength());
			player.prepare();
			player.start();
			removeAllOrder();
			mHandler.sendEmptyMessageDelayed(ORDER_STOP_PLAY, 100);//
			// 0.01�����ͣ
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void pause() {
		if (player.isPlaying()) {
			player.stop();
			mHandler.sendEmptyMessageDelayed(ORDER_START_PLAY, 10);// 0.99���ʼ����
		}
	}

	private void initReceiver() {
		receiver = new VolumeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		filter.addAction("android.media.MASTER_VOLUME_CHANGED_ACTION");
		filter.addAction("android.media.MASTER_MUTE_CHANGED_ACTION");
		filter.addAction(Intent.ACTION_SCREEN_OFF);// �����㲥
		filter.addAction(Intent.ACTION_SCREEN_ON);// �����㲥
		filter.addAction(Intent.ACTION_USER_PRESENT);// �����㲥
		filter.addAction(G.ACTION_NEED_INITALARM);
		filter.addAction(MessageUtils.ACTION_MESSAGE_SENT);
		// filter.addAction("com.fanfan.action.startBackground");
		this.registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// �ٴζ�̬ע��㲥
		initAlarmManager();
		// ��ɱ��������
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Intent startbroadcast = new Intent(G.ACTION_APP_REMOVED);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				ListenService.this, 0, startbroadcast,
				PendingIntent.FLAG_ONE_SHOT);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500,
				pendingIntent);
	}

	@Override
	public void onDestroy() {
		Intent startService = new Intent(this, ListenService.class);
		startService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(startService);
	}

	/********* ���ӷ��� ****************/

	private void initAlarmManager() {

		Intent intent = new Intent(G.ACTION_ALARM_SEND);
		Set<String> times = preferUtils
				.getStringSetPrefer(G.KEY_SAVE_ALARM_TIMES);
		Iterator<String> iterator = times.iterator();
		int count = 0;
		PendingIntent pendingIntent = null;
		while (iterator.hasNext()) {
			count++;
			String time = iterator.next();
			intent.putExtra(G.KEY_ALARM_TIME, time);
			pendingIntent = PendingIntent.getBroadcast(ListenService.this,
					count, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// ��ȡ����������
			am.cancel(pendingIntent);
			int cycle_type = preferUtils.getIntPrefer(time);
			long alarm_time = Long.parseLong(time);
			switch (cycle_type) {
			case G.CYCLE_EVERY_DAY:
			case G.CYCLE_SELF:
				// ʹ�ö�ʱ��
				// timer.schedule(task, when, period)
				// ��repeateģʽ�£����Զ�����֮ǰ��ʱ��㣬��������ж�
				if (alarm_time < System.currentTimeMillis()) {
					Date date = new Date(alarm_time);
					Calendar date_Calendar = Calendar.getInstance();
					date_Calendar.setTime(date);
					Calendar calendar = Calendar.getInstance(Locale.CHINA);
					calendar.set(Calendar.HOUR_OF_DAY,
							date_Calendar.get(Calendar.HOUR_OF_DAY));
					calendar.set(Calendar.MINUTE,
							date_Calendar.get(date_Calendar.MINUTE));
					calendar.set(Calendar.SECOND, 0);
					alarm_time = calendar.getTimeInMillis();
				}
				am.setRepeating(AlarmManager.RTC_WAKEUP, alarm_time,
						1000 * 60 * 60 * 24, pendingIntent);
				break;
			case G.CYCLE_ONE_TIME:
				if (alarm_time > System.currentTimeMillis()) {
					am.set(AlarmManager.RTC_WAKEUP, Long.parseLong(time),
							pendingIntent);
				}
				break;
			default:
				break;
			}
		}
	}
}
