package com.fanfan.backgroundService;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.fanfan.bodyguard.SettingActivity;
import com.fanfan.music.SystemRingUtils;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.SharePreferUtils;

public class MusicPlayService extends Service {

	private InnerReceiver receiver;
	private MediaPlayer player; // ��������
	private boolean isPause;
	private SharePreferUtils preferUtils;
	private String ring_type;
	private Ringtone ringtone;
	private AudioManager admanager;
	private Handler mHandler;
	private final int PLAY_OVER = 0xAA1;

	// ����һ���ڲ��㲥�����������ڽ��չ㲥
	private class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// ȡ��action
			String action = intent.getAction();
			// Log.i("info", "Service���յĹ㲥��" + action);
			if (G.ACTION_PLAY.equals(action)) {
				play();
			} else if (G.ACTION_PAUSE.equals(action)) {
				pause();
			} else if (G.ACTION_EXIT.equals(action)) {
				stopSelf();
			} else if (G.ACTION_PLAY_ITEM_RING.equals(action)) {
				// ��ֹͣ�󲥷�
				pause();
				isPause = false;
				ringtone = null;
				player = null;
				play();
			}
		}
	}

	public void play() {
		ring_type = preferUtils.getStringPrefer(G.KEY_RING_TYPE);
		// �Ƿ�����ͣ״̬
		Log.i("info", "�������֣�ring_type:" + ring_type);
		if (ring_type.equals(G.VALUE_SYSTEM_RING)) {
			// ��ϵͳ����
			String id_str = preferUtils.getStringPrefer(G.KEY_RING_PATH);
			if (!TextUtils.isEmpty(id_str)) {
				int id = Integer.parseInt(id_str);
				SystemRingUtils utils = new SystemRingUtils(
						MusicPlayService.this);
				ringtone = utils.getRingtoneById(id);
				admanager.setStreamVolume(AudioManager.STREAM_RING,
						admanager.getStreamMaxVolume(AudioManager.STREAM_RING),
						0);
				if (ringtone != null) {
					ringtone.play();
					// ��ѯ����Ƿ񲥷����
					mHandler.sendEmptyMessageDelayed(PLAY_OVER, 300);
					if (SettingActivity.isFront) {
						SettingActivity.getInstance().setPauseBtnBackground();
					}
				}
			}
		} else if (ring_type.equals(G.VALUE_MEDIA_RING)) {
			if (isPause) {
				player.start();
				if (SettingActivity.isFront) {
					SettingActivity.getInstance().setPauseBtnBackground();
				}
			} else {
				// ��ý������
				if (player != null) {
					player = null;
				}
				player = new MediaPlayer();

				admanager
						.setStreamVolume(AudioManager.STREAM_MUSIC, admanager
								.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
								0);
				player.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						player.stop();
						if (SettingActivity.isFront) {
							SettingActivity.getInstance()
									.setPlayBtnBackground();
						}

					}
				});
				try {
					// ý������
					player.reset();
					String path = preferUtils.getStringPrefer(G.KEY_RING_PATH);
					player.setDataSource(path);
					player.setLooping(true);// ѭ������
					player.prepare();
					player.start();
					if (SettingActivity.isFront) {
						SettingActivity.getInstance().setPauseBtnBackground();
					}
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

		}
	}

	public void pause() {
		ring_type = preferUtils.getStringPrefer(G.KEY_RING_TYPE);
		if (ring_type.equals(G.VALUE_SYSTEM_RING)) {
			if (ringtone != null && ringtone.isPlaying()) {
				CLog.i("info", "ϵͳ����ý����ͣ");
				ringtone.stop();
				isPause = true;
				if (SettingActivity.isFront) {
					SettingActivity.getInstance().setPlayBtnBackground();
				}
			}
		} else if (ring_type.equals(G.VALUE_MEDIA_RING)) {
			if (player != null && player.isPlaying()) {
				CLog.i("info", "ý����ͣ");
				player.pause();
				isPause = true;
				if (SettingActivity.isFront) {
					SettingActivity.getInstance().setPlayBtnBackground();
				}
			}
		}
	}

	@Override
	public void onCreate() {
		Log.i("info", "MusicPlayService����");
		super.onCreate();
		// ��ʼ����ر���
		isPause = false;
		preferUtils = new SharePreferUtils(this);
		admanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (ringtone == null) {
					mHandler.removeMessages(PLAY_OVER);
					return;
				}
				if (ringtone.isPlaying()) {
					mHandler.sendEmptyMessageDelayed(PLAY_OVER, 300);
				} else {
					mHandler.removeMessages(PLAY_OVER);
					if (SettingActivity.isFront) {
						SettingActivity.getInstance().setPlayBtnBackground();
					}
				}
			}
		};
		// ע��ù㲥��
		// admanager.isMusicActive();// �Ƿ����������ڲ���
		receiver = new InnerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(G.ACTION_PLAY);
		filter.addAction(G.ACTION_PAUSE);
		filter.addAction(G.ACTION_EXIT);
		filter.addAction(G.ACTION_PLAY_ITEM_RING);
		registerReceiver(receiver, filter);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ���ٲ�����
		if (player != null) {
			player.release();
		}
		// ȡ���㲥ע�ᡡ
		unregisterReceiver(receiver);
		// ���ٹ����߳�
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
