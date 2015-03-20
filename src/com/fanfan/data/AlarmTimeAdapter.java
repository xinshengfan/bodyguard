package com.fanfan.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fanfan.bodyguard.AddtimeActivity;
import com.fanfan.bodyguard.EditMessageActivity;
import com.fanfan.bodyguard.R;
import com.fanfan.utils.G;
import com.fanfan.utils.SharePreferUtils;

/**
 * 定时报平安保存的item
 * 
 * @author FANFAN
 * 
 */
public class AlarmTimeAdapter extends BaseAdapter {
	private ArrayList<Long> times;
	private LayoutInflater inflater;
	private Calendar calendar;
	private Context mContext;

	private SharePreferUtils preferUtils;

	public AlarmTimeAdapter(Context context, ArrayList<Long> times) {
		this.mContext = context;
		this.inflater = LayoutInflater.from(context);
		setTimes(times);
		this.calendar = Calendar.getInstance(Locale.CHINA);
		this.preferUtils = new SharePreferUtils(context);
	}

	public void setTimes(ArrayList<Long> times) {
		if (times != null) {
			Comparator<Long> comparator = new Comparator<Long>() {

				@Override
				public int compare(Long lhs, Long rhs) {
					return (int) (lhs - rhs);
				}
			};
			Collections.sort(times, comparator);
			this.times = times;
		} else {
			this.times = new ArrayList<Long>();
		}
	}

	public void addTime(long time) {
		this.times.add(time);
		this.notifyDataSetChanged();
	}

	public void updata(int position, long time) {
		this.times.set(position, time);
		this.notifyDataSetChanged();
	}

	public void saveTimeToPrefer() {
		Set<String> times_set = new HashSet<String>();
		for (long time : times) {
			times_set.add(String.valueOf(time));
		}
		preferUtils.setStringSetPrefer(G.KEY_SAVE_ALARM_TIMES, times_set);
		// 保存完后发送一个广播，重置后台闹钟服务
		mContext.sendBroadcast(new Intent(G.ACTION_NEED_INITALARM));
	}

	@Override
	public int getCount() {
		return times.size();
	}

	@Override
	public Object getItem(int position) {
		return times.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_alarm_times, null);
			vh = new ViewHolder();
			vh.tv_time = (TextView) convertView
					.findViewById(R.id.tv_item_time_alarm);
			vh.tv_date = (TextView) convertView
					.findViewById(R.id.tv_item_date_alarm);
			vh.imv_add = (ImageView) convertView
					.findViewById(R.id.imv_item_time_add);
			vh.ll_item_add_time = (LinearLayout) convertView
					.findViewById(R.id.ll_item_add_time);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		if (times.size() == 0
				|| (times.size() > 0 && position == times.size() - 1)) {
			vh.imv_add.setVisibility(View.VISIBLE);
			vh.imv_add.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, AddtimeActivity.class);
					intent.putExtra(G.KEY_INTENT_TIME_POSITION, times.size());
					EditMessageActivity.getInstance().startForResult(intent,
							times.size());
				}
			});
		} else {
			vh.imv_add.setVisibility(View.GONE);
		}
		final long time = times.get(position);
		Date date = new Date(time);
		calendar.setTime(date);
		vh.tv_time.setText(getDate(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
				+ getDate(calendar.get(Calendar.MINUTE)));
		String key_self = String.valueOf(time);
		switch (preferUtils.getIntPrefer(key_self)) {
		case G.CYCLE_EVERY_DAY:
			vh.tv_date.setText("每天");
			break;
		case G.CYCLE_SELF:
			vh.tv_date.setText("自定义");
			break;
		case G.CYCLE_ONE_TIME:
			vh.tv_date.setText((calendar.get(Calendar.MONTH) + 1) + "月"
					+ calendar.get(Calendar.DATE) + "日");
			if (time < System.currentTimeMillis()) {
				vh.tv_date.setTextColor(mContext.getResources().getColor(
						R.color.gray));
				vh.tv_time.setTextColor(mContext.getResources().getColor(
						R.color.gray));
			}
			break;
		default:
			break;
		}

		vh.ll_item_add_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, AddtimeActivity.class);
				intent.putExtra(G.KEY_INTENT_EXTRA_TIME, time);
				intent.putExtra(G.KEY_INTENT_TIME_POSITION, position);
				EditMessageActivity.getInstance().startForResult(intent,
						position);
			}
		});
		vh.ll_item_add_time.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				openPopMenu(v, position);
				return true;
			}
		});
		return convertView;
	}

	protected void openPopMenu(View v, final int position) {
		View popView = inflater.inflate(R.layout.popmenu_view, null);
		final PopupWindow popupWindow = new PopupWindow(popView,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		ImageView imv_edit = (ImageView) popView.findViewById(R.id.imv_edit);
		ImageView imv_delete = (ImageView) popView
				.findViewById(R.id.imv_delete);
		// 设置其外部点击时也会隐藏
		popupWindow.setBackgroundDrawable(new ColorDrawable(0));
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					// 隐藏
					popupWindow.dismiss();
					return true;
				}
				return false;
			}
		});
		popupWindow.showAsDropDown(v);
		imv_edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 传递数据，编辑
				Intent intent = new Intent(mContext, AddtimeActivity.class);
				intent.putExtra(G.KEY_INTENT_EXTRA_TIME, times.get(position));
				intent.putExtra(G.KEY_INTENT_TIME_POSITION, position);
				EditMessageActivity.getInstance().startForResult(intent,
						position);
				popupWindow.dismiss();
			}
		});
		imv_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 删除
				openDeleteDialog(position);
				popupWindow.dismiss();
			}
		});
	}

	protected void openDeleteDialog(final int position) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setMessage("确定删除当前时间点吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 要删除偏好设置中保存的所有数据
						preferUtils.deleteKey(G.SAVE_PEACE_TIME + position);
						String key = String.valueOf(times.get(position));
						preferUtils.deleteKey(key);
						preferUtils.deleteKey(key + "self");
						times.remove(times.get(position));
						saveTimeToPrefer();
						if (times.size() == 0) {
							EditMessageActivity.getInstance()
									.setaddTimeVisiBle();
						}
						notifyDataSetChanged();
					}
				}).setNegativeButton("取消", null);
		dialog.show();

	}

	private String getDate(int i) {
		return i < 10 ? ("0" + i) : "" + i;
	}

	class ViewHolder {
		TextView tv_time;
		TextView tv_date;
		ImageView imv_add;
		LinearLayout ll_item_add_time;
	}

}
