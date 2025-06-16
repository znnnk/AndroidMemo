package com.example.androidmemo;

import static android.app.Service.START_STICKY;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.androidmemo.AlarmReceiver;

import java.util.Date;

// 2. 修改 TimeService 使用 AlarmManager
public class TimeService extends Service {
    private static final String TAG = "TimeService";
    private static final int REQUEST_CODE = 123456;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        // 1. 获取参数
        long time = intent.getLongExtra("time", 0);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int notificationId = intent.getIntExtra("notificationId", 0);

        // 2. 验证参数
        if (time <= 0 || title == null || text == null) {
            Log.e(TAG, "Invalid parameters: time=" + time + ", title=" + title);
            return START_STICKY;
        }

        // 3. 创建 PendingIntent
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("time", time);
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("text", text);
        alarmIntent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 4. 设置闹钟（使用 ELAPSED_REALTIME_WAKEUP）
        long triggerTime = SystemClock.elapsedRealtime() + time;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }

        Log.d(TAG, "Alarm set for: " + new Date(triggerTime));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
