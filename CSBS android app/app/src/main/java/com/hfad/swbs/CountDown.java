package com.hfad.swbs;



import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.w3c.dom.Text;

public class CountDown extends Service {

    TextView textViewB;
    MyDatabaseHelper database = new MyDatabaseHelper(this);

    CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;
    long timerCountDown = 30000;
    WindowManager windowManager;
    ProgressBar countDownProgressBar;
    TextView second;
    TextView secondInCycle;
    int i = 1;

    int screenWidth;
    int screenHeight;
    boolean counting;
    boolean AutoMove = false;
    CardView floatingViewCard;
    View floatingView;
    ConstraintLayout constraintLayout;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final BroadcastReceiver countingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("haveCountDown")) {
                counting = intent.getBooleanExtra("key", false);
                Log.d("haveCountDown", "open:"+ counting);
            }
        }
    };



    @Override
    public void onCreate() {
        super.onCreate();


        IntentFilter intentFilter2 = new IntentFilter("counting");
        LocalBroadcastManager.getInstance(this).registerReceiver(countingReceiver, intentFilter2);



        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = layoutInflater.inflate(R.layout.activity_count_down, null);

        constraintLayout = floatingView.findViewById(R.id.constraintLayout);


        floatingViewCard = floatingView.findViewById(R.id.countdown_cardview);
        // Set the corner radius
        floatingViewCard.setRadius(80);
        floatingViewCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.CountDownBackGround));


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
//        Log.d("screenWidth", screenWidth+"");



        // 在這裡設置你的懸浮視窗的內容
        WindowManager.LayoutParams parms = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 設定浮動視窗的類型
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        parms.alpha = 0.9f;
        parms.gravity = Gravity.CENTER;
        parms.x = 0;
        parms.y = 20;
        textViewB = floatingView.findViewById(R.id.countdownB);
        textViewB.setTextColor(ContextCompat.getColor(this, R.color.white));
        textViewB.setText("廣播倒數 30");

        second = floatingView.findViewById(R.id.second);
        secondInCycle = floatingView.findViewById(R.id.secondInCycle);

        Button floatingButton2 = floatingView.findViewById(R.id.delay2);
        Button floatingButton1 = floatingView.findViewById(R.id.delay1);

        countDownProgressBar = floatingView.findViewById(R.id.countdown_progressbar);
        countDownProgressBar.setIndeterminate(false);
        countDownProgressBar.setMax(30);
        countDownProgressBar.setProgress(0);
        // 假設你有一個名為 floatingView 的 View 將要顯示
//        ObjectAnimator translationY = ObjectAnimator.ofFloat(floatingViewCard, "translationY", screenHeight,700);
//        ObjectAnimator scaleX = ObjectAnimator.ofFloat(floatingViewCard, "scaleX", 0f, 1f);
//        ObjectAnimator scaleY = ObjectAnimator.ofFloat(floatingViewCard, "scaleY", 0f, 1f);



        floatingButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerCountDown != 0){
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) floatingView.getLayoutParams();
//                    params.x -= screenWidth;
                    params.y -= screenHeight;
                    windowManager.updateViewLayout(floatingView, params);

                    timerCountDown += 60000;
                    long val = 30/timerCountDown;
                    i *= val;
                    countDownProgressBar.setMax(Math.round(timerCountDown/1000));
                    countDownProgressBar.setProgress(i);

                    addTime();
                    floatingButton2.setVisibility(View.GONE);
                    floatingButton1.setVisibility(View.GONE);

                    textViewB.setText("廣播倒數");
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) textViewB.getLayoutParams();
                    layoutParams.verticalBias = 0.5f; // Center vertically
                    layoutParams.horizontalBias = 0.5f; // Center horizontally
                    textViewB.setLayoutParams(layoutParams);
                    second.setVisibility(View.VISIBLE);
                    second.setText(Integer.toString((int) (timerCountDown/1000)));
                }


//                return false;

            }
        });
        floatingButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerCountDown != 0) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) floatingView.getLayoutParams();
//                    params.x -= screenWidth;
                    params.y -= screenHeight;
                    windowManager.updateViewLayout(floatingView, params);
                    timerCountDown += 120000;
                    long val = 30/timerCountDown;
                    i *= val;
                    countDownProgressBar.setMax(Math.round(timerCountDown/1000));
                    countDownProgressBar.setProgress(i);
//                    Log.d("countDown", timerCountDown+"");
                    addTime();
                    floatingButton2.setVisibility(View.GONE);
                    floatingButton1.setVisibility(View.GONE);
                    textViewB.setText("廣播倒數");
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) textViewB.getLayoutParams();
                    layoutParams.verticalBias = 0.5f; // Center vertically
                    layoutParams.horizontalBias = 0.5f; // Center horizontally
                    textViewB.setLayoutParams(layoutParams);
                    second.setVisibility(View.VISIBLE);
                    second.setText(Integer.toString((int) (timerCountDown/1000)));
                }

//                return false;

            }
        });

        windowManager.addView(floatingView, parms);




        countDownTimer = new CountDownTimer(timerCountDown, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                timerCountDown = l;
                long secondsLeft = l / 1000;
                textViewB.setText("廣播倒數 "+secondsLeft);
                countDownProgressBar.setProgress(i);
                i++;


            }

            @Override
            public void onFinish() {
                windowManager.removeView(floatingView);

                countDownProgressBar.setProgress(30); // 確保進度為 0
                AutoMove = false;
                database.editCounting(0);
                Intent intent1 = new Intent(CountDown.this, messageActivity.class);
                intent1.putExtra("fragmentTag", "New_message");
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
                stopSelf();
            }
        }.start();
    }


    private void addTime() {
        countDownTimer.cancel();
        long MaxTime = timerCountDown;
        countDownTimer = new CountDownTimer(timerCountDown, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                timerCountDown = l;
                long secondsLeft = l / 1000;

                if (MaxTime - timerCountDown > 5000) {
                    if (!AutoMove) {

                        textViewB.setVisibility(View.GONE);
                        second.setVisibility(View.GONE);
                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) floatingView.getLayoutParams();
                        params.x -= screenWidth;
                        params.y -= screenHeight;
                        windowManager.updateViewLayout(floatingView, params);
                        AutoMove = true;
                    }
                    secondInCycle.setText(Integer.toString((int) secondsLeft));
                    countDownProgressBar.setProgress(i);
                    i++;
                } else {
                    second.setText(Integer.toString((int) secondsLeft));
                    countDownProgressBar.setProgress(i);
                    i++;
                }

            }

            @Override
            public void onFinish() {
                AutoMove = false;
                countDownProgressBar.setProgress(i);
                windowManager.removeView(floatingView);
                database.editCounting(0);
                Intent intent1 = new Intent(CountDown.this, messageActivity.class);
                intent1.putExtra("fragmentTag", "New_message");
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent1);
                stopSelf();
            }
        }.start();
    }




}