package com.example.androidmemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取传递参数
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        // 添加空值检查
        if (title == null && text == null) {
            Log.e(TAG, "收到无效提醒: 标题或内容为空");
            return;
        }

        Log.d(TAG, "闹钟触发 - 接收广播");

        int notificationId = intent.getIntExtra("notificationId", 0);

        Log.d(TAG, "通知参数 - ID: " + notificationId + ", 标题: " + title + ", 内容: " + text);

        // 发送通知
        sendNotification(context, notificationId, title, text);
    }

    private void sendNotification(Context context, int id, String title, String text) {
        Log.d(TAG, "准备发送通知 - ID: " + id);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            Log.e(TAG, "NotificationManager为空，无法发送通知");
            return;
        }

        // 创建通知渠道（Android 8.0+ 必须）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "memo_channel",
                    "备忘录提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
            Log.d(TAG, "创建通知渠道: memo_channel");
        }

        try {
            Notification.Builder builder = new Notification.Builder(context, "memo_channel")
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_icon)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_DEFAULT);

            manager.notify(id, builder.build());
            Log.d(TAG, "通知发送成功 - ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "发送通知失败", e);
        }
    }
}