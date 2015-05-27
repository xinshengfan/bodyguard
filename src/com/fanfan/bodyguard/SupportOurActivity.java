package com.fanfan.bodyguard;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SupportOurActivity extends BaseActivity implements OnClickListener {
	private TextView tv_version, tv_user_feedback, tv_business,
			tv_contact_name, tv_contact_way, tv_alipay_account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supportour_activity);

		initView();
	}

	private void initView() {
		tv_version = (TextView) findViewById(R.id.tv_version);
		tv_user_feedback = (TextView) findViewById(R.id.tv_user_feedback);
		tv_business = (TextView) findViewById(R.id.tv_business);
		tv_contact_name = (TextView) findViewById(R.id.tv_contact_name);
		tv_contact_way = (TextView) findViewById(R.id.tv_contact_way);
		tv_alipay_account = (TextView) findViewById(R.id.tv_alipay_account);
		tv_user_feedback.setText("xinshengping@163.com");
		tv_business.setText("baijmh@163.com");
		tv_contact_name.setText("陈工");
		tv_contact_way.setText("15692880627");
		tv_alipay_account.setText("12482787@qq.com");
		PackageManager manager = this.getPackageManager();
		try {
			String name = manager.getPackageInfo(this.getPackageName(), 0).versionName;
			if (!TextUtils.isEmpty(name)) {
				tv_version.setText(getString(R.string.app_name_version) + name);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.support_imb_leftmenu:
			onBackPressed();
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.btn_openalipay:
			openAlipay();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void openAlipay() {
		if (isInstalledAlipay()) {
			try {
				ComponentName componentName = new ComponentName(
						"com.eg.android.AlipayGphone",
						"com.eg.android.AlipayGphone.AlipayLogin");
				Intent intent = new Intent();
				intent.setComponent(componentName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(SupportOurActivity.this, "打开支付宝失败",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} else {
			Toast.makeText(SupportOurActivity.this, "您还没有安装支付宝",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isInstalledAlipay() {
		try {
			getPackageManager().getApplicationInfo(
					"com.eg.android.AlipayGphone",
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
}
