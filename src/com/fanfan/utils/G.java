package com.fanfan.utils;

/**
 * 全局变量
 * 
 * @author FANFAN
 * 
 */
public class G {
	public static final String INTENT_KEY_EDITMESSAGE = "intentKeyEditmessage";
	public static final String VALUE_EDITMESSAGE_SOS = "valueEditmessageSOS";
	public static final String VALUE_EDITMESSAGE_PEACE = "valueEditmessagePEACE";
	/**** 音乐控制 ****/
	public static final String ACTION_PLAY = "com.fanfan.action.MUSICPLAY";
	public static final String ACTION_PAUSE = "com.fanfan.action.PAUSE";
	public static final String ACTION_EXIT = "com.fanfan.action.EXIT";
	public static final String ACTION_PLAY_ITEM_RING = "com.fanfan.action.playitemring";

	public static final String Monday = "Monday";
	public static final String Tuesday = "Tuesday";
	public static final String Wednesday = "Wednesday";
	public static final String Thursday = "Thursday";
	public static final String Friday = "Friday";
	public static final String Saturday = "Saturday";
	public static final String Sunday = "Sunday";

	public static final String ACTION_MESSAGE_HADSENT = "com.fanfan.action.messageHadSent";

	public static final String ACTION_ALARM_SEND = "com.fanfan.actionAlarmsend";
	public static final String ACTION_NEED_INITALARM = "com.fanfan.actionNeedInitalarm";
	/*** 手动移除App *****/
	public static final String ACTION_APP_REMOVED = "com.fanfan.action.appRemoved";
	public static final String ACTION_SERVICE_SEND = "com.fanfan.action.ServiceSend";
	/********* 偏好设置 ************/
	public static final String SHAREPREFER_NAME = "MyBodyguardFile";

	/********* 闹钟时间相关 ************/
	public static final String SAVE_PEACE_TIME = "savePeaceTime";
	// 保存时间设置选项，key为保存时间long值AddtimeActivity
	public static final String KEY_INTENT_RESULT = "keyIntentResult";
	public static final int CYCLE_EVERY_DAY = 0xD1;
	public static final int CYCLE_ONE_TIME = 0xD2;
	public static final int CYCLE_SELF = 0xD3;
	public static final String KEY_ALARM_TIME = "keyAlarmTime";
	// EditMessageActivity
	public static final String KEY_SAVE_MESSAGE = "keySaveMessage";
	public static final String KEY_SAVE_PEACE = "keySavePeaceMessage";
	public static final String KEY_SAVE_ALARM_TIMES = "keySaveAlarmTimes";
	// AlarmAdapter
	public static final String KEY_INTENT_EXTRA_TIME = "keyIntentExtraTime";
	public static final String KEY_INTENT_TIME_POSITION = "keyIntentTimePosition";
	// MediaRecoderUtils
	public static final String KEY_FILE_PATH = "keyFilePath";
	// SettingActivity
	public static final String KEY_RECORD_LENGTH = "keyrecordLength";
	public static final String KEY_TIME_INTERVAL = "keytimeInterval";
	public static final String KEY_PEACE_ONTIME = "keypeaceontime";
	public static final String KEY_PLAY_RING = "keyplayring";
	public static final String KEY_USE_RECORD = "keyuserecord";
	public static final String KEY_MUSIC_ID = "keymusicid";
	public static final String KEY_RING_TYPE = "keySaveRingType";
	/**
	 * 对于系统铃音，保存的是Ringtone.id;对于媒体铃音，保存的是音乐路径
	 */
	public static final String KEY_RING_PATH = "keyRingPath";
	public static final String VALUE_SYSTEM_RING = "valuesystemring";
	public static final String VALUE_MEDIA_RING = "valuemediaring";
	public static final String KEY_RING_NAME = "keyRingName"; // 保存媒体名

}
