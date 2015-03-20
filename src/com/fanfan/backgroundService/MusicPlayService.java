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
	private MediaPlayer player; // 播放器；
	private boolean isPause;
	private SharePreferUtils preferUtils;
	private String ring_type;
	private Ringtone ringtone;
	private AudioManager admanager;
	private Handler mHandler;
	private final int PLAY_OVER = 0xAA1;

	// 创建一个内部广播接收器，用于接收广播
	private class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 取出action
			String action = intent.getAction();
			// Log.i("info", "Service接收的广播：" + action);
			if (G.ACTION_PLAY.equals(action)) {
				play();
			} else if (G.ACTION_PAUSE.equals(action)) {
				pause();
			} else if (G.ACTION_EXIT.equals(action)) {
				stopSelf();
			} else if (G.ACTION_PLAY_ITEM_RING.equals(action)) {
				// 先停止后播放
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
		// 是否处于暂停状态
		Log.i("info", "播放音乐：ring_type:" + ring_type);
		if (ring_type.equals(G.VALUE_SYSTEM_RING)) {
			// 是系统铃音
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
					// 轮询检测是否播放完毕
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
				// 是媒体铃音
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
					// 媒体铃音
					player.reset();
					String path = preferUtils.getStringPrefer(G.KEY_RING_PATH);
					player.setDataSource(path);
					player.setLooping(true);// 循环播放
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
				CLog.i("info", "系统铃音媒体暂停");
				ringtone.stop();
				isPause = true;
				if (SettingActivity.isFront) {
					SettingActivity.getInstance().setPlayBtnBackground();
				}
			}
		} else if (ring_type.equals(G.VALUE_MEDIA_RING)) {
			if (player != null && player.isPlaying()) {
				CLog.i("info", "媒体暂停");
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
		Log.i("info", "MusicPlayService创建");
		super.onCreate();
		// 初始化相关变量
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
		// 注册该广播　
		// admanager.isMusicActive();// 是否有音乐正在播放
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
		// 销毁播放器
		if (player != null) {
			player.release();
		}
		// 取消广播注册　
		unregisterReceiver(receiver);
		// 销毁工作线程
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
