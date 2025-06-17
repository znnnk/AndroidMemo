package com.example.androidmemo;

import static android.app.Service.START_NOT_STICKY;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;

public class TimeService extends Service {
    private static final String TAG = "TimeService";
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务创建");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand 开始执行");

        if (intent == null) {
            Log.e(TAG, "接收到的Intent为null");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 获取参数
        long triggerAtMillis = intent.getLongExtra("triggerAtMillis", 0);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int notificationId = intent.getIntExtra("notificationId", 0);

        Log.d(TAG, "接收参数 - 触发时间: " + triggerAtMillis + " (" + new Date(triggerAtMillis) + ")");
        Log.d(TAG, "标题: " + title + ", 内容: " + text + ", 通知ID: " + notificationId);

        // 验证参数
        if (triggerAtMillis <= 0 || title == null || text == null) {
            Log.e(TAG, "参数无效: 触发时间=" + triggerAtMillis + ", 标题=" + title);
            stopSelf();
            return START_NOT_STICKY;
        }

        // 创建 PendingIntent
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction("com.example.androidmemo.ALARM_ACTION");
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("text", text);
        alarmIntent.putExtra("notificationId", notificationId);

        // 添加这行确保 Intent 的唯一性
        alarmIntent.setData(Uri.parse("content://" + System.currentTimeMillis()));

        PendingIntent pendingIntent = null;

        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要显式声明可变性
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0-11 使用不可变标志
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            // 旧版 Android
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        try {
            pendingIntent = PendingIntent.getBroadcast(
                    this,
                    notificationId, // 唯一ID
                    alarmIntent,
                    flags
            );
            Log.d(TAG, "PendingIntent创建成功");
        } catch (Exception e) {
            Log.e(TAG, "创建PendingIntent失败", e);

            // 添加详细的错误日志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.e(TAG, "Android 12+ 可能需要 FLAG_MUTABLE 标志");
            }

            // 尝试备选方案 - 使用 FLAG_MUTABLE
            try {
                Log.w(TAG, "尝试使用 FLAG_MUTABLE 创建PendingIntent");
                int mutableFlags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    mutableFlags |= PendingIntent.FLAG_MUTABLE;
                }

                pendingIntent = PendingIntent.getBroadcast(
                        this,
                        notificationId,
                        alarmIntent,
                        mutableFlags
                );
                Log.w(TAG, "备选方案成功 - 使用 FLAG_MUTABLE");
            } catch (Exception ex) {
                Log.e(TAG, "备选方案也失败", ex);
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        // 设置闹钟前的日志
        Log.d(TAG, "准备设置闹钟...");
        Log.d(TAG, "AlarmManager状态: " + (alarmManager != null ? "已初始化" : "未初始化"));
        Log.d(TAG, "PendingIntent状态: " + (pendingIntent != null ? "有效" : "无效"));

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager为空，无法设置闹钟");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 设置闹钟
        try {
            Log.d(TAG, "开始设置闹钟 - 触发时间: " + new Date(triggerAtMillis));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d(TAG, "检测Android 12+ 精确闹钟权限");
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "有精确闹钟权限，使用setExactAndAllowWhileIdle");
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                } else {
                    Log.w(TAG, "缺少精确闹钟权限，使用set方法回退");
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "使用setExactAndAllowWhileIdle (Android M+)");
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d(TAG, "使用setExact (Android KITKAT+)");
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            } else {
                Log.d(TAG, "使用set (旧版Android)");
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }

            Log.d(TAG, "闹钟设置成功! 触发时间: " + new Date(triggerAtMillis));
        } catch (SecurityException e) {
            Log.e(TAG, "设置闹钟时权限错误", e);
            Log.e(TAG, "缺少必要权限: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "设置闹钟时出错", e);
        }

        Log.d(TAG, "服务任务完成，准备停止服务");
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "服务销毁");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}