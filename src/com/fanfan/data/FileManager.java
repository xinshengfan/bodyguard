package com.fanfan.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fanfan.utils.CLog;

public class FileManager {
	private String BodyGuard_FILE_PATH = "/bodyguard/";
	private Context context;

	public FileManager(Context context) {
		this.context = context;
	}

	public String getRecordPath(Context context) {
		String path;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ BodyGuard_FILE_PATH;
		} else {
			path = context.getFilesDir().getAbsolutePath()
					+ BodyGuard_FILE_PATH;
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
			Log.d("debugfile", "create file dir");
		}
		return path;
	}

	public void deleteFileFirst(String otherPath, String filename2) {
		File SDFile = new File(otherPath + filename2);
		if (SDFile.exists()) {
			SDFile.delete();
		}

	}

	public void copyFileToSD(String filepath2, String filename2) {
		File fileToSD = new File(filepath2 + filename2);
		if (!fileToSD.exists()) {
			startCopyFile(fileToSD, filename2);
		} else {
			CLog.i("info", "文件" + fileToSD.getAbsolutePath() + " 已存在");
		}

	}

	public void startCopyFile(final File fileToSD, final String filename2) {
		new Thread() {
			public void run() {
				InputStream ins = null;
				FileOutputStream fos = null;
				try {
					fileToSD.createNewFile();
					ins = context.getResources().getAssets().open(filename2);
					fos = new FileOutputStream(fileToSD);
					byte[] data = new byte[8 * 1024];
					int len;
					while ((len = ins.read(data)) != -1) {
						fos.write(data, 0, len);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (ins == null || fos == null) {
							return;
						}
						ins.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();

	}
}
