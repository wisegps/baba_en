package com.wise.show;

import java.util.ArrayList;
import java.util.List;
import pubclas.GetSystem;
import com.wise.baba.R;
import com.wise.car.ModelsActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PinDaoActivity extends Activity {
	private static final String TAG = "PinDaoActivity";
	List<PinDaoData> pinDaoDatas = new ArrayList<PinDaoData>();
	PinDaoAdapter pinDaoAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_pindao);
		setData();
		ListView lv_pindao = (ListView) findViewById(R.id.lv_pindao);
		pinDaoAdapter = new PinDaoAdapter();
		lv_pindao.setAdapter(pinDaoAdapter);
		lv_pindao.setOnItemClickListener(onItemClickListener);
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0:
				// 车型 isNeedModel
				Intent intent0 = new Intent(PinDaoActivity.this,
						ModelsActivity.class);
				intent0.putExtra("isNeedModel", false);
				startActivityForResult(intent0, 1);
				break;

			case 1:
				// 性别
				setSex();
				break;

			case 2:
				// 城市
				// Intent intent = new Intent(PinDaoActivity.this,
				// SelectCityActivity.class);
				// intent.putExtra("isShow", true);
				// startActivityForResult(intent, 3);
				break;
			}
		}
	};
	String[] Sexs = { getResources().getString(R.string.man),
			getResources().getString(R.string.woman) };

	private void setSex() {
		new AlertDialog.Builder(PinDaoActivity.this).setTitle(R.string.sex)
				.setItems(Sexs, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							pinDaoDatas.get(1).setValue(
									getResources().getString(R.string.man));
							break;
						case 1:
							pinDaoDatas.get(1).setValue(
									getResources().getString(R.string.woman));
							break;
						}
						pinDaoAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == 3) {
			String carBrank = data.getStringExtra("brank");
			String carBrankId = data.getStringExtra("brankId");
			GetSystem.myLog(TAG, carBrank + " : " + carBrankId);
			pinDaoDatas.get(0).setValue(carBrank);
		} else if (requestCode == 3 && resultCode == 2) {
			String city = data.getStringExtra("city");
			GetSystem.myLog(TAG, "city : " + city);
			pinDaoDatas.get(2).setValue(city);
		}
		pinDaoAdapter.notifyDataSetChanged();
	}

	class PinDaoAdapter extends BaseAdapter {
		LayoutInflater layoutInflater = LayoutInflater
				.from(PinDaoActivity.this);

		@Override
		public int getCount() {
			return pinDaoDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return pinDaoDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = layoutInflater
						.inflate(R.layout.item_pindao, null);
				viewHolder = new ViewHolder();
				viewHolder.tv_key = (TextView) convertView
						.findViewById(R.id.tv_key);
				viewHolder.tv_value = (TextView) convertView
						.findViewById(R.id.tv_value);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.tv_key.setText(pinDaoDatas.get(position).getKey());
			viewHolder.tv_value.setText(pinDaoDatas.get(position).getValue());
			return convertView;
		}

		private class ViewHolder {
			TextView tv_key, tv_value;
		}
	}

	private void setData() {
		PinDaoData pinDaoData2 = new PinDaoData();
		pinDaoData2.setKey(getResources().getString(R.string.car_model));
		pinDaoData2.setValue("");
		pinDaoDatas.add(pinDaoData2);
		PinDaoData pinDaoData1 = new PinDaoData();
		pinDaoData1.setKey(getResources().getString(R.string.car_model));
		pinDaoData1.setValue("");
		pinDaoDatas.add(pinDaoData1);
		PinDaoData pinDaoData = new PinDaoData();
		pinDaoData.setKey(getResources().getString(R.string.county));
		pinDaoData.setValue("");
		pinDaoDatas.add(pinDaoData);
	}

	class PinDaoData {
		String key;
		String value;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}