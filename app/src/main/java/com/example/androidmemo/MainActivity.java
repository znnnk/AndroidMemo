package com.example.androidmemo;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1002;
    private MyDBOpenHelper dbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbOpenHelper = new MyDBOpenHelper(this);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new RemindList())
                    .commit();
        }
        this.setTitle("备忘录");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "通知权限已授予");
            } else {
                Toast.makeText(this, "需要通知权限才能显示提醒", Toast.LENGTH_LONG).show();
            }
        }
    }

    //添加menu菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();  //获得menu容器
        inflater.inflate(R.menu.menu, menu);//用menu.xml填充menu容器
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Add())
                    .commit();
            return true;
        } else if (id == R.id.menu_clear) {
            showClearAll();
            return true;
        } else if (id == R.id.menu_exit) {
            showExit();
            return true;
        } else if (id == R.id.menu_first) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RemindList())
                    .commit();
            return true;
        } else if (id == R.id.menu_undo) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UndoList())
                    .commit();
            return true;
        } else if (id == R.id.menu_done) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DoneList())
                    .commit();
            return true;
        } else if (id == R.id.menu_favorite) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FavoriteList())
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showClearAll() {
        new AlertDialog.Builder(MainActivity.this).setTitle("警告")
                .setMessage("数据删除之后将无法恢复！！\n您确定要删除全部事项吗?")
                .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SQLiteDatabase dbWriter = dbOpenHelper.getWritableDatabase();
                        dbWriter.delete("tb_ToDoItem", null, null);
                        Toast.makeText(getApplicationContext(), "数据已经全部删除！", Toast.LENGTH_SHORT).show();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new RemindList())
                                .commit();
                        dbWriter.close();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void showExit() {
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(MainActivity.this);
        exitAlert.setTitle("警告");
        exitAlert.setMessage("您确定要退出吗?");
        exitAlert.setNeutralButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                MainActivity.this.finish();
            }
        });
        exitAlert.setNegativeButton("取消", null);
        exitAlert.create();
        exitAlert.show();
    }
}