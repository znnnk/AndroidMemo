package com.example.androidmemo;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class BaseFragment extends Fragment {
    protected Drawable starFilled;
    protected Drawable starOutline;
    protected MyDBOpenHelper dbOpenHelper;

    // 公共的收藏状态切换方法
    protected void toggleStarStatus(String taskId, ImageView starIcon) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT isFavorite FROM tb_ToDoItem WHERE _id = ?",
                new String[]{taskId}
        );

        int newStatus = 0;
        if (cursor.moveToFirst()) {
            int currentStatus = cursor.getInt(0);
            newStatus = (currentStatus == 1) ? 0 : 1;

            ContentValues values = new ContentValues();
            values.put("isFavorite", newStatus);
            db.update("tb_ToDoItem", values, "_id=?", new String[]{taskId});
        }
        cursor.close();
//        db.close();

        // 更新图标状态（如果提供了视图）
        if (starIcon != null) {
            if (newStatus == 1) {
                starIcon.setImageDrawable(starFilled);
            } else {
                starIcon.setImageDrawable(starOutline);
            }
        }
    }
}