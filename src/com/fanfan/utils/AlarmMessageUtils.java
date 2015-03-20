package com.fanfan.utils;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.fanfan.bodyguard.MyApp;
import com.fanfan.data.ContactsUtils;

public class AlarmMessageUtils {
	private SharePreferUtils preferUtils;
	private MessageUtils messageUtils;
	private Context mContext;

	public AlarmMessageUtils(Context context) {
		this.mContext = context;
		preferUtils = new SharePreferUtils(context);
		messageUtils = new MessageUtils(context);
	}

	/**
	 * 发送彩信
	 * 
	 * @param text
	 * @return
	 */
	public boolean sendMMS(String text) {
		readContent(text);
		CLog.i("info", "发送彩信");
		String audioPath = preferUtils.getStringPrefer(G.KEY_FILE_PATH);
		if (TextUtils.isEmpty(audioPath)) {
			Toast.makeText(mContext, "没有找到音频文件", Toast.LENGTH_SHORT).show();
			return false;
		}
		Uri fileUri = queryUriforAudio(new File(audioPath));
		if (fileUri == null) {
			Toast.makeText(mContext, "没有找到Uri", Toast.LENGTH_SHORT).show();
			return false;
		}
		String subject = "救我";
		// 取出电话号码
		Set<String> saved_phone = preferUtils
				.getStringSetPrefer(ContactsUtils.KEY_SAVED_PHONE);
		if (saved_phone == null || saved_phone.size() == 0) {
			Toast.makeText(mContext, "没有保存的电话号码", Toast.LENGTH_SHORT).show();
			return false;
		}
		Iterator<String> iterator = saved_phone.iterator();
		while (iterator.hasNext()) {
			String phone_number = iterator.next();
			// messageUtils.send(mContext, phone_number, subject, text,
			// audioPath);
			messageUtils.sendMMsActivity(fileUri, subject, phone_number, text);
		}
		return true;
	}

	/**
	 * 查找在于SDcard中的Audio文件对应于MediaStore 的uri
	 * 
	 * @param file
	 *            音频文件
	 * @return
	 */
	public Uri queryUriforAudio(File file) {
		ContentResolver contentResolver = mContext.getContentResolver();
		final String where = MediaStore.Audio.Media.DATA + "='"
				+ file.getAbsolutePath() + "'";
		// CLog.i("info", "查询uri的file：" + file.getAbsolutePath() + " ; name:"
		// + file.getName());
		Cursor cursor = contentResolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null,
				null);
		if (cursor == null) {
			CLog.i("info", "没有找到文件");
			return null;
		}
		// while (cursor.moveToNext()) {
		// String str = cursor.getString(cursor.getColumnIndex(Media.DATA));
		// CLog.i("info", "stream:" + str);
		// }
		int id = -1;
		if (cursor != null) {
			cursor.moveToFirst();
			// CLog.i("info",
			// "name:"
			// + cursor.getString(cursor
			// .getColumnIndex(Media.DISPLAY_NAME)));
			if (!cursor.isAfterLast()) {
				id = cursor.getInt(0);
			}
			cursor.close();
		}
		if (id == -1) {
			return null;
		}
		return Uri
				.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						String.valueOf(id));
	}

	/**
	 * 发送普通信息
	 * 
	 * @param text
	 */
	public boolean sendNomalMessage(String text) {
		readContent(text);
		// 取出电话号码
		Set<String> saved_phone = preferUtils
				.getStringSetPrefer(ContactsUtils.KEY_SAVED_PHONE);
		if (saved_phone == null || saved_phone.size() == 0) {
			Toast.makeText(mContext, "没有保存的电话号码", Toast.LENGTH_SHORT).show();
			return false;
		}
		Iterator<String> iterator = saved_phone.iterator();
		while (iterator.hasNext()) {
			String phone_number = iterator.next();
			CLog.i("info", "发送的电话：" + phone_number);
			messageUtils.sendMessage(phone_number, text);
		}
		return true;
	}

	private boolean readContent(String text) {
		if (!messageUtils.isSimCard()) {
			Toast.makeText(mContext, messageUtils.getsimCardState(),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!TextUtils.isEmpty(MyApp.getInstance().getLastAddress())
				&& text.contains(MyApp.getInstance().getLastAddress())
				&& !TextUtils.isEmpty(MyApp.getInstance().getCurrentAddress())
				&& !MyApp.getInstance().getCurrentAddress()
						.equals(MyApp.getInstance().getLastAddress())) {
			// 替换地址
			text.replace(MyApp.getInstance().getLastAddress(), MyApp
					.getInstance().getCurrentAddress());
		} else if (TextUtils.isEmpty(MyApp.getInstance().getLastAddress())
				|| !text.contains(MyApp.getInstance().getLastAddress())) {
			// 没有地址,则追加在信息后边
			text += ",我在" + MyApp.getInstance().getCurrentAddress() + "附近。";
		}
		return true;
	}
}
