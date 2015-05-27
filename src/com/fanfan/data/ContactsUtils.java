package com.fanfan.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.fanfan.utils.SharePreferUtils;

/**
 * 提供联系人信息
 * 
 * @author FANFAN
 * 
 */
public class ContactsUtils {

	private ContentResolver resolver;
	private SharePreferUtils mSharePreferUtils;
	/** 用来保存选中的电话 ****/
	private Set<String> save_phone_set;
	public static final String KEY_SAVED_PHONE = "keysavedPhoneNumber";

	public ContactsUtils(Context context) {
		resolver = context.getContentResolver();
		mSharePreferUtils = new SharePreferUtils(context);
		this.save_phone_set = new HashSet<String>();
	}

	public int getContactsCount() {
		int count = 0;
		Uri uri = RawContacts.CONTENT_URI;
		String[] projection = { Contacts._ID };
		Cursor phoneCursor = resolver.query(uri, projection, null, null,
				Contacts.SORT_KEY_ALTERNATIVE);
		if (phoneCursor != null) {
			count = phoneCursor.getCount();
			phoneCursor.close();
		}
		return count;
	}

	public ArrayList<MyContacts> getAllContacts() {
		ArrayList<MyContacts> arrayList = null;
		String[] PHONES_PROJECTION = new String[] { Phone.DISPLAY_NAME,
				Phone.NUMBER, Phone.SORT_KEY_PRIMARY, Phone.CONTACT_ID };

		/** 联系人显示名称 **/
		int PHONES_DISPLAY_NAME_INDEX = 0;
		/** 电话号码 **/
		int PHONES_NUMBER_INDEX = 1;
		/** 联系人的ID **/
		int PHONES_CONTACT_ID_INDEX = 3;
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, Phone.SORT_KEY_PRIMARY);

