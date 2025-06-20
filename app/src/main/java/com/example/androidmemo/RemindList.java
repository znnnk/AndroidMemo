// RemindList.java
package com.example.androidmemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RemindList extends BaseFragment  {

    public RemindList() {}
    private SQLiteDatabase dbRead;
    private ListView listToDoToday, listToDoTomorrow, listToDoAfterTomorrow,
            listToDoAfterAll, listExpired;
    private TextView tvToday, tvTomorrow, tvAfterTomorrow, tvAfterAll, tvExpired;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.remind_list, container, false);

        // 初始化文本视图
        tvToday = (TextView) rootView.findViewById(R.id.tvToday);
        tvTomorrow = (TextView) rootView.findViewById(R.id.tvTomorrow);
        tvAfterTomorrow = (TextView) rootView.findViewById(R.id.tvAfterTomorrow);
        tvAfterAll = (TextView) rootView.findViewById(R.id.tvAfterAll);
        tvExpired = (TextView) rootView.findViewById(R.id.tvExpired);

        // 初始化列表视图
        listToDoToday = (ListView) rootView.findViewById(R.id.listToDoToday);
        listToDoTomorrow = (ListView) rootView.findViewById(R.id.listToDoTomorrow);
        listToDoAfterTomorrow = (ListView) rootView.findViewById(R.id.listToDoAfterTomorrow);
        listToDoAfterAll = (ListView) rootView.findViewById(R.id.listToDoAfterAll);
        listExpired = (ListView) rootView.findViewById(R.id.listExpired);

        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        starFilled = getResources().getDrawable(R.drawable.ic_star_filled);
        starOutline = getResources().getDrawable(R.drawable.ic_star_outline);

        // 设置日期显示
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy年MM月dd日");
        long currentTime = System.currentTimeMillis();
        tvToday.setText(dateFormatter.format(new Date(currentTime)));
        tvTomorrow.setText(dateFormatter.format(new Date(currentTime + 86400000)));
        tvAfterTomorrow.setText(dateFormatter.format(new Date(currentTime + 86400000 * 2)));
        tvAfterAll.setText(dateFormatter.format(new Date(currentTime + 86400000 * 3)) + "之后");
        tvExpired.setText("今天之前"); // 设置过期分组标题

        // 获取可读数据库
        dbRead = dbOpenHelper.getReadableDatabase();

        // 加载所有任务列表
        readToDoList(new Date(currentTime), listToDoToday, 0);
        readToDoList(new Date(currentTime + 86400000), listToDoTomorrow, 0);
        readToDoList(new Date(currentTime + 86400000 * 2), listToDoAfterTomorrow, 0);
        readToDoList(new Date(currentTime + 86400000 * 3), listToDoAfterAll, 1);
        readExpiredTasks(listExpired); // 过期任务

        return rootView;
    }

    // 新增方法：读取过期任务
    protected void readExpiredTasks(ListView expiredList) {
        SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = dayFormatter.format(new Date());

        ArrayList<HashMap<String, String>> taskList = new ArrayList<>();

        // 查询所有过期任务（提醒日期在今天之前）
        Cursor result = dbRead.query("tb_ToDoItem",
                new String[]{"_id", "remindTitle", "remindText", "remindDate", "haveDo", "isFavorite"},
                "substr(remindDate, 1, 10) < ?",
                new String[]{todayStr},
                null, null,
                "isFavorite DESC, remindDate DESC", null); // 按收藏优先，然后按提醒时间倒序

        while (result.moveToNext()) {
            HashMap<String, String> temp = new HashMap<>();
            temp.put("_id", String.valueOf(result.getInt(0)));
            temp.put("remindTitle", result.getString(1));
            temp.put("remindDate", "过期时间：" + result.getString(3)); // 显示完整日期时间
            temp.put("remindText", result.getString(2));
            temp.put("taskHaveDo", result.getInt(4) == 0 ? "×未处理" : "√已处理");
            temp.put("isFavorite", String.valueOf(result.getInt(5)));
            taskList.add(temp);
        }
        result.close();

        // 创建适配器并设置监听器
        createAdapterAndSetListeners(taskList, expiredList);
    }

    protected void readToDoList(Date toDoDay, ListView toDoList, int listType) {
        SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<HashMap<String, String>> taskList = new ArrayList<>();

        // 获取基准日期字符串
        String baseDateStr = dayFormatter.format(toDoDay);
        String threeDaysLaterStr = dayFormatter.format(new Date(toDoDay.getTime()));

        Cursor result = null;
        try {
            // 查询数据库并按优先级排序
            result = dbRead.query("tb_ToDoItem",
                    new String[]{"_id", "remindTitle", "remindText", "remindDate", "haveDo", "isFavorite"},
                    null, null, null, null,
                    "isFavorite DESC, remindDate ASC", null);

            if (result != null) {
                while (result.moveToNext()) {
                    String remindDateStr = result.getString(3);
                    String remindDateOnly = remindDateStr.substring(0, 10);

                    // 今日/明日/后日列表
                    if (listType == 0) {
                        if (remindDateOnly.equals(baseDateStr)) {
                            addTaskToList(result, taskList, false);
                        }
                    }
                    // 三天后列表
                    else if (listType == 1) {
                        try {
                            // 使用字符串比较日期
                            if (remindDateOnly.compareTo(threeDaysLaterStr) >= 0) {
                                addTaskToList(result, taskList, true);
                            }
                        } catch (Exception e) {
                            Log.e("DateCompare", "日期比较错误: " + e.getMessage());
                        }
                    }
                }
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }

        // 创建适配器并设置监听器
        createAdapterAndSetListeners(taskList, toDoList);
    }

    private void addTaskToList(Cursor cursor, ArrayList<HashMap<String, String>> taskList, boolean showFullDate) {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("_id", String.valueOf(cursor.getInt(0)));
        temp.put("remindTitle", cursor.getString(1));

        // 根据类型决定显示完整日期还是仅时间
        String timeDisplay = showFullDate ?
                "提醒时间：" + cursor.getString(3) :
                "提醒时间：" + cursor.getString(3).substring(11);

        temp.put("remindDate", timeDisplay);
        temp.put("remindText", cursor.getString(2));
        temp.put("taskHaveDo", cursor.getInt(4) == 0 ? "×未处理" : "√已处理");
        temp.put("isFavorite", String.valueOf(cursor.getInt(5)));
        taskList.add(temp);
    }

    private void createAdapterAndSetListeners(final ArrayList<HashMap<String, String>> taskList, final ListView listView) {
        final SimpleAdapter listViewAdapter = new SimpleAdapter(
                getActivity(),
                taskList,
                R.layout.remind_list_item,
                new String[]{"remindDate", "remindTitle", "remindText", "taskHaveDo"},
                new int[]{R.id.remind_listitem_remindDate, R.id.remind_listitem_taskTitle,
                        R.id.remind_listitem_taskText, R.id.remind_listitem_haveDo}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                final String taskId = item.get("_id");
                final String isFavorite = item.get("isFavorite");
                ImageView starIcon = view.findViewById(R.id.starIcon);
                starIcon.setImageDrawable("1".equals(isFavorite) ? starFilled : starOutline);

                starIcon.setOnClickListener(v -> toggleStarStatus(taskId, starIcon));
                return view;
            }
        };

        listView.setAdapter(listViewAdapter);
        setListViewHeight(listView);

        // 短按查看详情
        listView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> item = (HashMap<String, String>) listViewAdapter.getItem(position);
            showTaskDetailsDialog(item.get("_id"));
        });

        // 长按删除
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            HashMap<String, String> item = (HashMap<String, String>) listViewAdapter.getItem(position);
            showDeleteConfirmationDialog(item.get("_id"), item.get("remindTitle"));
            return true;
        });
    }

    private void showTaskDetailsDialog(final String taskID) {
        Cursor result = dbRead.query("tb_ToDoItem", null, "_id=?", new String[]{taskID}, null, null, null);
        if (result.moveToFirst()) {
            StringBuilder details = new StringBuilder();
            details.append("标题：").append(result.getString(1)).append("\n");
            details.append("创建时间：").append(result.getString(2)).append("\n");
            details.append("最后修改：").append(result.getString(3)).append("\n");
            details.append("内容：").append(result.getString(4)).append("\n");
            details.append("提醒时间：").append(result.getString(5)).append("\n");
            details.append(result.getInt(6) == 0 ? "×未处理" : "√已处理");

            new AlertDialog.Builder(getActivity())
                    .setTitle("详细信息")
                    .setMessage(details.toString())
                    .setNegativeButton("设为已处理", (dialog, which) -> updateTaskStatus(taskID))
                    .setNeutralButton("修改内容", (dialog, which) -> openUpdateFragment(taskID))
                    .create()
                    .show();
        }
        result.close();
    }

    private void updateTaskStatus(String taskID) {
        SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("haveDo", 1);
        dbWriter.update("tb_ToDoItem", cv, "_id=?", new String[]{taskID});
        dbWriter.close();
        refreshFragment();
    }

    private void openUpdateFragment(String taskID) {
        Bundle bundle = new Bundle();
        bundle.putString("taskID", taskID);
        Update updateFragment = new Update();
        updateFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, updateFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmationDialog(final String taskID, String title) {
        new AlertDialog.Builder(getActivity())
                .setTitle("警告")
                .setMessage("删除待办事项？\n\n标题：" + title)
                .setPositiveButton("删除", (dialog, which) -> deleteTask(taskID))
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void deleteTask(String taskID) {
        SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
        dbWriter.delete("tb_ToDoItem", "_id=?", new String[]{taskID});
        dbWriter.close();
        refreshFragment();
    }

    private void refreshFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RemindList())
                .commit();
    }

    public static void setListViewHeight(ListView listview) {
        ListAdapter adapter = listview.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listview);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listview.getLayoutParams();
        params.height = totalHeight + (listview.getDividerHeight() * (listview.getCount() - 1));
        listview.setLayoutParams(params);
    }
}