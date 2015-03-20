package com.fanfan.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.pinyin4j.PinyinHelper;

import com.fanfan.data.MyContacts;

public class Sort {

	/**
	 * ��Ӣ�Ļ������
	 * 
	 * @param strArr
	 *            ԭʼ��ͨѶ¼����
	 * @return ������ͨѶ¼����
	 */
	public static String[] autoSort(String[] strArr) {
		String temp = "";

		String headchar1;

		String headchar2;

		for (int i = 0; i < strArr.length; i++) {
			for (int j = i; j < strArr.length; j++) {
				headchar1 = getPinYinHeadChar(strArr[i]).toUpperCase(
						Locale.CHINA);
				headchar2 = getPinYinHeadChar(strArr[j]).toUpperCase(
						Locale.CHINA);
				if (headchar1.charAt(0) > headchar2.charAt(0)) {
					temp = strArr[i];
					strArr[i] = strArr[j];
					strArr[j] = temp;
				}
			}
		}
		return strArr;
	}

	/**
	 * ��Ӣ�Ļ������
	 * 
	 * @param strArr
	 *            ԭʼ��ͨѶ¼����
	 * @return ������ͨѶ¼����
	 */
	// public static void autoSort(List<String> strArr) {
	//
	// String temp = "";
	//
	// String headchar1;
	//
	// String headchar2;
	//
	// int length = strArr.size();
	//
	// for (int i = 0; i < length; i++) {
	// for (int j = i; j < length; j++) {
	// headchar1 = getPinYinHeadChar(strArr.get(i)).toUpperCase();
	// headchar2 = getPinYinHeadChar(strArr.get(j)).toUpperCase();
	// if (headchar1.compareTo(headchar2) > 0) {
	// temp = strArr.get(i);
	// strArr.set(i, strArr.get(j));
	// strArr.set(j, temp);
	// }
	// }
	// }
	// }

	/**
	 * ��Ӣ�Ļ������
	 * 
	 * @param strArr
	 *            ԭʼ��ͨѶ¼����
	 * @return ������ͨѶ¼����
	 */
	public static void autoSort(List<MyContacts> contacts) {

		MyContacts temp = null;

		String headchar1;

		String headchar2;

		int length = contacts.size();

		for (int i = 0; i < length; i++) {
			for (int j = i; j < length; j++) {
				headchar1 = getAllPinYinHeadChar(contacts.get(i).getName())
						.toUpperCase(Locale.CHINA);
				headchar2 = getAllPinYinHeadChar(contacts.get(j).getName())
						.toUpperCase(Locale.CHINA);
				if (headchar1.compareTo(headchar2) > 0) {
					temp = contacts.get(i);
					contacts.set(i, contacts.get(j));
					contacts.set(j, temp);
				}
			}
		}
	}

	/**
	 * �õ���ǰ��ϵ�����Ƶĵ�һ�����ֵ�����ĸ
	 * 
	 * @param str
	 *            ��ϵ������
	 * @return ÿ�����ֵ�����ĸ
	 */
	public static String getPinYinHeadChar(String str) {
		String convert = "";
		if (str.matches("\\d{11}")) {
			return "#";
		}
		char word = str.charAt(0);
		String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
		if (pinyinArray != null)
			convert = String.valueOf(pinyinArray[0].charAt(0)).toUpperCase(
					Locale.CHINA);
		else {
			convert = String.valueOf(word).toUpperCase(Locale.CHINA);
		}
		return convert;
	}

	/**
	 * ʵ�����ݷ���
	 * 
	 * @param strArr
	 *            ������ͨѶ¼����
	 * @return ����������˷�����ĸ��ArrayList��������������ĸΪB��������֮ǰ���һ��Bʵ�ַ��飩
	 */
	public static ArrayList<String> addChar(String[] strArr) {
		String headchar = "";
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < strArr.length; i++) {
			headchar = String.valueOf(getPinYinHeadChar(strArr[i]).charAt(0))
					.toUpperCase(Locale.CHINA);
			if (!list.contains(headchar)) {
				list.add(headchar);
				list.add(strArr[i]);
			} else {
				list.add(strArr[i]);
			}
		}
		return list;
	}

	/**
	 * ʵ�����ݷ���
	 * 
	 * @param strArr
	 *            ������ͨѶ¼����
	 * @return ����������˷�����ĸ��ArrayList��������������ĸΪB��������֮ǰ���һ��Bʵ�ַ��飩
	 */
	public static ArrayList<String> addChar(List<String> strArr) {
		String headchar = "";
		ArrayList<String> list = new ArrayList<String>();
		int length = strArr.size();
		for (int i = 0; i < length; i++) {
			headchar = String.valueOf(getPinYinHeadChar(strArr.get(i)))
					.substring(0, 1).toUpperCase(Locale.CHINA);
			CLog.i("info", "headchar:" + headchar);
			if (!list.contains(headchar)) {
				CLog.i("info", "��ӵ�header:" + headchar);
				list.add(headchar);
				list.add(strArr.get(i));
			} else {
				list.add(strArr.get(i));
			}
			CLog.i("info", "for list size:" + list.size());
		}
		return list;
	}

	public ArrayList<String> toArrayList(String[] str) {
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 0; i < str.length; i++)
			arrayList.add(str[i]);
		return arrayList;

	}

	/**
	 * 
	 * @param str��ϵ������
	 * @return ��ϵ��������ÿ�����ֵ�����ĸ
	 */
	public static String getAllPinYinHeadChar(String str) {
		String convert = "";
		if (str.matches("\\d{11}")) {
			return "#";
		}
		for (int j = 0; j < str.length(); j++) {
			char word = str.charAt(j);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
			if (pinyinArray != null) {
				// for (int i = 0; i < pinyinArray.length; i++) {
				convert += pinyinArray[0].charAt(0);
				// }
			} else {
				convert += word;
			}
		}
		return convert;
	}
}
