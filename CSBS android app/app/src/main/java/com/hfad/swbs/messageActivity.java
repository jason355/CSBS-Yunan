package com.hfad.swbs;




import static androidx.core.content.FileProvider.getUriForFile;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;


import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;


public class messageActivity extends AppCompatActivity implements GitHubService.VersionCheckListener  {

    MyDatabaseHelper database;
    private DrawerLayout drawerLayout;

    boolean isConnected;
    boolean login_result;

    ImageView NetworkStat;
    ProgressBar roundBar;
    Handler handler;

    FloatingActionButton reConnect_fab;

    ProgressBar progressBar;
    ImageView update_icon;
    String fragmentTag;
    TextView update_text;
    private NavigationView navView;
    History_message historyMessageFragment;
    private websocket websocket;


    int isMessageOpen;

    View mainLayout;
    PowerManager.WakeLock wakeLock;
    String url = "https://github.com/jason355/CSBS-Yunan/releases/download/";
    private String[] update;
    long downloadId;


    // 連線廣播接收器
    private final BroadcastReceiver connectionStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "websocket_connection_status")) {
                roundBar.setVisibility(View.INVISIBLE);
                isConnected = intent.getBooleanExtra("is_connected", false);
                Log.d("isConnected", "connect:"+ isConnected);
                if (isConnected) {
                    NetworkStat.setImageResource(R.mipmap.network_good);
                    reConnect_fab.setVisibility(View.INVISIBLE);
                }else {
                    NetworkStat.setImageResource(R.mipmap.network_error);
                    reConnect_fab.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private final BroadcastReceiver login_to_server_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "login_to_server_broadcast")) {

                login_result = intent.getBooleanExtra("result", true);
                Log.d("Server back result", String.valueOf(login_result));

                if (!login_result) {
                    showReminder(messageActivity.this, "教室代碼已被使用", "很抱歉，您目前使用的教室代碼已被其他裝置使用，將無法接收到任何廣播訊息，請將其他設備斷開連線或是洽資訊組");
                    NetworkStat.setImageResource(R.mipmap.class_code_error);
                    reConnect_fab.setVisibility(View.VISIBLE);
                }
            }
        }
    };



    @SuppressLint({"SetTextI18n", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 設定讓此畫面保持開啟
        database = new MyDatabaseHelper(this);
        GitHubService.checkUpdate(this); // 檢查github上是否有新版本
        // 確認前景服務有啟動，主要因第一次安裝時不會透過開機來啟動前景服務
        if (!isMyServiceRunning(MyForegroundTimerService.class)) {
            Intent serviceIntentTimer = new Intent(this, MyForegroundTimerService.class);
            this.startForegroundService(serviceIntentTimer);
        }
        if (!isMyServiceRunning(MyForegroundWebsocketService.class)) {
            Intent serviceIntentWebsocket = new Intent(this, MyForegroundWebsocketService.class);
            this.startForegroundService(serviceIntentWebsocket);
        }

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



//        if (Build.VERSION.SDK_INT >= 27) {
//            setShowWhenLocked(true);
//            setTurnScreenOn(true);
//        }

        // 設定上一頁按鈕功能，若沒有設定會跳到上一個 Activity
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(messageActivity.this, "對不起 請使用 home 鍵回覆。", Toast.LENGTH_SHORT).show();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        update = new String[2];
        setContentView(R.layout.activity_message);
        drawerLayout = findViewById(R.id.drawer_layout); // 側欄布局
        navView = findViewById(R.id.nav_view); // 側欄
        FloatingActionButton fab = findViewById(R.id.fab); // 側欄懸浮按鈕
        reConnect_fab = findViewById(R.id.reConnect_fab); // 重新連線懸浮按鈕
        reConnect_fab.setVisibility(View.INVISIBLE); // 重新連線懸浮預設隱藏
        NetworkStat = findViewById(R.id.NetWorkStat); // 連線狀態圖示
        NetworkStat.setImageResource(R.mipmap.network_good); // 預設有連線
        roundBar = findViewById(R.id.reConnect_progressBar); // 重新連線圓環
        mainLayout  = findViewById(R.id.drawer_layout); // 提供偵測布局被點擊的 View (視圖)
        historyMessageFragment = new History_message(); // 建立 historyMessage fragment(片段) 的實例
        update_icon = findViewById(R.id.update); // App 更新按鈕
        progressBar = findViewById(R.id.update_progressBar); // App 更新進度條
        update_text = findViewById(R.id.update_text); // 更新版本文字

        // 更新按鈕偵測
        update_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installApk(messageActivity.this, "app-release.apk");
                update_text.setText("");
            }
        });




        // websocket 實例化
        websocket = new websocket(this);
        // 重新連線按鈕偵測
        reConnect_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                websocket.startWebSocket();
                roundBar.setVisibility(View.VISIBLE);

            }
        });

        // 設定側欄按鈕
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer();
            }
        });

        // 建立 ActionBarDrawerToggle 實例，以便將抽屜指示器的狀態與 DrawerLayout 同步
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, // 上下文
                drawerLayout,  // 與 ActionBarDrawerToggle 關聯的 DrawerLayout
                R.string.open_drawer, // "打開抽屜" 的字符串資源
                R.string.close_drawer // "關閉抽屜" 的字符串資源
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle); // 將 ActionBarDrawerToggle 添加為 DrawerLayout 的抽屜監聽器
        actionBarDrawerToggle.syncState(); // 同步 ActionBarDrawerToggle 的狀態，以確保抽屜指示器顯示正確

        // 設定側欄監聽器
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                int id = item.getItemId();

                if (id == R.id.nav_New) { // 新訊息按下
                    // Handle item 1 click
                    database.editMessageStat(1); // 更新資料庫畫面狀態
                    FragmentManager fragment = getSupportFragmentManager(); // 取得 FragmentManager 實例
                    FragmentTransaction fragmentTransaction = fragment.beginTransaction(); // 開始 Fragment 事務
                    fragmentTransaction.replace(R.id.fragmentContainerView, New_message.class, null); // 替換當前的 Fragment 為 New_message 類別的 Fragment
                    fragmentTransaction.commit(); // 提交事務以確認 Fragment 替換
                    Toast.makeText(messageActivity.this, "新訊息", Toast.LENGTH_SHORT).show(); // 顯示一個短暫的 Toast 訊息，表示新訊息已顯示
                } else if (id == R.id.nav_history) {
                    // Handle item 2 click
                    database.editMessageStat(2); // 更新資料庫畫面狀態
                    FragmentManager fragment = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragment.beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainerView, historyMessageFragment, null);
                    fragmentTransaction.commit();
                    Toast.makeText(messageActivity.this, "歷史訊息", Toast.LENGTH_SHORT).show();
                    mainLayout.setOnTouchListener(new View.OnTouchListener() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            isMessageOpen = database.checkMessageStat();
                            if (event.getAction() == MotionEvent.ACTION_DOWN && isMessageOpen == 2) {
                                historyMessageFragment.endSpotlightMode();
                                return true;
                            }
                            return false;
                        }
                    });
                } else if (id == R.id.nav_setting) {
                    database.editMessageStat(5);
                    Toast.makeText(messageActivity.this, "關於", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(messageActivity.this, Setting.class);
                    startActivity(intent);
                }
                drawerLayout.closeDrawers(); // Close the drawer after handling the item click
                return true;
            }
        });
    }
    @Override
    public void onVersionCheckResult(String[] data) {
        // 在這裡處理版本檢查結果
        update[0] = data[0];
        update[1] = data[1];
        if (update[1].equals("True") && database.checkUpdate() != Integer.parseInt(update[0])){ // 若有且更新版本不等於資料庫版本
            Log.d("update", "1");
            database.editUpdate(Integer.parseInt(update[0])); // 更新資料庫中版本
            update_icon.setVisibility(View.INVISIBLE); // 更新按鈕不可見 以防下載中啟動造成 APK 毀損
            Toast.makeText(messageActivity.this, "新版本下載中...", Toast.LENGTH_LONG).show(); // 顯示提示文字
            url += update[0]+"/app-release.apk"; // 設定url https://github.com/jason355/CSBS-Yunan/releases/download/142/app-release.apk
            Log.d("url", url);
            downloadApk(messageActivity.this, url); // 啟動下載函數
        } else if (database.checkUpdate() != -1) { // 若有已下載的新版本
            Log.d("update", "1");
            update_icon.setVisibility(View.VISIBLE); // 顯示更新圖片
            update_text.setText("Update version: "+transform_Version_With_Dot(update[0])); // 設置更新文本

        } else {
            Log.d("update", "False");
            update_icon.setVisibility(View.INVISIBLE);
            database.editUpdate(-1);
        }
    }

    // 下載 APK
    public  void downloadApk(Context context, String apkUrl) {
        String get = "1";
        while (get.equals("1")) { // 刪除檔案中的原 apk
            get = FileManager.deleteOldApkFiles(context, "app-release.apk");
            Log.d("get", get);
        }
        // 取得下載的實例
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(apkUrl); // 將 網址字串轉換成Uri格式
        DownloadManager.Request request = new DownloadManager.Request(uri); // 建立 DownloadManager.Request 物件，用於下載 APK 檔案
        request.setTitle("app-release"); // 設定下載檔案的標題
        request.setDescription("Downloading APK"); // 設定下載檔案的描述
        request.setDestinationInExternalFilesDir(context,null ,"app-release.apk"); // 設定下載檔案的目的地路徑為外部存儲器的應用程式私有目錄
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // 設定下載完成後顯示通知
        downloadId = downloadManager.enqueue(request); // 使用 DownloadManager 開始執行下載請求，並獲取下載 ID
        progressBar.setVisibility(View.VISIBLE); // 設置進度條可見
        update_text.setText("Update version: "+transform_Version_With_Dot(update[0])); // 設置更新文本
        // 設定進度條顏色
        progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.readBut)));

        handler = new Handler();
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = FileManager.getDownloadProgress(context, downloadId);
//                Log.d("progress", String.valueOf(progress));
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                    update_icon.setVisibility(View.VISIBLE);
//                    database.editUpdate(Integer.parseInt(update[0]));
                } else if(progress != -1) {
                    progressBar.setProgress(progress);
                    handler.postDelayed(this, 500); // 1000毫秒 = 1秒
                } else {
                    update_text.setText("Download failed");
                    update_text.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(messageActivity.this, R.color.Unread)));
                }

            }
        };
        updateTimeRunnable.run();
    }


    public  void installApk(Context context, String apkFileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PackageManager packageManager = context.getPackageManager();
            if (!packageManager.canRequestPackageInstalls()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            } else {
                File apkFile = new File(context.getExternalFilesDir(null), "app-release.apk");
                Log.d("apkFilePath", apkFile.getAbsolutePath());
                if (apkFile.exists()) {
                    Log.d("APK File", "Exists");
                } else {
                    Log.d("APK File", "Does not exist");
                }
                Uri apkUri = getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", apkFile);

                Log.d("apkUri", String.valueOf(apkUri));
                update_icon.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                database.editUpdate(-1);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setDataAndType(apkUri,  "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        } else {
            File apkFile = new File(context.getExternalFilesDir(null), "app-release.apk");
            Log.d("apkFilePath", apkFile.getAbsolutePath());

            if (apkFile.exists()) {
                Log.d("APK File", "Exists");
            } else {
                Log.d("APK File", "Does not exist");
            }

            Uri apkUri = getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", apkFile);
            update_icon.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            database.editUpdate(-1);
            Log.d("apkUri", String.valueOf(apkUri));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
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
//        wakeLock.acquire(10*60*1000L);



        IntentFilter intentFilter = new IntentFilter("websocket_connection_status");
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionStatusReceiver, intentFilter);

        IntentFilter intentFilter1 = new IntentFilter("login_to_server_broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(login_to_server_Receiver, intentFilter1);

        Intent intent = getIntent();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SharedPreferences sharedPreferences = getSharedPreferences("fragmentTag", MODE_PRIVATE);
        fragmentTag = intent.getStringExtra("fragmentTag");
        if (fragmentTag == null) {
            fragmentTag = sharedPreferences.getString("key", "New_message");
        }
        if (fragmentTag.equals("New_message")){
            database.editMessageStat(1);
            int messageStat = database.checkMessageStat();
            Log.d("messageStat", "="+messageStat);
            fragmentTransaction.replace(R.id.fragmentContainerView, New_message.class, null);
            fragmentTransaction.commit();
        }
        else if (fragmentTag.equals("History_message")) {
            database.editMessageStat(2);
            fragmentTransaction.replace(R.id.fragmentContainerView, historyMessageFragment, null);
            fragmentTransaction.commit();
            mainLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && isMessageOpen == 2) {
                        historyMessageFragment.endSpotlightMode();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public  void showReminder(Context context, String title, String message) {
        if (!isFinishing() && ! isDestroyed()) {
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

    }


    private String transform_Version_With_Dot (String version) {
        StringBuilder result = new StringBuilder();
        int stringLen = version.length();
        for (int i = stringLen-1; i >=0; i--) {
            if (i == stringLen-1 || i == stringLen-2) {
                result.insert(0,"."+version.charAt(i));
            }
            else {
                result.insert(0,version.charAt(i));
            }
        }
        result.insert(0, "v");
        return result.toString();
    }

    @Override
    public void onPause() {
        super.onPause();
        database.editMessageStat(0);
        SharedPreferences.Editor editor = getSharedPreferences("fragmentTag", MODE_PRIVATE).edit();
        editor.putString("key", fragmentTag);
        editor.apply();
        fragmentTag = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionStatusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(login_to_server_Receiver);


    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionStatusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(login_to_server_Receiver);


    }





    private void openDrawer() {
        drawerLayout.openDrawer(navView);
    }
}

