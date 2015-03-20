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
	 * 从数据库中取出音乐的数据 限制条数
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
	 * 从数据库中取出音乐的数据
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
	 * 分页加载
	 * 
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public ArrayList<Music> getMusics(int page, int pageSize) {
		ArrayList<Music> musics = null;
		int postion = page * pageSize; // 用于计算加载的位置；
		// Log.i("info", "要加载的位置：" + postion);
		String[] projection = { Media._ID, Media.TITLE, Media.DURATION,
				Media.DATA };
		Cursor c = cr.query(uri, projection, null, null, null);
		// 判断，如果要加载的位置大于了查询的条目数，就返回
		if (c != null && postion > c.getCount()) {
			// Log.i("info", "Cursor中总共条目数：" + c.getCount());
			return null;
		}
		// 从指定位置开始加载
		if (c != null && c.moveToPosition(postion)) {
			// Log.i("info", "游标当前位置：" + c.getPosition());
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
