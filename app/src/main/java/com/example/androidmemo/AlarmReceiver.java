package com.example.androidmemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

// 1. 创建 AlarmManager 广播接收器
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm triggered");

        // 获取传递参数
        long time = intent.getLongExtra("time", 0);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int notificationId = intent.getIntExtra("notificationId", 0);

        // 发送通知
        sendNotification(context, notificationId, title, text);
    }

    private void sendNotification(Context context, int id, String title, String text) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建通知渠道（Android 8.0+ 必须）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "memo_channel",
                    "备忘录提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        Notification.Builder builder = new Notification.Builder(context, "memo_channel")
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT);

        manager.notify(id, builder.build());
    }
}

