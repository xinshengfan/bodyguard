package com.fanfan.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fanfan.bodyguard.R;
import com.fanfan.music.Music;
import com.fanfan.utils.SharePreferUtils;

public class MediaRingAdapter extends BaseAdapter {
	private ArrayList<Music> mymusics;
	private LayoutInflater inflater;
	// 将选中路径作为Key以存放是否被选中；
	private SharePreferUtils preferUtils;
	/***
	 * 在重用item时会造成checkbox重复选择，产生混乱，故用一个Map保存选择状态
	 */
	Map<Integer, Boolean> select_Map;

	public MediaRingAdapter(Context context, ArrayList<Music> musics) {
		this.inflater = LayoutInflater.from(context);
		this.preferUtils = new SharePreferUtils(context);
		setMyRings(musics);
		setSelect_Map();
	}

	public Map<Integer, Boolean> getSelect_Map() {
		return select_Map;
	}

	public void setSelect_Map() {
		this.select_Map = new HashMap<Integer, Boolean>();
		for (int i = 0; i < mymusics.size(); i++) {
			select_Map.put(i, preferUtils.getBooleanPrefer(mymusics.get(i)
					.getMusicPath()));
		}
	}

	public void setMyRings(ArrayList<Music> musics) {
		if (musics != null) {
			boolean isHadSelect = false;
			for (Music m : musics) {
				if (preferUtils.getBooleanPrefer(m.getMusicPath())) {
					isHadSelect = true;
				}
			}
			if (musics.size() > 0 && !isHadSelect) {
				// 没有选择铃音，默认选择第一个
				preferUtils
						.setBooleanPrefer(musics.get(0).getMusicPath(), true);
			}
			this.mymusics = musics;
		} else {
			this.mymusics = new ArrayList<Music>();
		}
	}

	public void updataMusic(ArrayList<Music> musics) {
		if (musics == null) {
			return;
		}
		this.mymusics.addAll(musics);
		for (int i = 0; i < this.mymusics.size(); i++) {
			select_Map.put(i, preferUtils.getBooleanPrefer(mymusics.get(i)
					.getMusicPath()));
		}
		this.notifyDataSetChanged();
	}

	/**
	 * 只能选一个，清空所有
	 * 
	 * @param myRings
	 */
	public void clearAndSetSelect(Music music) {
		if (music != null) {
			for (Music m : mymusics) {
				preferUtils.setBooleanPrefer(m.getMusicPath(), false);
			}
			preferUtils.setBooleanPrefer(music.getMusicPath(), true);
		}
	}

	public void clearAndSetMap(int position) {
		for (Integer key : select_Map.keySet()) {
			select_Map.put(key, false);
		}
		select_Map.put(position, true);
	}

	@Override
	public int getCount() {
		return mymusics.size();
	}

	@Override
	public Music getItem(int position) {
		return mymusics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_music, null);
			vh = new ViewHolder();
			vh.tv_ring_name = (TextView) convertView
					.findViewById(R.id.tv_item_music);
			vh.radio_btn = (RadioButton) convertView
					.findViewById(R.id.radio_item_music);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		Music m = mymusics.get(position);
		vh.tv_ring_name.setText(m.getName());
		vh.radio_btn.setChecked(select_Map.get(position));
		return convertView;
	}

	public class ViewHolder {
		public TextView tv_ring_name;
		public RadioButton radio_btn;
	}

}