		if (phoneCursor != null) {
			arrayList = new ArrayList<MyContacts>();
			while (phoneCursor.moveToNext()) {
				MyContacts contacts = new MyContacts();

				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				// 得到联系人名称
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);
				// 得到联系人ID
				Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);
				String firstname = String
						.valueOf(
								phoneCursor
										.getString(
												phoneCursor
														.getColumnIndex(Phone.SORT_KEY_PRIMARY))
										.charAt(0)).toUpperCase();
				if (contactName.equals(phoneNumber)) {
					firstname = "#";
				}
				contacts.setFristName(firstname);
				contacts.setName(contactName);
				contacts.setNumber(phoneNumber);
				contacts.setId(contactid);
				// 保存是否为选中状态
				if (mSharePreferUtils.getBooleanPrefer(phoneNumber)) {
					contacts.setSelect(true);
					save_phone_set.add(phoneNumber);
				} else {
					contacts.setSelect(false);
				}
				if (!arrayList.contains(contacts)) {
					arrayList.add(contacts);
				}
			}
		}
		phoneCursor.close();
		// 保存选中的号码到本地
		mSharePreferUtils.setStringSetPrefer(KEY_SAVED_PHONE, save_phone_set);
		return arrayList;
	}
}
// public ArrayList<MyContacts> getContactsByLimits(int page, int pagesize) {
// ArrayList<MyContacts> arrayList = null;
// int position = page * pagesize;
// Uri uri = RawContacts.CONTENT_URI;
// String[] projection = { Contacts._ID, Contacts.DISPLAY_NAME,
// Contacts.SORT_KEY_PRIMARY };
// Cursor phoneCursor = resolver.query(uri, projection, null, null,
// Contacts.SORT_KEY_PRIMARY);
// if (phoneCursor != null && position > phoneCursor.getCount()) {
// return null;
// }
// if (phoneCursor != null && phoneCursor.moveToPosition(position)) {
// arrayList = new ArrayList<MyContacts>();
// while (phoneCursor.moveToNext()) {
// MyContacts contacts = new MyContacts();
// String id = phoneCursor.getString(phoneCursor
// .getColumnIndex(Contacts._ID));
// String firstname = String
// .valueOf(
// phoneCursor
// .getString(
// phoneCursor
// .getColumnIndex(Contacts.SORT_KEY_PRIMARY))
// .charAt(0)).toUpperCase();
// if (firstname.matches("\\d+")) {
// firstname = "#";
// }
// contacts.setFristName(firstname);
// String name = phoneCursor.getString(phoneCursor
// .getColumnIndex(Contacts.DISPLAY_NAME));
// // CLog.i("info", "加载的联系人：" + name);
// sb.append(name + ",");
// if (!TextUtils.isEmpty(name) && name.matches("\\d+")) {
// // // 没有存储姓名时，默认电话就是名字
// String phone_number = name;
// contacts.setName(name);
// contacts.setNumber(phone_number);
// contacts.setId(Integer.parseInt(id));
// // 保存是否为选中状态
// if (mSharePreferUtils.getBooleanPrefer(phone_number)) {
// contacts.setSelect(true);
// save_phone_set.add(phone_number);
// } else {
// contacts.setSelect(false);
// }
// c++;
// arrayList.add(contacts);
// } else {
// // // 得到手机号码
// Map<Integer, String> phoneNumber_map = getContactPhoneNumber(id);
//
// if (phoneNumber_map != null && phoneNumber_map.size() > 0) {
// Iterator<Entry<Integer, String>> iterator = phoneNumber_map
// .entrySet().iterator();
// while (iterator.hasNext()) {
// Map.Entry<java.lang.Integer, java.lang.String> entry =
// (Map.Entry<java.lang.Integer, java.lang.String>) iterator
// .next();
// String phone_number = entry.getValue();
// if (phone_number.equals(name)) {
// name = phone_number;
// }
// contacts.setName(name);
// contacts.setNumber(phone_number);
// contacts.setId(Integer.parseInt(id));
// // 保存是否为选中状态
// if (mSharePreferUtils
// .getBooleanPrefer(phone_number)) {
// contacts.setSelect(true);
// save_phone_set.add(phone_number);
// } else {
// contacts.setSelect(false);
// }
// c++;
// arrayList.add(contacts);
// }
// } else {
// c++;
// CLog.i("info", "号码为null的联系人" + name);
// }
// }
// if (arrayList.size() == pagesize) {
// break;
// }
// }
// phoneCursor.close();
// }
// // 保存选中的号码到本地
// mSharePreferUtils.setStringSetPrefer(KEY_SAVED_PHONE, save_phone_set);
// return arrayList;
// }
//
// public ArrayList<MyContacts> getContactsByLeast(int page, int pagesize) {
// ArrayList<MyContacts> arrayList = null;
// int position = page * pagesize;
// Uri uri = RawContacts.CONTENT_URI;
// String[] projection = { Contacts._ID, Contacts.DISPLAY_NAME,
// Contacts.SORT_KEY_PRIMARY };
// Cursor phoneCursor = resolver.query(uri, projection, null, null,
// Contacts.SORT_KEY_PRIMARY);
// if (phoneCursor != null && position > phoneCursor.getCount()) {
// return null;
// }
// if (phoneCursor != null && phoneCursor.moveToPosition(position)) {
// arrayList = new ArrayList<MyContacts>();
// while (phoneCursor.moveToNext()) {
//
// MyContacts contacts = new MyContacts();
// String id = phoneCursor.getString(phoneCursor
// .getColumnIndex(Contacts._ID));
// String firstname = String
// .valueOf(
// phoneCursor
// .getString(
// phoneCursor
// .getColumnIndex(Contacts.SORT_KEY_PRIMARY))
// .charAt(0)).toUpperCase();
// if (firstname.matches("\\d+")) {
// firstname = "#";
// }
// contacts.setFristName(firstname);
// String name = phoneCursor.getString(phoneCursor
// .getColumnIndex(Contacts.DISPLAY_NAME));
// sb.append(name + ",");
// if (!TextUtils.isEmpty(name) && name.matches("\\d+")) {
// // 没有存储姓名时，默认电话就是名字
// String phone_number = name;
// contacts.setName(name);
// contacts.setNumber(phone_number);
// contacts.setId(Integer.parseInt(id));
// // 保存是否为选中状态
// if (mSharePreferUtils.getBooleanPrefer(phone_number)) {
// contacts.setSelect(true);
// save_phone_set.add(phone_number);
// } else {
// contacts.setSelect(false);
// }
// c++;
// arrayList.add(contacts);
// } else {
// // 得到手机号码
// Map<Integer, String> phoneNumber_map = getContactPhoneNumber(id);
//
// if (phoneNumber_map != null && phoneNumber_map.size() > 0) {
// Iterator<Entry<Integer, String>> iterator = phoneNumber_map
// .entrySet().iterator();
// while (iterator.hasNext()) {
// Map.Entry<java.lang.Integer, java.lang.String> entry =
// (Map.Entry<java.lang.Integer, java.lang.String>) iterator
// .next();
// String phone_number = entry.getValue();
// contacts.setName(name);
// contacts.setNumber(phone_number);
// contacts.setId(Integer.parseInt(id));
// // 保存是否为选中状态
// if (mSharePreferUtils
// .getBooleanPrefer(phone_number)) {
// contacts.setSelect(true);
// save_phone_set.add(phone_number);
// } else {
// contacts.setSelect(false);
// }
// c++;
// arrayList.add(contacts);
// }
// } else {
// c++;
// CLog.i("info", "号码为null的联系人" + name);
// }
// }
// // if (arrayList.size() == pagesize) {
// // break;
// // }
// }
// phoneCursor.close();
// }
// // 保存选中的号码到本地
// mSharePreferUtils.setStringSetPrefer(KEY_SAVED_PHONE, save_phone_set);
// String[] strs = sb.toString().split(",");
// CLog.i("info", "C:" + c + "\nsb:" + sb.toString());
// CLog.i("info", "strscount:" + strs.length + "\nd:" + d);
// return arrayList;
// }
//
// @SuppressLint("UseSparseArrays")
// private Map<Integer, String> getContactPhoneNumber(String id) {
// Map<Integer, String> phoneNumbers = null;
// Uri uri = Data.CONTENT_URI;
// String[] projection = { Data.DATA1, Data.DATA2 };
// String selection = Data.CONTACT_ID + "=? and " + Data.MIMETYPE + "=?";
// String[] selectionArgs = { "" + id, "vnd.android.cursor.item/phone_v2" };
// Cursor c = resolver.query(uri, projection, selection, selectionArgs,
// null);
// if (c != null && c.moveToFirst()) {
// phoneNumbers = new HashMap<Integer, String>();
// do {
// d++;
// String number = c.getString(c.getColumnIndex(Data.DATA1));
// int type = c.getInt(c.getColumnIndex(Data.DATA2));
// if (!TextUtils.isEmpty(number)) {
// phoneNumbers.put(type, number);
// }
// } while (c.moveToNext());
// c.close();
// } else {
// CLog.i("info", "Cursor==null的id" + id);
// }
// return phoneNumbers;
// }
//
// }
