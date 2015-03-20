package com.fanfan.bodyguard;

import java.util.ArrayList;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fanfan.data.ContactsAdapter;
import com.fanfan.data.ContactsAdapter.ViewHolder;
import com.fanfan.data.ContactsUtils;
import com.fanfan.data.MyContacts;
import com.fanfan.utils.SharePreferUtils;

public class ShowContactsActivity extends BaseActivity implements
		OnClickListener {
	private ListView mListView;
	private ArrayList<MyContacts> myContacts;
	private ArrayList<MyContacts> partContacts;
	// private ArrayList<ArrayList<MyContacts>> partContactsList;
	private ContactsAdapter mContactsAdapter;
	private ContactsUtils mContactsUtils;
	private Handler mHandler;
	private final int LOAD_CONCTATS_OVER = 1;
	private ProgressBar mProgressBar;
	private Thread workThread;
	private SharePreferUtils mSharePreferUtils;
	private RelativeLayout frame_toast;
	private Animation slid_in_Animation, slide_out_Animation;
	private Set<String> saved_phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showcontacts_activity);

		initView();
		initData();
	}

	private void initData() {
		saved_phone = mSharePreferUtils
				.getStringSetPrefer(ContactsUtils.KEY_SAVED_PHONE);
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.listView_contacts);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_showcontacts);
		frame_toast = (RelativeLayout) findViewById(R.id.rela_toast);
		slid_in_Animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_bottom);
		slide_out_Animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_to_bottom);
		mContactsUtils = new ContactsUtils(this);
		this.mSharePreferUtils = new SharePreferUtils(this);
		myContacts = new ArrayList<MyContacts>();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == LOAD_CONCTATS_OVER) {
					mHandler.removeMessages(LOAD_CONCTATS_OVER);
					if (mContactsAdapter == null) {
						mListView.setVisibility(View.VISIBLE);
						frame_toast.setVisibility(View.VISIBLE);
						frame_toast.startAnimation(slid_in_Animation);
						mProgressBar.setVisibility(View.GONE);
						mContactsAdapter = new ContactsAdapter(
								ShowContactsActivity.this, myContacts);
						mListView.setAdapter(mContactsAdapter);
					}
				}
			}
		};
		workThread = new Thread() {
			public void run() {
				// partContacts = mContactsUtils.getContactsByLimits(page,
				// PAGESIZE);
				partContacts = mContactsUtils.getAllContacts();
				myContacts.addAll(partContacts);
				partContacts.clear();
				MyApp.getInstance().setMyContacts(myContacts);
				mHandler.sendEmptyMessage(LOAD_CONCTATS_OVER);
			};
		};
		if (MyApp.getInstance().getMyContacts() != null) {
			mListView.setVisibility(View.VISIBLE);
			frame_toast.startAnimation(slid_in_Animation);
			mProgressBar.setVisibility(View.GONE);
			mContactsAdapter = new ContactsAdapter(ShowContactsActivity.this,
					MyApp.getInstance().getMyContacts());
			mListView.setAdapter(mContactsAdapter);
		} else {
			mListView.setVisibility(View.GONE);
			frame_toast.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			workThread.start();
		}
		addListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (MyApp.getInstance().getMyContacts() != null) {
			myContacts = MyApp.getInstance().getMyContacts();
		}
	}

	private void addListener() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactsAdapter.ViewHolder vh = (ViewHolder) view.getTag();
				// 在每次获取点击的item时改变checkbox的状态
				vh.cb_isSelect.toggle();
				// 同时修改map的值保存状态
				mContactsAdapter.getSelect_Map().put(position,
						vh.cb_isSelect.isChecked());
				// 选中了保存在偏好设置中
				mSharePreferUtils.setBooleanPrefer(
						mContactsAdapter.getItem(position).getNumber(),
						vh.cb_isSelect.isChecked());
				if (!vh.cb_isSelect.isChecked()) {
					// 去除选中，从保存的号码中去掉
					saved_phone.remove(mContactsAdapter.getItem(position)
							.getNumber());
				} else {
					saved_phone.add(mContactsAdapter.getItem(position)
							.getNumber());
				}
				// 再保存
				mSharePreferUtils.setStringSetPrefer(
						ContactsUtils.KEY_SAVED_PHONE, saved_phone);
				myContacts.get(position).setSelect(vh.cb_isSelect.isChecked());
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.showcontact_imb_leftmenu:
			backAnimation();
			break;
		case R.id.bt_cancelselected:
			for (int i = 0; i < myContacts.size(); i++) {
				if (mContactsAdapter.getSelect_Map().get(i)) {
					mContactsAdapter.getSelect_Map().put(i, false);
					myContacts.get(i).setSelect(false);
					mSharePreferUtils.setBooleanPrefer(mContactsAdapter
							.getItem(i).getNumber(), false);
				}
			}
			mContactsAdapter.notifyDataSetChanged();
			break;
		case R.id.bt_save:
			Toast.makeText(ShowContactsActivity.this, "已保存", Toast.LENGTH_SHORT)
					.show();
			backAnimation();
			break;
		default:
			break;
		}

	}

	private void backAnimation() {
		frame_toast.startAnimation(slide_out_Animation);
		slide_out_Animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				frame_toast.setVisibility(View.GONE);
				startActivity(new Intent(ShowContactsActivity.this,
						SettingActivity.class));
				overridePendingTransition(R.anim.push_right_in,
						R.anim.push_right_out);
				ShowContactsActivity.this.finish();

			}
		});

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 更新选择变动后的数据
		if (myContacts != null) {
			MyApp.getInstance().setMyContacts(myContacts);
		}
	}

}
