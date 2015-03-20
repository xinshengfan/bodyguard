package com.fanfan.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.SendReq;

/**
 * 发送短信和彩信
 * 
 * @author FANFAN
 * 
 */
public class MessageUtils {
	private SmsManager manager;
	private Context mContext;
	private TelephonyManager telemanager;
	/** 短信发送完成 */
	public static final String ACTION_MESSAGE_SENT = "com.action.fan.messagesent";
	/** 短信成功送达 */
	public static final String ACTION_MESSAGE_RECEIVE = "com.action.fan.messagereceive";
	public static final String KEY_SENT_MESSAGE_NUMBER = "keysentmessagenumber";
	public static final String KEY_SENT_MESSAGE_CONTENT = "keysentmessagecontent";
	/** 电信彩信中心url，代理，端口 */
	public static String mmscUrl_ct = "http://mmsc.vnet.mobi";
	public static String mmsProxy_ct = "10.0.0.200";
	/** 移动彩信中心url，代理，端口 */
	public static String mmscUrl_cm = "http://mmsc.monternet.com";
	public static String mmsProxy_cm = "010.000.000.172";
	/** 联通彩信中心url，代理，端口 */
	public static String mmscUrl_uni = "http://mmsc.vnet.mobi";
	public static String mmsProxy_uni = "10.0.0.172";
	boolean isSend;
	private static Handler myHandler;
	private String messActivity_name = "com.android.mms.ui.ComposeMessageActivity";
	private String phone_name;

	public MessageUtils(Context content) {
		this.mContext = content;
		manager = SmsManager.getDefault();
		telemanager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		myHandler = new Handler();
		
		String phoneInfo = "BOARD: " + android.os.Build.BOARD;
		phoneInfo += ", MANUFACTURER: " + android.os.Build.MANUFACTURER;
		phoneInfo += ", MODEL: " + android.os.Build.MODEL;
		CLog.i("info", "$$$$$$$$$$ 生产厂家 $$$$$$$$$+\n" + phoneInfo);
		phone_name = android.os.Build.MANUFACTURER;
		if ("xiaomi".equalsIgnoreCase(phone_name)) {
			messActivity_name = "com.android.mms.ui.NewMessageActivity";
		}
	}

	/**
	 * 发送短信
	 * 
	 * @param phonenumber
	 * @param content
	 */
	public void sendMessage(String phonenumber, String content) {
		if (content.length() > 70) {
			// 拆分
			ArrayList<String> parts = manager.divideMessage(content);
			for (String text : parts) {
				startSent(phonenumber, text);
			}
		} else {
			startSent(phonenumber, content);
		}
	}

	private void startSent(String phonenumber, String text) {
		Intent intent = new Intent(ACTION_MESSAGE_SENT);
		intent.putExtra(KEY_SENT_MESSAGE_NUMBER, phonenumber);
		intent.putExtra(KEY_SENT_MESSAGE_CONTENT, text);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent delivery_intent = new Intent(ACTION_MESSAGE_RECEIVE);
		PendingIntent deliveryIntent = PendingIntent.getBroadcast(mContext, 1,
				delivery_intent, PendingIntent.FLAG_UPDATE_CURRENT);
		manager.sendTextMessage(phonenumber, null, text, pendingIntent,
				deliveryIntent);
	}

	/**
	 * 在信息发送完成后须手动将发送的信息插入数据库；
	 * 
	 * @param phoneaddress
	 * @param content
	 */
	public void saveMessageToDB(String phoneaddress, String content) {
		Uri uri = Uri.parse("content://sms/sent");
		ContentResolver cr = mContext.getContentResolver();
		// 写入数据库
		ContentValues values = new ContentValues();
		values.put("address", phoneaddress);
		values.put("body", content);
		values.put("date", System.currentTimeMillis());
		cr.insert(uri, values);
	}

	public boolean isSimCard() {
		return telemanager.getSimState() == TelephonyManager.SIM_STATE_READY;
	}

	public String getsimCardState() {
		switch (telemanager.getSimState()) {
		case TelephonyManager.SIM_STATE_ABSENT:
			return "没有SIM卡";
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return "需要NetworkPIN解锁";
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return "需要PIN解锁";
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return "需要PUK解锁";
		case TelephonyManager.SIM_STATE_UNKNOWN:
			return "未知状态";
		case TelephonyManager.SIM_STATE_READY:
			return "sim卡就绪";
		default:
			break;
		}
		return "";
	}

