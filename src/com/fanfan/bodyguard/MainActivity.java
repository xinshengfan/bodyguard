package com.fanfan.bodyguard;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClientOption.LocationMode;
import com.fanfan.backgroundService.ListenService;
import com.fanfan.backgroundService.MusicPlayService;
import com.fanfan.utils.AlarmMessageUtils;
import com.fanfan.utils.BaiduUtils;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.GPSUtils;
import com.fanfan.utils.LockPatternUtils;
import com.fanfan.utils.MediaRecoderUtils;
import com.fanfan.utils.MessageUtils;
import com.fanfan.utils.NetUtils;
import com.fanfan.utils.RootUtils;
import com.fanfan.utils.SharePreferUtils;
import com.fanfan.utils.UpdataInfo;
import com.fanfan.view.LockPatternView;
import com.fanfan.view.LockPatternView.Cell;
import com.fanfan.view.LockPatternView.DisplayMode;
import com.fanfan.view.LockPatternView.OnPatternListener;

public class MainActivity extends BaseActivity implements OnClickListener {
	private TextView tv_sos, tv_peace;
	private BaiduUtils utils;
	private AlarmMessageUtils alarmMessageUtils;
	private SharePreferUtils preferUtils;
	private Handler mHandler;
	private RelativeLayout rela_record;
	private LinearLayout ll_main_record;
	private ProgressBar mProgressBar;
	private MediaRecoderUtils recoderUtils;
	private Timer timer;
	private TextView tv_record_time;
	private Animation slid_in_Animation, slide_out_Animation;
	private final int WHAT_SEND_OVER = 0xA1;
	private final int WHAT_SEND_MMS = 0xA2;
	private final int WHAT_CAN_SEND = 0xA3;
	private final int WHAT_UPDATA_GPS = 0xA4;
	private final int WHAT_CHECK_VERSION = 0xA5;
	private final int WHAT_CHECK_OVER = 0xA6;

	public enum SendType {
		SOS, PEACE
	}

	private SendType mSendType;
	private boolean canSend = true;
	private boolean isPlayServiceStart = false;
	private LinearLayout rela_play;
	private TextView tv_play;
	private ImageButton imb_play;
	private Animation slide_right_in, slide_right_out;
	// 手势锁
	private RelativeLayout rela_gestureLock;
	private LockPatternView patternview;
	private LockPatternUtils lockUtils;
	private TextView tv_lock;
	private boolean backIsUse = true;
	private AlertDialog dialog;
	private UpdataInfo info;
	private int currentVersion;
	private boolean is_sos_press_flag;

