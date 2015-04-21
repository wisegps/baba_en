package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.location.LocationListener;

import data.CarData;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class CarLocationActivity extends Activity implements ConnectionCallbacks,
	OnConnectionFailedListener,LocationListener{
	
	private GoogleMap mMap;
	LinearLayout ll_location_bottom;
	PopupWindow mPopupWindow;
	CarData carData;
	boolean isHotLocation;
	int index;
	double latitude, longitude;// 当前位置
	/** 获取gps信息 **/
	private static final int get_gps = 1;
	private static final int set_vibrate = 2;
	AppApplication app;
	ImageView iv_traffic, iv_tracking;
	/** 跟踪 **/
	boolean isTracking = false;
	/** 实时路口 **/
	boolean isTraffic = false;
	

	
	private GoogleApiClient mGoogleApiClient;
	
	private static final LocationRequest REQUEST = LocationRequest.create()
	            .setInterval(5000)         // 5 seconds
	            .setFastestInterval(16)    // 16ms = 60fps
	            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	public void onCreate(Bundle savedInstanceState){

	   	super.onCreate(savedInstanceState);
	   	requestWindowFeature(Window.FEATURE_NO_TITLE);
	   	setContentView(R.layout.activity_car_location);
	   	
		app = (AppApplication) getApplication();

		//定位
		 mGoogleApiClient = new GoogleApiClient.Builder(this)
         .addApi(LocationServices.API)
         .addConnectionCallbacks(this)
         .addOnConnectionFailedListener(this)
         .build();

		// 就初始化控件
		ll_location_bottom = (LinearLayout) findViewById(R.id.ll_location_bottom);
		ImageView iv_more = (ImageView) findViewById(R.id.iv_more);
		iv_more.setOnClickListener(onClickListener);

		iv_tracking = (ImageView) findViewById(R.id.iv_tracking);
		iv_tracking.setOnClickListener(onClickListener);
		iv_traffic = (ImageView) findViewById(R.id.iv_traffic);
		iv_traffic.setOnClickListener(onClickListener);
		TextView tv_car_name = (TextView) findViewById(R.id.tv_car_name);

		index = getIntent().getIntExtra("index", 0);
		isHotLocation = getIntent().getBooleanExtra("isHotLocation", false);
		if (app.carDatas != null && app.carDatas.size() > 0) {
			carData = app.carDatas.get(index);
			tv_car_name.setText(carData.getNick_name());
		}
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
	   	initMapView();//初始化地图
		if (isHotLocation) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isStop) {
						// 获取gps信息
						try {
							Thread.sleep(10000);
							String gpsUrl = Constant.BaseUrl + "device/"
									+ carData.getDevice_id()
									+ "/active_gps_data?auth_code="
									+ app.auth_code
									+ "&update_time=2014-01-01%2019:06:43";
							new NetThread.GetDataThread(handler, gpsUrl,
									get_gps).start();
						}catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_gps:
				jsonGps(msg.obj.toString());
				break;
			case set_vibrate:
				jsonVibrate(msg.obj.toString());
				break;
			}
		}
	};	


	private void initMapView(){	  

		if(mMap==null){
			mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();	  
			UiSettings setting = mMap.getUiSettings();
			setting.setTiltGesturesEnabled(true);
			setting.setCompassEnabled(true);
			setting.setZoomControlsEnabled(true);
			setting.setIndoorLevelPickerEnabled(true);
		    
		    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);	
			mMap.setBuildingsEnabled(true);
			mMap.setIndoorEnabled(true);
			mMap.setTrafficEnabled(true);
			mMap.isTrafficEnabled();

			getCarLocation();
			
			System.out.println("google Maps start ok");
		}
		
	}

//** 获取GPS信息 **//*
private void jsonGps(String str) {
	if (!isStop) {
		return;
	}

	LatLng startTracking = new LatLng(carData.getLat(), carData.getLon());
	
	System.out.println("汽车的位置" + "纬度:" + carData.getLat() + "   " + "经度:" + carData.getLon());

	try {
		JSONObject jsonObject = new JSONObject(str)
				.getJSONObject("active_gps_data");
		double lat = jsonObject.getDouble("lat");
		double lon = jsonObject.getDouble("lon");

		if (isTracking) {
			LatLng endTracking = new LatLng(lat, lon);
			trackingCar(startTracking, endTracking);
		}
		getCarLocation();
		carData.setLat(lat);
		carData.setLon(lon);
	} catch (JSONException e) {
		e.printStackTrace();
	}
}




