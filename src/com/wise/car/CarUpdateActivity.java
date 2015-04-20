package com.wise.car;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import nadapter.OpenDateDialog;
import nadapter.OpenDateDialogListener;
import pubclas.Constant;
import pubclas.NetThread;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import data.CarData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 修改车辆信息
 * 
 * @author honesty
 * 
 */
public class CarUpdateActivity extends Activity {
	
	private final String TAG = "CarUpdateActivity";
	
	private final int inspection = 1;
	private final int buy_date = 2;
	private final int year_check = 3;
	private final int update = 4;

	private static final int getFuelPrice = 6;

	EditText et_nick_name,
			 et_insurance_tel, 
			 et_insurance_no, 
			 et_maintain_tel;	
	EditText et_oil_price;// 加油价格
	TextView 	tv_models, 
				tv_gas_no, 
				tv_insurance_company, 
				tv_insurance_date,
				tv_maintain_company,
				tv_buy_date, 
				tv_year_check;
	int index = 0;
	CarData carData;
	CarData carNewData = new CarData();

	String car_brand = "";
	String car_brand_id = "";
	String car_series = "";
	String car_series_id = "";
	String car_type = "";
	String car_type_id = "";
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_update);
		
		app = (AppApplication) getApplication();
		index = getIntent().getIntExtra("index", 0);		
		carData = app.carDatas.get(index);
		carNewData = carData;

		init();
		setData();
		setTime();
		getFuelPrice();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;		
			case R.id.iv_save:	
				if (app.isTest) {
					Toast.makeText(CarUpdateActivity.this,R.string.no_function, 
							Toast.LENGTH_SHORT).show();
					return;
				}
				try{
					Save();//保存修改的数据
				}catch (Exception e) {
					break;
				}
				break;
			case R.id.tv_models:
				startActivityForResult(new Intent(CarUpdateActivity.this,
						ModelsActivity.class), 2);
				break;
			case R.id.tv_gas_no:
				startActivityForResult(new Intent(CarUpdateActivity.this,
						PetrolGradeActivity.class), 2);
				break;
			case R.id.tv_insurance_date:
				ShowDate(inspection);
				break;
			case R.id.tv_buy_date:
				ShowDate(buy_date);
				break;
			case R.id.tv_year_check:
				ShowDate(year_check);
				break;
			}
		}
	};
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case update:
				try{
					jsonSave(msg.obj.toString());
				}catch (Exception e) {
					break;
				}
				break;			
			case getFuelPrice:
				jsonFuelPrice(msg.obj.toString());
				break;
			}
		}
	};

	/** 获取油价 **/
	private void getFuelPrice() {
		try {
			String url = Constant.BaseUrl + "base/city/"	
					+ URLEncoder.encode("深圳", "UTF-8");
			new NetThread.GetDataThread(handler, url, getFuelPrice).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String fuel90 = "0";
	String fuel93 = "0";
	String fuel97 = "0";
	String fuel0 = "0";

	/** 解析油价 **/
	private void jsonFuelPrice(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result)
					.getJSONObject("fuel_price");
			fuel90 = jsonObject.getString("fuel90");
			fuel93 = jsonObject.getString("fuel93");
			fuel97 = jsonObject.getString("fuel97");
			fuel0 = jsonObject.getString("fuel0");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ShowDate(int index) {
		OpenDateDialog.ShowDate(CarUpdateActivity.this, index);
	}
	//解析返回来的状态码，status==0 表示修改成功
	private void jsonSave(String str) {	
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getInt("status_code") == 0) {	
				app.carDatas.set(index, carNewData);
				setResult(3);
				Toast.makeText(CarUpdateActivity.this, R.string.save_success,
						Toast.LENGTH_SHORT).show();
				finish();//关闭界面

			} else {			
				Toast.makeText(CarUpdateActivity.this, R.string.save_faild,
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(CarUpdateActivity.this, R.string.save_faild,
					Toast.LENGTH_SHORT).show();
		}
	}
		
//把数据填到List<NameValuePair> params通过httpclient 提交
	private void Save() {
		if (et_nick_name.getText().toString().equals("")) {
			Toast.makeText(CarUpdateActivity.this, R.string.car_name_null,
					Toast.LENGTH_SHORT).show();
			return;
		}

		String nick_name = et_nick_name.getText().toString();
		String gas_no = tv_gas_no.getText().toString();
		String fuel_price = et_oil_price.getText().toString().trim();
		
		//取出保险公司名字
		String insurance_company = tv_insurance_company.getText().toString();		
		String insurance_tel = et_insurance_tel.getText().toString();
		String insurance_date = tv_insurance_date.getText().toString();
		String insurance_no = et_insurance_no.getText().toString();
		
		//取出汽车 4 s 店 名字
		String maintain_company = tv_maintain_company.getText().toString();	
		String maintain_tel = et_maintain_tel.getText().toString();
		String buy_date = tv_buy_date.getText().toString();
		
		carNewData.setDevice_id(carData.getDevice_id());
		carNewData.setNick_name(nick_name);
		carNewData.setCar_brand(car_brand);
		carNewData.setCar_series(car_series);
		carNewData.setCar_type(car_type);
		carNewData.setInsurance_company(insurance_company);		
		carNewData.setInsurance_tel(insurance_tel);
		carNewData.setInsurance_date(insurance_date);
		carNewData.setInsurance_no(insurance_no);		
		carNewData.setMaintain_company(maintain_company);
		carNewData.setMaintain_tel(maintain_tel);
		carNewData.setBuy_date(buy_date);
		carNewData.setGas_no(gas_no);
		carNewData.setCar_brand_id(car_brand_id);
		carNewData.setCar_series_id(car_series_id);
		carNewData.setCar_type_id(car_type_id);
		carNewData.setFuel_price(Double.valueOf(fuel_price));

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//一下是完整参数 不可缺    vio_citys 的格式是 jsonlist格式  空的时候 是  []
		params.add(new BasicNameValuePair("obj_name", ""));
		params.add(new BasicNameValuePair("nick_name", nick_name));
		params.add(new BasicNameValuePair("car_brand", car_brand));
		params.add(new BasicNameValuePair("car_series", car_series));
		params.add(new BasicNameValuePair("car_type", car_type));
		params.add(new BasicNameValuePair("vio_citys","[]"));
		params.add(new BasicNameValuePair("engine_no",""));
		params.add(new BasicNameValuePair("frame_no", ""));
		params.add(new BasicNameValuePair("reg_no", ""));
		params.add(new BasicNameValuePair("insurance_company",
				insurance_company));
		params.add(new BasicNameValuePair("insurance_tel", insurance_tel));
		params.add(new BasicNameValuePair("insurance_date", insurance_date));
		params.add(new BasicNameValuePair("insurance_no", insurance_no));
		params.add(new BasicNameValuePair("maintain_company", maintain_company));
		params.add(new BasicNameValuePair("maintain_tel", maintain_tel));
		params.add(new BasicNameValuePair("maintain_last_mileage", "0"));
		params.add(new BasicNameValuePair("maintain_last_date", "2014-10-10"));
		params.add(new BasicNameValuePair("buy_date", buy_date));
		params.add(new BasicNameValuePair("gas_no", gas_no));
		params.add(new BasicNameValuePair("car_brand_id", car_brand_id));
		params.add(new BasicNameValuePair("car_series_id", car_series_id));
		params.add(new BasicNameValuePair("car_type_id", car_type_id));
		params.add(new BasicNameValuePair("fuel_price", fuel_price));
		
		String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id()
				+ "?auth_code=" + app.auth_code;	
		System.out.println("URL:" + url);		
		new NetThread.putDataThread(handler, url, params, update).start();		
	}
	
	private void setTime() {
		OpenDateDialog.SetCustomDateListener(new OpenDateDialogListener() {
			@Override
			public void OnDateChange(String Date, int index) {
				switch (index) {
				case inspection:
					tv_insurance_date.setText(Date);
					break;
				case buy_date:
					tv_buy_date.setText(Date);
					break;
				case year_check:
					tv_year_check.setText(Date);
					break;
				}
			}
		});
	}

	private void setData() {
		Log.d(TAG, carData.toString());
		car_brand = carData.getCar_brand();
		car_brand_id = carData.getCar_brand_id();
		car_series = carData.getCar_series();
		car_series_id = carData.getCar_series_id();
		car_type = carData.getCar_type();
		car_type_id = carData.getCar_type_id();
		et_nick_name.setText(carData.getNick_name());

		tv_models.setText(carData.getCar_series() + carData.getCar_type());
		tv_gas_no.setText(carData.getGas_no());
		et_oil_price.setText("" + carData.getFuel_price());

		// 保险公司 和 4S店
		tv_insurance_company.setText(carData.getInsurance_company());
		tv_maintain_company.setText(carData.getMaintain_company());
		et_insurance_tel.setText(carData.getInsurance_tel());
		tv_insurance_date.setText(carData.getInsurance_date());
		et_insurance_no.setText(carData.getInsurance_no());
			
		et_maintain_tel.setText(carData.getMaintain_tel());
		tv_buy_date.setText(carData.getBuy_date());
		tv_year_check.setText(carData.getAnnual_inspect_date());
	}

	private void init() {
		ImageView iv_save = (ImageView) findViewById(R.id.iv_save);
		iv_save.setOnClickListener(onClickListener);

		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
	
		et_nick_name = (EditText) findViewById(R.id.et_nick_name);
		tv_models = (TextView) findViewById(R.id.tv_models);
		tv_models.setOnClickListener(onClickListener);
		tv_gas_no = (TextView) findViewById(R.id.tv_gas_no);
		tv_gas_no.setOnClickListener(onClickListener);
		et_oil_price = (EditText) findViewById(R.id.et_oil_price);

		tv_insurance_company = (EditText) findViewById(R.id.tv_insurance_company);
		et_insurance_tel = (EditText) findViewById(R.id.et_insurance_tel);
		tv_insurance_date = (TextView) findViewById(R.id.tv_insurance_date);
		tv_insurance_date.setOnClickListener(onClickListener);
		et_insurance_no = (EditText) findViewById(R.id.et_insurance_no);
			
		tv_maintain_company = (EditText) findViewById(R.id.tv_maintain_company);
		et_maintain_tel = (EditText) findViewById(R.id.et_maintain_tel);
		tv_buy_date = (TextView) findViewById(R.id.tv_buy_date);
		tv_buy_date.setOnClickListener(onClickListener);
		
		tv_year_check = (TextView) findViewById(R.id.tv_year_check);
		tv_year_check.setOnClickListener(onClickListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {// 汽车型号
			car_brand = data.getStringExtra("brank");
			car_brand_id = data.getStringExtra("brankId");
			car_series = data.getStringExtra("series");
			car_series_id = data.getStringExtra("seriesId");
			car_type = data.getStringExtra("type");
			car_type_id = data.getStringExtra("typeId");
			tv_models.setText(car_series + car_type);
		} else if (resultCode == 3) {// 汽油标号返回
			tv_gas_no.setText(data.getStringExtra("result"));
			// 0#,90#,93#,97#
			int position = data.getIntExtra("position", 0);
			switch (position) {
			case 0:
				et_oil_price.setText(fuel0);
				break;
			case 1:
				et_oil_price.setText(fuel90);
				break;
			case 2:
				et_oil_price.setText(fuel93);
				break;
			case 3:
				et_oil_price.setText(fuel97);
				break;
			}
		} 
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
