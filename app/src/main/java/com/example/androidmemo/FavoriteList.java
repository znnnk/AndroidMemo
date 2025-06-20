package com.example.androidmemo;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FavoriteList extends BaseFragment {
    public FavoriteList() {
    }

    private SQLiteDatabase dbRead;
    private ListView listFavorite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.favorite_list, container, false);
        TextView tvTitle = (TextView) rootView.findViewById(R.id.tvFavoriteTitle);
        tvTitle.setText("收藏的任务");

        listFavorite = (ListView) rootView.findViewById(R.id.listFavorite);

        // 初始化公共资源
        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        starFilled = getResources().getDrawable(R.drawable.ic_star_filled);
        starOutline = getResources().getDrawable(R.drawable.ic_star_outline);

        dbRead = dbOpenHelper.getReadableDatabase();

        readFavoriteList();
        return rootView;
    }

    protected void readFavoriteList() {
        ArrayList taskList = new ArrayList<HashMap<String, String>>();
        // 查询所有收藏的任务
        Cursor result = dbRead.query("tb_ToDoItem", new String[]{
                        "_id", "remindTitle", "remindText", "remindDate", "haveDo", "isFavorite"},
                "isFavorite=?", new String[]{"1"}, null, null, "remindDate ASC", null);

        while (result.moveToNext()) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put("_id", String.valueOf(result.getInt(0)));
            temp.put("remindTitle", result.getString(1));
            temp.put("remindDate", "提醒时间：" + result.getString(3));
            temp.put("remindText", result.getString(2));
            temp.put("taskHaveDo", result.getInt(4) == 0 ? "×未处理" : "√已处理");
            temp.put("isFavorite", String.valueOf(result.getInt(5)));
            taskList.add(temp);
        }

        // 使用自定义的SimpleAdapter实现收藏功能
        final SimpleAdapter listViewAdapter = new SimpleAdapter(
                getActivity(),
                taskList,
                R.layout.remind_list_item,
                new String[]{"remindDate", "remindTitle", "remindText", "taskHaveDo"},
                new int[]{R.id.remind_listitem_remindDate, R.id.remind_listitem_taskTitle, R.id.remind_listitem_taskText, R.id.remind_listitem_haveDo}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                final String taskId = item.get("_id");
                final String isFavorite = item.get("isFavorite");
                ImageView starIcon = view.findViewById(R.id.starIcon);
                if ("1".equals(isFavorite)) {
                    starIcon.setImageDrawable(starFilled);
                } else {
                    starIcon.setImageDrawable(starOutline);
                }
                starIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleStarStatus(taskId, starIcon);
                        // 刷新列表
                        readFavoriteList();
                    }
                });
                return view;
            }
        };

        listFavorite.setAdapter(listViewAdapter);
//        return rootView;
    }
}