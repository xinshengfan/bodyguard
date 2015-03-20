package com.fanfan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fanfan.bodyguard.MyApp;
import com.fanfan.bodyguard.R;

public class NetUtils {
	private static String url;
	private static String token;
	// 读取配置文件
	static {
		XmlPullParser parser = MyApp.getInstance().getResources()
				.getXml(R.xml.config);
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = parser.getName();
				if ("url".equals(name)) {
					url = parser.nextText().trim();
				}
				if ("token".equals(name)) {
					token = parser.nextText().trim();
				}
				eventType = parser.next();
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static UpdataInfo getUpdataInfo() throws Exception {
		UpdataInfo info = null;
		String jsonResult = get(url + token);
		if (jsonResult != null) {
			JSONObject jsonObject = new JSONObject(jsonResult);
			// String code = jsonObject.getString("resultcode");
			// if ("200".equals(code)) {
			info = new UpdataInfo();
			info.version = jsonObject.getInt("version");
			info.changelog = jsonObject.getString("changelog");
			info.installUrl = jsonObject.getString("installUrl");
			// }
		}
		return info;
	}

	/**
	 * 手机当前是否正确连接到网络
	 * 
	 * @return　正确连接到网络返回true,否则返回false
	 */
	public static boolean isConnectNet() {
		boolean isConnectNet = false;
		ConnectivityManager cmManager = (ConnectivityManager) MyApp
				.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cmManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			isConnectNet = true;
		}
		return isConnectNet;
	}

	/**
	 * 请求资源
	 * 
	 * @param urlAll
	 *            　请求路径
	 * @return　将返回结果解析成一个字符串；
	 */
	private static String get(String urlAll) {
		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";// 模拟浏览器
		try {
			URL url = new URL(urlAll);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(30000);
			connection.setConnectTimeout(30000);
			connection.setRequestProperty("User-agent", userAgent);
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			result = sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
