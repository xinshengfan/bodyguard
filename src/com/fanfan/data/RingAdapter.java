package com.fanfan.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fanfan.bodyguard.R;
import com.fanfan.music.MyRing;
import com.fanfan.utils.SharePreferUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class RingAdapter extends BaseAdapter {
	private ArrayList<MyRing> myRings;
	private LayoutInflater inflater;
	// 将选中的铃音的name作为Key以存放是否被选中；
	private SharePreferUtils preferUtils;
	/***
	 * 在重用item时会造成checkbox重复选择，产生混乱，故用一个Map保存选择状态
	 */
	Map<Integer, Boolean> select_Map;

	public RingAdapter(Context context, ArrayList<MyRing> myRings) {
		this.inflater = LayoutInflater.from(context);
		this.preferUtils = new SharePreferUtils(context);
		setMyRings(myRings);
		setSelect_Map();
	}

	public void setMyRings(ArrayList<MyRing> myRings) {
		if (myRings != null) {
			boolean isHadSelect = false;
			for (MyRing ring : myRings) {
				if (preferUtils.getBooleanPrefer(ring.getName())) {
					isHadSelect = true;
				}
			}
			if (myRings.size() > 0 && !isHadSelect) {
				// 没有选择铃音，默认选择第一个
				preferUtils.setBooleanPrefer(myRings.get(0).getName(), true);
			}
			this.myRings = myRings;
		} else {
			this.myRings = new ArrayList<MyRing>();
		}
	}

	public Map<Integer, Boolean> getSelect_Map() {
		return select_Map;
	}

	public void setSelect_Map() {
		this.select_Map = new HashMap<Integer, Boolean>();
		for (int i = 0; i < myRings.size(); i++) {
			select_Map.put(i,
					preferUtils.getBooleanPrefer(myRings.get(i).getName()));
		}
	}

	/**
	 * 只能选一个，清空所有
	 * 
	 * @param myRings
	 */
	public void clearAndSetSelect(MyRing ring) {
		if (ring != null) {
			for (MyRing r : myRings) {
				preferUtils.setBooleanPrefer(r.getName(), false);
			}
			preferUtils.setBooleanPrefer(ring.getName(), true);
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
		return myRings.size();
	}

	@Override
	public MyRing getItem(int position) {
		return myRings.get(position);
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
		MyRing ring = myRings.get(position);
		vh.tv_ring_name.setText(ring.getName());
		vh.radio_btn.setChecked(select_Map.get(position));
		return convertView;
	}

	public class ViewHolder {
		public TextView tv_ring_name;
		public RadioButton radio_btn;
	}

}
