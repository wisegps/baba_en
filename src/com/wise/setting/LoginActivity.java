package com.wise.setting;

import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.wise.baba.AppApplication;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import com.wise.notice.NoticeActivity;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.JsonData;
import pubclas.NetThread;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 登录界面
 * 
 * @author honesty
 * 
 */
public class LoginActivity extends Activity implements TagAliasCallback {
	private static final String TAG = "LoginActivity";

	private final static int login_account = 1;
	private static final int get_data = 3;

	TextView tv_note;
	EditText et_account, et_pwd;
	Button bt_login;
	String account;
	String pwd;

	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ManageActivity.getActivityInstance().addActivity(LoginActivity.this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		app = (AppApplication) getApplication();
		JPushInterface.init(getApplicationContext());
		tv_note = (TextView) findViewById(R.id.tv_note);
		
		
		et_account = (EditText) findViewById(R.id.et_account);
		et_account.addTextChangedListener(textWatcher);
		et_account.setTypeface(Typeface.SANS_SERIF);//修复账号输入的字体和密码的地址不一样
		
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_pwd.addTextChangedListener(textWatcher);
		et_pwd.setTypeface(Typeface.SANS_SERIF); //修复账号输入的字体和密码的字体不一样
		
		bt_login = (Button) findViewById(R.id.bt_login);
		bt_login.setOnClickListener(onClickListener);
		TextView tv_register = (TextView) findViewById(R.id.tv_register);
		tv_register.setOnClickListener(onClickListener);
		TextView tv_rest_pwd = (TextView) findViewById(R.id.tv_rest_pwd);
		tv_rest_pwd.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_account = preferences.getString(Constant.sp_account, "");
		et_account.setText(sp_account);

		app.isTest = false;
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_register:
				Intent intent = new Intent(LoginActivity.this,
						RegisterActivity.class);
				intent.putExtra("mark", 0);
				intent.putExtra("fastTrack", true);
				startActivity(intent);
				break;
			case R.id.tv_rest_pwd:
				Intent intent1 = new Intent(LoginActivity.this,
						RegisterActivity.class);
				intent1.putExtra("mark", 1);
				startActivity(intent1);
				break;
			case R.id.bt_login:
				Login();
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case login_account:
				jsonLogin(msg.obj.toString());
				break;
			case get_data:
				app.carDatas.clear();
				app.carDatas.addAll(JsonData.jsonCarInfo(msg.obj.toString()));
				// 发广播
				Intent intent = new Intent(Constant.A_RefreshHomeCar);
				sendBroadcast(intent);
				// TODO 做出相应的跳转
				Intent intent1 = LoginActivity.this.getIntent();
				getActivityState(intent1);
				break;
			}
		}
	};

	private void Login() {
		if (GetSystem.isNetworkAvailable(LoginActivity.this)) {
			if (app.isTest) {
				account = "demo@bibibaba.cn";
				pwd = "demo123";
			} else {
				account = et_account.getText().toString().trim();
				pwd = et_pwd.getText().toString().trim();
				if (account.equals("") || pwd.equals("")) {
					Toast.makeText(LoginActivity.this,
							R.string.account_password, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}
			bt_login.setEnabled(false);
			bt_login.setText(getResources().getString(R.string.logining));
			String url = Constant.BaseUrl + "user_login?account=" + account
					+ "&password=" + GetSystem.getM5DEndo(pwd);
			new NetThread.GetDataThread(handler, url, login_account).start();
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					LoginActivity.this);
			dialog.setTitle(R.string.note);
			dialog.setMessage(R.string.net_connection);
			dialog.setPositiveButton(R.string.open,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(
									"android.settings.WIFI_SETTINGS"));
						}
					});
			dialog.setNegativeButton(R.string.cancel, null);
			dialog.show();
		}
	}

	private void jsonLogin(String str) {
		bt_login.setText(getResources().getString(R.string.login));
		bt_login.setEnabled(true);
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				app.cust_id = jsonObject.getString("cust_id");
				app.auth_code = jsonObject.getString("auth_code");
				if (!app.isTest) {
					// 保存账号密码
					SharedPreferences preferences = getSharedPreferences(
							Constant.sharedPreferencesName,
							Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString(Constant.sp_account, account);
					editor.putString(Constant.sp_pwd, GetSystem.getM5DEndo(pwd));
					editor.commit();
				}
				setJpush();
				getData();
				setResult(1);
				finish();
			} else {
				tv_note.setVisibility(View.VISIBLE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 页面跳转方法，根据登录前传过来的跳转类型进行相应界面的跳转
	private void getActivityState(Intent i) {
		int state = i.getIntExtra("ActivityState", 0);
		switch (state) {
		case SetActivity.SMS:
			startActivity(new Intent(LoginActivity.this, NoticeActivity.class));
			break;
		}
	}

	/** 获取车辆信息 **/
	private void getData() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/vehicle?auth_code=" + app.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_data)).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			setResult(1);
			finish();
		}
	}

	@Override
	public void gotResult(int arg0, String arg1, Set<String> arg2) {
		GetSystem.myLog(TAG, "arg0 = " + arg0 + " , arg1 = " + arg1);
	}

	private void setJpush() {
		GetSystem.myLog(TAG, "设置推送");
		Set<String> tagSet = new LinkedHashSet<String>();
		tagSet.add(app.cust_id);
		// 调用JPush API设置Tag
		JPushInterface.setAliasAndTags(getApplicationContext(), null, tagSet,
				this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			tv_note.setVisibility(View.INVISIBLE);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
}