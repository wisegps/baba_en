package com.wise.baba;

import pubclas.Constant;
import pubclas.GetSystem;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {
	TextView tv_version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_check_update = (TextView) findViewById(R.id.tv_check_update);
		tv_check_update.setOnClickListener(onClickListener);
		tv_version = (TextView) findViewById(R.id.tv_version);
		setVersion();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_check_update:

				break;
			}
		}

	};

	private void setVersion() {
		// Alpha Beta
		tv_version.setText("BaBa V"
				+ GetSystem
						.GetVersion(AboutActivity.this, Constant.PackageName));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}