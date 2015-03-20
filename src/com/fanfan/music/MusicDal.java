package com.fanfan.music;

import java.util.ArrayList;

import com.fanfan.music.Music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;

public class MusicDal {
	private ContentResolver cr;
	private Uri uri = Media.EXTERNAL_CONTENT_URI;

	public MusicDal(Context context) {
		cr = context.getContentResolver();
	}

	/**
	 * �����ݿ���ȡ�����ֵ����� ��������
	 * 
	 * @return
	 */
	public ArrayList<Music> getMusicsByLimit(int pageSize) {
		ArrayList<Music> musics = null;
		String[] projection = { Media._ID, Media.TITLE, Media.DURATION,
				Media.DATA };
		Cursor c = cr.query(uri, projection, null, null, null);
		if (c != null) {
			musics = new ArrayList<Music>();
			while (c.moveToNext()) {
				Music music = new Music();
				music.setId(c.getInt(c.getColumnIndex(Media._ID)));
				music.setName(c.getString(c.getColumnIndex(Media.TITLE)));
				music.setMusicPath(c.getString(c.getColumnIndex(Media.DATA)));
				music.setDuration(c.getLong(c.getColumnIndex(Media.DURATION)));
				musics.add(music);
				if (musics.size() == pageSize) {
					break;
				}
			}
		}
		c.close();
		return musics;
	}

	/**
	 * �����ݿ���ȡ�����ֵ�����
	 * 
	 * @return
	 */
	public ArrayList<Music> getMusicsAll() {
		ArrayList<Music> musics = null;
		String[] projection = { Media._ID, Media.TITLE, Media.DURATION,
				Media.DATA };
		Cursor c = cr.query(uri, projection, null, null, null);
		if (c != null) {
			musics = new ArrayList<Music>();
			while (c.moveToNext()) {
				Music music = new Music();
				music.setId(c.getInt(c.getColumnIndex(Media._ID)));
				music.setName(c.getString(c.getColumnIndex(Media.TITLE)));
				music.setMusicPath(c.getString(c.getColumnIndex(Media.DATA)));
				music.setDuration(c.getLong(c.getColumnIndex(Media.DURATION)));
				musics.add(music);
			}
		}
		c.close();
		return musics;
	}

	/**
	 * ��ҳ����
	 * 
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public ArrayList<Music> getMusics(int page, int pageSize) {
		ArrayList<Music> musics = null;
		int postion = page * pageSize; // ���ڼ�����ص�λ�ã�
		// Log.i("info", "Ҫ���ص�λ�ã�" + postion);
		String[] projection = { Media._ID, Media.TITLE, Media.DURATION,
				Media.DATA };
		Cursor c = cr.query(uri, projection, null, null, null);
		// �жϣ����Ҫ���ص�λ�ô����˲�ѯ����Ŀ�����ͷ���
		if (c != null && postion > c.getCount()) {
			// Log.i("info", "Cursor���ܹ���Ŀ����" + c.getCount());
			return null;
		}
		// ��ָ��λ�ÿ�ʼ����
		if (c != null && c.moveToPosition(postion)) {
			// Log.i("info", "�α굱ǰλ�ã�" + c.getPosition());
			musics = new ArrayList<Music>();
			while (c.moveToNext()) {
				Music music = new Music();
				music.setId(c.getInt(c.getColumnIndex(Media._ID)));
				music.setName(c.getString(c.getColumnIndex(Media.TITLE)));
				music.setMusicPath(c.getString(c.getColumnIndex(Media.DATA)));
				music.setDuration(c.getLong(c.getColumnIndex(Media.DURATION)));
				musics.add(music);
				if (musics.size() == pageSize) {
					break;
				}
			}
		}
		c.close();
		return musics;
	}

}