	public class MessageSendBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (G.ACTION_MESSAGE_HADSENT.equals(action)) {
				if (mSendType == SendType.SOS) {
					tv_sos.setText("发送成功");
				} else if (mSendType == SendType.PEACE) {
					tv_peace.setText("发送成功");
				}

			} else if (MessageUtils.ACTION_MESSAGE_RECEIVE.equals(action)) {
				if (mSendType == SendType.SOS) {
					tv_sos.setText("对方已接收");
				} else if (mSendType == SendType.PEACE) {
					tv_peace.setText("对方已接收");
				}
				mHandler.sendEmptyMessageDelayed(WHAT_SEND_OVER, 1500);
			} else if (ListenService.ACTION_HAND_SOS.equals(action)) {
				CLog.i("info", "接收到报警信息处理");
				if (dialog.isShowing()) {
					dialog.cancel();
				}
				if (canSend) {
					playRing();
					showGestureLock(true);
					sendSMS();
				}
			}

		}
	}

	private MessageSendBroadcast receiver;
	private boolean isSend;
	private String message;
	private boolean isPlaying;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// RootUtils.upgradeRootPermission(getPackageCodePath());

		startService(new Intent(this, ListenService.class));
		intiData();
		intiView();
		judgeVersion();
	}

	private void judgeVersion() {
		if (NetUtils.isConnectNet()) {
			PackageManager manager = this.getPackageManager();
			try {
				currentVersion = manager.getPackageInfo(this.getPackageName(),
						0).versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			mHandler.sendEmptyMessageDelayed(WHAT_CHECK_VERSION, 1000L);
		}
	}

	public void showGestureLock(final boolean isVisible) {
		if (isVisible) {
			CLog.i("info", "显示密码界面");
			rela_gestureLock.setVisibility(View.VISIBLE);
			backIsUse = false;
		} else {
			rela_gestureLock.startAnimation(slide_out_Animation);
			slide_out_Animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					rela_gestureLock.setVisibility(View.GONE);
				}
			});
			backIsUse = true;
		}
	}

	/**
	 * 播放铃音
	 */
	public void playRing() {
		if (preferUtils.getBooleanPrefer(G.KEY_PLAY_RING)
				&& !isPlayServiceStart) {
			// 启动播放音乐服务
			startService(new Intent(MainActivity.this, MusicPlayService.class));
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					sendBroadcast(new Intent(G.ACTION_PLAY));
					isPlaying = true;
					isPlayServiceStart = true;
					showPlayView(true);
					imb_play.setBackgroundResource(R.drawable.ic_stop_press);
					tv_play.setText("正在播放："
							+ preferUtils.getStringPrefer(G.KEY_RING_NAME));
				}
			}, 1000);
		}
	}

	protected void showPlayView(boolean visible) {
		if (visible) {
			CLog.i("info", "显示音乐界面");
			rela_play.setVisibility(View.VISIBLE);
			rela_play.startAnimation(slide_right_in);
			slide_right_in.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					rela_play.setVisibility(View.VISIBLE);
				}
			});
		} else {
			CLog.i("info", "开始动画");
			rela_play.setAnimation(slide_right_out);
		}

	}

	public void playClick() {
		Intent intent = new Intent();
		if (isPlaying) {
			intent.setAction(G.ACTION_PAUSE);
			isPlaying = false;
			imb_play.setBackgroundResource(R.drawable.ic_play_press);
		} else {
			intent.setAction(G.ACTION_PLAY);
			isPlaying = true;
			imb_play.setBackgroundResource(R.drawable.ic_stop_press);
		}
		CLog.i("info", "发送的广播:" + intent.getAction());
		sendBroadcast(intent);
	}

	private void intiData() {
		utils = new BaiduUtils();
		alarmMessageUtils = new AlarmMessageUtils(this);
		preferUtils = new SharePreferUtils(this);
		lockUtils = new LockPatternUtils(this);
		receiver = new MessageSendBroadcast();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case WHAT_SEND_OVER:
					if (mSendType == SendType.SOS) {
						tv_sos.setText("SOS");
						tv_sos.setClickable(true);
						tv_peace.setClickable(true);
						tv_sos.setBackgroundResource(R.drawable.btn_sos);
					} else if (mSendType == SendType.PEACE) {
						tv_peace.setText("报平安");
						tv_sos.setClickable(true);
						tv_peace.setClickable(true);
						tv_peace.setBackgroundResource(R.drawable.btn_sos);
					}
					is_sos_press_flag = true;
					break;
				case WHAT_SEND_MMS:
					if (alarmMessageUtils.sendMMS(message)) {
						tv_sos.setText("发送成功");
					} else {
						tv_sos.setText("发送失败");
					}
					mHandler.sendEmptyMessageDelayed(WHAT_SEND_OVER, 1500);
					break;
				case WHAT_CAN_SEND:
					// canSend = true;
					sendSMS();
					break;
				case WHAT_UPDATA_GPS:
					utils.initLocation(LocationMode.Hight_Accuracy,
							BaiduUtils.TEMPCOOR_GCJ, 3000);
					utils.isopenGps(true);
					utils.start();
					break;
				case WHAT_CHECK_VERSION:
					handleVersion();
					break;
				case WHAT_CHECK_OVER:
					showVersionDialog();
					break;
				default:
					break;
				}
			}
		};
		timer = new Timer();

		IntentFilter filter = new IntentFilter();
		filter.addAction(MessageUtils.ACTION_MESSAGE_RECEIVE);
		filter.addAction(ListenService.ACTION_HAND_SOS);
		filter.addAction(G.ACTION_MESSAGE_HADSENT);
		registerReceiver(receiver, filter);
		dialog = new AlertDialog.Builder(this).setMessage("GPS未开启，是否需要开启？")
				.setNegativeButton("是", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						try {
							startActivityForResult(intent, 1);
						} catch (ActivityNotFoundException activityNotFoundException) {
							intent.setAction(Settings.ACTION_SETTINGS);
							try {
								startActivityForResult(intent, 1);
							} catch (Exception e) {

							}
						}
					}
				}).setPositiveButton("否", null).create();
		checkGPS();
		is_sos_press_flag = true;
	}

	protected void handleVersion() {
		// 若已正确连网，就去获取最新版本信息
		new Thread() {
			public void run() {
				try {
					info = NetUtils.getUpdataInfo();
					if (info != null && info.version > currentVersion) {
						mHandler.sendEmptyMessage(WHAT_CHECK_OVER);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	protected void showVersionDialog() {
		new AlertDialog.Builder(this)
				.setTitle("版本更新提示")
				.setMessage("更新内容：\n" + info.changelog + "\n")
				.setPositiveButton("立即更新",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AsychDownLoadApp downLoadApp = new AsychDownLoadApp(
										MainActivity.this);
								downLoadApp.execute(info.installUrl);
							}
						}).setNegativeButton("下次再说", null).show();
	}

	private void checkGPS() {
		if (!GPSUtils.isOPen(this)) {
			dialog.show();
		}
	}

	private void intiView() {
		tv_sos = (TextView) findViewById(R.id.tv_sos);
		tv_peace = (TextView) findViewById(R.id.tv_peace);
		rela_record = (RelativeLayout) findViewById(R.id.rela_main_record);
		ll_main_record = (LinearLayout) findViewById(R.id.ll_main_record);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1_record);
		rela_record.setVisibility(View.GONE);
		tv_record_time = (TextView) findViewById(R.id.tv_record_time);
		rela_play = (LinearLayout) findViewById(R.id.rela_main_play);
		tv_play = (TextView) findViewById(R.id.tv_play_name);
		imb_play = (ImageButton) findViewById(R.id.imb_main_play);
		rela_gestureLock = (RelativeLayout) findViewById(R.id.rela_gestureLock);
		patternview = (LockPatternView) findViewById(R.id.patternview);
		tv_lock = (TextView) findViewById(R.id.tv_lockhint);
		slid_in_Animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_bottom);
		slide_out_Animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_to_bottom);
		slide_right_in = AnimationUtils.loadAnimation(this,
				R.anim.push_right_in);
		slide_right_out = AnimationUtils.loadAnimation(this,
				R.anim.push_right_out);
		adjustViewHeight(tv_sos, 25);
		adjustViewHeight(tv_peace, 25);
		adjustViewWidth(patternview, 90);

		if (!preferUtils.isFirstMainActivity() && !lockUtils.isSetGesureLock()) {
			// 不是第一次使用，还没有设置手势，则直接去设置;
			CLog.i("info",
					"preferUtils.isFirstUse():" + preferUtils.isFirstUse()
							+ " ;isSetGesureLock: "
							+ lockUtils.isSetGesureLock());
			startActivityForResult(
					new Intent(this, SetGesureLockActivity.class), 0);
		}
		addListener();
	}

	private void addListener() {
		rela_play.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (rela_play.isShown()) {
					float y = 0, dx, dy = 0;
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						y = event.getRawY();
						break;
					case MotionEvent.ACTION_MOVE:
						dx = event.getRawX() - rela_play.getWidth();
						if (dx < 0 && Math.abs(dx) < rela_play.getWidth()) {
							if (Math.abs(dx) > rela_play.getWidth()
									- imb_play.getWidth()) {
								dx = (imb_play.getWidth() + 30)
										- rela_play.getWidth();
							}
							rela_play.setTranslationX(dx);
						}
						break;
					case MotionEvent.ACTION_UP:
						dx = event.getRawX() - rela_play.getWidth();
						if (dx < 0 && Math.abs(dx) < rela_play.getWidth()) {
							if (Math.abs(dx) > (rela_play.getWidth() / 2)) {
								dx = (imb_play.getWidth() + 30)
										- rela_play.getWidth();
								if (!isPlaying) {
									// 已经暂停直接飞出
									dx = -rela_play.getWidth();
								}
							} else {
								dx = 0;
							}
						}

						rela_play.setTranslationX(dx);
						break;
					default:
						break;
					}
					return true;
				}
				return false;
			}
		});
		patternview.setOnPatternListener(new OnPatternListener() {

			@Override
			public void onPatternStart() {

			}

			@Override
			public void onPatternDetected(List<Cell> pattern) {
				int result = lockUtils.checkPattern(pattern);
				switch (result) {
				case 0:
					// 密码错误
					tv_lock.setText(getResources().getString(
							R.string.lock_pwderror));
					tv_lock.setTextColor(getResources().getColor(R.color.red));
					patternview.setDisplayMode(DisplayMode.Wrong);
					break;
				case 1:
					// 密码正确
					tv_lock.setText(getResources().getString(
							R.string.lock_pwdcorrect));
					tv_lock.setTextColor(getResources().getColor(
							R.drawable.fanfan_white));
					showGestureLock(false);
					// 移除发送信息命令
					removeAllHander();
					patternview.clearPattern();
					break;
				case -1:
					// 还没有设置密码
					startActivityForResult(new Intent(MainActivity.this,
							SetGesureLockActivity.class), 0);
					break;
				default:
					break;
				}
			}

			@Override
			public void onPatternCleared() {

			}

			@Override
			public void onPatternCellAdded(List<Cell> pattern) {

			}
		});
		rela_gestureLock.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					patternview.clearPattern();
				}
				// 让下层失去焦点
				return true;
			}
		});
	}

	protected void removeAllHander() {
		mHandler.removeMessages(WHAT_CAN_SEND);
		mHandler.removeMessages(WHAT_SEND_MMS);
		mHandler.removeMessages(WHAT_SEND_OVER);
		mHandler.removeMessages(WHAT_UPDATA_GPS);

	}

	@Override
	protected void onResume() {
		super.onResume();
		MyApp.getInstance().setFront(true);
		utils.initLocation(LocationMode.Hight_Accuracy,
				BaiduUtils.TEMPCOOR_GCJ, 3000);
		utils.isopenGps(true);
		utils.start();

	}

	@Override
	protected void onStop() {
		super.onStop();
		MyApp.getInstance().setFront(false);
	}

	@Override
	protected void onDestroy() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		rela_record.setVisibility(View.GONE);
		sendBroadcast(new Intent(G.ACTION_EXIT));
		isPlayServiceStart = false;
		super.onDestroy();

	}

	@SuppressLint("ResourceAsColor")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_sos:
			if (is_sos_press_flag) {
				is_sos_press_flag = false;
				if (sendSMS()) {
					// 短信成功发出后再发送彩信
					sendMMS();
				}
			}
			break;
		case R.id.tv_peace:
			String peace_msg = preferUtils.getStringPrefer(G.KEY_SAVE_PEACE);
			if (TextUtils.isEmpty(peace_msg)) {
				peace_msg = String
						.format(getResources()
								.getString(R.string.peace_message),
								new Object[] { MyApp.getInstance()
										.getCurrentAddress() });
			}
			isSend = alarmMessageUtils.sendNomalMessage(peace_msg);
			if (isSend) {
				tv_peace.setText("发送中...");
				tv_peace.setClickable(false);
				tv_sos.setClickable(false);
				mSendType = SendType.PEACE;
				tv_peace.setBackgroundResource(R.drawable.btn_sos_pressed);
			} else {
				Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.imv_praise:
			startActivity(new Intent(MainActivity.this,
					SupportOurActivity.class));
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
			break;
		case R.id.imv_share:
			openShareDialog();
			break;
		case R.id.imv_settting:
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.rela_main_play:
		case R.id.tv_play_name:
		case R.id.imb_main_play:
			if (isPlayServiceStart) {
				playClick();
			}
		default:
			break;
		}

	}

	private void openShareDialog() {
		Intent send_intent = new Intent(Intent.ACTION_SEND);
		send_intent.setType("text/plain");
		send_intent.putExtra(Intent.EXTRA_TEXT,
				getResources().getString(R.string.share_content));
		startActivity(Intent.createChooser(send_intent, "分享是福"));
	}

	private boolean sendSMS() {
		message = preferUtils.getStringPrefer(G.KEY_SAVE_MESSAGE);
		if (TextUtils.isEmpty(message)) {
			message = String.format(getResources().getString(R.string.message),
					new Object[] { MyApp.getInstance().getCurrentAddress() });
		}
		mSendType = SendType.SOS;
		canSend = false;
		isSend = alarmMessageUtils.sendNomalMessage(message);
		if (isSend) {
			tv_sos.setText("发送中...");
			tv_sos.setClickable(false);
			tv_peace.setClickable(false);
			tv_sos.setBackgroundResource(R.drawable.btn_sos_pressed);
		} else {
			Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT)
					.show();
		}
		long interval_time = preferUtils.getIntPrefer(G.KEY_TIME_INTERVAL) * 60 * 1000;
		mHandler.sendEmptyMessageDelayed(WHAT_UPDATA_GPS,
				interval_time - 5 * 1000);// 提前5秒更新地点
		mHandler.sendEmptyMessageDelayed(WHAT_CAN_SEND, interval_time);
		return isSend;
	}

	private void sendMMS() {
		if (preferUtils.getBooleanPrefer(G.KEY_USE_RECORD)) {
			visileRecord(true);
			mProgressBar.setMax(preferUtils.getIntPrefer(G.KEY_RECORD_LENGTH));
			// 先发送普通短信，再录音，录音完成再发送彩信
			tv_sos.setClickable(false);
			tv_peace.setClickable(false);
			tv_sos.setBackgroundResource(R.drawable.btn_sos_pressed);
			try {
				recoderUtils = new MediaRecoderUtils(MainActivity.this);
				recoderUtils.startRecord();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			timer.schedule(new TimerTask() {
				int count = 0;

				@Override
				public void run() {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mProgressBar.setProgress(++count);
							tv_record_time.setText("00:"
									+ (count < 10 ? "0" + count : count));
							if (count == preferUtils
									.getIntPrefer(G.KEY_RECORD_LENGTH)) {
								CLog.i("info", "录音结束");
								recoderUtils.stopRecord();
								visileRecord(false);
								mHandler.sendEmptyMessageDelayed(WHAT_SEND_MMS,
										1000);
								timer.cancel();
							}
						}
					});
				}
			}, 0, 1000);
		}

	}

	private void visileRecord(boolean isVisible) {
		if (isVisible) {
			rela_record.setVisibility(View.VISIBLE);
			ll_main_record.startAnimation(slid_in_Animation);
			slid_in_Animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					ll_main_record.setVisibility(View.VISIBLE);
				}
			});
		} else {
			ll_main_record.startAnimation(slide_out_Animation);
			slide_out_Animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					tv_sos.setText("彩信发送中..");
					tv_sos.setClickable(false);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					rela_record.setVisibility(View.GONE);
				}
			});
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 手势密码
		if (requestCode == 0) {
			if (resultCode == 1) {
				Toast.makeText(MainActivity.this, "设置手势密码成功",
						Toast.LENGTH_SHORT).show();
			} else if (resultCode == 0) {
				Toast.makeText(MainActivity.this, "设置手势密码失败",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 判断用户按两次退出
	private long exitTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 用户按下返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (backIsUse) {
				if (System.currentTimeMillis() - exitTime > 2000) {
					// 不是连续单击
					Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
					// 计算新的值
					exitTime = System.currentTimeMillis();
				} else {
					// 是连续单击两次
					MyApp.getInstance().exitAll();
					finish();
					// System.exit(0);
				}
			}
			// 事件处理完成，不需要向上传递；
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
