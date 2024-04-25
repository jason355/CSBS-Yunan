package com.hfad.swbs;


import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    MyDatabaseHelper database = new MyDatabaseHelper(this);
    TextView warning;
    EditText messageView;
    ContentValues values = new ContentValues();

    boolean isFirst = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        setContentView(R.layout.activity_main);
        String ClassNum = database.getClassNumber();




        if (ClassNum != "-1" && ClassNum != "null") {
            database.close();
            Intent intent = new Intent(this, messageActivity.class);
            intent.putExtra("fragmentTag", "New_message");
            startActivity(intent);
        } else {
            Log.d("Class Number ", "There is no value in database");
        }

        messageView = findViewById(R.id.classPin);
        messageView.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String userInput = messageView.getText().toString();
                    Log.d("Enter pressed", "Get Class number" + userInput );
                    checkClassNumber(userInput);
                    return true; // 返回 true 表示已處理此事件
                }
                return false; // 返回 false 表示未處理此事件，繼續處理其他事件
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warning = findViewById(R.id.Warning);
                messageView = findViewById(R.id.classPin);
                // 取得輸入數字
                String messageText = messageView.getText().toString();
                Log.d("messageInput", messageText);
                checkClassNumber(messageText);


            }
        });
    }

    private void checkClassNumber (String ClassNumber) {
        Log.d("messageInput", ClassNumber);
        if (!ClassNumber.isEmpty()) {
            try {
                int classNumber = Integer.parseInt(ClassNumber);
                if ((classNumber >= 701 && classNumber <= 705) ||
                        (classNumber >= 801 && classNumber <= 805) ||
                        (classNumber >= 901 && classNumber <= 905) ||
                        (classNumber >= 101 && classNumber <= 106) ||
                        (classNumber >= 111 && classNumber <= 116) ||
                        (classNumber >= 121 && classNumber <= 126)) {

                    SQLiteDatabase initDatabase = database.getWritableDatabase();
                    // 匯入資料庫
                    values.put("classNumber", ClassNumber);
                    initDatabase.insert("initData", null, values);

                    database.close();
                    Intent intent = new Intent(this, messageActivity.class);
                    intent.putExtra("fragmentTag", "New_message");
                    startActivity(intent);
                } else {
                    warning.setText("班級號碼不在指定範圍內");
                }

            } catch (java.lang.NumberFormatException e)  {
                warning.setText("Please enter a class number");


            }
        } else {
            warning.setText("Please Enter Class number.");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "對不起 請使用 home 鍵回覆。", Toast.LENGTH_SHORT).show();
    }



}