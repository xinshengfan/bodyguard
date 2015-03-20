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
 * ¼������
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
		// ������Ƶ��ԴΪ��˷�
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// ���������ʽ
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
		// ���ñ��뷽ʽ
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
		// ������Ƶ�ļ�
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
		// ָ������ļ�
		mediaRecorder.setOutputFile(recordFile.getAbsolutePath());
		// ����
		preferUtils.setStringPrefer(G.KEY_FILE_PATH,
				recordFile.getAbsolutePath());
		isStart = false;
	}

	/**
	 * ��ʼ¼��
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
	 * ֹͣ¼��
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
	 * ���Է���������Ϣ��Intent.ACTION_MEDIA_MOUNTED
	 * �Ƕ�ý������ȫɨ�裻Intent.ACTION_MEDIA_SCANNER_SCAN_FILE���� ɨ��ĳ���ļ���
	 * 
	 * �����ԣ������������android4.4���µİ汾����û������ģ�<br>
	 * ���Ǵ�android4.4��ʼ��ʹ�������������������µ��쳣��
	 * 
	 * Permission Denial: not allowed to send broadcast
	 * android.intent.action.MEDIA_MOUNTED
	 * 
	 * @param filename
	 */
	private void updateGallery(String filename)// filename�����ǵ��ļ�ȫ����������׺Ŷ
	{
		// CLog.i("info", "���ɨ����ļ���" + filename);
		MediaScannerConnection.scanFile(mContext, new String[] { filename },
				null, new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						// CLog.i("info", "Scanned " + path + ":");
						// CLog.i("info", "-> uri=" + uri);
					}
				});
	}
}