	/**
	 * 调用彩信发送界面
	 * 
	 * @param url
	 */
	public void sendMMsActivity(Uri url, String subject, String phonenumber,
			String context) {
		Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mms://"));
		intent.setClassName("com.android.mms",
				messActivity_name);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("compose_mode", false);
		intent.putExtra("exit_on_sent", true);
		intent.putExtra(Intent.EXTRA_STREAM, url);// uri为你的附件的uri
		intent.putExtra("subject", subject); // 彩信的主题
		intent.putExtra("address", phonenumber); // 彩信发送目的号码
		intent.putExtra("sms_body", context); // 彩信中文字内容
		intent.putExtra(Intent.EXTRA_TEXT, "it's EXTRA_TEXT");
		intent.setType("audio/*");// 彩信附件类型
		intent.setClassName("com.android.mms",
				"com.android.mms.ui.ComposeMessageActivity");
		mContext.startActivity(intent);
		// mContext.startActivity(Intent.createChooser(intent, "MMS:"));
	}

	// public void sendMMS(final Context context, final String phone,
	// final String subject, final String message, String imagePath,
	// String audioPath) {
	// new Thread() {
	// @Override
	// public void run() {
	// try {
	// Carrier carrier = null;
	// APN apn = null;
	// Settings sendSettings = new Settings();
	// TelephonyManager tel = (TelephonyManager)
	// getSystemService(Context.TELEPHONY_SERVICE);
	// String networkOperator = tel.getNetworkOperator();
	// if (networkOperator != null) {
	// int mcc = Integer.parseInt(networkOperator.substring(0,
	// 3));
	// String s = networkOperator.substring(3);
	// int mnc = Integer.parseInt(s);
	// carrier = Carrier.getCarrier(mcc, mnc);
	// carrier.getsmsemail();
	// apn = carrier.getAPN();
	// }
	//
	// sendSettings.setMmsc(apn.mmsc);
	// sendSettings.setProxy(apn.proxy);
	// sendSettings.setPort(Integer.valueOf(apn.port).toString());
	// sendSettings.setGroup(true);
	// sendSettings.setDeliveryReports(false);
	// sendSettings.setSplit(false);
	// sendSettings.setSplitCounter(false);
	// sendSettings.setStripUnicode(false);
	// sendSettings.setSignature("");
	// sendSettings.setSendLongAsMms(true);
	// sendSettings.setSendLongAsMmsAfter(3);
	// sendSettings.setRnrSe(null);
	// Transaction sendTransaction = new Transaction(instance,
	// sendSettings);
	// Message mMessage = new Message(message, phone);
	// mMessage.setSubject(subject);
	// mMessage.setType(Message.TYPE_SMSMMS);
	// sendTransaction.sendNewMessage(mMessage, 0);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// };
	// }.start();
	// }

