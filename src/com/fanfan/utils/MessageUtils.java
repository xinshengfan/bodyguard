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
 * ���Ͷ��źͲ���
 * 
 * @author FANFAN
 * 
 */
public class MessageUtils {
	private SmsManager manager;
	private Context mContext;
	private TelephonyManager telemanager;
	/** ���ŷ������ */
	public static final String ACTION_MESSAGE_SENT = "com.action.fan.messagesent";
	/** ���ųɹ��ʹ� */
	public static final String ACTION_MESSAGE_RECEIVE = "com.action.fan.messagereceive";
	public static final String KEY_SENT_MESSAGE_NUMBER = "keysentmessagenumber";
	public static final String KEY_SENT_MESSAGE_CONTENT = "keysentmessagecontent";
	/** ���Ų�������url�������˿� */
	public static String mmscUrl_ct = "http://mmsc.vnet.mobi";
	public static String mmsProxy_ct = "10.0.0.200";
	/** �ƶ���������url�������˿� */
	public static String mmscUrl_cm = "http://mmsc.monternet.com";
	public static String mmsProxy_cm = "010.000.000.172";
	/** ��ͨ��������url�������˿� */
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
		CLog.i("info", "$$$$$$$$$$ �������� $$$$$$$$$+\n" + phoneInfo);
		phone_name = android.os.Build.MANUFACTURER;
		if ("xiaomi".equalsIgnoreCase(phone_name)) {
			messActivity_name = "com.android.mms.ui.NewMessageActivity";
		}
	}

	/**
	 * ���Ͷ���
	 * 
	 * @param phonenumber
	 * @param content
	 */
	public void sendMessage(String phonenumber, String content) {
		if (content.length() > 70) {
			// ���
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
	 * ����Ϣ������ɺ����ֶ������͵���Ϣ�������ݿ⣻
	 * 
	 * @param phoneaddress
	 * @param content
	 */
	public void saveMessageToDB(String phoneaddress, String content) {
		Uri uri = Uri.parse("content://sms/sent");
		ContentResolver cr = mContext.getContentResolver();
		// д�����ݿ�
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
			return "û��SIM��";
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return "��ҪNetworkPIN����";
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return "��ҪPIN����";
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return "��ҪPUK����";
		case TelephonyManager.SIM_STATE_UNKNOWN:
			return "δ֪״̬";
		case TelephonyManager.SIM_STATE_READY:
			return "sim������";
		default:
			break;
		}
		return "";
	}

	/**
	 * ���ò��ŷ��ͽ���
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
		intent.putExtra(Intent.EXTRA_STREAM, url);// uriΪ��ĸ�����uri
		intent.putExtra("subject", subject); // ���ŵ�����
		intent.putExtra("address", phonenumber); // ���ŷ���Ŀ�ĺ���
		intent.putExtra("sms_body", context); // ��������������
		intent.putExtra(Intent.EXTRA_TEXT, "it's EXTRA_TEXT");
		intent.setType("audio/*");// ���Ÿ�������
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
	 * apn������wap���������netʱ����Ϊ���ӳ�ʱ���޷����ͣ�
	 * 
	 * �����ڷ����ű���ͨ��wap�������ܷ��ͣ���ʹ����ϵͳ���淢����ʱ��
	 * 
	 * �������㲻��wap ������Զ��л�������������������л���ȥ���������ģ��ĺ��ģ�
	 * 
	 * ��ʵ���ǣ�����ڵ��÷�����ʱ���л�apn��wap����������Ϻ����л���ȥ������
	 * 
	 * @param context
	 *            ���ݲ�ͬ�ƶ���Ӧ�̣���Ҫ���ò�ͬ��url��proxy
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
				// ��Ϊ�ƶ�������46000�µ�IMSI�Ѿ����꣬����������һ��46002��ţ�134/159�Ŷ�ʹ���˴˱��
				// �й��ƶ�
				list.add(mmscUrl_cm);
				list.add(mmsProxy_cm);
			} else if (imsi.startsWith("46001")) {
				// �й���ͨ
				list.add(mmscUrl_uni);
				list.add(mmsProxy_uni);
			} else if (imsi.startsWith("46003")) {
				// �й�����
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
	 * ����ǰ��apn����㲻�Ƿ���������Ҫ��wapʱ������Ҫ�л�
	 * 
	 * @param context
	 * @return
	 */
	private static boolean shouldChangeApn(final Context context) {

		final String wapId = getWapApnId(context);
		String apnId = getApn(context);
		// ����ǰapn����wap�����л���wap
		if (!wapId.equals(apnId)) {
			APN_NET_ID = apnId;
			setApn(context, wapId);
			// �л�apn��Ҫһ��ʱ�䣬���õȴ�2��
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
	 * ���е�apn��Ϣ���Ǵ������ݱ��
	 * 
	 * ����ͨ��adb pull data/data/com.android.providers.telephony
	 * 
	 * d:/ ֱ�ӿ�����������ǰ�����ֻ���rootȨ��
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
	 * ���ý����
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
			CLog.i("info", "����Apnʧ�ܣ�" + e.getMessage());
			e.printStackTrace();
		}
	}

	private static String APN_NET_ID = null;

	/**
	 * ȡ��wap�����c��id
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
	 * ������������
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
			CLog.i("info", "��ʼ���� ");
			HttpResponse response = client.execute(target, post);
			StatusLine status = response.getStatusLine();
			CLog.i("info", "status : " + status.getStatusCode());
			if (status.getStatusCode() != 200) {
				throw new IOException("HTTP error: " + status.getReasonPhrase());
			}
			// ���ŷ�����Ϻ����Ƿ���Ҫ�ѽ�����л�����
			if (null != APN_NET_ID) {
				setApn(context, APN_NET_ID);
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			CLog.d("info", "���ŷ���ʧ�ܣ�" + e.getMessage());
			// ����ʧ�ܴ���
		}
		return false;
	}

	/**
	 * �����Žӿ�
	 * 
	 * @param context
	 * @param phone
	 *            �ֻ���
	 * @param subject
	 *            ����
	 * @param text
	 *            ����
	 * @param imagePath
	 *            ͼƬ·��
	 * @param audioPath
	 *            ��Ƶ·��
	 */
	public static void send(final Context context, String phone,
			String subject, String text, String imagePath, String audioPath) {
		CLog.i("MmsTestActivity", "���Բ���" + subject);

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
				// ��Ϊ���л�apn��������Ҫһ��ʱ�䣬������Ҫ����һ�����Բ���
				int retry = 0;
				while (retry < 5) {
					CLog.d("info", "���Դ�����" + (retry + 1));
					retry++;
					try {
						if (sendMMMS(list, context, bytesToSend)) {
							myHandler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(context, "���ŷ��ͳɹ���",
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
						Toast.makeText(context, "���ŷ���ʧ�ܣ�", Toast.LENGTH_LONG)
								.show();
					}
				});
			}
		});
		t.start();

	}

	/**
	 * ������Ƶ����
	 * 
	 * @param context
	 * @param phone
	 *            �绰����
	 * @param subject
	 *            ����
	 * @param text
	 *            �ı�
	 * @param audioPath
	 *            ��Ƶ·��
	 */
	public boolean send(final Context context, String phone, String subject,
			String text, String audioPath) {
		CLog.i("MmsTestActivity", "���Բ���" + subject);

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
				// ��Ϊ���л�apn��������Ҫһ��ʱ�䣬������Ҫ����һ�����Բ���
				int retry = 0;
				do {
					CLog.d("info", "���Դ�����" + (retry + 1));
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
