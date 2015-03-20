package com.fanfan.bodyguard;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class AboutMeActivity extends BaseActivity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutme_activity);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.about_imb_leftmenu:
			onBackPressed();
			overridePendingTransition(R.anim.push_right_in,
					R.anim.push_right_out);
			break;

		default:
			break;
		}

	}
}
