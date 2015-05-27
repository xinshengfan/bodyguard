package com.fanfan.music;

import java.util.ArrayList;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * 获取系统铃音
 * 
 * @author FANFAN
 * 
 */
public class SystemRingUtils {
	private Context mContext;

	public SystemRingUtils(Context context) {
		this.mContext = context;
	}

	public ArrayList<Ringtone> getSystemRing() {
		ArrayList<Ringtone> ringtones = new ArrayList<Ringtone>();
		RingtoneManager manager = new RingtoneManager(mContext);
		manager.setType(RingtoneManager.TYPE_ALL);
		Cursor cursor = manager.getCursor();
		int count = cursor.getCount();
		for (int i = 0; i < count; i++) {
			ringtones.add(manager.getRingtone(i));
		}
		cursor.close();
		return ringtones;
	}

	public ArrayList<MyRing> getMyRingList() {
		ArrayList<MyRing> resArr = new ArrayList<MyRing>();
		RingtoneManager manager = new RingtoneManager(mContext);
		manager.setType(RingtoneManager.TYPE_ALL);
		Cursor cursor = manager.getCursor();
		if (cursor.moveToFirst()) {
			while (cursor.moveToNext()) {
				MyRing ring = new MyRing();
				ring.setId(Integer.parseInt(cursor
						.getString(RingtoneManager.ID_COLUMN_INDEX)));
				ring.setUri(cursor.getString(RingtoneManager.URI_COLUMN_INDEX));
				ring.setName((cursor
						.getString(RingtoneManager.TITLE_COLUMN_INDEX)));
				resArr.add(ring);
			}
		}
		cursor.close();
		return resArr;
	}

	public String getRingNameById(int id) {
		String ringName = "";
		RingtoneManager manager = new RingtoneManager(mContext);
		manager.setType(RingtoneManager.TYPE_ALL);
		Cursor cursor = manager.getCursor();
		if (cursor.moveToFirst()) {
			while (cursor.moveToNext()) {
				int read_id = Integer.parseInt(cursor
						.getString(RingtoneManager.ID_COLUMN_INDEX));
				if (id == read_id) {
					ringName = cursor
							.getString(RingtoneManager.TITLE_COLUMN_INDEX);
					break;
				}
			}
		}
		cursor.close();
		return ringName;
	}

	public Ringtone getRingtoneById(int id) {
		RingtoneManager manager = new RingtoneManager(mContext);
		Ringtone tone = null;
		manager.setType(RingtoneManager.TYPE_ALL);
		tone = manager.getRingtone(id);
		return tone;
	}

	public Uri getRingtoneUriById(int id) {
		RingtoneManager manager = new RingtoneManager(mContext);
		String toneUri = null;
		manager.setType(RingtoneManager.TYPE_ALL);
		Cursor cursor = manager.getCursor();
		if (cursor.moveToFirst()) {
			while (cursor.moveToNext()) {
				int read_id = Integer.parseInt(cursor
						.getString(RingtoneManager.ID_COLUMN_INDEX));
				if (id == read_id) {
					toneUri = cursor
							.getString(RingtoneManager.URI_COLUMN_INDEX);
					break;
				}
			}
		}
		cursor.close();
		return getUriById(Uri.parse(toneUri), id);
	}

	public Ringtone getRingtone(Uri uri) {
		return RingtoneManager.getRingtone(mContext, uri);
	}

	public Ringtone getRingtone(int pos) {
		RingtoneManager manager = new RingtoneManager(mContext);
		manager.setType(RingtoneManager.TYPE_ALL);
		return manager.getRingtone(pos);

	}

	private Uri getUriById(Uri uri, long paramLong) {
		return ContentUris.withAppendedId(uri, paramLong);
	}
}
