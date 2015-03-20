package com.fanfan.utils;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.fanfan.bodyguard.MyApp;

public class BaiduUtils {
	private LocationClient mLocationClient;
	// private LocationMode typeMode = LocationMode.Hight_Accuracy;// 高精度
	/**
	 * gcj02:代表国测局加密经纬度坐标,默认<br>
	 * bd09ll:百度加密经纬度坐标<br>
	 * bd09:百度加密墨卡托坐标
	 */
	public static final String TEMPCOOR_GCJ = "gcj02";
	public static final String TEMPCOOR_BD09 = "bd09ll";
	public static final String TEMPCOOR_BD = "bd09";
	private String tempcoor = "gcj02";// 返回的定位结果是百度经纬度，默认值gcj02
	private LocationClientOption option;

	public BaiduUtils() {
		mLocationClient = MyApp.getInstance().mLocationClient;
		option = new LocationClientOption();
	}

	/**
	 * 初始化百度地图选项
	 * 
	 * @param mode
	 * @param temcpoor
	 * @param span
	 */
	public void initLocation(LocationMode tempMode, String temcpoor, int span) {
		option.setLocationMode(tempMode);// 设置定位模式
		option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
		option.setScanSpan(span);// 设置发起定位请求的间隔时间
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
	}

	public void isopenGps(boolean isOpenGps) {
		option.setOpenGps(isOpenGps);
	}

	public void start() {
		mLocationClient.start();

	}

	/**
	 * 0：正常发起了定位。
	 * 
	 * 1：服务没有启动。
	 * 
	 * 2：没有监听函数。
	 * 
	 * 6：请求间隔过短。 前后两次请求定位时间间隔不能小于1000ms。
	 * 
	 * -1:locationClient is null or locationClient has not start;
	 * 
	 * @return
	 */
	public int requestCode() {
		if (mLocationClient != null && mLocationClient.isStarted()) {
			return mLocationClient.requestLocation();
		}
		return -1;
	}

	public void stop() {
		mLocationClient.stop();
	}
}
