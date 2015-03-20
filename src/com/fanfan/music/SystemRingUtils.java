package com.fanfan.music;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;

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
				// CLog.i("info",
				// "系统铃音Uri:" + ring.getUri() + "; id:" + ring.getId()
				// + " ; name:" + ring.getName());
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
		Cursor cursor = manager.getCursor();
		if (cursor.moveToFirst()) {
			while (cursor.moveToNext()) {
				int read_id = Integer.parseInt(cursor
						.getString(RingtoneManager.ID_COLUMN_INDEX));
				if (id == read_id) {
					tone = manager.getRingtone(id);
					break;
				}
			}
		}
		cursor.close();
		return tone;
	}
}
