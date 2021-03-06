package com.wise.state;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.car.DevicesAddActivity;
import com.wise.notice.LetterActivity;
import customView.FaultDeletionView;
import customView.OnViewChangeListener;
import data.CarData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 车况检测
 * 
 * @author honesty
 * 
 */
public class FaultDetectionActivity extends Activity {
	private static final String TAG = "FaultDetectionActivity";
	private static final int getData = 1;
	private static final int refresh = 2;
	private static final int getFault = 3;
	/** 刷新分数 **/
	private static final int refresh_score = 4;

	TextView tv_name;
	TextView tv_guzhang, tv_guzhang_icon, tv_dianyuan, tv_dianyuan_icon,
			tv_jinqi, tv_jinqi_icon, tv_daisu, tv_daisu_icon, tv_lengque,
			tv_lengque_icon, tv_paifang, tv_paifang_icon;
	LinearLayout ll_fault;
	ImageView iv_left, iv_right;
	/** 体检返回分数 **/
	private int mTotalProgress = 100;
	/** 动画过程显示分数 **/
	private int mCurrentProgress = 100;

	private static final int Point = 6;
	int Interval = 0;
	String fault_content = "";
	int index;
	boolean isCheck = false;
	FaultDeletionView hs_car;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_detection);
		app = (AppApplication) getApplication();
		initView();
		ll_fault = (LinearLayout) findViewById(R.id.ll_fault);
		iv_right = (ImageView) findViewById(R.id.iv_right);
		iv_right.setOnClickListener(onClickListener);
		iv_left = (ImageView) findViewById(R.id.iv_left);
		iv_left.setOnClickListener(onClickListener);
		hs_car = (FaultDeletionView) findViewById(R.id.hs_car);
		hs_car.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view) {
				// 关闭线程
				mCurrent = 100;
				index = view;
				fristSetLeftRight();
				getSpHistoryData(index);
			}

			@Override
			public void OnLastView() {
			}

			@Override
			public void OnFinish(int index) {
			}
		});
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		index = getIntent().getIntExtra("index", 0);

		fristSetLeftRight();
		initDataView();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hs_car.snapFastToScreen(index);
				getSpHistoryData(index);
				getData(index);
			}
		}, 50);

	}

	private void fristSetLeftRight() {
		if (app.carDatas.size() == 1) {
			iv_left.setVisibility(View.GONE);
			iv_right.setVisibility(View.GONE);
		} else if (index == 0) {
			iv_left.setVisibility(View.GONE);
			iv_right.setVisibility(View.VISIBLE);
		} else if (index == (app.carDatas.size() - 1)) {
			iv_left.setVisibility(View.VISIBLE);
			iv_right.setVisibility(View.GONE);
		} else {
			iv_left.setVisibility(View.VISIBLE);
			iv_right.setVisibility(View.VISIBLE);
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.iv_left) {
				iv_right.setVisibility(View.VISIBLE);
				if (index != 0) {
					index--;
					hs_car.snapToScreen(index);
					if (index == 0) {
						iv_left.setVisibility(View.GONE);
					}
				}
			} else if (v.getId() == R.id.iv_right) {
				iv_left.setVisibility(View.VISIBLE);
				if (index != (app.carDatas.size() - 1)) {
					index++;
					hs_car.snapToScreen(index);
					if (index == (app.carDatas.size() - 1)) {
						iv_right.setVisibility(View.GONE);
					}
				}
			} else if (v.getId() == R.id.tasks_view) {
				getData(index);
			} else if (v.getId() == R.id.iv_back) {
				Back();
				finish();
			} else {
				try {
					String Device_id = app.carDatas.get(index).getDevice_id();
					Intent intent2 = new Intent(FaultDetectionActivity.this,
							DevicesAddActivity.class);
					intent2.putExtra("car_series_id", app.carDatas.get(index)
							.getCar_series_id());
					intent2.putExtra("car_series", app.carDatas.get(index)
							.getCar_series());
					Intent intent = new Intent(FaultDetectionActivity.this,
							DyActivity.class);
					intent.putExtra("device_id", Device_id);
					intent.putExtra("state", true);
					JSONObject jsonObject = new JSONObject(result);
					switch (v.getId()) {
					case R.id.rl_guzhang:
						if (Device_id == null || Device_id.equals("")) {
							intent.putExtra("car_id", app.carDatas.get(index)
									.getObj_id());
							startActivityForResult(intent2, 2);
						} else {
							Intent intent1 = new Intent(
									FaultDetectionActivity.this,
									FaultDetailActivity.class);
							intent1.putExtra("fault_content", fault_content);
							intent1.putExtra("device_id", Device_id);
							intent1.putExtra("index", index);
							startActivity(intent1);
						}

						break;
					case R.id.rl_dianyuan:
						if (Device_id == null || Device_id.equals("")) {
							intent.putExtra("car_id", app.carDatas.get(index)
									.getObj_id());
							startActivityForResult(intent2, 2);
						} else {
							intent.putExtra("type", 1);
							intent.putExtra("title",
									getString(R.string.dian_yuan));
							intent.putExtra("name", getString(R.string.voltage));
							intent.putExtra("range",
									jsonObject.getString("dpdy_range"));
							intent.putExtra("if_err",
									!jsonObject.getBoolean("if_dpdy_err"));
							intent.putExtra("current",
									jsonObject.getString("dpdy"));
							intent.putExtra("if_lt_err",
									!jsonObject.getBoolean("if_lt_dpdy_err"));
							intent.putExtra("lt",
									jsonObject.getString("lt_dpdy"));
							intent.putExtra("url",
									jsonObject.getString("dpdy_content"));
							startActivity(intent);
						}
						break;
					case R.id.rl_jinqi:
						if (Device_id == null || Device_id.equals("")) {
							intent.putExtra("car_id", app.carDatas.get(index)
									.getObj_id());
							startActivityForResult(intent2, 2);
						} else {
							intent.putExtra("type", 2);
							intent.putExtra("title", getString(R.string.jin_qi));
							intent.putExtra("name",
									getString(R.string.jie_qi_men));
							intent.putExtra("range",
									jsonObject.getString("jqmkd_range"));
							intent.putExtra("if_err",
									!jsonObject.getBoolean("if_jqmkd_err"));
							intent.putExtra("current",
									jsonObject.getString("jqmkd"));
							intent.putExtra("if_lt_err",
									!jsonObject.getBoolean("if_lt_jqmkd_err"));
							intent.putExtra("lt",
									jsonObject.getString("lt_jqmkd"));
							intent.putExtra("url",
									jsonObject.getString("jqmkd_content"));
							startActivity(intent);
						}
						break;
					case R.id.rl_daisu:
						if (Device_id == null || Device_id.equals("")) {
							intent.putExtra("car_id", app.carDatas.get(index)
									.getObj_id());
							startActivityForResult(intent2, 2);
						} else {
							intent.putExtra("type", 3);
							intent.putExtra("title", getString(R.string.dai_su));
							intent.putExtra("name",
									getString(R.string.dai_su_state));
							intent.putExtra("range",
									jsonObject.getString("fdjzs_range"));
							intent.putExtra("if_err",
									!jsonObject.getBoolean("if_fdjzs_err"));
							intent.putExtra("current",
									jsonObject.getString("fdjzs"));
							intent.putExtra("if_lt_err",
									!jsonObject.getBoolean("if_lt_fdjzs_err"));
							intent.putExtra("lt",
									jsonObject.getString("lt_fdjzs"));
							intent.putExtra("url",
									jsonObject.getString("fdjzs_content"));
							startActivity(intent);
						}
						break;
					case R.id.rl_lengque:
						if (Device_id == null || Device_id.equals("")) {
							intent.putExtra("car_id", app.carDatas.get(index)
									.getObj_id());
							startActivityForResult(intent2, 2);
						} else {
							intent.putExtra("type", 4);
							intent.putExtra("title",
									getString(R.string.leng_que));
							intent.putExtra("name",
									getString(R.string.water_state));
							intent.putExtra("range",
									jsonObject.getString("sw_range"));
							intent.putExtra("if_err",
									!jsonObject.getBoolean("if_sw_err"));
							intent.putExtra("current",
									jsonObject.getString("sw"));
							intent.putExtra("if_lt_err",
									!jsonObject.getBoolean("if_lt_sw_err"));
							intent.putExtra("lt", jsonObject.getString("lt_sw"));
							intent.putExtra("url",
									jsonObject.getString("sw_content"));
							startActivity(intent);
						}
						break;
					case R.id.rl_paifang:
						if (Device_id == null || Device_id.equals("")) {
							intent.putExtra("car_id", app.carDatas.get(index)
									.getObj_id());
							startActivityForResult(intent2, 2);
						} else {
							intent.putExtra("type", 5);
							intent.putExtra("title",
									getString(R.string.pai_fang));
							intent.putExtra("name",
									getString(R.string.san_yuan_state));
							intent.putExtra("range",
									jsonObject.getString("chqwd_range"));
							intent.putExtra("if_err",
									!jsonObject.getBoolean("if_chqwd_err"));
							intent.putExtra("current",
									jsonObject.getString("chqwd"));
							intent.putExtra("if_lt_err",
									!jsonObject.getBoolean("if_lt_chqwd_err"));
							intent.putExtra("lt",
									jsonObject.getString("lt_chqwd"));
							intent.putExtra("url",
									jsonObject.getString("chqwd_content"));
							startActivity(intent);
						}
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				result = msg.obj.toString();
				jsonHealth(msg.obj.toString());
				// 体检
				new Thread(new ProgressRunable(msg.arg1)).start();
				break;
			case refresh:
				refreshHealth(msg.arg1);
				break;
			case getFault:
				fault_content = msg.obj.toString();
				break;
			case refresh_score:
				carViews.get(msg.arg1).getTv_score()
						.setText(String.valueOf(mCurrentProgress));
				if (msg.arg2 == 0) {
					carViews.get(msg.arg1).getTv_detection_status()
							.setText(getString(R.string.click_medical));
				}
				break;
			}
		}
	};
	List<CarView> carViews = new ArrayList<CarView>();

	/** 滑动车辆布局 **/
	private void initDataView() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		hs_car.removeAllViews();
		for (int i = 0; i < app.carDatas.size(); i++) {
			View v = LayoutInflater.from(this).inflate(
					R.layout.item_fault_detection, null);
			hs_car.addView(v);
			TasksCompletedView mTasksView = (TasksCompletedView) v
					.findViewById(R.id.tasks_view);
			mTasksView.setOnClickListener(onClickListener);
			TextView tv_score = (TextView) v.findViewById(R.id.tv_score);
			TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
			TextView tv_detection_status = (TextView) v
					.findViewById(R.id.tv_detection_status);

			CarView carView = new CarView();
			carView.setmTasksView(mTasksView);
			carView.setTv_score(tv_score);
			carView.setTv_title(tv_title);
			carView.setTv_detection_status(tv_detection_status);
			carViews.add(carView);

			tv_name.setText(app.carDatas.get(i).getCar_series() + "("
					+ app.carDatas.get(i).getNick_name() + ")");
			String result = preferences.getString(Constant.sp_health_score
					+ app.carDatas.get(i).getObj_id(), "");
			tv_detection_status.setText(getString(R.string.click_medical));
			if (result.equals("")) {// 未体检过
				carView.getmTasksView().setProgress(100);
				tv_score.setText("0");
				tv_title.setText(getString(R.string.no_medical));
			} else {
				try {
					JSONObject jsonObject = new JSONObject(result);
					// 健康指数
					int health_score = jsonObject.getInt("health_score");
					carView.getmTasksView().setProgress(health_score);
					tv_score.setText(String.valueOf(health_score));
					tv_title.setText(getString(R.string.health_index));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** 获取历史消息 **/
	private void getSpHistoryData(int index) {
		if (app.carDatas == null || app.carDatas.size() == 0) {
			return;
		}
		CarData carData = app.carDatas.get(index);
		tv_name.setText(carData.getNick_name());

		String Device_id = carData.getDevice_id();
		if (Device_id == null || Device_id.equals("")) {
			carViews.get(index).getmTasksView().setProgress(100);
			carViews.get(index).getTv_score().setText(String.valueOf(0));

			tv_guzhang.setText(getString(R.string.no_bind));
			tv_guzhang
					.setTextColor(getResources().getColor(R.color.blue_press));
			Drawable drawable = getResources().getDrawable(
					R.drawable.icon_guzhang_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_guzhang_icon.setCompoundDrawables(drawable, null, null, null);

			tv_dianyuan.setText(getString(R.string.no_bind));
			tv_dianyuan.setTextColor(getResources()
					.getColor(R.color.blue_press));
			drawable = getResources().getDrawable(
					R.drawable.icon_dianyuan_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_dianyuan_icon.setCompoundDrawables(drawable, null, null, null);

			tv_jinqi.setText(getString(R.string.no_bind));
			tv_jinqi.setTextColor(getResources().getColor(R.color.blue_press));
			drawable = getResources().getDrawable(R.drawable.icon_jinqi_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_jinqi_icon.setCompoundDrawables(drawable, null, null, null);

			tv_daisu.setText(getString(R.string.no_bind));
			tv_daisu.setTextColor(getResources().getColor(R.color.blue_press));
			drawable = getResources().getDrawable(R.drawable.icon_daisu_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_daisu_icon.setCompoundDrawables(drawable, null, null, null);

			tv_lengque.setText(getString(R.string.no_bind));
			tv_lengque
					.setTextColor(getResources().getColor(R.color.blue_press));
			drawable = getResources().getDrawable(
					R.drawable.icon_lengque_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_lengque_icon.setCompoundDrawables(drawable, null, null, null);

			tv_paifang.setText(getString(R.string.no_bind));
			tv_paifang
					.setTextColor(getResources().getColor(R.color.blue_press));
			drawable = getResources().getDrawable(
					R.drawable.icon_paifang_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_paifang_icon.setCompoundDrawables(drawable, null, null, null);
		} else {
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			result = preferences.getString(Constant.sp_health_score
					+ app.carDatas.get(index).getObj_id(), "");
			if (result.equals("")) {// 未体检过
				carViews.get(index).getmTasksView().setProgress(100);
				carViews.get(index).getTv_score().setText(String.valueOf(0));

				tv_guzhang.setText(getString(R.string.no_fault));
				tv_guzhang.setTextColor(getResources().getColor(
						R.color.blue_press));
				Drawable drawable = getResources().getDrawable(
						R.drawable.icon_guzhang_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				tv_guzhang_icon
						.setCompoundDrawables(drawable, null, null, null);

				tv_dianyuan.setText(getString(R.string.voltage_state_good));
				tv_dianyuan.setTextColor(getResources().getColor(
						R.color.blue_press));
				drawable = getResources().getDrawable(
						R.drawable.icon_dianyuan_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				tv_dianyuan_icon.setCompoundDrawables(drawable, null, null,
						null);

				tv_jinqi.setText(getString(R.string.jie_qi_men_good));
				tv_jinqi.setTextColor(getResources().getColor(
						R.color.blue_press));
				drawable = getResources().getDrawable(
						R.drawable.icon_jinqi_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				tv_jinqi_icon.setCompoundDrawables(drawable, null, null, null);

				tv_daisu.setText(getString(R.string.dai_su_stable));
				tv_daisu.setTextColor(getResources().getColor(
						R.color.blue_press));
				drawable = getResources().getDrawable(
						R.drawable.icon_daisu_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				tv_daisu_icon.setCompoundDrawables(drawable, null, null, null);

				tv_lengque.setText(getString(R.string.water_normal));
				tv_lengque.setTextColor(getResources().getColor(
						R.color.blue_press));
				drawable = getResources().getDrawable(
						R.drawable.icon_lengque_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				tv_lengque_icon
						.setCompoundDrawables(drawable, null, null, null);

				tv_paifang.setText(getString(R.string.san_yuan_state_good));
				tv_paifang.setTextColor(getResources().getColor(
						R.color.blue_press));
				drawable = getResources().getDrawable(
						R.drawable.icon_paifang_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				tv_paifang_icon
						.setCompoundDrawables(drawable, null, null, null);

			} else {
				try {
					JSONObject jsonObject = new JSONObject(result);
					// 健康指数
					int health_score = jsonObject.getInt("health_score");
					carViews.get(index).getTv_score()
							.setText(String.valueOf(health_score));
					carViews.get(index).getmTasksView()
							.setProgress(health_score);

					JSONArray jsonErrArray = jsonObject
							.getJSONArray("active_obd_err");
					if (jsonErrArray.length() > 0) {
						String url = Constant.BaseUrl
								+ "device/fault_desc_new?auth_code="
								+ app.auth_code;
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("brand", app.carDatas
								.get(index).getCar_brand()));
						params.add(new BasicNameValuePair("obd_err", jsonObject
								.getString("active_obd_err")));
						new NetThread.postDataThread(handler, url, params,
								getFault).start();
						tv_guzhang.setText(getString(R.string.have)
								+ jsonErrArray.length()
								+ getString(R.string.fault));
						tv_guzhang.setTextColor(getResources().getColor(
								R.color.yellow));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_guzhang_abnormal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_guzhang_icon.setCompoundDrawables(drawable, null,
								null, null);
					} else {
						tv_guzhang.setText(getString(R.string.no_fault));
						tv_guzhang.setTextColor(getResources().getColor(
								R.color.blue_press));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_guzhang_normal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_guzhang_icon.setCompoundDrawables(drawable, null,
								null, null);
					}

					// 电源系统
					boolean if_dpdy_err = !jsonObject.getBoolean("if_dpdy_err");
					dpdy_content = jsonObject.getString("dpdy_content");
					if (if_dpdy_err) {
						tv_dianyuan
								.setText(getString(R.string.voltage_state_good));
						tv_dianyuan.setTextColor(getResources().getColor(
								R.color.blue_press));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_dianyuan_normal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_dianyuan_icon.setCompoundDrawables(drawable, null,
								null, null);
					} else {
						tv_dianyuan
								.setText(getString(R.string.voltage_state_abnormal));
						tv_dianyuan.setTextColor(getResources().getColor(
								R.color.yellow));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_dianyuan_abnormal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_dianyuan_icon.setCompoundDrawables(drawable, null,
								null, null);
					}
					// 进气系统
					boolean if_jqmkd_err = !jsonObject
							.getBoolean("if_jqmkd_err");
					jqmkd_content = jsonObject.getString("jqmkd_content");
					if (if_jqmkd_err) {
						tv_jinqi.setText(getString(R.string.jie_qi_men_good));
						tv_jinqi.setTextColor(getResources().getColor(
								R.color.blue_press));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_jinqi_normal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_jinqi_icon.setCompoundDrawables(drawable, null,
								null, null);
					} else {
						tv_jinqi.setText(getString(R.string.jie_qi_men_abnormal));
						tv_jinqi.setTextColor(getResources().getColor(
								R.color.yellow));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_jinqi_abnormal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_jinqi_icon.setCompoundDrawables(drawable, null,
								null, null);
					}
					// 怠速控制系统
					boolean if_fdjzs_err = !jsonObject
							.getBoolean("if_fdjzs_err");
					fdjzs_content = jsonObject.getString("fdjzs_content");
					if (if_fdjzs_err) {
						tv_daisu.setText(getString(R.string.dai_su_stable));
						tv_daisu.setTextColor(getResources().getColor(
								R.color.blue_press));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_daisu_normal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_daisu_icon.setCompoundDrawables(drawable, null,
								null, null);
					} else {
						tv_daisu.setText(getString(R.string.dai_su_abnormal));
						tv_daisu.setTextColor(getResources().getColor(
								R.color.yellow));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_daisu_abnormal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_daisu_icon.setCompoundDrawables(drawable, null,
								null, null);
					}
					// 冷却系统
					boolean if_sw_err = !jsonObject.getBoolean("if_sw_err");
					sw_content = jsonObject.getString("sw_content");
					if (if_sw_err) {
						tv_lengque.setText(getString(R.string.water_normal));
						tv_lengque.setTextColor(getResources().getColor(
								R.color.blue_press));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_lengque_normal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_lengque_icon.setCompoundDrawables(drawable, null,
								null, null);
					} else {
						tv_lengque.setText(getString(R.string.water_abnormall));
						tv_lengque.setTextColor(getResources().getColor(
								R.color.yellow));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_lengque_abnormal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_lengque_icon.setCompoundDrawables(drawable, null,
								null, null);
					}
					// 排放系统
					boolean if_chqwd_err = !jsonObject
							.getBoolean("if_chqwd_err");
					chqwd_content = jsonObject.getString("chqwd_content");
					if (if_chqwd_err) {
						tv_paifang
								.setText(getString(R.string.san_yuan_state_good));
						tv_paifang.setTextColor(getResources().getColor(
								R.color.blue_press));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_paifang_normal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_paifang_icon.setCompoundDrawables(drawable, null,
								null, null);
					} else {
						tv_paifang
								.setText(getString(R.string.san_yuan_state_abnormal));
						tv_paifang.setTextColor(getResources().getColor(
								R.color.yellow));
						Drawable drawable = getResources().getDrawable(
								R.drawable.icon_paifang_abnormal);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						tv_paifang_icon.setCompoundDrawables(drawable, null,
								null, null);
					}
					// 获取历史消息
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class CarView {
		TextView tv_score;
		TextView tv_title;
		TextView tv_detection_status;
		TasksCompletedView mTasksView;

		public TextView getTv_detection_status() {
			return tv_detection_status;
		}

		public void setTv_detection_status(TextView tv_detection_status) {
			this.tv_detection_status = tv_detection_status;
		}

		public TextView getTv_title() {
			return tv_title;
		}

		public void setTv_title(TextView tv_title) {
			this.tv_title = tv_title;
		}

		public TasksCompletedView getmTasksView() {
			return mTasksView;
		}

		public void setmTasksView(TasksCompletedView mTasksView) {
			this.mTasksView = mTasksView;
		}

		public TextView getTv_score() {
			return tv_score;
		}

		public void setTv_score(TextView tv_score) {
			this.tv_score = tv_score;
		}
	}

	/** 初始化数据 **/
	private void initapp() {
		j = 0;
		mCurrent = 0;
		mTotalProgress = 100;
		Interval = 30 / Point;
		carViews.get(index).getmTasksView().setProgress(100);
		carViews.get(index).getTv_score().setText(String.valueOf(100));
		carViews.get(index).getTv_detection_status()
				.setText(getString(R.string.medicaling));
		// 始化数据
		tv_guzhang.setText(getString(R.string.fault_detection));
		tv_guzhang.setTextColor(getResources().getColor(R.color.blue_press));
		Drawable drawable = getResources().getDrawable(
				R.drawable.icon_guzhang_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_guzhang_icon.setCompoundDrawables(drawable, null, null, null);

		tv_dianyuan.setText(getString(R.string.voltage_detection));
		tv_dianyuan.setTextColor(getResources().getColor(R.color.blue_press));
		drawable = getResources().getDrawable(R.drawable.icon_dianyuan_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_dianyuan_icon.setCompoundDrawables(drawable, null, null, null);

		tv_jinqi.setText(getString(R.string.jie_qi_men_detection));
		tv_jinqi.setTextColor(getResources().getColor(R.color.blue_press));
		drawable = getResources().getDrawable(R.drawable.icon_jinqi_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_jinqi_icon.setCompoundDrawables(drawable, null, null, null);

		tv_daisu.setText(getString(R.string.dai_su_detection));
		tv_daisu.setTextColor(getResources().getColor(R.color.blue_press));
		drawable = getResources().getDrawable(R.drawable.icon_daisu_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_daisu_icon.setCompoundDrawables(drawable, null, null, null);

		tv_lengque.setText(getString(R.string.water_detection));
		tv_lengque.setTextColor(getResources().getColor(R.color.blue_press));
		drawable = getResources().getDrawable(R.drawable.icon_lengque_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_lengque_icon.setCompoundDrawables(drawable, null, null, null);

		tv_paifang.setText(getString(R.string.san_yuan_detection));
		tv_paifang.setTextColor(getResources().getColor(R.color.blue_press));
		drawable = getResources().getDrawable(R.drawable.icon_paifang_normal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_paifang_icon.setCompoundDrawables(drawable, null, null, null);
	}

	private void initView() {
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_guzhang = (TextView) findViewById(R.id.tv_guzhang);
		tv_guzhang_icon = (TextView) findViewById(R.id.tv_guzhang_icon);
		tv_dianyuan = (TextView) findViewById(R.id.tv_dianyuan);
		tv_dianyuan_icon = (TextView) findViewById(R.id.tv_dianyuan_icon);
		tv_jinqi = (TextView) findViewById(R.id.tv_jinqi);
		tv_jinqi_icon = (TextView) findViewById(R.id.tv_jinqi_icon);
		tv_daisu = (TextView) findViewById(R.id.tv_daisu);
		tv_daisu_icon = (TextView) findViewById(R.id.tv_daisu_icon);
		tv_lengque = (TextView) findViewById(R.id.tv_lengque);
		tv_lengque_icon = (TextView) findViewById(R.id.tv_lengque_icon);
		tv_paifang = (TextView) findViewById(R.id.tv_paifang);
		tv_paifang_icon = (TextView) findViewById(R.id.tv_paifang_icon);
		RelativeLayout rl_guzhang = (RelativeLayout) findViewById(R.id.rl_guzhang);
		rl_guzhang.setOnClickListener(onClickListener);
		RelativeLayout rl_dianyuan = (RelativeLayout) findViewById(R.id.rl_dianyuan);
		rl_dianyuan.setOnClickListener(onClickListener);
		RelativeLayout rl_jinqi = (RelativeLayout) findViewById(R.id.rl_jinqi);
		rl_jinqi.setOnClickListener(onClickListener);
		RelativeLayout rl_daisu = (RelativeLayout) findViewById(R.id.rl_daisu);
		rl_daisu.setOnClickListener(onClickListener);
		RelativeLayout rl_lengque = (RelativeLayout) findViewById(R.id.rl_lengque);
		rl_lengque.setOnClickListener(onClickListener);
		RelativeLayout rl_paifang = (RelativeLayout) findViewById(R.id.rl_paifang);
		rl_paifang.setOnClickListener(onClickListener);
	}

	int i = 0;
	int j = 0;
	/**
	 * 总分100，得分60，到计时40；总共有6个点
	 * 
	 */
	// class ProgressThread extends Thread{
	// @Override
	// public void run() {
	// super.run();
	// Message message = new Message();
	// message.what = refresh_score;
	// handler.sendMessage(message);
	// }
	// }
	int mCurrent = 0;

	class ProgressRunable implements Runnable {
		int count = 30;
		int index;

		public ProgressRunable(int index) {
			this.index = index;
		}

		@Override
		public void run() {
			// 设置3s检测完毕
			isCheck = true;
			while (count > mCurrent) {
				count--;
				// 计算 60 100分 间隔 40分 40/30
				mCurrentProgress = (int) (100 - (100 - mTotalProgress)
						* (1 - (float) count / 30));
				carViews.get(index).getmTasksView()
						.setProgress(mCurrentProgress);
				Message message = new Message();
				message.what = refresh_score;
				message.arg1 = index;
				message.arg2 = count;
				handler.sendMessage(message);
				i++;
				if (i == Interval) {
					i = 0;
					j++;
					Message message1 = new Message();
					message1.what = refresh;
					message1.arg1 = j;
					handler.sendMessage(message1);
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			isCheck = false;
		}
	}

	// 弹出体检提示，并且获取提示数据
	// private void getMedical(final String Device_id, final int index) {
	// if (isCheck) {
	// Toast.makeText(FaultDetectionActivity.this, R.string.medicaling,
	// Toast.LENGTH_SHORT).show();
	// return;
	// }
	// // 弹出提示框
	// AlertDialog.Builder dialog = new AlertDialog.Builder(
	// FaultDetectionActivity.this);
	// dialog.setTitle(R.string.note);
	// dialog.setMessage(R.string.start_car_medical);
	// dialog.setPositiveButton(R.string.determine,
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// initapp();
	// String url;
	// try {
	// url = Constant.BaseUrl
	// + "device/"
	// + Device_id
	// + "/health_exam?auth_code="
	// + app.auth_code
	// + "&brand="
	// + URLEncoder.encode(app.carDatas.get(index)
	// .getCar_brand(), "UTF-8");
	//
	// new NetThread.GetDataThread(handler, url, getData,
	// index).start();
	// } catch (UnsupportedEncodingException e) {
	// e.printStackTrace();
	// }
	// }
	// }).setNegativeButton(R.string.cancel, null).show();
	// }

	// // TODO 判断车辆是否为启动状态
	// boolean state = false;

	/** 获取健康数据 **/
	private void getData(int index) {
		try {
			String Device_id = app.carDatas.get(index).getDevice_id();
			if (Device_id == null || Device_id.equals("")) {
				Intent intent = new Intent(FaultDetectionActivity.this,
						DevicesAddActivity.class);
				intent.putExtra("car_id", app.carDatas.get(index).getObj_id());
				intent.putExtra("car_series_id", app.carDatas.get(index)
						.getCar_series_id());
				intent.putExtra("car_series", app.carDatas.get(index)
						.getCar_series());
				startActivityForResult(intent, 2);
				return;
			}
			// String uni_status = app.carDatas.get(index).getUni_status();
			// String rcv_time = app.carDatas.get(index).getRcv_time();
			// if (uni_status == null || rcv_time == null) {
			// getMedical(Device_id, index);
			// return;
			// }
			// JSONArray jsonArray = new JSONArray(uni_status);
			// if (jsonArray.toString() == null ||
			// jsonArray.toString().equals("")) {
			//
			// } else {
			// for (int i = 0; i < jsonArray.length(); i++) {
			// if (jsonArray.getInt(i) == 8196) {
			// state = true;
			// break;
			// }
			// }
			// }
			// if (!state || (GetSystem.spacingNowTime(rcv_time) / 60) > 10) {
			// getMedical(Device_id, index);
			// } else {
			if (isCheck) {
				Toast.makeText(FaultDetectionActivity.this,
						R.string.medicaling, Toast.LENGTH_SHORT).show();
				return;
			}
			initapp();
			String url;
			try {
				url = Constant.BaseUrl
						+ "device/"
						+ Device_id
						+ "/health_exam?auth_code="
						+ app.auth_code
						+ "&brand="
						+ URLEncoder.encode(app.carDatas.get(index)
								.getCar_brand(), "UTF-8");

				new NetThread.GetDataThread(handler, url, getData, index)
						.start();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String result = "";
	String dpdy_content, jqmkd_content, fdjzs_content, sw_content,
			chqwd_content;

	private void refreshHealth(int j) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			switch (j) {
			case 1:
				JSONArray jsonErrArray = jsonObject
						.getJSONArray("active_obd_err");
				if (jsonErrArray.length() > 0) {
					String url = Constant.BaseUrl
							+ "device/fault_desc_new?auth_code="
							+ app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("brand", app.carDatas
							.get(index).getCar_brand()));
					params.add(new BasicNameValuePair("obd_err", jsonObject
							.getString("active_obd_err")));
					new NetThread.postDataThread(handler, url, params, getFault)
							.start();
					tv_guzhang
							.setText(getString(R.string.have)
									+ jsonErrArray.length()
									+ getString(R.string.fault));
					tv_guzhang.setTextColor(getResources().getColor(
							R.color.yellow));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_guzhang_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_guzhang_icon.setCompoundDrawables(drawable, null, null,
							null);
				} else {
					tv_guzhang.setText(getString(R.string.no_fault));
					tv_guzhang.setTextColor(getResources().getColor(
							R.color.blue_press));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_guzhang_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_guzhang_icon.setCompoundDrawables(drawable, null, null,
							null);
				}
				break;
			case 2:
				// 电源系统
				boolean if_dpdy_err = !jsonObject.getBoolean("if_dpdy_err");
				dpdy_content = jsonObject.getString("dpdy_content");
				if (if_dpdy_err) {
					tv_dianyuan.setText(getString(R.string.voltage_state_good));
					tv_dianyuan.setTextColor(getResources().getColor(
							R.color.blue_press));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_dianyuan_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_dianyuan_icon.setCompoundDrawables(drawable, null, null,
							null);
				} else {
					tv_dianyuan
							.setText(getString(R.string.voltage_state_abnormal));
					tv_dianyuan.setTextColor(getResources().getColor(
							R.color.yellow));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_dianyuan_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_dianyuan_icon.setCompoundDrawables(drawable, null, null,
							null);
				}
				break;
			case 3:
				// 进气系统
				boolean if_jqmkd_err = !jsonObject.getBoolean("if_jqmkd_err");
				jqmkd_content = jsonObject.getString("jqmkd_content");
				if (if_jqmkd_err) {
					tv_jinqi.setText(getString(R.string.jie_qi_men_good));
					tv_jinqi.setTextColor(getResources().getColor(
							R.color.blue_press));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_jinqi_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_jinqi_icon.setCompoundDrawables(drawable, null, null,
							null);
				} else {
					tv_jinqi.setText(getString(R.string.jie_qi_men_abnormal));
					tv_jinqi.setTextColor(getResources().getColor(
							R.color.yellow));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_jinqi_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_jinqi_icon.setCompoundDrawables(drawable, null, null,
							null);
				}
				break;
			case 4:
				// 怠速控制系统
				boolean if_fdjzs_err = !jsonObject.getBoolean("if_fdjzs_err");
				fdjzs_content = jsonObject.getString("fdjzs_content");
				if (if_fdjzs_err) {
					tv_daisu.setText(getString(R.string.dai_su_stable));
					tv_daisu.setTextColor(getResources().getColor(
							R.color.blue_press));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_daisu_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_daisu_icon.setCompoundDrawables(drawable, null, null,
							null);
				} else {
					tv_daisu.setText(getString(R.string.dai_su_abnormal));
					tv_daisu.setTextColor(getResources().getColor(
							R.color.yellow));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_daisu_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_daisu_icon.setCompoundDrawables(drawable, null, null,
							null);
				}
				break;
			case 5:
				// 冷却系统
				boolean if_sw_err = !jsonObject.getBoolean("if_sw_err");
				sw_content = jsonObject.getString("sw_content");
				if (if_sw_err) {
					tv_lengque.setText(getString(R.string.water_normal));
					tv_lengque.setTextColor(getResources().getColor(
							R.color.blue_press));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_lengque_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_lengque_icon.setCompoundDrawables(drawable, null, null,
							null);
				} else {
					tv_lengque.setText(getString(R.string.water_abnormall));
					tv_lengque.setTextColor(getResources().getColor(
							R.color.yellow));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_lengque_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_lengque_icon.setCompoundDrawables(drawable, null, null,
							null);
				}
				break;
			case 6:
				// 排放系统
				boolean if_chqwd_err = !jsonObject.getBoolean("if_chqwd_err");
				chqwd_content = jsonObject.getString("chqwd_content");
				if (if_chqwd_err) {
					tv_paifang.setText(getString(R.string.san_yuan_state_good));
					tv_paifang.setTextColor(getResources().getColor(
							R.color.blue_press));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_paifang_normal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_paifang_icon.setCompoundDrawables(drawable, null, null,
							null);
				} else {
					tv_paifang
							.setText(getString(R.string.san_yuan_state_abnormal));
					tv_paifang.setTextColor(getResources().getColor(
							R.color.yellow));
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_paifang_abnormal);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_paifang_icon.setCompoundDrawables(drawable, null, null,
							null);
				}
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 存储
	private void jsonHealth(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			int health_score = jsonObject.getInt("health_score");
			// 分数
			mTotalProgress = health_score;
			Interval = 30 / Point;
			// 体检结果存起来
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString(Constant.sp_health_score
					+ app.carDatas.get(index).getObj_id(), str);
			editor.commit();
			carViews.get(index).getTv_title()
					.setText(getString(R.string.health_index));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void Back() {
		Intent intent = new Intent();
		intent.putExtra("health_score", mTotalProgress);
		setResult(2, intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Back();
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {// 绑定终端返回
			initDataView();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hs_car.snapFastToScreen(index);
					getSpHistoryData(index);
				}
			}, 50);
		}
	}
}
