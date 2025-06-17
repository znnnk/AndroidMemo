package com.example.androidmemo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;

public class StartNotification {
    private static final String TAG = "StartNotification";

    public static void startTimeService(long timeInMillis, String title, String text, Context context) {
        Log.d(TAG, "开始设置时间服务 - 相对时间: " + timeInMillis + "ms, 标题: " + title);

        int notificationID = 0;
        MyDBOpenHelper dbHelper = new MyDBOpenHelper(context);

        try (SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
             Cursor result = dbRead.query("tb_Remind", new String[]{"notificationID"},
                     null, null, null, null, null, null)) {

            if (result != null && result.moveToFirst()) {
                notificationID = result.getInt(0);
                Log.d(TAG, "从数据库获取通知ID: " + notificationID);

                try (SQLiteDatabase dbWriter = dbHelper.getWritableDatabase()) {
                    ContentValues cv = new ContentValues();
                    cv.put("notificationID", notificationID + 1);
                    int updated = dbWriter.update("tb_Remind", cv, null, null);
                    Log.d(TAG, "更新通知ID为: " + (notificationID + 1) + ", 更新行数: " + updated);
                }
            } else {
                Log.w(TAG, "未在数据库中找到通知ID，使用默认值0");
            }
        } catch (Exception e) {
            Log.e(TAG, "数据库错误", e);
        }

        // 计算绝对触发时间
        long triggerAtMillis = System.currentTimeMillis() + timeInMillis;
        Log.d(TAG, "计算绝对触发时间: " + triggerAtMillis + " (" + new Date(triggerAtMillis) + ")");

        Intent intent = new Intent(context, TimeService.class);
        intent.putExtra("triggerAtMillis", triggerAtMillis);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("notificationId", notificationID);

        Log.d(TAG, "准备启动TimeService...");
        try {
            context.startService(intent);
            Log.d(TAG, "TimeService启动成功");
        } catch (Exception e) {
            Log.e(TAG, "启动TimeService失败", e);
        }
    }
}