package com.fanfan.bodyguard;

import android.os.Handler;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.fanfan.utils.BaiduUtils;
import com.fanfan.utils.CLog;

/**
 * 61 ： GPS定位结果
 * 
 * 62 ： 扫描整合定位依据失败。此时定位结果无效。
 * 
 * 63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
 * 
 * 65 ： 定位缓存的结果。
 * 
 * 66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果
 * 
 * 67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果
 * 
 * 68 ： 网络连接失败时，查找本地离线定位时对应的返回结果
 * 
 * 161： 表示网络定位结果
 * 
 * 162~167： 服务端定位失败
 * 
 * 502：key参数错误
 * 
 * 505：key不存在或者非法
 * 
 * 601：key服务被开发者自己禁用
 * 
 * 602：key mcode不匹配
 * 
 * 501～700：key验证失败
 * 
 * @author FANFAN
 * 
 */
public class MyLocationListener implements BDLocationListener {
	private Handler mHandler;

	public MyLocationListener() {
		mHandler = new Handler();
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		StringBuffer sb = new StringBuffer();
		sb.append("time : ");
		sb.append(location.getTime());
		sb.append("\nerror code : ");
		sb.append(location.getLocType());
		sb.append("\nlatitude : ");
		sb.append(location.getLatitude());
		sb.append("\nlontitude : ");
		sb.append(location.getLongitude());
		sb.append("\nradius : ");
		sb.append("\n省份：" + location.getProvince());
		sb.append("\n城市：" + location.getCity());
		sb.append("\n区县：" + location.getDistrict());
		sb.append("\nstreet:" + location.getStreet());
		sb.append("\nStreetNumber:" + location.getStreetNumber());
		sb.append("\n定位精度半径：" + location.getRadius());
		sb.append("\n:LocType：" + location.getLocType());
		if (location.getLocType() == BDLocation.TypeGpsLocation) {
			sb.append("\nspeed : ");
			sb.append(location.getSpeed());
			sb.append("\nsatellite : ");
			sb.append(location.getSatelliteNumber());
			sb.append("\ndirection : ");
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
			sb.append("\n获得手机方向,手机上部正朝向北的方向为0°方向" + location.getDirection());
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
			// 运营商信息
			sb.append("\noperationers : ");
			sb.append(location.getOperators());
		}
		if (!TextUtils.isEmpty(location.getAddrStr())) {
			MyApp.getInstance().setCurrentAddress(location.getAddrStr());
			// 三秒后停止定位
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					BaiduUtils utils = new BaiduUtils();
					utils.stop();
				}
			}, 3000);
		}
		 CLog.i("info", sb.toString());
	}

}
