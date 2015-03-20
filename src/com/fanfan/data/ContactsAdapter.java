package com.fanfan.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fanfan.bodyguard.R;
import com.fanfan.utils.CLog;

public class ContactsAdapter extends BaseAdapter {
	private ArrayList<MyContacts> myContacts;
	private LayoutInflater inflater;

	/***
	 * 在重用item时会造成checkbox重复选择，产生混乱，故用一个Map保存选择状态
	 */
	Map<Integer, Boolean> select_Map;

	public ContactsAdapter(Context context, ArrayList<MyContacts> myContacts) {
		this.inflater = LayoutInflater.from(context);
		setMyContacts(myContacts);
		initMap();
	}

	private void initMap() {
		this.select_Map = new HashMap<Integer, Boolean>();
		for (int i = 0; i < myContacts.size(); i++) {
			select_Map.put(i, myContacts.get(i).isSelect());
		}
	}

	public Map<Integer, Boolean> getSelect_Map() {
		return select_Map;
	}

	public void addContacts(ArrayList<MyContacts> contacts) {
		CLog.i("info", "添加联系人");
		if (contacts == null) {
			return;
		}
		CLog.i("info", "contactssize:" + contacts.size());
		this.myContacts.addAll(contacts);
		for (int i = 0; i < myContacts.size(); i++) {
			select_Map.put(i, myContacts.get(i).isSelect());
		}
		CLog.i("info", "listsize:" + myContacts.size());

		this.notifyDataSetChanged();
	}

	public void setMyContacts(ArrayList<MyContacts> myContacts) {
		if (myContacts == null) {
			this.myContacts = new ArrayList<MyContacts>();
		} else {
			this.myContacts = myContacts;
		}
	}

	public ArrayList<MyContacts> getMyContacts() {
		return myContacts;
	}

	@Override
	public int getCount() {
		return myContacts.size();
	}

	@Override
	public MyContacts getItem(int position) {
		return myContacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_contact, null);
			vh = new ViewHolder();
			vh.tv_name = (TextView) convertView
					.findViewById(R.id.tv_contact_name);
			vh.tv_number = (TextView) convertView
					.findViewById(R.id.tv_contact_number);
			vh.cb_isSelect = (CheckBox) convertView
					.findViewById(R.id.checkBox_contact);
			vh.tv_index = (TextView) convertView
					.findViewById(R.id.tv_index_contacts);
			vh.ll_item = (LinearLayout) convertView
					.findViewById(R.id.ll_item_contacts);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		final MyContacts contact = myContacts.get(position);
		if (position == 0) {
			vh.tv_index.setText(contact.getFristName());
			vh.tv_index.setVisibility(View.VISIBLE);
			vh.ll_item.setVisibility(View.VISIBLE);
		} else {
			String lastCataLog = myContacts.get(position - 1).getFristName();
			if (!TextUtils.isEmpty(contact.getFristName())
					&& lastCataLog.equals(contact.getFristName())) {
				vh.tv_index.setVisibility(View.GONE);
				vh.ll_item.setVisibility(View.VISIBLE);
			} else {
				vh.tv_index.setText(contact.getFristName());
				vh.tv_index.setVisibility(View.VISIBLE);
				vh.ll_item.setVisibility(View.VISIBLE);
			}
		}
		if (!TextUtils.isEmpty(contact.getName())) {
			vh.tv_name.setText(contact.getName());
		} else {
			vh.tv_name.setText(contact.getNumber());
		}
		vh.tv_number.setText(contact.getNumber());
		if (position <= select_Map.size()) {
			vh.cb_isSelect.setChecked(select_Map.get(position));
		}

		return convertView;
	}

	public class ViewHolder {
		public TextView tv_name;
		public TextView tv_number;
		public CheckBox cb_isSelect;
		public TextView tv_index;
		public LinearLayout ll_item;

	}

}
