package com.wise.setting;

import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.NetThread;

import com.wise.baba.AppApplication;
import com.wise.baba.ManageActivity;
import com.wise.baba.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 注册界面
 * 
 * @author Administrator
 * 
 */
public class RegisterActivity extends Activity {
	private static final int exists = 1;

	TextView tv_title, tv_note;
	EditText et_account;
	boolean isPhone = true;
	String account;
	/**
	 * 0 注册 ， 1 重置 ，2第三方注册,3修改手机,4修改邮箱
	 */
	int mark = 0;
	boolean fastTrack = false;
	// 终端验证
	boolean remove = false;
	boolean device_update = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ManageActivity.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_register);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_note = (TextView) findViewById(R.id.tv_note);
		Button bt_register = (Button) findViewById(R.id.bt_register);
		bt_register.setOnClickListener(onClickListener);
		et_account = (EditText) findViewById(R.id.et_account);
		Intent intent = getIntent();
		mark = intent.getIntExtra("mark", 0);
		fastTrack = intent.getBooleanExtra("fastTrack", false);

		// 终端验证
		remove = intent.getBooleanExtra("remove", false);
		device_update = intent.getBooleanExtra("device_update", false);
		String account = intent.getStringExtra("account");
		if (mark == 0) {
			tv_title.setText(getResources().getString(R.string.registered));
			bt_register.setText(getResources().getString(R.string.registered));
			tv_note.setVisibility(View.VISIBLE);
		} else if (mark == 1) {
			if (remove) {
				tv_title.setText(getResources().getString(
						R.string.remove_binding));
				bt_register.setText(getResources().getString(
						R.string.authenticate));
				et_account.setText(account);
				et_account.setEnabled(false);
			} else if (device_update) {
				tv_title.setText(getResources().getString(
						R.string.update_terminal));
				bt_register.setText(getResources().getString(
						R.string.authenticate));
				et_account.setText(account);
				et_account.setEnabled(false);
			} else {
				tv_title.setText(getResources().getString(
						R.string.forget_password));
				bt_register.setText(getResources().getString(
						R.string.forget_password));
			}
			tv_note.setVisibility(View.GONE);
		} else if (mark == 3) {
			tv_title.setText(getResources().getString(R.string.update_phone));
			bt_register.setText(getResources().getString(R.string.next));
			tv_note.setVisibility(View.GONE);
			et_account.setText(intent.getStringExtra("phone"));
			et_account.setHint(getResources().getString(R.string.input_phone));
		} else if (mark == 4) {
			tv_title.setText(getResources().getString(R.string.update_email));
			bt_register.setText(getResources().getString(R.string.next));
			tv_note.setVisibility(View.GONE);
			et_account.setText(intent.getStringExtra("email"));
			et_account.setHint(getResources().getString(R.string.input_email));
		}
		// setNote();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_register:
				if (mark == 3) {
					RegisterPhone();
				} else if (mark == 4) {
					RegisterEmail();
				} else {
					Register();
				}
				break;
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case exists:
				jsonExists(msg.obj.toString());
				break;

			default:
				break;
			}
		}
	};

	private void RegisterPhone() {
		account = et_account.getText().toString().trim();
		String url = Constant.BaseUrl + "exists?query_type=6&value=" + account;
		if (account.equals("")) {
			Toast.makeText(RegisterActivity.this, R.string.write_phone,
					Toast.LENGTH_SHORT).show();
		} else if (account.length() == 11 && isNumeric(account)) {
			isPhone = true;
			new Thread(new NetThread.GetDataThread(handler, url, exists))
					.start();
		} else {
			Toast.makeText(RegisterActivity.this, R.string.phone_error,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void RegisterEmail() {
		account = et_account.getText().toString().trim();
		String url = Constant.BaseUrl + "exists?query_type=6&value=" + account;
		if (account.equals("")) {
			Toast.makeText(RegisterActivity.this, R.string.write_email,
					Toast.LENGTH_SHORT).show();
		} else if (isEmail(account)) {
			isPhone = false;
			new Thread(new NetThread.GetDataThread(handler, url, exists))
					.start();
		} else {
			Toast.makeText(RegisterActivity.this, R.string.email_error,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void Register() {
		account = et_account.getText().toString().trim();
		String url = Constant.BaseUrl + "exists?query_type=6&value=" + account;
		if (account.equals("")) {
			Toast.makeText(RegisterActivity.this, R.string.phone_and_email,
					Toast.LENGTH_SHORT).show();
		} else if (account.length() == 11 && isNumeric(account)) {
			isPhone = true;
			new Thread(new NetThread.GetDataThread(handler, url, exists))
					.start();
		} else if (isEmail(account)) {
			isPhone = false;
			new Thread(new NetThread.GetDataThread(handler, url, exists))
					.start();
		} else {
			Toast.makeText(RegisterActivity.this, R.string.account_error,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void jsonExists(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			boolean isExist = jsonObject.getBoolean("exist");
			if (isExist) {// true ,账号已存在
				if (mark == 0) {
					Toast.makeText(RegisterActivity.this,
							R.string.account_login, Toast.LENGTH_SHORT).show();
				} else if (mark == 1) {// 重置密码
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							RegisterActivity.this);
					if (isPhone) {
						dialog.setTitle(R.string.determine);
						dialog.setMessage(getResources().getString(
								R.string.send_to_phone)
								+ account);
					} else {
						dialog.setTitle(R.string.determine);
						dialog.setMessage(getResources().getString(
								R.string.send_to_email)
								+ account);
					}
					dialog.setPositiveButton(R.string.determine,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											RegisterActivity.this,
											CaptchaActivity.class);
									if (remove) {
										intent.putExtra("remove", remove);
									}
									if (device_update) {
										intent.putExtra("device_update",
												device_update);
									}
									intent.putExtra("account", account);
									intent.putExtra("isPhone", isPhone);
									intent.putExtra("mark", mark);
									startActivityForResult(intent, 2);
								}
							}).setNegativeButton(R.string.cancel, null).show();
				} else if (mark == 3) {
					Toast.makeText(RegisterActivity.this, R.string.phone_exist,
							Toast.LENGTH_SHORT).show();
				} else if (mark == 4) {
					Toast.makeText(RegisterActivity.this, R.string.email_exist,
							Toast.LENGTH_SHORT).show();
				}
			} else {// false,可以注册
				if (mark == 0) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							RegisterActivity.this);
					if (isPhone) {
						dialog.setTitle(getResources().getString(
								R.string.phone_determine));
						dialog.setMessage(getResources().getString(
								R.string.send_to_phone)
								+ account);
					} else {
						dialog.setTitle(getResources().getString(
								R.string.email_determine));
						dialog.setMessage(getResources().getString(
								R.string.send_to_email)
								+ account);
					}
					dialog.setPositiveButton(R.string.determine,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											RegisterActivity.this,
											CaptchaActivity.class);
									intent.putExtra("account", account);
									intent.putExtra("isPhone", isPhone);
									intent.putExtra("mark", mark);
									intent.putExtra("fastTrack", fastTrack);
									startActivityForResult(intent, 2);
								}
							}).setNegativeButton(R.string.cancel, null).show();
				} else if (mark == 1) {// 没有账号
					Toast.makeText(RegisterActivity.this,
							R.string.account_null, Toast.LENGTH_SHORT).show();
				} else if (mark == 3 || mark == 4) {
					Intent intent = new Intent(RegisterActivity.this,
							CaptchaActivity.class);
					intent.putExtra("account", account);
					intent.putExtra("isPhone", isPhone);
					intent.putExtra("mark", mark);
					startActivityForResult(intent, 1);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	public static boolean isEmail(String str) {
		Pattern pattern = Pattern
				.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		return pattern.matcher(str).matches();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 2:
			setResult(2, data);
			finish();
			break;
		case 5:
			setResult(6);
			finish();
		case 7:
			setResult(8);
			finish();
		default:
			break;
		}
	}

	private void setNote() {
		SpannableString sp = new SpannableString(getResources().getString(
				R.string.agreed_baba));
		sp.setSpan(new URLSpan("http://api.bibibaba.cn/help/fwtk"), 16, 27,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_note.setText(sp);
		tv_note.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}