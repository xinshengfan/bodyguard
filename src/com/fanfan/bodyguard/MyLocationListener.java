package com.fanfan.bodyguard;

import android.os.Handler;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.fanfan.utils.BaiduUtils;
import com.fanfan.utils.CLog;

/**
 * 61 �� GPS��λ���
 * 
 * 62 �� ɨ�����϶�λ����ʧ�ܡ���ʱ��λ�����Ч��
 * 
 * 63 �� �����쳣��û�гɹ���������������󡣴�ʱ��λ�����Ч��
 * 
 * 65 �� ��λ����Ľ����
 * 
 * 66 �� ���߶�λ�����ͨ��requestOfflineLocaiton����ʱ��Ӧ�ķ��ؽ��
 * 
 * 67 �� ���߶�λʧ�ܡ�ͨ��requestOfflineLocaiton����ʱ��Ӧ�ķ��ؽ��
 * 
 * 68 �� ��������ʧ��ʱ�����ұ������߶�λʱ��Ӧ�ķ��ؽ��
 * 
 * 161�� ��ʾ���綨λ���
 * 
 * 162~167�� ����˶�λʧ��
 * 
 * 502��key��������
 * 
 * 505��key�����ڻ��߷Ƿ�
 * 
 * 601��key���񱻿������Լ�����
 * 
 * 602��key mcode��ƥ��
 * 
 * 501��700��key��֤ʧ��
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
		sb.append("\nʡ�ݣ�" + location.getProvince());
		sb.append("\n���У�" + location.getCity());
		sb.append("\n���أ�" + location.getDistrict());
		sb.append("\nstreet:" + location.getStreet());
		sb.append("\nStreetNumber:" + location.getStreetNumber());
		sb.append("\n��λ���Ȱ뾶��" + location.getRadius());
		sb.append("\n:LocType��" + location.getLocType());
		if (location.getLocType() == BDLocation.TypeGpsLocation) {
			sb.append("\nspeed : ");
			sb.append(location.getSpeed());
			sb.append("\nsatellite : ");
			sb.append(location.getSatelliteNumber());
			sb.append("\ndirection : ");
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
			sb.append("\n����ֻ�����,�ֻ��ϲ������򱱵ķ���Ϊ0�㷽��" + location.getDirection());
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
			// ��Ӫ����Ϣ
			sb.append("\noperationers : ");
			sb.append(location.getOperators());
		}
		if (!TextUtils.isEmpty(location.getAddrStr())) {
			MyApp.getInstance().setCurrentAddress(location.getAddrStr());
			// �����ֹͣ��λ
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
