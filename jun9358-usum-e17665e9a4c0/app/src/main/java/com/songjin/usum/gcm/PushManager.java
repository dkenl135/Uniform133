package com.songjin.usum.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.songjin.usum.R;
import com.songjin.usum.controllers.activities.BaseActivity;
import com.songjin.usum.controllers.activities.MainActivity;
import com.songjin.usum.controllers.fragments.SettingFragment;
import com.songjin.usum.entities.AlarmEntity;
import com.songjin.usum.managers.RequestManager;
import com.songjin.usum.socketIo.SocketIO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class PushManager {
    public static final int PUSH_TYPE_TRANSACTION = 1;
    public static final int PUSH_TYPE_TIMELINE = 2;
    public static final int PUSH_TYPE_RESERVATION = 3;
    public static final int PUSH_TYPE_SCHOOL_RANK_UPDATED = 4;

    private static final String TAG = "PushManager";

    public static void sendTransactionPush(ArrayList<String> userIds, String msg) {
        for (String id :
                userIds) {
            SocketIO.sendGcm(id, msg, new RequestManager.OnSetToken() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onException() {
                }
            });
        }
    }



    public static void registerToken(final String uuid, final String deviceId, final String pushType, final String token) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://kapi.kakao.com/v1/push/register");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("charset", "utf-8");
//                    conn.setRequestProperty("Authorization", "KakaoAK cfa39b812b10bafebb44ffc2898a0169");
                    conn.setRequestProperty("Authorization", "KakaoAK a6a1e884f2ecbbb1dcf154a7e49e40f0");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    String urlParameters = "uuid="+ uuid + "&device_id=" + deviceId + "&push_type=" + pushType + "&push_token=" + token;
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(urlParameters);
                    writer.flush();
                    writer.close();


                    int code = conn.getResponseCode();
                    String message = conn.getResponseMessage();

                    InputStreamReader in;
                    if (code == 200)
                        in = new InputStreamReader(conn.getInputStream());
                    else
                        in = new InputStreamReader(conn.getErrorStream());
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    BufferedReader reader = new BufferedReader(in);

                    while ((inputLine = reader.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void deregisterToken(final String uuid, final String deviceId, final String pushType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://kapi.kakao.com/v1/push/deregister");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setRequestProperty("Authorization", "KakaoAK cfa39b812b10bafebb44ffc2898a0169");
//                    conn.setRequestProperty("Authorization", "KakaoAK a6a1e884f2ecbbb1dcf154a7e49e40f0");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    String urlParameters = "uuid="+ uuid + "&device_id=" + deviceId + "&push_type=" + pushType;
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(urlParameters);
                    writer.flush();
                    writer.close();

//                    StringBuilder builder = new StringBuilder();
//                    builder.append("uuids").append("=").append(array.toString()).append("&");
//                    builder.append("push_message").append("=").append(json);
//
//                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
//                    pw.write(builder.toString());
//                    pw.flush();
//                    pw.close();

                    int code = conn.getResponseCode();
                    String message = conn.getResponseMessage();

                    InputStreamReader in;
                    if (code == 200)
                        in = new InputStreamReader(conn.getInputStream());
                    else
                        in = new InputStreamReader(conn.getErrorStream());
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    BufferedReader reader = new BufferedReader(in);

                    while ((inputLine = reader.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void searchToken(final String uuid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://kapi.kakao.com/v1/push/tokens");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setRequestProperty("Authorization", "KakaoAK cfa39b812b10bafebb44ffc2898a0169");
//                    conn.setRequestProperty("Authorization", "KakaoAK a6a1e884f2ecbbb1dcf154a7e49e40f0");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    String urlParameters = "uuid="+ uuid;
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(urlParameters);
                    writer.flush();
                    writer.close();

//                    StringBuilder builder = new StringBuilder();
//                    builder.append("uuids").append("=").append(array.toString()).append("&");
//                    builder.append("push_message").append("=").append(json);
//
//                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
//                    pw.write(builder.toString());
//                    pw.flush();
//                    pw.close();

                    int code = conn.getResponseCode();
                    String message = conn.getResponseMessage();

                    InputStreamReader in;
                    if (code == 200)
                        in = new InputStreamReader(conn.getInputStream());
                    else
                        in = new InputStreamReader(conn.getErrorStream());
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    BufferedReader reader = new BufferedReader(in);

                    while ((inputLine = reader.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendReservationPushToMe(String msg) {
        generateNotification(BaseActivity.context, new AlarmEntity(msg, PUSH_TYPE_RESERVATION));
    }

    public static void sendSchoolRankUpdatedPushToMe(String msg) {
        generateNotification(BaseActivity.context, new AlarmEntity(msg, PUSH_TYPE_SCHOOL_RANK_UPDATED));
    }

    public static void generateNotification(Context context, AlarmEntity msg) {
        String alert = "";
        if (!TextUtils.isEmpty(msg.message)) {
            alert = msg.message.replace("\\r\\n", "\n");
        }

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context).setWhen(when)
                .setSmallIcon(icon).setContentTitle(context.getString(R.string.app_name))
                .setContentText(alert).setContentIntent(intent).setTicker(alert)
                .setAutoCancel(true).getNotification();

        notificationManager.notify(0, notification);

        msg.timestamp = System.currentTimeMillis();
        SettingFragment.addReceivedPushMessage(msg);
    }
}
