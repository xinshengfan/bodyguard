package com.fanfan.utils;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.fanfan.data.FileManager;

/**
 * 录音工具
 * 
 * @author FANFAN
 * 
 */
public class MediaRecoderUtils {
	private MediaRecorder mediaRecorder;
	private final String RECORD_FILE_NAME = "sosRecord";
	private SharePreferUtils preferUtils;
	private final String KEY_FILE_COUNT = "keyFileCount";

	private File recordFile;
	private Context mContext;
	private boolean isStart;

	@SuppressLint("InlinedApi")
	public MediaRecoderUtils(Context context) throws IOException {
		this.mContext = context;
		preferUtils = new SharePreferUtils(context);
		mediaRecorder = new MediaRecorder();
		// 设置音频来源为麦克风
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置输出格式
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
		// 设置编码方式
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
		// 创建音频文件
		FileManager fileManager = new FileManager(mContext);
		int recordcount = preferUtils.getIntPrefer(KEY_FILE_COUNT);
		recordFile = new File(fileManager.getRecordPath(context),
				RECORD_FILE_NAME + recordcount + ".amr");
		if (recordFile.exists()) {
			recordFile = new File(fileManager.getRecordPath(context),
					RECORD_FILE_NAME + (recordcount + 1) + ".amr");
			preferUtils.setIntPrefer(KEY_FILE_COUNT, (recordcount + 1));
		} else {
			recordFile.createNewFile();
		}
		// 指定输出文件
		mediaRecorder.setOutputFile(recordFile.getAbsolutePath());
		// 保存
		preferUtils.setStringPrefer(G.KEY_FILE_PATH,
				recordFile.getAbsolutePath());
		isStart = false;
	}

	/**
	 * 开始录音
	 * 
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public void startRecord() throws IllegalStateException, Exception {
		mediaRecorder.prepare();
		mediaRecorder.start();
		isStart = true;
	}

	/**
	 * 停止录音
	 */
	public void stopRecord() {
		if (mediaRecorder!=null && isStart) {
			mediaRecorder.setOnErrorListener(null);
			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
		}
		updateGallery(recordFile.getName());
		// mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED));
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(recordFile));
		mContext.sendBroadcast(intent);
	}

	/**
	 * 可以发送两个消息：Intent.ACTION_MEDIA_MOUNTED
	 * 是对媒体库进行全扫描；Intent.ACTION_MEDIA_SCANNER_SCAN_FILE则是 扫描某个文件；
	 * 
	 * 经测试，这个方法对于android4.4以下的版本，是没有问题的，<br>
	 * 但是从android4.4开始，使用这个方法，会出现如下的异常：
	 * 
	 * Permission Denial: not allowed to send broadcast
	 * android.intent.action.MEDIA_MOUNTED
	 * 
	 * @param filename
	 */
	private void updateGallery(String filename)// filename是我们的文件全名，包括后缀哦
	{
		// CLog.i("info", "添加扫描的文件：" + filename);
		MediaScannerConnection.scanFile(mContext, new String[] { filename },
				null, new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						// CLog.i("info", "Scanned " + path + ":");
						// CLog.i("info", "-> uri=" + uri);
					}
				});
	}
}
