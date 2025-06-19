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

        // 更新图标状态（如果提供了视图）
        if (starIcon != null) {
            if (newStatus == 1) {
                starIcon.setImageDrawable(starFilled);
            } else {
                starIcon.setImageDrawable(starOutline);
            }
        }

        refreshCurrentFragment();
    }

    // 新增方法：刷新当前Fragment
    protected void refreshCurrentFragment() {
        if (getActivity() != null) {
            Fragment currentFragment = getActivity().getFragmentManager()
                    .findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof RemindList) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RemindList())
                        .commit();
            } else if (currentFragment instanceof FavoriteList) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FavoriteList())
                        .commit();
            } else if (currentFragment instanceof UndoList) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new UndoList())
                        .commit();
            } else if (currentFragment instanceof DoneList) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DoneList())
                        .commit();
            }
        }
    }
}