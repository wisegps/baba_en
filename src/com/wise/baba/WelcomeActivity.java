package com.wise.baba;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import com.wise.setting.LoginActivity;
import com.wise.state.FaultActivity;
import customView.WaitLinearLayout;
import customView.WaitLinearLayout.OnFinishListener;
import data.CarData;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.JsonData;
import pubclas.NetThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

public class WelcomeActivity extends Activity implements TagAliasCallback {
	private static final String TAG = "WelcomeActivity";
	private static final int login_account = 1;
	private static final int Wait = 2;
	private static final int get_data = 3;
	private static final int get_customer = 4;

	/** 是否自动登录 **/
	boolean isLogin = true;
	/** 是否登录完毕 **/
	boolean isLoging = false;
	/** 登录发生异常 **/
	boolean isException = false;
	/** 界面关闭关闭线程 **/
	boolean isDestory = false;
	boolean isWait = false;
	/** 是否从通知栏里启动 **/
	boolean isSpecify = false;
	Bundle bundle;

	WaitLinearLayout ll_wait;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);
		app = (AppApplication) getApplication();

		GetSystem.myLog(TAG, "启动程序-------------------------------------------");
		clearData();
		Intent intent = getIntent();

		isSpecify = intent.getBooleanExtra("isSpecify", false);
		bundle = intent.getExtras();

		ll_wait = (WaitLinearLayout) findViewById(R.id.ll_wait);
		ll_wait.setOnFinishListener(onFinishListener);
		ll_wait.setWheelImage(R.drawable.wheel_white);
		ll_wait.setShadowImage(R.drawable.shadow_white);
		ll_wait.startWheel();
		if (GetSystem.isNetworkAvailable(WelcomeActivity.this)) {
			getLogin();
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					WelcomeActivity.this);
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

	String strData = "";
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Wait:
				isWait = true;
				TurnActivity();
				break;
			case login_account:
				jsonLogin(msg.obj.toString());
				break;
			case get_data:
				isLoging = true;
				strData = msg.obj.toString();
				GetSystem.myLog(TAG,
						"get_Data ,app.carDatas = " + app.carDatas.size());
				TurnActivity();
				break;
			case get_customer:
				jsonCustomer(msg.obj.toString());
				break;
			}
		}
	};

	// 13138154075 e10adc3949ba59abbe56e057f20f883e
	private void getLogin() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_account = preferences.getString(Constant.sp_account, "");
		String sp_pwd = preferences.getString(Constant.sp_pwd, "");
		new WaitThread().start();
		if (sp_account.equals("")) {
			JPushInterface.stopPush(getApplicationContext());
			isLogin = false;
		} else {// 登录
			String url = Constant.BaseUrl + "user_login?account=" + sp_account
					+ "&password=" + sp_pwd;
			new NetThread.GetDataThread(handler, url, login_account).start();
		}
	}

	// 解析登录
	private void jsonLogin(String str) {
		if (str.equals("")) {
			JPushInterface.stopPush(getApplicationContext());
			GetSystem.myLog(TAG, "网络连接异常");
			GetSystem.myLog(TAG, "clearData,Variable.carDatas = "
					+ app.carDatas.size());
			isException = true;
			TurnActivity();
		} else {
			try {
				JSONObject jsonObject = new JSONObject(str);
				if (jsonObject.getString("status_code").equals("0")) {
					app.cust_id = jsonObject.getString("cust_id");
					app.auth_code = jsonObject.getString("auth_code");
					setJpush();
					GetCustomer();
					getData();
				} else {
					JPushInterface.stopPush(getApplicationContext());
					isLogin = false;
					GetSystem.myLog(TAG,
							"jsonLogin status_code ,Variable.carDatas = "
									+ app.carDatas.size());
					TurnActivity();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				isException = true;
				TurnActivity();
			}
		}
	}

	private void GetCustomer() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "?auth_code=" + app.auth_code  + "&lang=en";
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}

	private void jsonCustomer(String str) {
		SharedPreferences preferences1 = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor1 = preferences1.edit();
		editor1.putString(Constant.sp_customer + app.cust_id, str);
		editor1.commit();
		try {
			JSONObject jsonObject = new JSONObject(str);
			app.cust_name = jsonObject.getString("cust_name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getData() {
		GetSystem.myLog(TAG,
				"getData ,Variable.carDatas = " + app.carDatas.size());
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "/vehicle?auth_code=" + app.auth_code  + "&lang=en";
		new NetThread.GetDataThread(handler, url, get_data).start();
	}

	class WaitThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				Thread.sleep(3000);
				Message message = new Message();
				message.what = Wait;
				handler.sendMessage(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void TurnActivity() {
		GetSystem.myLog(TAG, "TurnActivity ,Variable.carDatas = "
				+ app.carDatas.size() + "isWait = " + isWait + " , isLogin = "
				+ isLogin + " , isException = " + isException
				+ " , isLoging = " + isLoging);
		if (isDestory) {
			finish();
			return;
		}
		if (isWait) {// 城市读取完毕，延时
			if (!isLogin) {
				// 未登录跳转
				ll_wait.runFast();
			} else {
				if (isException) {// 程序异常
					GetSystem.myLog(TAG, "isException runFast");
					ll_wait.runFast();
				} else if (isLoging) {// 登录流程走完
					GetSystem.myLog(TAG, "isLoging runFast");
					ll_wait.runFast();
				}
			}
		}
	}

	@Override
	public void gotResult(int arg0, String arg1, Set<String> arg2) {
		GetSystem.myLog(TAG, "arg0 = " + arg0 + " , arg1 = " + arg1);
	}

	private void setJpush() {
		JPushInterface.resumePush(getApplicationContext());
		GetSystem.myLog(TAG, "setJpush");
		Set<String> tagSet = new LinkedHashSet<String>();
		tagSet.add(app.cust_id);
		// 调用JPush API设置Tag
		JPushInterface.setAliasAndTags(getApplicationContext(), null, tagSet,
				this);
	}

	OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			if (isDestory) {
				finish();
				return;
			}
			GetSystem.myLog(TAG, "runFast OnFinish");
			// 未登录跳转
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			// app.City = preferences.getString(Constant.sp_city, "");
			if (!isLogin) {
				// 是否需要选择城市
				// if (app.City.equals("")) {
				// GetSystem.myLog(TAG, "未登录选择城市,Variable.carDatas = "
				// + app.carDatas.size());
				// Intent intent = new Intent(WelcomeActivity.this,
				// SelectCityActivity.class);
				// intent.putExtra("Welcome", true);
				// startActivity(intent);
				// finish();
				// } else {
				GetSystem.myLog(TAG, "未登录跳转,Variable.carDatas = "
						+ app.carDatas.size());
				Intent intent = new Intent(WelcomeActivity.this,
						FaultActivity.class);
				startActivity(intent);
				finish();
				// }
			} else {
				if (isException) {// 程序异常
					GetSystem.myLog(TAG, "runFast isException");
					Intent intent = new Intent(WelcomeActivity.this,
							FaultActivity.class);
					GetSystem.myLog(TAG, "程序异常,Variable.carDatas = "
							+ app.carDatas.size());
					startActivity(intent);
					finish();
				} else if (isLoging) {// 登录流程走完
					GetSystem.myLog(TAG, "runFast isLoging");
					app.carDatas.clear();
					app.carDatas.addAll(JsonData.jsonCarInfo(strData));
					Intent intent = new Intent(WelcomeActivity.this,
							FaultActivity.class);
					if (isSpecify) {
						intent.putExtra("isSpecify", isSpecify);
						intent.putExtras(bundle);
					}
					GetSystem.myLog(TAG, "登录流程走完,Variable.carDatas = "
							+ app.carDatas.size());
					startActivity(intent);
					finish();
				}
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isDestory = true;
	}

	/** 清空静态数据 **/
	private void clearData() {
		app.auth_code = null;
		app.cust_id = null;
		app.cust_name = "";
		app.carDatas = new ArrayList<CarData>();
		GetSystem.myLog(TAG, "clearData,app.carDatas = " + app.carDatas.size());
	}
}