package com.example.androidmemo;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Update extends Fragment {

    public Update() {
    }

    private SQLiteDatabase dbRead;
    private MyDBOpenHelper dbOpenHelper;
    private Button btnUpdate, btnCancel;
    private EditText taskEdit, dateEdit, timeEdit, remarkEdit;
    private CheckBox favoriteCheckBox;
    private Date remindDate = new Date(System.currentTimeMillis());
    private Calendar newRemindDate = Calendar.getInstance();
    private boolean isFavorite = false;
    private String updateID; // 将updateID提升为类变量

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.update, container, false);
        btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);
        btnCancel = (Button) rootView.findViewById(R.id.btnUpdateCancel);
        taskEdit = (EditText) rootView.findViewById(R.id.etUpdateTask);
        dateEdit = (EditText) rootView.findViewById(R.id.etUpdateDate);
        timeEdit = (EditText) rootView.findViewById(R.id.etUpdateTime);
        remarkEdit = (EditText) rootView.findViewById(R.id.etUpdateRemark);
        favoriteCheckBox = (CheckBox) rootView.findViewById(R.id.cbFavorite);

        // 检查Activity是否存在
        if (getActivity() == null) {
            return rootView;
        }

        // 初始化数据库
        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        updateID = getArguments().getString("taskID"); // 存储到类变量

        // 加载任务数据
        loadTaskData();

        // 设置收藏复选框状态和监听器
        favoriteCheckBox.setChecked(isFavorite);
        favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 实时更新收藏状态
                isFavorite = isChecked;
                updateFavoriteStatus(updateID, isChecked);
            }
        });

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy年MM月dd日");
        final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

        // 设置初始日期和时间
        dateEdit.setText(dateFormatter.format(remindDate));
        timeEdit.setText(timeFormatter.format(remindDate));
        newRemindDate.setTime(remindDate);

        // 日期选择器
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        newRemindDate.set(year, month, day);
                        dateEdit.setText(dateFormatter.format(new Date(newRemindDate.getTimeInMillis())));
                    }
                }, newRemindDate.get(Calendar.YEAR), newRemindDate.get(Calendar.MONTH),
                        newRemindDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 时间选择器
        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        newRemindDate.set(newRemindDate.get(Calendar.YEAR),
                                newRemindDate.get(Calendar.MONTH),
                                newRemindDate.get(Calendar.DAY_OF_MONTH),
                                hourOfDay, minute);
                        timeEdit.setText(timeFormatter.format(new Date(newRemindDate.getTimeInMillis())));
                    }
                }, newRemindDate.get(Calendar.HOUR_OF_DAY),
                        newRemindDate.get(Calendar.MINUTE), true).show();
            }
        });

        // 取消按钮
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        // 更新按钮
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateTask(updateID);
            }
        });

        return rootView;
    }

    // 加载任务数据
    private void loadTaskData() {
        dbRead = dbOpenHelper.getReadableDatabase();
        Cursor result = null;
        try {
            result = dbRead.query("tb_ToDoItem", null, "_id=?",
                    new String[]{updateID}, null, null, null);
            if (result != null && result.moveToFirst()) {
                taskEdit.setText(result.getString(1));
                remarkEdit.setText(result.getString(4));

                // 确保正确获取收藏状态
                int favoriteIndex = result.getColumnIndex("isFavorite");
                if (favoriteIndex != -1) {
                    isFavorite = result.getInt(favoriteIndex) == 1;
                } else {
                    Log.e("Update", "isFavorite column not found");
                    // 回退到默认列位置
                    isFavorite = result.getInt(7) == 1; // 假设isFavorite在第8列
                }

                // 设置提醒日期
                int remindDateIndex = result.getColumnIndex("remindDate");
                if (remindDateIndex != -1) {
                    String remindDateStr = result.getString(remindDateIndex);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        remindDate = dateFormat.parse(remindDateStr);
                        newRemindDate.setTime(remindDate);
                    } catch (Exception e) {
                        Log.e("Update", "Error parsing remindDate", e);
                    }
                }
            } else {
                Toast.makeText(getActivity(), "任务不存在", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    // 实时更新收藏状态到数据库
    private void updateFavoriteStatus(String taskId, boolean isFavorite) {
        SQLiteDatabase db = null;
        try {
            db = dbOpenHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("isFavorite", isFavorite ? 1 : 0);
            int rowsAffected = db.update("tb_ToDoItem", values, "_id=?", new String[]{taskId});

            // 显示更新状态提示
            if (rowsAffected > 0) {
                String message = isFavorite ? "已收藏" : "已取消收藏";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                // 更新复选框状态以确保UI同步
                favoriteCheckBox.setChecked(isFavorite);
            } else {
                Toast.makeText(getActivity(), "收藏状态更新失败", Toast.LENGTH_SHORT).show();
                // 恢复之前的复选框状态
                favoriteCheckBox.setChecked(!isFavorite);
            }
        } catch (Exception e) {
            Log.e("Update", "Error updating favorite status", e);
            Toast.makeText(getActivity(), "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // 更新整个任务
    private void updateTask(String taskId) {
        SQLiteDatabase dbWriter = null;
        try {
            dbWriter = dbOpenHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("remindTitle", taskEdit.getText().toString());
            cv.put("modifyDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(System.currentTimeMillis()));
            cv.put("remindDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(newRemindDate.getTimeInMillis()));
            cv.put("remindText", remarkEdit.getText().toString());
            cv.put("isFavorite", isFavorite ? 1 : 0);

            int rowsAffected = dbWriter.update("tb_ToDoItem", cv, "_id=?", new String[]{taskId});

            if (rowsAffected > 0) {
                // 更新通知
                StartNotification.startTimeService(
                        newRemindDate.getTimeInMillis() - System.currentTimeMillis(),
                        taskEdit.getText().toString(),
                        remarkEdit.getText().toString(),
                        getActivity().getApplicationContext()
                );
            }
        } catch (Exception e) {
            Log.e("Update", "Error updating task", e);
            Toast.makeText(getActivity(), "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (dbWriter != null) {
                dbWriter.close();
            }
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbRead != null && dbRead.isOpen()) {
            dbRead.close();
        }
    }
}