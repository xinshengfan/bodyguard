package com.fanfan.utils;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.fanfan.bodyguard.MyApp;

public class BaiduUtils {
	private LocationClient mLocationClient;
	// private LocationMode typeMode = LocationMode.Hight_Accuracy;// �߾���
	/**
	 * gcj02:�������ּ��ܾ�γ������,Ĭ��<br>
	 * bd09ll:�ٶȼ��ܾ�γ������<br>
	 * bd09:�ٶȼ���ī��������
	 */
	public static final String TEMPCOOR_GCJ = "gcj02";
	public static final String TEMPCOOR_BD09 = "bd09ll";
	public static final String TEMPCOOR_BD = "bd09";
	private String tempcoor = "gcj02";// ���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
	private LocationClientOption option;

	public BaiduUtils() {
		mLocationClient = MyApp.getInstance().mLocationClient;
		option = new LocationClientOption();
	}

	/**
	 * ��ʼ���ٶȵ�ͼѡ��
	 * 
	 * @param mode
	 * @param temcpoor
	 * @param span
	 */
	public void initLocation(LocationMode tempMode, String temcpoor, int span) {
		option.setLocationMode(tempMode);// ���ö�λģʽ
		option.setCoorType(tempcoor);// ���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
		option.setScanSpan(span);// ���÷���λ����ļ��ʱ��
		option.setIsNeedAddress(true);// ���صĶ�λ���������ַ��Ϣ
		option.setNeedDeviceDirect(true);// ���صĶ�λ��������ֻ���ͷ�ķ���
		mLocationClient.setLocOption(option);
	}

	public void isopenGps(boolean isOpenGps) {
		option.setOpenGps(isOpenGps);
	}

	public void start() {
		mLocationClient.start();

	}

	/**
	 * 0�����������˶�λ��
	 * 
	 * 1������û��������
	 * 
	 * 2��û�м���������
	 * 
	 * 6�����������̡� ǰ����������λʱ��������С��1000ms��
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
