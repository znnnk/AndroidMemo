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

    private MyDBOpenHelper dbOpenHelper;
    private Button btnUpdate, btnCancel;
    private EditText taskEdit, dateEdit, timeEdit, remarkEdit;
    private CheckBox favoriteCheckBox;
    private Calendar newRemindDate = Calendar.getInstance();
    private boolean isFavorite = false;
    private String updateID;

    // 定义日期和时间格式化器
    private final SimpleDateFormat uiDateFormatter = new SimpleDateFormat("yyyy年MM月dd日");
    private final SimpleDateFormat uiTimeFormatter = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dbDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.update, container, false);
        btnUpdate = rootView.findViewById(R.id.btnUpdate);
        btnCancel = rootView.findViewById(R.id.btnUpdateCancel);
        taskEdit = rootView.findViewById(R.id.etUpdateTask);
        dateEdit = rootView.findViewById(R.id.etUpdateDate);
        timeEdit = rootView.findViewById(R.id.etUpdateTime);
        remarkEdit = rootView.findViewById(R.id.etUpdateRemark);
        favoriteCheckBox = rootView.findViewById(R.id.cbFavorite);

        if (getActivity() == null) {
            return rootView;
        }

        dbOpenHelper = new MyDBOpenHelper(getActivity().getApplicationContext());
        updateID = getArguments().getString("taskID");

        // 先加载任务数据再设置UI
        loadTaskData();

        // 设置收藏复选框状态和监听器
        favoriteCheckBox.setChecked(isFavorite);
        favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFavorite = isChecked;
                updateFavoriteStatus(updateID, isChecked);
            }
        });

        // 日期选择器
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        newRemindDate.set(year, month, day);
                        dateEdit.setText(uiDateFormatter.format(newRemindDate.getTime()));
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
                        newRemindDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        newRemindDate.set(Calendar.MINUTE, minute);
                        timeEdit.setText(uiTimeFormatter.format(newRemindDate.getTime()));
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
        SQLiteDatabase dbRead = dbOpenHelper.getReadableDatabase();
        Cursor result = null;
        try {
            result = dbRead.query("tb_ToDoItem", null, "_id=?",
                    new String[]{updateID}, null, null, null);

            if (result != null && result.moveToFirst()) {
                taskEdit.setText(result.getString(1)); // remindTitle
                remarkEdit.setText(result.getString(4)); // remindText

                // 获取收藏状态
                int favoriteIndex = result.getColumnIndex("isFavorite");
                if (favoriteIndex != -1) {
                    isFavorite = result.getInt(favoriteIndex) == 1;
                } else {
                    // 回退到默认列位置
                    isFavorite = result.getInt(7) == 1;
                }

                // 获取提醒时间
                int remindDateIndex = result.getColumnIndex("remindDate");
                if (remindDateIndex != -1) {
                    String remindDateStr = result.getString(remindDateIndex);
                    try {
                        Date dbRemindDate = dbDateFormatter.parse(remindDateStr);
                        if (dbRemindDate != null) {
                            newRemindDate.setTime(dbRemindDate);

                            // 关键修复：使用UI格式化器更新编辑框
                            dateEdit.setText(uiDateFormatter.format(dbRemindDate));
                            timeEdit.setText(uiTimeFormatter.format(dbRemindDate));
                        }
                    } catch (Exception e) {
                        Log.e("Update", "Error parsing remindDate", e);
                        // 使用当前时间作为默认值
                        newRemindDate.setTimeInMillis(System.currentTimeMillis());
                        dateEdit.setText(uiDateFormatter.format(new Date()));
                        timeEdit.setText(uiTimeFormatter.format(new Date()));
                    }
                } else {
                    // 如果remindDate列不存在，使用当前时间
                    newRemindDate.setTimeInMillis(System.currentTimeMillis());
                    dateEdit.setText(uiDateFormatter.format(new Date()));
                    timeEdit.setText(uiTimeFormatter.format(new Date()));
                }
            } else {
                Toast.makeText(getActivity(), "任务不存在", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        } finally {
            if (result != null) {
                result.close();
            }
            if (dbRead != null) {
                dbRead.close();
            }
        }
    }

    // 实时更新收藏状态到数据库
    private void updateFavoriteStatus(String taskId, boolean isFavorite) {
        try (SQLiteDatabase db = dbOpenHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("isFavorite", isFavorite ? 1 : 0);
            int rowsAffected = db.update("tb_ToDoItem", values, "_id=?", new String[]{taskId});

            if (rowsAffected > 0) {
                String message = isFavorite ? "已收藏" : "已取消收藏";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                favoriteCheckBox.setChecked(isFavorite);
            } else {
                Toast.makeText(getActivity(), "收藏状态更新失败", Toast.LENGTH_SHORT).show();
                favoriteCheckBox.setChecked(!isFavorite);
            }
        } catch (Exception e) {
            Log.e("Update", "Error updating favorite status", e);
            Toast.makeText(getActivity(), "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 更新整个任务
    private void updateTask(String taskId) {
        try (SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put("remindTitle", taskEdit.getText().toString());
            cv.put("modifyDate", dbDateFormatter.format(new Date()));
            cv.put("remindDate", dbDateFormatter.format(newRemindDate.getTime()));
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
                Toast.makeText(getActivity(), "任务更新成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "任务更新失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Update", "Error updating task", e);
            Toast.makeText(getActivity(), "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            getFragmentManager().popBackStack();
        }
    }
}