	/**
	 * apn必须是wap，当如果是net时，因为连接超时而无法发送，
	 * 
	 * 正以内发彩信必须通过wap接入点才能发送，即使是在系统界面发彩信时，
	 * 
	 * 如果接入点不是wap ，则会自动切换过来，待发送完后再切换回去，所以这个模块的核心，
	 * 
	 * 其实就是，如何在调用发彩信时，切换apn至wap，待发送完毕后再切换回去。。。
	 * 
	 * @param context
	 *            根据不同移动供应商，需要设置不同的url和proxy
	 * @return
	 */
	private static List<String> getSimMNC(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telManager.getSubscriberId();
		if (imsi != null) {
			CLog.i("info", "imsi:" + imsi);
			ArrayList<String> list = new ArrayList<String>();
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
				// 中国移动
				list.add(mmscUrl_cm);
				list.add(mmsProxy_cm);
			} else if (imsi.startsWith("46001")) {
				// 中国联通
				list.add(mmscUrl_uni);
				list.add(mmsProxy_uni);
			} else if (imsi.startsWith("46003")) {
				// 中国电信
				list.add(mmscUrl_ct);
				list.add(mmsProxy_ct);
			}
			//shouldChangeApn(context);
			CLog.i("info", "apn :" + list);
			return list;
		}
		return null;
	}

	/**
	 * 当当前的apn接入点不是发彩信所需要的wap时，就需要切换
	 * 
	 * @param context
	 * @return
	 */
	private static boolean shouldChangeApn(final Context context) {

		final String wapId = getWapApnId(context);
		String apnId = getApn(context);
		// 若当前apn不是wap，则切换至wap
		if (!wapId.equals(apnId)) {
			APN_NET_ID = apnId;
			setApn(context, wapId);
			// 切换apn需要一定时间，先让等待2秒
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/**
	 * 所有的apn信息都是存在数据表里，
	 * 
	 * 可以通过adb pull data/data/com.android.providers.telephony
	 * 
	 * d:/ 直接拷出来，不过前提是手机有root权限
	 * 
	 * @param context
	 * @return
	 */
	private static String getApn(Context context) {
		ContentResolver resoler = context.getContentResolver();
		String[] projection = new String[] { "_id" };
		Cursor cur = resoler.query(
				Uri.parse("content://telephony/carriers/preferapn"),
				projection, null, null, null);
		String apnId = null;
		if (cur != null && cur.moveToFirst()) {
			do {
				apnId = cur.getString(cur.getColumnIndex("_id"));
			} while (cur.moveToNext());
		}
		return apnId;
	}

	/**
	 * 设置接入点
	 * 
	 * @param id
	 */
	private static void setApn(Context context, String id) {
		try {
			Uri uri = Uri.parse("content://telephony/carriers/preferapn");
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			values.put("apn_id", id);
			resolver.update(uri, values, null, null);
		} catch (Exception e) {
			CLog.i("info", "设置Apn失败，" + e.getMessage());
			e.printStackTrace();
		}
	}

	private static String APN_NET_ID = null;

	/**
	 * 取到wap接入點的id
	 * 
	 * @return
	 */
	private static String getWapApnId(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		String[] projection = new String[] { "_id", "proxy" };
		Cursor cur = contentResolver.query(
				Uri.parse("content://telephony/carriers"), projection,
				"current = 1", null, null);
		if (cur != null && cur.moveToFirst()) {
			do {
				String id = cur.getString(0);
				String proxy = cur.getString(1);
				if (!TextUtils.isEmpty(proxy)) {
					return id;
				}
			} while (cur.moveToNext());
		}
		return null;
	}

	private static final String HDR_KEY_ACCEPT = "Accept";
	private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
	private static final String HDR_VALUE_ACCEPT = "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
	private static String HDR_VALUE_ACCEPT_LANGUAGE = "zh-cn, zh;q=1.0,en;q=0.5";

	/**
	 * 处理发彩信请求
	 * 
	 * @param list
	 * @param context
	 * @param pdu
	 * @return
	 * @throws Exception
	 */
	public static boolean sendMMMS(List<String> list, final Context context,
			byte[] pdu) throws Exception {
		if (list == null) {
			return false;
		}
		String mmsUrl = (String) list.get(0);
		String mmsProxy = (String) list.get(1);
		HttpClient client = null;
		try {
			URI hostUrl = new URI(mmsUrl);
			HttpHost target = new HttpHost(hostUrl.getHost(),
					hostUrl.getPort(), HttpHost.DEFAULT_SCHEME_NAME);
			client = AndroidHttpClient.newInstance("Android-Mms/2.0");
			HttpPost post = new HttpPost(mmsUrl);
			ByteArrayEntity entity = new ByteArrayEntity(pdu);
			entity.setContentType("application/vnd.wap.mms-message");
			post.setEntity(entity);
			post.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
			post.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);
			HttpParams params = client.getParams();
			HttpProtocolParams.setContentCharset(params, "UTF-8");
			ConnRouteParams.setDefaultProxy(params, new HttpHost(mmsProxy, 80));
			CLog.i("info", "开始请求 ");
			HttpResponse response = client.execute(target, post);
			StatusLine status = response.getStatusLine();
			CLog.i("info", "status : " + status.getStatusCode());
			if (status.getStatusCode() != 200) {
				throw new IOException("HTTP error: " + status.getReasonPhrase());
			}
			// 彩信发送完毕后检查是否需要把接入点切换回来
			if (null != APN_NET_ID) {
				setApn(context, APN_NET_ID);
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			CLog.d("info", "彩信发送失败：" + e.getMessage());
			// 发送失败处理
		}
		return false;
	}

	/**
	 * 发彩信接口
	 * 
	 * @param context
	 * @param phone
	 *            手机号
	 * @param subject
	 *            主题
	 * @param text
	 *            文字
	 * @param imagePath
	 *            图片路径
	 * @param audioPath
	 *            音频路径
	 */
	public static void send(final Context context, String phone,
			String subject, String text, String imagePath, String audioPath) {
		CLog.i("MmsTestActivity", "测试彩信" + subject);

		SendReq sendRequest = new SendReq();
		EncodedStringValue[] sub = EncodedStringValue.extract(subject);
		if (sub != null && sub.length > 0) {
			sendRequest.setSubject(sub[0]);
		}
		EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(phone);
		if (phoneNumbers != null && phoneNumbers.length > 0) {
			sendRequest.addTo(phoneNumbers[0]);
		}
		PduBody pduBody = new PduBody();
		if (!TextUtils.isEmpty(text)) {
			PduPart partPdu3 = new PduPart();
			partPdu3.setCharset(CharacterSets.UTF_8);
			partPdu3.setName("mms_text.txt".getBytes());
			partPdu3.setContentType("text/plain".getBytes());
			partPdu3.setData(text.getBytes());
			pduBody.addPart(partPdu3);
		}
		if (!TextUtils.isEmpty(imagePath)) {
			PduPart partPdu = new PduPart();
			partPdu.setCharset(CharacterSets.UTF_8);
			partPdu.setName("camera.jpg".getBytes());
			partPdu.setContentType("image/png".getBytes());
			partPdu.setDataUri(Uri
					.parse("http://up.2cto.com/2012/0414/20120414101424728.jpg"));
			partPdu.setDataUri(Uri.fromFile(new File(imagePath)));
			pduBody.addPart(partPdu);
		}
		if (!TextUtils.isEmpty(audioPath)) {
			PduPart partPdu2 = new PduPart();
			partPdu2.setCharset(CharacterSets.UTF_8);
			partPdu2.setName("speech_test.amr".getBytes());
			partPdu2.setContentType("audio/amr".getBytes());
			// partPdu2.setContentType("audio/amr-wb".getBytes());
			//
			partPdu2.setDataUri(Uri
					.parse("file://mnt//sdcard//.lv//audio//1326786209801.amr"));
			partPdu2.setDataUri(Uri.fromFile(new File(audioPath)));
			pduBody.addPart(partPdu2);
		}

		sendRequest.setBody(pduBody);
		final PduComposer composer = new PduComposer(context, sendRequest);
		final byte[] bytesToSend = composer.make();
		final List<String> list = getSimMNC(context);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// 因为在切换apn过程中需要一定时间，所以需要加上一个重试操作
				int retry = 0;
				while (retry < 5) {
					CLog.d("info", "重试次数：" + (retry + 1));
					retry++;
					try {
						if (sendMMMS(list, context, bytesToSend)) {
							myHandler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(context, "彩信发送成功！",
											Toast.LENGTH_LONG).show();
								}
							});
							return;
						}
						Thread.sleep(2000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				myHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, "彩信发送失败！", Toast.LENGTH_LONG)
								.show();
					}
				});
			}
		});
		t.start();

	}

	/**
	 * 发送音频彩信
	 * 
	 * @param context
	 * @param phone
	 *            电话号码
	 * @param subject
	 *            主题
	 * @param text
	 *            文本
	 * @param audioPath
	 *            音频路径
	 */
	public boolean send(final Context context, String phone, String subject,
			String text, String audioPath) {
		CLog.i("MmsTestActivity", "测试彩信" + subject);

		SendReq sendRequest = new SendReq();
		EncodedStringValue[] sub = EncodedStringValue.extract(subject);
		if (sub != null && sub.length > 0) {
			sendRequest.setSubject(sub[0]);
		}
		EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(phone);
		if (phoneNumbers != null && phoneNumbers.length > 0) {
			sendRequest.addTo(phoneNumbers[0]);
		}
		PduBody pduBody = new PduBody();
		if (!TextUtils.isEmpty(text)) {
			PduPart partPdu3 = new PduPart();
			partPdu3.setCharset(CharacterSets.UTF_8);
			partPdu3.setName("mms_text.txt".getBytes());
			partPdu3.setContentType("text/plain".getBytes());
			partPdu3.setData(text.getBytes());
			pduBody.addPart(partPdu3);
		}
		if (!TextUtils.isEmpty(audioPath)) {
			PduPart partPdu2 = new PduPart();
			partPdu2.setCharset(CharacterSets.UTF_8);
			partPdu2.setName("sosRecord.amr".getBytes());
			partPdu2.setContentType("audio/amr".getBytes());
			partPdu2.setDataUri(Uri.fromFile(new File(audioPath)));
			pduBody.addPart(partPdu2);
		}
		sendRequest.setBody(pduBody);
		final PduComposer composer = new PduComposer(context, sendRequest);
		final byte[] bytesToSend = composer.make();
		final List<String> list = getSimMNC(context);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// 因为在切换apn过程中需要一定时间，所以需要加上一个重试操作
				int retry = 0;
				do {
					CLog.d("info", "重试次数：" + (retry + 1));
					try {
						isSend = sendMMMS(list, context, bytesToSend);
						// Thread.sleep(2000);
					} catch (Exception e) {
						e.printStackTrace();
						isSend = false;
					}
					retry++;
				} while (retry < 5);
			}
		});
		t.start();
		return isSend;
	}
}
