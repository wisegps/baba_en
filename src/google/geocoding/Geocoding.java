package google.geocoding;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import android.os.Handler;
import android.os.Message;


/*	date:2015-4-17
	author:吴文德
	fun:连接google geocoding api	
	handler  LatLng   what
*/

public class Geocoding {
		
	public static class GetGoogleGeocoding extends Thread{
		Handler handler;
		LatLng latlng;
		int what;
		
		public  GetGoogleGeocoding (Handler handler,LatLng latlng,int what) {
			this.handler = handler;
			this.latlng = latlng;
			this.what =what;
		}
		
		@Override
		public void run() {
			super.run();
			try {
				// 组装反向地理编码的接口地址
				StringBuilder url = new StringBuilder();
				url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
				url.append(latlng.latitude).append(",")
						.append(latlng.longitude);
				url.append("&sensor=false");
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url.toString());
				// 在请求消息头中指定语言，保证服务器会返回英文数据			
				httpGet.addHeader("Accept-Language", "en-us");
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					String response = EntityUtils.toString(entity, "utf-8");
					
//					System.out.println("获取地理位置反解析信息：" +  response);
					
					JSONObject jsonObject = new JSONObject(response);
					// 获取results节点下的位置信息
					JSONArray resultArray = jsonObject.getJSONArray("results");
					if (resultArray.length() > 0) {
						JSONObject subObject = resultArray.getJSONObject(0);
						// 取出格式化后的位置信息
						String address = subObject.getString("formatted_address");
						Message message = new Message();
						message.what = what;
						message.obj = address;
						handler.sendMessage(message);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}		
	}
}
