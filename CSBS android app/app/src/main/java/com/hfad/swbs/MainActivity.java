package com.hfad.swbs;


import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity {
    TextView warning;
    EditText editPin;
    EditText editName;
    private ProgressBar loginProgressBar;


    private websocket websocket;
    ContentValues values = new ContentValues();

    boolean login_result;
    MyDatabaseHelper database;

    private final BroadcastReceiver login_to_server_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "login_to_server_broadcast")) {

                login_result = intent.getBooleanExtra("result", true);
                Log.d("Server back result", String.valueOf(login_result));

                if (login_result) {
                    Intent intent_message = new Intent(MainActivity.this, messageActivity.class);
                    intent.putExtra("fragmentTag", "New_message");
                    startActivity(intent_message);
                } else {
                    loginProgressBar.setVisibility(View.INVISIBLE);
                    showReminder(MainActivity.this, "教室代碼已被使用", "很抱歉，您目前使用的教室代碼已被其他裝置使用，請使用其他代碼謝謝!");
                    database.writeClassNumber( "-2");
                    database.writeClassName( "-2");
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new MyDatabaseHelper(this);


        editPin = findViewById(R.id.classPin);
        editName = findViewById(R.id.editClassName);
        websocket = new websocket(this);
        loginProgressBar = findViewById(R.id.login_progressbar);
        // 檢查顯示在其他應用程式最上層設定，Android 11.0 若沒有設定則倒數會跳不出來
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+ getPackageName()));
            startActivityForResult(intent, 100);
        }

        // 檢查是否允許安裝不明來源的程式
        PackageManager packageManager = getPackageManager();
        if (!packageManager.canRequestPackageInstalls()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

//        SQLiteDatabase db = database.getReadableDatabase();
        String ClassNum = database.getClassNumber(null);

        if (!Objects.equals(ClassNum, "-1") && !Objects.equals(ClassNum, "null")) {
            database.close();
            Intent intent = new Intent(this, messageActivity.class);
            intent.putExtra("fragmentTag", "New_message");
            startActivity(intent);
        } else {
            Log.d("Class Number ", "There is no value in database");
        }

        editPin.setInputType(InputType.TYPE_CLASS_NUMBER);
        editPin.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(3)
        });
        editPin.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String userInput = editPin.getText().toString();
                    if (userInput.length() == 3) {
                        checkEditTexts();
                    } else {
                        showToast("請輸入三位教室代碼");
                    }
                    return true; // 返回 true 表示已處理此事件
                }
                return false; // 返回 false 表示未處理此事件，繼續處理其他事件
            }
        });

        editName.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(10)
        });
        editName.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String userInput = editPin.getText().toString();
                    if (userInput.length() <= 10 && userInput.length() > 0 && !TextUtils.isEmpty(userInput.trim())) {
                        Log.d("Enter pressed", "Get Class Name" + userInput );
                        checkEditTexts();
                    } else {
                        showToast("請輸入非空格之教室名稱");
                    }
                    return true; // 返回 true 表示已處理此事件
                }
                return false; // 返回 false 表示未處理此事件，繼續處理其他事件
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               checkEditTexts();
            }
        });
    }

    private void loginToServer (String ClassNumber, String ClassName) {
        Log.d("messageInput", ClassNumber);
        try {
            SQLiteDatabase initDatabase = database.getWritableDatabase();
            // 匯入資料庫

            ContentValues values = new ContentValues();
            String oldClassNumber = database.getClassNumber(null);

            if ("-1".equals(oldClassNumber)) {
                Log.d("writeClassName", oldClassNumber);

                values.put("classNumber", ClassNumber);
                values.put("className", ClassName);
                initDatabase.insert("initData", null, values);
                initDatabase.close();
            } else {
                values.put("classNumber", ClassNumber);

                // 要覆寫的條件是 id 欄位等於 1
                String whereClause = "id = ?";
                String[] whereArgs = {String.valueOf(1)};

                // 執行 UPDATE SQL 查詢
                initDatabase.update("initData", values, whereClause, whereArgs);

                values.put("className", ClassName);
                // 執行 UPDATE SQL 查詢
                initDatabase.update("initData", values, whereClause, whereArgs);
                initDatabase.close();
            }

//            database.writeClassNumber(ClassNumber);
//            database.writeClassName(ClassName);
            loginProgressBar.setVisibility(View.VISIBLE);




//            if (!isMyServiceRunning(MyForegroundWebsocketService.class)) {
//                Intent serviceIntentWebsocket = new Intent(this, MyForegroundWebsocketService.class);
//                this.startForegroundService(serviceIntentWebsocket);
//            }
            Intent serviceIntentWebsocket = new Intent(this, MyForegroundWebsocketService.class);
            this.startForegroundService(serviceIntentWebsocket);

        } catch (java.lang.NumberFormatException e)  {
            showToast("請輸入三位教室代碼與教室名稱");
        }
    }


    private void checkEditTexts() {
        // Get text from both EditText fields
        String text1 = editPin.getText().toString();
        String text2 = editName.getText().toString();

        // Check if both fields are filled
        if (text1.length() == 3 && !TextUtils.isEmpty(text1)) {
            if (text2.length() <= 10 && text2.length() > 0 && !TextUtils.isEmpty(text2.trim())) {
                loginToServer(text1, text2);
            } else {
                showToast("請輸入非空格之教室名稱");
            }
        } else {
            showToast("請輸入三位教室代碼");

        }
    }

    public static void showReminder(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 用户点击 OK 按钮时执行的操作
                        dialog.dismiss(); // 关闭对话框
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter1 = new IntentFilter("login_to_server_broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(login_to_server_Receiver, intentFilter1);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(login_to_server_Receiver);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(login_to_server_Receiver);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showToast("你要知道，人生有時候不一定跟你想的一樣");
    }


    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

}