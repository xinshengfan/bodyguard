package com.fanfan.bodyguard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.fanfan.utils.G;
import com.fanfan.utils.SharePreferUtils;

public class AddtimeActivity extends BaseActivity implements OnClickListener {
	private TimePicker mTimePicker;
	private SharePreferUtils preferUtils;
	private int position;
	public static String SAVE_CYCLE;
	public static String SAVE_CYCLE_SELF;
	private int current_Type;
	private Calendar calendar;
	private long data_long;
	private CheckBox box_monday, box_Tuesday, box_wednesday, box_thursday,
			box_friday, box_saturday, box_sunday;
	private Set<String> self_date;
	private AlertDialog.Builder dialogBuilder;
	boolean isGoon;
	private Handler mHandler;
	private final int JUDGE_VALID = 0xB0;
	private final int JUDGE_GOON = 0xB1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addtime);

		initView();
		iniData();
	}

	@SuppressLint("HandlerLeak")
	private void iniData() {
		position = getIntent().getIntExtra(G.KEY_INTENT_TIME_POSITION, -1);
		data_long = getIntent().getLongExtra(G.KEY_INTENT_EXTRA_TIME,
				System.currentTimeMillis());
		SAVE_CYCLE = String.valueOf(data_long);
		SAVE_CYCLE_SELF = SAVE_CYCLE + "self";
		calendar = Calendar.getInstance(Locale.CHINA);
		Date date = new Date(data_long);
		calendar.setTime(date);
		mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		isGoon = true;
		current_Type = preferUtils.getIntPrefer(SAVE_CYCLE);
		if (current_Type == 0) {
			current_Type = G.CYCLE_EVERY_DAY;
		}
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case JUDGE_VALID:
					judgeIsRepetion();
					break;
				case JUDGE_GOON:
					preferUtils.setLongPrefer(G.SAVE_PEACE_TIME + position,
							calendar.getTimeInMillis());
					saveSelfToPrefer(calendar.getTimeInMillis());
					Toast.makeText(AddtimeActivity.this, "已保存",
							Toast.LENGTH_SHORT).show();
					Intent result_intent = new Intent(AddtimeActivity.this,
							EditMessageActivity.class);
					result_intent.putExtra(G.KEY_INTENT_RESULT, position);
					setResult(RESULT_OK, result_intent);
					AddtimeActivity.this.finish();
					overridePendingTransition(R.anim.push_right_in,
							R.anim.push_right_out);
					break;
				default:
					break;
				}
			}
		};
		addListener();
	}

	/**
	 * 保存自定义的设置
	 * 
	 * @param timeInMillis
	 */
	protected void saveSelfToPrefer(long timeInMillis) {
		// 先删除原值
		preferUtils.deleteKey(SAVE_CYCLE);
		SAVE_CYCLE = String.valueOf(timeInMillis);
		preferUtils.setIntPrefer(SAVE_CYCLE, current_Type);
		if (current_Type == G.CYCLE_SELF) {
			preferUtils.deleteKey(SAVE_CYCLE_SELF);
			SAVE_CYCLE_SELF = SAVE_CYCLE + "self";
			preferUtils.setStringSetPrefer(SAVE_CYCLE_SELF, self_date);
		}
	}

	private void addListener() {
		mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
			}
		});
	}

	private void initView() {
		preferUtils = new SharePreferUtils(this);
		mTimePicker = (TimePicker) findViewById(R.id.timePicker_select_time);
		mTimePicker.setIs24HourView(true);
		dialogBuilder = new AlertDialog.Builder(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addtime_imb_leftmenu:
			onBackPressed();
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
			break;
		case R.id.rela_set_addtime:
			openSetCycleDialog();
			break;
		case R.id.rela_set_adddate:
			openSetDateDialog();
			break;
		case R.id.bt_add_time:
			// 保存
			// 对时间只保存到分钟
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			judgeIsValid();
			break;
		case R.id.row_monday:
			if (box_monday.isChecked()) {
				box_monday.setChecked(false);
				self_date.remove(G.Monday);
			} else {
				box_monday.setChecked(true);
				self_date.add(G.Monday);
			}
			break;
		case R.id.row_Tuesday:
			if (box_Tuesday.isChecked()) {
				box_Tuesday.setChecked(false);
				self_date.remove(G.Tuesday);
			} else {
				box_Tuesday.setChecked(true);
				self_date.add(G.Tuesday);
			}
			break;
		case R.id.row_wednesday:
			if (box_wednesday.isChecked()) {
				box_wednesday.setChecked(false);
				self_date.remove(G.Wednesday);
			} else {
				box_wednesday.setChecked(true);
				self_date.add(G.Wednesday);
			}
			break;
		case R.id.row_Thursday:
			if (box_thursday.isChecked()) {
				box_thursday.setChecked(false);
				self_date.remove(G.Thursday);
			} else {
				box_thursday.setChecked(true);
				self_date.add(G.Thursday);
			}
			break;
		case R.id.row_friday:
			if (box_friday.isChecked()) {
				box_friday.setChecked(false);
				self_date.remove(G.Friday);
			} else {
				box_friday.setChecked(true);
				self_date.add(G.Friday);
			}
			break;
		case R.id.row_saturday:
			if (box_saturday.isChecked()) {
				box_saturday.setChecked(false);
				self_date.remove(G.Saturday);
			} else {
				box_saturday.setChecked(true);
				self_date.add(G.Saturday);
			}
			break;
		case R.id.row_sunday:
			if (box_sunday.isChecked()) {
				box_sunday.setChecked(false);
				self_date.remove(G.Sunday);
			} else {
				box_sunday.setChecked(true);
				self_date.add(G.Sunday);
			}
			break;
		default:
			break;
		}
	}

	private void judgeIsValid() {
		if (current_Type == G.CYCLE_ONE_TIME
				&& calendar.getTimeInMillis() < System.currentTimeMillis()) {
			dialogBuilder
					.setMessage(
							String.format(
									getResources().getString(
											R.string.self_hint_tomorrow),
									new Object[] { calendar
											.get(Calendar.HOUR_OF_DAY)
											+ ":"
											+ (calendar.get(Calendar.MINUTE) < 10 ? "0"
													+ calendar
															.get(Calendar.MINUTE)
													: calendar
															.get(Calendar.MINUTE)) }))
					.setNegativeButton("继续",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									calendar.set(Calendar.DATE,
											calendar.get(Calendar.DATE) + 1);
									mHandler.sendEmptyMessage(JUDGE_VALID);
								}
							}).setPositiveButton("取消", null);
			dialogBuilder.show();
			Looper.getMainLooper();
			Looper.loop();
		} else {
			mHandler.sendEmptyMessage(JUDGE_VALID);
		}
	}

	/**
	 * 判断是否有相同或相似的时间点
	 * 
	 * @return
	 */
	private boolean judgeIsRepetion() {
		Set<String> times = preferUtils
				.getStringSetPrefer(G.KEY_SAVE_ALARM_TIMES);
		String currentTime = String.valueOf(calendar.getTimeInMillis());
		Iterator<String> iterator = times.iterator();
		boolean isExcepetion = false;
		while (iterator.hasNext()) {
			String savedtime = iterator.next();
			long cur = Long.parseLong(currentTime);
			long save = Long.parseLong(savedtime);
			// if (cur == save) {
			// isExcepetion = true;
			// Toast.makeText(AddtimeActivity.this, "已保存有相同的时间点",
			// Toast.LENGTH_SHORT).show();
			// break;
			// }
			// Date date = new Date(cur);
			// Date date1 = new Date(save);
			if (save != data_long && Math.abs(cur - save) < 5 * 60 * 1000) {
				if (preferUtils.getIntPrefer(SAVE_CYCLE) == G.CYCLE_ONE_TIME
						&& save < System.currentTimeMillis()) {
					break;
				} else {
					isExcepetion = true;
					// 保存的值小于5分钟
					isGoon = openHintDialog(save);
					if (isGoon) {
						break;
					}
				}
			}
		}
		if (!isExcepetion) {
			// 没有异常情况
			mHandler.sendEmptyMessage(JUDGE_GOON);
		}
		return isGoon;

	}

	@SuppressLint("SimpleDateFormat")
	private boolean openHintDialog(long save) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Date date = new Date(save);
		dialogBuilder
				.setMessage(
						String.format(
								getResources().getString(
										R.string.self_hint_cleartime),
								new Object[] { format.format(date) }))
				.setNegativeButton("继续", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						isGoon = true;
						mHandler.sendEmptyMessage(JUDGE_GOON);
					}
				})
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						isGoon = false;
					}
				}).show();
		Looper.getMainLooper();
		Looper.loop();
		return isGoon;
	}

	/**
	 * 设定日期选择
	 */
	private void openSetDateDialog() {
		final DatePickerDialog date_dialog = new DatePickerDialog(
				AddtimeActivity.this, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						calendar.set(Calendar.YEAR, year);
						calendar.set(Calendar.MONTH, monthOfYear);
						calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					}
				}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
		date_dialog.show();
	}

	/**
	 * 设定周期选择
	 */
	@SuppressLint("InflateParams")
	private void openSetCycleDialog() {
		final Dialog cycle_dialog = new Dialog(AddtimeActivity.this,
				R.style.DialogMain);

		View view = LayoutInflater.from(AddtimeActivity.this).inflate(
				R.layout.dialog_add_cycle, null);
		cycle_dialog.setContentView(view);
		RadioButton rd_everyday = (RadioButton) view
				.findViewById(R.id.set_cycle_day);
		RadioButton rd_one = (RadioButton) view
				.findViewById(R.id.set_cycle_one);
		RadioButton rd_self = (RadioButton) view
				.findViewById(R.id.set_cycle_self);
		// long str_time = preferUtils.getLongPrefer(SAVE_PEACE_TIME +
		// position);
		// current_Type = preferUtils.getIntPrefer(SAVE_CYCLE);
		switch (current_Type) {
		case G.CYCLE_EVERY_DAY:
			rd_everyday.setChecked(true);
			break;
		case G.CYCLE_ONE_TIME:
			rd_one.setChecked(true);
			break;
		case G.CYCLE_SELF:
			rd_self.setChecked(true);
			break;
		default:
			break;
		}

		rd_everyday.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				current_Type = G.CYCLE_EVERY_DAY;

				cycle_dialog.dismiss();
			}
		});
		rd_one.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				current_Type = G.CYCLE_ONE_TIME;
				cycle_dialog.dismiss();
			}
		});
		rd_self.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				current_Type = G.CYCLE_SELF;
				cycle_dialog.dismiss();
				openSelfDialog();
			}
		});
		cycle_dialog.show();
	}

	/**
	 * 打开选定周期对话框
	 */
	protected void openSelfDialog() {
		final Dialog self_dialog = new Dialog(AddtimeActivity.this,
				R.style.DialogMain);
		View view = LayoutInflater.from(AddtimeActivity.this).inflate(
				R.layout.setting_selfcycle_dialog, null);
		self_dialog.setContentView(view);
		box_monday = (CheckBox) view.findViewById(R.id.chbox_monday);
		box_Tuesday = (CheckBox) view.findViewById(R.id.chbox_Tuesday);
		box_wednesday = (CheckBox) view.findViewById(R.id.chbox_wednesday);
		box_thursday = (CheckBox) view.findViewById(R.id.chbox_Thursday);
		box_friday = (CheckBox) view.findViewById(R.id.chbox_Friday);
		box_saturday = (CheckBox) view.findViewById(R.id.chbox_Saturday);
		box_sunday = (CheckBox) view.findViewById(R.id.chbox_sunday);
		self_date = preferUtils.getStringSetPrefer(SAVE_CYCLE_SELF);
		initCheckbox();
		Button btn_cancle = (Button) view.findViewById(R.id.btn_self_cancel);
		Button btn_sure = (Button) view.findViewById(R.id.btn_self_sure);
		btn_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				self_dialog.dismiss();
			}
		});
		btn_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				preferUtils.setStringSetPrefer(SAVE_CYCLE_SELF, self_date);
				self_dialog.dismiss();
			}
		});
		self_dialog.show();
	}

	private void initCheckbox() {
		if (self_date.contains(G.Monday)) {
			box_monday.setChecked(true);
		}
		if (self_date.contains(G.Tuesday)) {
			box_Tuesday.setChecked(true);
		}
		if (self_date.contains(G.Wednesday)) {
			box_wednesday.setChecked(true);
		}
		if (self_date.contains(G.Thursday)) {
			box_thursday.setChecked(true);
		}
		if (self_date.contains(G.Friday)) {
			box_friday.setChecked(true);
		}
		if (self_date.contains(G.Saturday)) {
			box_saturday.setChecked(true);
		}
		if (self_date.contains(G.Sunday)) {
			box_sunday.setChecked(true);
		}

	}
}
