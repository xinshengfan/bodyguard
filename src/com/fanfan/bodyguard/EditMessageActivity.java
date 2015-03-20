package com.fanfan.bodyguard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fanfan.data.AlarmTimeAdapter;
import com.fanfan.utils.CLog;
import com.fanfan.utils.G;
import com.fanfan.utils.SharePreferUtils;

@SuppressLint("InflateParams")
public class EditMessageActivity extends BaseActivity implements
		OnClickListener {
	private static EditMessageActivity instance;
	private EditText et_message;
	private TextView tv_reminder;
	private LinearLayout ll_message;
	private int message_length;

	private SharePreferUtils preferUtils;
	private String value;
	private ListView lv_alarm_times;
	private ArrayList<Long> savetimes;
	private AlarmTimeAdapter timeAdapter;
	// private ImageView imv_add_time;
	private TextView tv_add_time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 启动activity时自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		setContentView(R.layout.editmessage_activity);
		instance = this;
		initView();
		intiData();
	}

	private void intiData() {
		preferUtils = new SharePreferUtils(this);
		value = getIntent().getStringExtra(G.INTENT_KEY_EDITMESSAGE);
		String saved_message = "";
		if (G.VALUE_EDITMESSAGE_SOS.equals(value)) {
			et_message.setText(String.format(
					getResources().getString(R.string.message),
					new Object[] { MyApp.getInstance().getCurrentAddress() }));
			saved_message = preferUtils.getStringPrefer(G.KEY_SAVE_MESSAGE);
			lv_alarm_times.setVisibility(View.GONE);
			tv_add_time.setVisibility(View.GONE);
			adjustViewHeight(ll_message, 35);
		} else if (G.VALUE_EDITMESSAGE_PEACE.equals(value)) {
			et_message.setText(String.format(
					getResources().getString(R.string.peace_message),
					new Object[] { MyApp.getInstance().getCurrentAddress() }));
			saved_message = preferUtils.getStringPrefer(G.KEY_SAVE_PEACE);
			lv_alarm_times.setVisibility(View.VISIBLE);
			adjustViewHeight(ll_message, 20);
			Set<String> alarm_times = preferUtils
					.getStringSetPrefer(G.KEY_SAVE_ALARM_TIMES);
			savetimes = new ArrayList<Long>();
			Iterator<String> iterator = alarm_times.iterator();
			while (iterator.hasNext()) {
				savetimes.add(Long.parseLong(iterator.next()));
			}
			if (savetimes.size() == 0) {
				tv_add_time.setVisibility(View.VISIBLE);
			} else {
				tv_add_time.setVisibility(View.GONE);
			}
			timeAdapter = new AlarmTimeAdapter(EditMessageActivity.this,
					savetimes);
			lv_alarm_times.setAdapter(timeAdapter);
		}
		if (!TextUtils.isEmpty(saved_message)) {
			if (!TextUtils.isEmpty(MyApp.getInstance().getLastAddress())
					&& saved_message.contains(MyApp.getInstance()
							.getLastAddress())
					&& !TextUtils.isEmpty(MyApp.getInstance()
							.getCurrentAddress())
					&& !MyApp.getInstance().getCurrentAddress()
							.equals(MyApp.getInstance().getLastAddress())) {
				// 替换地址
				saved_message.replace(MyApp.getInstance().getLastAddress(),
						MyApp.getInstance().getCurrentAddress());
			}
			et_message.setText(saved_message);
		}
		et_message.setSelection(et_message.getText().toString().length());
		messageLengthReminder();
		et_message.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				messageLengthReminder();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	public void setaddTimeVisiBle() {
		if (G.VALUE_EDITMESSAGE_PEACE.equals(value)) {
			tv_add_time.setVisibility(View.VISIBLE);
		}
	}

	private void initView() {
		et_message = (EditText) findViewById(R.id.et_message);
		tv_reminder = (TextView) findViewById(R.id.tv_messageLength);
		ll_message = (LinearLayout) findViewById(R.id.ll_message);
		lv_alarm_times = (ListView) findViewById(R.id.lv_alarm_times);
		// imv_add_time = (ImageView) findViewById(R.id.imv_time_add);
		tv_add_time = (TextView) findViewById(R.id.tv_hint_add);
	}

	protected void messageLengthReminder() {
		message_length = et_message.getText().toString().length();
		int groups = 1;
		int per_length = 0;
		if (message_length < 70) {
			groups = 1;
			per_length = message_length;
		} else if (message_length > 70) {
			groups = (message_length % 70 == 0) ? (message_length / 70)
					: (message_length / 70 + 1);
			per_length = message_length - ((groups - 1) * 70);
		}
		tv_reminder.setText(per_length + "/" + groups);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_savemessage:
			saveMessageToPrefer();

			Toast.makeText(EditMessageActivity.this, "已保存", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.editmessage_imb_leftmenu:
			saveMessageToPrefer();
			startActivity(new Intent(EditMessageActivity.this,
					SettingActivity.class));

			EditMessageActivity.this.finish();
			break;
		case R.id.tv_hint_add:
			Intent intent = new Intent(EditMessageActivity.this,
					AddtimeActivity.class);
			intent.putExtra(G.KEY_INTENT_TIME_POSITION, 0);
			startForResult(intent, 0);
			break;
		default:
			break;
		}
	}

	private void saveMessageToPrefer() {
		MyApp.getInstance().setLastAddress(
				MyApp.getInstance().getCurrentAddress());
		if (G.VALUE_EDITMESSAGE_SOS.equals(value)) {
			preferUtils.setStringPrefer(G.KEY_SAVE_MESSAGE, et_message
					.getText().toString().trim());
		} else if (G.VALUE_EDITMESSAGE_PEACE.equals(value)) {
			preferUtils.setStringPrefer(G.KEY_SAVE_PEACE, et_message.getText()
					.toString().trim());
			this.timeAdapter.saveTimeToPrefer();
		}

	}

	public static EditMessageActivity getInstance() {
		return instance;
	}

	public void startForResult(Intent intent, int code) {
		startActivityForResult(intent, code);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	@Override
	protected void onStart() {
		super.onStart();
		CLog.i("info", "onStart");
		if (G.VALUE_EDITMESSAGE_PEACE.equals(value) && this.timeAdapter != null) {
			this.timeAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		CLog.i("info", "onActivityResult:" + resultCode);
		if (resultCode == RESULT_OK) {
			int postion = data.getIntExtra(G.KEY_INTENT_RESULT, -1);
			if (postion == -1) {
				return;
			} else {
				long time = preferUtils.getLongPrefer(G.SAVE_PEACE_TIME
						+ postion);
				if (postion < timeAdapter.getCount()) {
					this.timeAdapter.updata(postion, time);
				} else {
					this.timeAdapter.addTime(time);
				}
				tv_add_time.setVisibility(View.GONE);
				this.timeAdapter.saveTimeToPrefer();
			}
		}
	}

}