private void trackingCar(LatLng lng1, LatLng lng2) {
//	List<LatLng> points = new ArrayList<LatLng>();
//	points.add(lng1);
//	points.add(lng2);
//	OverlayOptions ooPolyline = new PolylineOptions().color(0xFF0000C6)
//			.points(points);
//	mBaiduMap.addOverlay(ooPolyline);
	
//	carMarker.remove();
	mMap.addPolyline((new PolylineOptions())
            .add(lng1,lng2)
            .width(3)
            .color(Color.BLUE)
            .geodesic(true));
//	getCarMarker(lng2);


}

	LatLng circle;
	Marker carMarker = null;
	boolean isFristCarLocation = true;
	

	
	/*显示当前车辆位子*/
	private void getCarLocation() {
		try {
			if (!isHotLocation) {
				return;
			}
			circle = new LatLng(carData.getLat(), carData.getLon());
			if (carMarker != null) {
				carMarker.remove();
			}
			getCarMarker(circle);//显示CAR图标
			
			
			if (isFristCarLocation) {
				isFristCarLocation = false;
				// 第一次将镜头移动到车的位置到地图中间
			    CameraPosition carLocation = new CameraPosition.Builder().target(circle).zoom(13.5f).bearing(0).tilt(0).build();
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(carLocation));	
			} else {
				if (isTracking) {
					//跟踪的时候将镜头移动车的位置到地图中间
				    CameraPosition shenzhen = new CameraPosition.Builder().target(circle).zoom(13.5f).bearing(0).tilt(0).build();
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(shenzhen));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getCarMarker(LatLng latLng){
		
		BitmapDescriptor bitmap = BitmapDescriptorFactory// 构建Marker图标
				.fromResource(R.drawable.body_icon_location2);
		// 构建MarkerOption，用于在地图上添加Marker
		MarkerOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
				.position(latLng).icon(bitmap);	
		carMarker = (Marker) (mMap.addMarker(option));// 在地图上添加Marker，并显示
		
	}

	/** 页面destory时改为false **/
	boolean isStop = true;
	private static final int SEARCH_CODE = 8;

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_more:
				showMorePop();//popupwindow菜单
				break;
			case R.id.tv_vibrate:
				if (mPopupWindow != null) {
					mPopupWindow.dismiss();
				}
				showVibratePop();
				break;
			case R.id.bt_set_vibrate:
				if (app.carDatas != null && app.carDatas.size() > 0) {
					String rcv_time = app.carDatas.get(index).getRcv_time();
					if ((GetSystem.spacingNowTime(rcv_time) / 60) > 10
							|| rcv_time == null) {
						// 弹出提示框
						AlertDialog.Builder dialog = new AlertDialog.Builder(
						CarLocationActivity.this);
						dialog.setTitle(R.string.note);
						dialog.setMessage(R.string.car_offline);
						dialog.setPositiveButton(R.string.set_up,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										setVibrate();
									}
								}).setNegativeButton(R.string.cancel, null)
								.show();

					} else {
						setVibrate();
					}
				} else {
					Toast.makeText(CarLocationActivity.this, R.string.add_car,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.iv_traffic:
				if (isTraffic) {
					isTraffic = false;	
					iv_traffic
							.setImageResource(R.drawable.main_icon_roadcondition_off);
					Toast.makeText(CarLocationActivity.this,
							R.string.road_close, Toast.LENGTH_SHORT).show();
				} else {
					isTraffic = true;
					iv_traffic
							.setImageResource(R.drawable.main_icon_roadcondition_on);
					Toast.makeText(CarLocationActivity.this,
							R.string.road_open, Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.iv_tracking:
				if (isHotLocation) {
					// 追踪
					isTracking = !isTracking;
					if (isTracking) {
						iv_tracking.setImageResource(R.drawable.car_track_no);
						Toast.makeText(CarLocationActivity.this,
								"track the vehicle", Toast.LENGTH_SHORT).show();
					} else {
						iv_tracking.setImageResource(R.drawable.car_track);
						Toast.makeText(CarLocationActivity.this,
								"cancel the tracking", Toast.LENGTH_SHORT)
								.show();
//						isFristCarLocation = true;//移动车的位置到地图中间
					}
					if (!isTracking) {
						mMap.clear();
						getCarLocation();
						drawPhoneLocation(latitude, longitude);
					}
				} else {
					showHotDialog();
				}
				break;
			}
		}
	};		
	


		/** 显示更多菜单 **/
		private void showMorePop() {
			LayoutInflater mLayoutInflater = LayoutInflater
					.from(CarLocationActivity.this);
			View popunwindwow = mLayoutInflater.inflate(R.layout.pop_location_more,
					null);
			TextView tv_vibrate = (TextView) popunwindwow
					.findViewById(R.id.tv_vibrate);
			tv_vibrate.setOnClickListener(onClickListener);
			mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.showAsDropDown(findViewById(R.id.iv_more), 0, 0);
		}

		ProgressBar pb_vibrate;

		/** 显示设置震动窗口 **/
		private void showVibratePop() {
			int Height = ll_location_bottom.getMeasuredHeight();
			LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View popunwindwow = mLayoutInflater.inflate(R.layout.pop_vibrate, null);
			mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.showAtLocation(findViewById(R.id.ll_location_bottom),
					Gravity.BOTTOM, 0, Height);

			Button bt_set_vibrate = (Button) popunwindwow
					.findViewById(R.id.bt_set_vibrate);

			pb_vibrate = (ProgressBar) popunwindwow.findViewById(R.id.pb_vibrate);

			bt_set_vibrate.setOnClickListener(onClickListener);

			final TextView tv_vibrate = (TextView) popunwindwow
					.findViewById(R.id.tv_vibrate);
			// 刷新
			SeekBar sb_vibrate = (SeekBar) popunwindwow
					.findViewById(R.id.sb_vibrate);
			vibrate = carData.getSensitivity(); // null,默认赋值为0（已改）
			sb_vibrate.setProgress(carData.getSensitivity());
			if (carData.getSensitivity() == 0) {
				tv_vibrate.setText(getResources().getString(R.string.closed));
			} else {
				tv_vibrate.setText("" + carData.getSensitivity());
			}
			sb_vibrate.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					if (progress == 0) {
						tv_vibrate.setText(getResources()
								.getString(R.string.closed));
						vibrate = 0;
					} else {
						tv_vibrate.setText("" + progress);
						vibrate = progress;
					}
				}
			});
		}

		private String COMMAND_VIBRATEALERT = "16391";
		int vibrate = 0;

		/** 设置震动 **/
		private void setVibrate() {
			pb_vibrate.setVisibility(View.VISIBLE);

			String url = Constant.BaseUrl + "command?auth_code=" + app.auth_code;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("device_id", carData.getDevice_id()));
			params.add(new BasicNameValuePair("cmd_type", COMMAND_VIBRATEALERT));
			params.add(new BasicNameValuePair("params", "{sensitivity: " + vibrate
					+ "}"));
			Log.e("my_log", "vibrate :" + vibrate);
			new NetThread.postDataThread(handler, url, params, set_vibrate).start();
		}

		private void jsonVibrate(String result) {
			pb_vibrate.setVisibility(View.GONE);
			try {
				JSONObject jsonObject = new JSONObject(result);
				if (jsonObject.getInt("status_code") == 0) {
					carData.setSensitivity(vibrate);
					Toast.makeText(getApplicationContext(),
							R.string.sensitivity_set_success, Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.sensitivity_set_faild, Toast.LENGTH_SHORT)
							.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						R.string.sensitivity_set_faild, Toast.LENGTH_SHORT).show();
			}
		}
		
		
	private void showHotDialog(){
		// 弹出提示框
		AlertDialog.Builder dialog = new AlertDialog.Builder(
				CarLocationActivity.this);
		dialog.setTitle(R.string.note);
		if (app.carDatas == null || app.carDatas.size() == 0) {
			dialog.setMessage(R.string.terminal_use);
		} else {
			dialog.setMessage(R.string.bind_terminal);
		}
		dialog.setPositiveButton(R.string.determine,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(CarLocationActivity.this,
								CarActivity.class));
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}


	
	Marker phoneMark;

	/** 在地图上标记当前设备位置 **/
	private void drawPhoneLocation(double latitude, double longitude) {
		// 如果有当前位置，则先删除
		if (phoneMark != null) {
			phoneMark.remove();
		}
		LatLng phoneLatLng = new LatLng(latitude, longitude);
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.person);
		MarkerOptions option = new MarkerOptions().anchor(0.5f, 1.0f)
				.position(phoneLatLng).icon(bitmap);
		phoneMark = (Marker) (mMap.addMarker(option));
	}

	@Override
	protected void onResume() {
		super.onResume();
		initMapView();	
		mGoogleApiClient.connect();
		System.out.println("google Maps onResume");
	}
	
	  @Override
	protected void onPause() {
		super.onPause();
		mGoogleApiClient.disconnect();
		mMap.stopAnimation();	
		System.out.println("google Maps onPause");
	}
	  
	  @Override
		protected void onDestroy() {
			super.onDestroy();
			mMap.clear();			
			mMap=null;
			isStop = false;
			isTracking = false;
		}
		

	@Override
	public void onLocationChanged(Location location) {
		if (location == null || mMap == null)// map view 销毁后不在处理新接收的位置
			return;
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		app.Lat = latitude;
		app.Lon = longitude;
		drawPhoneLocation(latitude, longitude);
		
		/*System.out.println("时间:" + location.getTime());
		System.out.println("海拔："+location.getAltitude());*/
		System.out.println("我的位置" +"纬度:" + latitude + "   " + "经度:" + longitude);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		 LocationServices.FusedLocationApi.requestLocationUpdates(
	                mGoogleApiClient,
	                REQUEST,
	                this);  // LocationListener
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub	
	}

}
