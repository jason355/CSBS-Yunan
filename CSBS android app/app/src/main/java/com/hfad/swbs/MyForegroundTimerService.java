package com.hfad.swbs;



import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Timer;

public class MyForegroundTimerService extends Service {

    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "ForegroundTimerServiceChannel";

    int count = 0;
    int result;
    private timer clock;
    boolean canPass = true;
    int isMessageOpen;
    int haveCD;
    long delay = 10000;
    int counting;
    private PowerManager.WakeLock wakeLock;


    MyDatabaseHelper database;



    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            delay = 1000;
            result = clock.startClock();
            haveCD = database.checkHaveCountDown();
            Log.d("haveCountDown", "="+haveCD);


//            Log.d("isMessageOpen", String.valueOf(isMessageOpen));
            if (result == 0) {
                isMessageOpen = database.checkMessageStat();
                counting = database.checkCounting();

                // 有新消息，顯示通知
                if ((database.checkNewMessage(1) || database.checkNewMessage(2) || database.checkNewMessage(3)) && isMessageOpen != 1  && counting == 0) {
//                    wakeLock.acquire(10*60*1000L /*10 minutes*/);
                    Log.d("Test", "1");

                    database.editHaveCountDown(1);
                    delay = 10000;
                    Intent intent1 = new Intent(MyForegroundTimerService.this, messageActivity.class);
                    intent1.putExtra("fragmentTag", "New_message");
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }
            } else if (result == 1) {
                isMessageOpen = database.checkMessageStat();
                // 启动倒计时
                if ((database.checkNewMessage(1) || database.checkNewMessage(2) || database.checkNewMessage(3))) {
                    Log.d("Test", "2");

                    haveCD = database.checkHaveCountDown();
                    counting = database.checkCounting();
                    Log.d("haveCountDown", "="+haveCD);
                    Log.d("counting", "="+counting);
                    Log.d("isMessageOpen", "="+isMessageOpen);
                    if (isMessageOpen == 0 && haveCD == 0) {
//                        wakeLock.acquire(10*60*1000L /*10 minutes*/);
                        isOpenBroadCast(1);
                        haveCountDown(true);
                        delay = 30000;
                        iscounting(true);
                        database.editHaveCountDown(1);
                        database.editCounting(1);
                        Intent intentCountDownService = new Intent(MyForegroundTimerService.this, CountDown.class);
                        startService(intentCountDownService);
                        iscounting(true);

                    } else if (!(isMessageOpen == 0 || isMessageOpen == 1) || (haveCD == 1 && (isMessageOpen == 0) && counting == 0)) {
                        Log.d("Test", "3");


                        delay = 10000;
//                        wakeLock.acquire(10*60*1000L /*10 minutes*/);
                        Intent intentMessageActivity = new Intent(MyForegroundTimerService.this, messageActivity.class);
                        intentMessageActivity.putExtra("fragmentTag", "New_message");
                        intentMessageActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentMessageActivity);
                    }
                }
            } else if (result == -2) {
                handler.removeCallbacksAndMessages(null);
                stopForeground(true);
                stopSelf();

            } else if (result != -1) {
                canPass = false;
                delay = result;
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);


            } else if (result == -1) {
                delay = 1;
            }
            else {
                Log.d("MyForegroundTimerService", "Closing");
                stopForeground(true);
                stopSelf();
            }
            handler.postDelayed(this, delay);
        }
    };

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        database = new MyDatabaseHelper(this);
        clock = new timer(this);
        // 抓資料完再啟動 runnable
        clock.init(breaktime -> {
            Log.d("Timer", "breakTime ready, starting runnable");
            handler.postDelayed(runnable, 0); // ✅ 確保 breakTime 已有值
        });

    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, buildNotification());
        startForeground(NOTIFICATION_ID, notification);





//        handler.postDelayed(runnable, 0);


        return START_STICKY;






        // 返回 START_STICKY，表示 Service 被終止後會自動重啟

    }


    private void isOpenBroadCast(int isOpen) {
        Intent MessageOpen = new Intent("MessageActivity_Open");
        MessageOpen.putExtra("is_open", isOpen);
        LocalBroadcastManager.getInstance(this).sendBroadcast(MessageOpen);
    }

    private void haveCountDown(boolean yeah) {
        Intent intent = new Intent("haveCountDown");
        intent.putExtra("key", yeah);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void iscounting(boolean counting) {
        Intent intent = new Intent("counting");
        intent.putExtra("key", counting);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止任務，並移除前景狀態
        stopForeground(true);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private Notification buildNotification() {
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID);

        return builder
                .setContentTitle("Foreground Service")
                .setContentText("Running in the background")
                .setSmallIcon(R.mipmap.logo)
                .build();
    }
}
