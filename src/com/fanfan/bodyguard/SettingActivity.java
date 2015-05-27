package com.fanfan.bodyguard;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fanfan.backgroundService.MusicPlayService;
import com.fanfan.data.MediaRingAdapter;
import com.fanfan.data.MediaRingAdapter.ViewHolder;
import com.fanfan.data.RingAdapter;
import com.fanfan.music.Music;
import com.fanfan.music.MusicDal;
import com.fanfan.music.MyRing;
import com.fanfan.music.SystemRingUtils;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.GPSUtils;
import com.fanfan.utils.LockPatternUtils;
import com.fanfan.utils.SharePreferUtils;
import com.fanfan.view.SlipSwitchView;
import com.fanfan.view.SlipSwitchView.OnSwitchListener;

public class SettingActivity extends BaseActivity implements OnClickListener,
		OnSwitchListener {
	private static SettingActivity instance;
	private TextView tv_recordtime, tv_ring_name;
	private SlipSwitchView useRecord_switch, playvoice_switch, saypaece_switch,
			opengps_swtich;
	private RelativeLayout rela_set_peaceontime;
	private LinearLayout rela_select_ring;
	private ImageButton imb_ring_play;
	private SharePreferUtils preferUtils;
	private final String KEY_KEY = "shortcutKey";
	private final int Up_key = 0xB1, Down_key = 0xB2;

	public enum Ring_Type {
		SYSTEM_RING, MEDIA_RING
	};

	private Ring_Type type;
	private boolean isPlaying;
	public static boolean isFront;
	private LockPatternUtils patternUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		instance = this;
		initData();
		initView();
	}

	public static SettingActivity getInstance() {
		return instance;
	}

	public void setPlayBtnBackground() {
		isPlaying = false;
		imb_ring_play.setBackgroundResource(R.drawable.ic_play_press);
	}

	public void setPauseBtnBackground() {
		isPlaying = true;
		imb_ring_play.setBackgroundResource(R.drawable.ic_stop_press);
	}

	private void initData() {
		preferUtils = new SharePreferUtils(this);
		patternUtils = new LockPatternUtils(this);
	}

	private void initView() {
		tv_recordtime = (TextView) findViewById(R.id.tv_hintrecordtime);
		useRecord_switch = (SlipSwitchView) findViewById(R.id.setting_slipview_userecord);
		playvoice_switch = (SlipSwitchView) findViewById(R.id.setting_play_voice);
		saypaece_switch = (SlipSwitchView) findViewById(R.id.setting_switch_say_peace);
		opengps_swtich = (SlipSwitchView) findViewById(R.id.setting_open_gps);
		rela_set_peaceontime = (RelativeLayout) findViewById(R.id.rela_set_peaceontime);
		rela_select_ring = (LinearLayout) findViewById(R.id.rela_select_ring);
		imb_ring_play = (ImageButton) findViewById(R.id.imb_ring_play);
		tv_ring_name = (TextView) findViewById(R.id.tv_ring_name);
		useRecord_switch.setOnSwitchListener(this);
		useRecord_switch.setMargin(0);
		useRecord_switch.setSlipButtonCanMoveAble(false);
		playvoice_switch.setOnSwitchListener(this);
		playvoice_switch.setMargin(0);
		playvoice_switch.setSlipButtonCanMoveAble(false);
		saypaece_switch.setOnSwitchListener(this);
		saypaece_switch.setMargin(0);
		saypaece_switch.setSlipButtonCanMoveAble(false);
		opengps_swtich.setOnSwitchListener(this);
		opengps_swtich.setMargin(0);
		opengps_swtich.setSlipButtonCanMoveAble(false);
		playvoice_switch.setSwitchState(preferUtils
				.getBooleanPrefer(G.KEY_PLAY_RING));
		useRecord_switch.setSwitchState(preferUtils
				.getBooleanPrefer(G.KEY_USE_RECORD));
		saypaece_switch.setSwitchState(preferUtils
				.getBooleanPrefer(G.KEY_PEACE_ONTIME));
		opengps_swtich.setSwitchState(GPSUtils.isOPen(this));
		if (preferUtils.getBooleanPrefer(G.KEY_PLAY_RING)) {
			rela_select_ring.setVisibility(View.VISIBLE);
			tv_ring_name.setText(preferUtils.getStringPrefer(G.KEY_RING_NAME));
			// 启动播放音乐服务
			startService(new Intent(SettingActivity.this,
					MusicPlayService.class));
		} else {
			rela_select_ring.setVisibility(View.GONE);
		}
		if (preferUtils.getBooleanPrefer(G.KEY_PEACE_ONTIME)) {
			rela_set_peaceontime.setVisibility(View.VISIBLE);
		} else {
			rela_set_peaceontime.setVisibility(View.GONE);
		}

		if (preferUtils.isFirstSettingActivity()) {
			if (!patternUtils.isSetGesureLock()) {
				startActivityForResult(new Intent(SettingActivity.this,
						SetGesureLockActivity.class), 0);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onRestart();
		opengps_swtich.setSwitchState(GPSUtils.isOPen(this));
		opengps_swtich.postInvalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isFront = true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_imb_leftmenu:
			onBackPressed();
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
			break;
		case R.id.rela_set_shortcutkey:
			showChoiceDialog();
			break;
		case R.id.rela_set_notificationnumber:
			startActivity(new Intent(SettingActivity.this,
					ShowContactsActivity.class));
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.rela_set_notifiactioncontent:
			Intent intent1 = new Intent(SettingActivity.this,
					EditMessageActivity.class);
			intent1.putExtra(G.INTENT_KEY_EDITMESSAGE, G.VALUE_EDITMESSAGE_SOS);
			startActivity(intent1);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.rela_set_peaceontime:
			Intent intent2 = new Intent(SettingActivity.this,
					EditMessageActivity.class);
			intent2.putExtra(G.INTENT_KEY_EDITMESSAGE,
					G.VALUE_EDITMESSAGE_PEACE);
			startActivity(intent2);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.rela_select_ring:
			playClick();
			break;
		case R.id.rela_set_modifylock:
			startActivityForResult(new Intent(SettingActivity.this,
					SetGesureLockActivity.class), 0);
			break;
		case R.id.setting_set_interval:
			openIntervalDialog();
			break;
		case R.id.rela_set_about:
			startActivity(new Intent(SettingActivity.this,
					AboutMeActivity.class));
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		default:
			break;
		}
	}

	@SuppressLint("InflateParams")
	private void openIntervalDialog() {
		final Dialog settingDialog = new Dialog(SettingActivity.this,
				R.style.DialogMain);
		View view = LayoutInflater.from(SettingActivity.this).inflate(
				R.layout.setting_record_length_dialog, null);
		settingDialog.setContentView(view);
		Button btnSure = (Button) view.findViewById(R.id.btn_sure);
		TextView tv_time_unit = (TextView) view.findViewById(R.id.tv_time_unit);
		tv_time_unit.setText(getResources().getString(R.string.minute));
		TextView tv_dialog_hintmms = (TextView) view
				.findViewById(R.id.tv_dialog_hintmms);
		tv_dialog_hintmms.setText(getResources().getString(
				R.string.interval_reminder));
		NumberPicker numberPicker = (NumberPicker) view
				.findViewById(R.id.numberPicker);
		int length = preferUtils.getIntPrefer(G.KEY_TIME_INTERVAL);
		numberPicker.setMaxValue(500);
		numberPicker.setValue(length);
		numberPicker.setMinValue(0);
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				preferUtils.setIntPrefer(G.KEY_TIME_INTERVAL, newVal);
			}
		});
		btnSure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				settingDialog.dismiss();
			}
		});
		settingDialog.show();
	}

	public void playClick() {
		Intent intent = new Intent();
		if (isPlaying) {
			intent.setAction(G.ACTION_PAUSE);
			isPlaying = false;
			imb_ring_play.setBackgroundResource(R.drawable.ic_play_press);
		} else {
			intent.setAction(G.ACTION_PLAY);
			isPlaying = true;
			imb_ring_play.setBackgroundResource(R.drawable.ic_stop_press);
		}
		sendBroadcast(intent);
	}

	private void showChoiceDialog() {

		final Dialog settingDialog = new Dialog(SettingActivity.this,
				R.style.DialogMain);
		View view = LayoutInflater.from(SettingActivity.this).inflate(
				R.layout.setting_key_choose_dialog, null);
		settingDialog.setContentView(view);
		Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

		// RadioGroup radioGroup = (RadioGroup) view
		// .findViewById(R.id.setting_dialog_key);
		RadioButton radioUpkey = (RadioButton) view
				.findViewById(R.id.setting_up_key);
		RadioButton radioDownkey = (RadioButton) view
				.findViewById(R.id.setting_down_key);
		if (preferUtils.getIntPrefer(KEY_KEY) == Up_key) {
			radioUpkey.setChecked(true);
		} else {
			preferUtils.setIntPrefer(KEY_KEY, Down_key);
			radioDownkey.setChecked(true);
		}

		radioUpkey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				preferUtils.setIntPrefer(KEY_KEY, Up_key);
				settingDialog.dismiss();
			}

		});
		radioDownkey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				preferUtils.setIntPrefer(KEY_KEY, Down_key);
				settingDialog.dismiss();
			}

		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				settingDialog.dismiss();
			}
		});

		settingDialog.show();

	}

	@Override
	public void onSwitched(View view, boolean isSwitchOn) {
		switch (view.getId()) {
		case R.id.setting_slipview_userecord:
			preferUtils.setBooleanPrefer(G.KEY_USE_RECORD, isSwitchOn);
			if (isSwitchOn) {
				openDialogSetLength();
			}
			break;
		case R.id.setting_play_voice:
			preferUtils.setBooleanPrefer(G.KEY_PLAY_RING, isSwitchOn);
			if (isSwitchOn) {
				// 启动播放音乐服务
				startService(new Intent(SettingActivity.this,
						MusicPlayService.class));
				openDialogSetRing();
			} else {
				rela_select_ring.setVisibility(View.GONE);
				sendBroadcast(new Intent(G.ACTION_PAUSE));
				// sendBroadcast(new Intent(G.ACTION_EXIT));
			}
			break;
		case R.id.setting_switch_say_peace:
			preferUtils.setBooleanPrefer(G.KEY_PEACE_ONTIME, isSwitchOn);
			if (isSwitchOn) {
				rela_set_peaceontime.setVisibility(View.VISIBLE);
			} else {
				rela_set_peaceontime.setVisibility(View.GONE);
			}
			break;
		case R.id.setting_open_gps:
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
			break;
		default:
			break;
		}

	}

	/**
	 * 打开铃音设置面板
	 */
	@SuppressLint("InflateParams")
	private void openDialogSetRing() {
		final Dialog ringDialog = new Dialog(SettingActivity.this,
				R.style.DialogMain);
		View view = LayoutInflater.from(SettingActivity.this).inflate(
				R.layout.setting_music_choice_dialog, null);
		LinearLayout raLayout = (LinearLayout) view
				.findViewById(R.id.ll_set_music);
		adjustView(raLayout, 80);
		ringDialog.setContentView(view);
		ringDialog.setCancelable(false);
		final Button btn_system_ring = (Button) view
				.findViewById(R.id.bt_systemt_music);
		final Button btn_media_ring = (Button) view
				.findViewById(R.id.bt_media_music);
		final ListView lv = (ListView) view.findViewById(R.id.lv_music);
		SystemRingUtils ringUtils = new SystemRingUtils(SettingActivity.this);
		final ArrayList<MyRing> myRings = ringUtils.getMyRingList();
		final RingAdapter ringAdapter = new RingAdapter(SettingActivity.this,
				myRings);

		final MusicDal musicDal = new MusicDal(SettingActivity.this);
		final ArrayList<Music> musics = musicDal.getMusicsByLimit(30);
		final MediaRingAdapter mediaRingAdapter = new MediaRingAdapter(
				SettingActivity.this, musics);
		if (TextUtils.isEmpty(preferUtils.getStringPrefer(G.KEY_RING_TYPE))) {
			type = Ring_Type.SYSTEM_RING;
			preferUtils.setStringPrefer(G.KEY_RING_TYPE, G.VALUE_SYSTEM_RING);
			if (myRings != null && myRings.size() > 0) {
				preferUtils.setStringPrefer(G.KEY_RING_PATH,
						String.valueOf(myRings.get(0).getId()));
			}
			lv.setAdapter(ringAdapter);
			btn_system_ring.setBackgroundResource(R.drawable.btn_music_pressed);
		} else {
			if (G.VALUE_SYSTEM_RING.equals(preferUtils
					.getStringPrefer(G.KEY_RING_TYPE))) {
				// 还要根据保存的不同的值进行处理
				btn_system_ring
						.setBackgroundResource(R.drawable.btn_music_pressed);
				btn_media_ring
						.setBackgroundResource(R.drawable.btn_music_select);
				String id_str = preferUtils.getStringPrefer(G.KEY_RING_PATH);
				if (!TextUtils.isEmpty(id_str)) {
					ringAdapter.clearAndSetMap(Integer.parseInt(id_str));
				}
				lv.setAdapter(ringAdapter);
			} else if (G.VALUE_MEDIA_RING.equals(preferUtils
					.getStringPrefer(G.KEY_RING_TYPE))) {
				btn_media_ring
						.setBackgroundResource(R.drawable.btn_music_pressed);
				btn_system_ring
						.setBackgroundResource(R.drawable.btn_music_select);
				mediaRingAdapter.clearAndSetMap(preferUtils
						.getIntPrefer(G.KEY_MUSIC_ID));
				lv.setAdapter(mediaRingAdapter);
			}
		}
		btn_system_ring.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lv.setAdapter(ringAdapter);
				type = Ring_Type.SYSTEM_RING;
				preferUtils.setStringPrefer(G.KEY_RING_TYPE,
						G.VALUE_SYSTEM_RING);
				btn_system_ring
						.setBackgroundResource(R.drawable.btn_music_pressed);
				btn_media_ring
						.setBackgroundResource(R.drawable.btn_music_select);
			}
		});
		btn_media_ring.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lv.setAdapter(mediaRingAdapter);
				type = Ring_Type.MEDIA_RING;
				preferUtils
						.setStringPrefer(G.KEY_RING_TYPE, G.VALUE_MEDIA_RING);
				btn_media_ring
						.setBackgroundResource(R.drawable.btn_music_pressed);
				btn_system_ring
						.setBackgroundResource(R.drawable.btn_music_select);
			}
		});
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String type_str = preferUtils.getStringPrefer(G.KEY_RING_TYPE);
				MyApp.getInstance().setSettingRing(true);
				if (G.VALUE_SYSTEM_RING.equals(type_str)) {
					MyRing ring = ringAdapter.getItem(position);
					ringAdapter.clearAndSetSelect(ring);
					ringAdapter.clearAndSetMap(position);
					RingAdapter.ViewHolder vh = (com.fanfan.data.RingAdapter.ViewHolder) view
							.getTag();
					vh.radio_btn.toggle();
					ringAdapter.notifyDataSetChanged();
					// 保存选中的内容
					preferUtils.setStringPrefer(G.KEY_RING_TYPE,
							G.VALUE_SYSTEM_RING);
					preferUtils.setStringPrefer(G.KEY_RING_PATH,
							String.valueOf(ring.getId()));
					preferUtils.setStringPrefer(G.KEY_RING_NAME, ring.getName());
					sendBroadcast(new Intent(G.ACTION_PLAY_ITEM_RING));
				} else if (G.VALUE_MEDIA_RING.equals(type_str)) {
					Music m = mediaRingAdapter.getItem(position);
					mediaRingAdapter.clearAndSetSelect(m);
					mediaRingAdapter.clearAndSetMap(position);
					MediaRingAdapter.ViewHolder vh = (ViewHolder) view.getTag();
					vh.radio_btn.toggle();
					mediaRingAdapter.notifyDataSetChanged();
					// 保存选中的内容
					preferUtils.setStringPrefer(G.KEY_RING_TYPE,
							G.VALUE_MEDIA_RING);
					preferUtils.setStringPrefer(G.KEY_RING_PATH,
							m.getMusicPath());
					preferUtils.setIntPrefer(G.KEY_MUSIC_ID, m.getId());
					preferUtils.setStringPrefer(G.KEY_RING_NAME, m.getName());
					sendBroadcast(new Intent(G.ACTION_PLAY_ITEM_RING));
				}
			}
		});
		lv.setOnScrollListener(new OnScrollListener() {
			boolean isAdd;
			int page = 0;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE && isAdd) {
					ArrayList<Music> musics = musicDal.getMusics(page, 30);
					mediaRingAdapter.updataMusic(musics);
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == mediaRingAdapter
						.getCount()) {
					page++;
					isAdd = true;
				}

			}
		});
		Button btn_sure = (Button) view.findViewById(R.id.btn_musicSure);
		btn_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rela_select_ring.setVisibility(View.VISIBLE);
				tv_ring_name.setText(preferUtils
						.getStringPrefer(G.KEY_RING_NAME));
				MyApp.getInstance().setSettingRing(false);
				ringDialog.dismiss();
			}
		});
		ringDialog.show();
	}

	/**
	 * 调面板高度
	 * 
	 * @param v
	 * @param height
	 */
	private void adjustView(View v, float height) {
		DisplayMetrics dms = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dms);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v
				.getLayoutParams();
		// int screenwidth = dms.widthPixels;
		int screenheight = dms.heightPixels;
		// params.width = (int) (screenwidth * (width / 100F));
		params.height = (int) (screenheight * (height / 100F));
		v.setLayoutParams(params);
	}

	/**
	 * 打开设置录音长度设置面板
	 */
	@SuppressLint({ "InflateParams", "NewApi" })
	private void openDialogSetLength() {
		final Dialog settingDialog = new Dialog(SettingActivity.this,
				R.style.DialogMain);
		View view = LayoutInflater.from(SettingActivity.this).inflate(
				R.layout.setting_record_length_dialog, null);
		settingDialog.setContentView(view);
		Button btnSure = (Button) view.findViewById(R.id.btn_sure);
		TextView tv_time_unit = (TextView) view.findViewById(R.id.tv_time_unit);
		tv_time_unit.setText(getResources().getString(R.string.second));
		TextView tv_dialog_hintmms = (TextView) view
				.findViewById(R.id.tv_dialog_hintmms);
		tv_dialog_hintmms.setText(getResources().getString(
				R.string.record_reminder));
		NumberPicker numberPicker = (NumberPicker) view
				.findViewById(R.id.numberPicker);
		int length = preferUtils.getIntPrefer(G.KEY_RECORD_LENGTH);
		numberPicker.setMaxValue(300);
		numberPicker.setValue(length);
		numberPicker.setMinValue(0);
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				preferUtils.setIntPrefer(G.KEY_RECORD_LENGTH, newVal);
			}
		});
		btnSure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				settingDialog.dismiss();
			}
		});
		settingDialog.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 手势密码
		if (requestCode == 0) {
			if (resultCode == 1) {
				Toast.makeText(SettingActivity.this, "设置手势密码成功",
						Toast.LENGTH_SHORT).show();
			} else if (resultCode == 0) {
				Toast.makeText(SettingActivity.this, "设置手势密码失败",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			sendBroadcast(new Intent(G.ACTION_EXIT));
			isFront = false;
			onBackPressed();
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		sendBroadcast(new Intent(G.ACTION_EXIT));
		super.onDestroy();
	}
}
