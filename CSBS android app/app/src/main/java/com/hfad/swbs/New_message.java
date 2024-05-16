package com.hfad.swbs;



import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;


import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.CountDownTimer;

import android.os.Handler;
import android.os.PowerManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link New_message#newInstance} factory method to
 * create an instance of this fragment.
 */
public class New_message extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    int seeing = 0; // 現在正在顯示的訊息索引值

    TextView teacher;
    TextView fromWhere;

    TextView readText;
    TextView content;
    TextView class_num;
    TextView sendtime;
    TextView page_num;
    TextView unshow;
    TextView historyMessage;
    TextView nowtime;
    CardView cardView;
    Button readfab;
    String[][] message; // 訊息陣列
    String[][] messageTemp; // 暫存訊息陣列

    private Handler handler;

    private CountDownTimer countDownTimer;
    private LinearLayout progressBarContainer;
    int currentProgressBarIndex = 0; // 當前進度條索引值
    int numberofProgressBar = 0; // 進度條數量

    MyDatabaseHelper database;

    private MediaPlayer mediaPlayer;

    private  int TOTAL_TIME_MS = 10000;  // 進度條總時間（毫秒）
    private static final int INTERVAL_MS = 20;

    public New_message() {
    }


    public static New_message newInstance(String param1, String param2) {
        New_message fragment = new New_message();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_message, container, false);
        view.setTag("New_message");

        database = new MyDatabaseHelper(requireContext());

        teacher = view.findViewById(R.id.teacher); // 教師名稱 textview
        fromWhere = view.findViewById(R.id.fromWhere); // 組別 textview
        content = view.findViewById(R.id.content); // 內容 textview
        sendtime = view.findViewById(R.id.sendtime); // 傳送時間 textview
        page_num = view.findViewById(R.id.page_num); // 頁碼 textview
        historyMessage = view.findViewById(R.id.historyMessage); // "歷史訊息" textview
        progressBarContainer = view.findViewById(R.id.progressBarContainer); // 進度條容器
        class_num = view.findViewById(R.id.class_num); // 班級 textview
        cardView = view.findViewById(R.id.cardView); // 卡片 view
        readfab = view.findViewById(R.id.readbutton); // 已讀按鈕
        readText = view.findViewById(R.id.readText); // 已讀未讀 textview
        unshow = view.findViewById(R.id.unshow); // 未讀數量 textview
        nowtime = view.findViewById(R.id.nowtime); // 現在時間

        handler = new Handler();
        startUpdatingTime(); // 顯示現在時間

        Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();

        class_num.setText(database.getClassName());

        // 已讀按鈕監聽器設定
        readfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (message[currentProgressBarIndex].length == 6) {
                    database.editIsNew(message[currentProgressBarIndex][4], 0);
                    if (database.countUnread() > 0){
                        unshow.setText("未讀"+database.countUnread()+"則");
                    } else {
                        unshow.setVisibility(View.INVISIBLE);
                    }

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    if (message.length > 1) {
                        for (int i = 0; i < message.length; i++){
                            View progressBar = progressBarContainer.getChildAt(0);
                            progressBarContainer.removeView(progressBar);
                        }
                    }

                    message = removeMessage(message, currentProgressBarIndex);
                    int messageLen = message.length;

                    messageTemp = database.getNewMessage(1);
                    // 檢查是否有新訊息
                    if (messageTemp != null && messageTemp.length != 0) {

                        Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();
//                        historyMessage.setVisibility(View.INVISIBLE);

                        // 結合新訊息與原訊息
                        message = combineMessage(message, messageTemp);
                        // 判斷結合後訊息長度
                        if (message.length == 1) {
                            currentProgressBarIndex = 0;
                            seeing =0;
                            showMessage(message);
                            oneMessageTimer(view);
                        } else if (numberofProgressBar != 0) {
                            currentProgressBarIndex--;
                            if (database.checkIsNew(message[(currentProgressBarIndex + 1) % numberofProgressBar][4]) == 3) {
                                currentProgressBarIndex = messageLen;
                                seeing = messageLen;
                                numberofProgressBar = messageLen;
                                addPrograssbar(view,  numberofProgressBar);
                                setProgressBarToEnd(currentProgressBarIndex-1);
                                showMessage(message);
                                startNextProgressBar_New(database, view);
                            } else {
                                numberofProgressBar = message.length;
                                addPrograssbar(view,  numberofProgressBar);
                                setProgressBarToEnd(currentProgressBarIndex);
                                currentProgressBarIndex = (currentProgressBarIndex + 1) % numberofProgressBar;
                                if (currentProgressBarIndex == 0) {
                                    resetProgressBar();
                                }

                                showMessage(message);
                                startNextProgressBar_New(database, view);

                            }
                        }
                    // 無新訊息 抓取歷史訊息
                    } else if (message.length == 0) {
                        message = database.getMessage();

                        if (message != null && message.length != 0) {

                            int numberofProgressBar = message.length;
                            historyMessage.setVisibility(View.VISIBLE);
                            if (numberofProgressBar != 1) {
                                addPrograssbar(view, numberofProgressBar);
                                currentProgressBarIndex = 0;
                                seeing = 0;
                                showMessage(message);
                                startNextProgressBar(database, view);
                            }
                            else {
                                historyMessage.setVisibility(View.VISIBLE);
                                seeing = 0;
                                showMessage(message);
                                oneMessageTimer_His(view);
                            }
                        } else {
                            content.setText("無新訊息");
                        }
                    } else if (message.length != 1) {
                        if (currentProgressBarIndex >= message.length) {
                            currentProgressBarIndex = 0;
                        }
                        addPrograssbar(view, message.length);
                        setProgressBarToEnd(currentProgressBarIndex);
                        if (seeing > message.length) {
                            seeing = 0;
                        } else {
                            seeing--;
                        }

                        showMessage(message);
                        startNextProgressBar_New(database, view);
                    } else {
                        currentProgressBarIndex = 0;
                        seeing =0;
                        showMessage(message);
                        oneMessageTimer(view);
                    }
                }
            }
        });



        // 取得最新訊息 存入 message
        getUnReadMessage();
        if (message != null && message.length != 0) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage)); // cardview 顏色設定
//            unshow.setText("未讀"+message.length+"則"); // 顯示未讀訊息數量
            database.editMessageStat(1); // 將顯示狀態寫成 1 新訊息
            historyMessage.setVisibility(View.INVISIBLE); // 隱藏 historyMessage
            int numberofProgressBar = message.length;
            if (numberofProgressBar != 1) {
                addPrograssbar(view, numberofProgressBar); // 建立進度條
                showMessage(message); // 顯示訊息
                startNextProgressBar_New(database, view); // 啟動倒數計時功能，控制訊息輪播
            }
            else {
                showMessage(message); // 顯示訊息
                oneMessageTimer(view); // 啟動倒數計時功能
            }
        } else {
            database.editMessageStat(3); // 更新顯示狀態 3 歷史訊息
            message = database.getMessage(); // 取得歷史訊息
            cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage)); // 設定cardveiw 背景顏色
            // 判斷是否有歷史訊息
            if (message != null && message.length != 0) {
                int numberofProgressBar = message.length; // 取得訊息長度
                historyMessage.setVisibility(View.VISIBLE); // 顯示"歷史訊息" textview
                if (numberofProgressBar != 1) {
                    addPrograssbar(view, numberofProgressBar); // 建立進度條
                    showMessage(message); // 顯示訊息
                    startNextProgressBar(database, view); // 啟動倒數與輪播
                }
                else {
                    historyMessage.setVisibility(View.VISIBLE); // 顯示"歷史訊息" textview
                    showMessage(message); // 顯示訊息
                    oneMessageTimer_His(view); // 啟動倒數
                }
            } else {
                content.setText("無新訊息");
            }
        }
        return view;

    }


    private void startUpdatingTime() {
        // 創建一個runnable 處理時間更新
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // 取得當前時間
                Date currentTime = new Date();

                // 格式化時間
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                String formattedTime = sdf.format(currentTime);
                // 設定現在時間
                nowtime.setText(formattedTime);

                // 1s 後再次執行此 Runnable
                handler.postDelayed(this, 1000); // 1000毫秒 = 1秒
            }
        };

        // 首次執行 Runnable
        updateTimeRunnable.run();
    }

    // 顯示訊息
    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void showMessage(String[][] message) {
        int len = message.length; // 取得訊息長度
        // 目前索引值若大於等於 message長度，則歸零
        if (seeing >= len) {
            seeing = 0;
        }
        // 判斷是否為未讀訊息
        if (database.checkIsNew(message[seeing][4]) != 0){
            readText.setText("未讀");
            readText.setTextColor(ContextCompat.getColor(getContext(), R.color.Unread));
            readfab.setVisibility(View.VISIBLE);
        } else {
            readText.setText("已讀");
            readText.setTextColor(ContextCompat.getColor(getContext(), R.color.read));
            readfab.setVisibility(View.INVISIBLE);
        }
        teacher.setText(message[seeing][0]);
        fromWhere.setText(message[seeing][1]);
        content.setText(message[seeing][2]);
        content.setLinkTextColor(getResources().getColor(R.color.url));
        sendtime.setText(message[seeing][3]);
        page_num.setText(seeing + 1 + "/" + message.length);
        seeing++;

    }




    // 顯示新訊息
    @SuppressLint("SetTextI18n")
    private void startNextProgressBar_New(MyDatabaseHelper database, View view) {
        numberofProgressBar = message.length; // 取得訊息長度
        database.editIsNew(message[currentProgressBarIndex][4], 3); // 更新目前訊息之狀態 3
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage)); // 設定背景顏色
        // 取得 currentProgressBar 所對應的 progressBar
        final ProgressBar currentProgressBar = (ProgressBar) progressBarContainer.getChildAt(currentProgressBarIndex);
        unshow.setVisibility(View.VISIBLE); // 顯示未讀數量
        unshow.setText("未讀"+numberofProgressBar+"則"); // 設定未讀數量
        // 判斷是否需要聲音
        if (Objects.equals(message[currentProgressBarIndex][5], "1")) {
            // 播放聲音
            mediaPlayer = createAndStartMediaPlayer(getContext(), R.raw.marimba);
            message[currentProgressBarIndex][5] = "0"; // 將聲音欄位改成 0
            database.editSound(message[currentProgressBarIndex][4], 0); // 將資料庫中聲音欄位改成 0
        }

        // 建立倒數計時器
        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {

            // 每 Interval_ms 秒更新一次
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((TOTAL_TIME_MS - millisUntilFinished) * 100 / TOTAL_TIME_MS); // 計算目前進度
                currentProgressBar.setProgress(progress); // 更新進度條
            }

            // 當完成倒數
            @Override
            public void onFinish() {
                currentProgressBar.setProgress(100); // 進度條設定成 100
                messageTemp = database.getNewMessage(1); // 取得 isNew = 1 之新訊息
                if (messageTemp != null && messageTemp.length != 0){ // 若不為空值
                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();
                    historyMessage.setVisibility(View.INVISIBLE);
                    // 刪除進度條
                    for (int i = 0; i < message.length; i++){
                        View progressBar = progressBarContainer.getChildAt(0);
                        progressBarContainer.removeView(progressBar);
                    }
                    // 檢查原陣列若均"顯示"過，則跳到原陣列之尾端，顯示新訊息
                    if (numberofProgressBar != 0) {
                        if (database.checkIsNew(message[(currentProgressBarIndex + 1) % numberofProgressBar][4]) == 3) {
                            currentProgressBarIndex = message.length -1; // 取得原 message 的尾端
                            seeing = message.length; // 取得當前訊息長度
                        }
                    }
                    message = combineMessage(message, messageTemp); // 整合新訊息與原訊息
                    numberofProgressBar = message.length;
                    unshow.setVisibility(View.VISIBLE); // 顯示訊息
                    unshow.setText("未讀"+numberofProgressBar+"則"); // 設定未讀訊息數量
                    addPrograssbar(view,  numberofProgressBar); // 新增進度條
                    setProgressBarToEnd(currentProgressBarIndex); // 將部分進度條填滿
                }
                currentProgressBarIndex = (currentProgressBarIndex + 1) % numberofProgressBar; // 計算下一個索引值
                if (currentProgressBarIndex == 0) {
                    resetProgressBar();
                }

                showMessage(message); // 顯示訊息
                startNextProgressBar_New(database, view); // 執行下一次倒數

            }

        };
        // 首次倒數開始
        countDownTimer.start();


    }

    // 移除index訊息
    private static String[][] removeMessage(String[][] array, int indexToRemove) {
        String[][] newArray = new String[array.length - 1][6]; // 建立新陣列
        int top = 0; // 設定前索引值
        // 變例所有訊息，直到 i = indexToRemove，挑過此索引值
        for (int i = 0; i < array.length; i++) {
            if (i != indexToRemove) {
                for (int j = 0; j < 6; j++) {
                    newArray[top][j] = array[i][j];
                }
                top++;
            }
        }
        return newArray; // 回傳新陣列
    }

    // 歷史訊息輪播
    private void startNextProgressBar(MyDatabaseHelper database, View view) {
        unshow.setVisibility(View.INVISIBLE); // 隱藏未讀數量
        readfab.setVisibility(View.INVISIBLE); // 隱藏已讀按鈕
        int numberofProgressBar = message.length; // 取得進度條長度
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage)); // 設定cardview顏色
        readText.setText("已讀"); // 設定"已讀"
        readText.setTextColor(ContextCompat.getColor(getContext(), R.color.read)); // 設定 已讀顏色

        // 取得 currentProgressBarIndex 對應的 porgressBar
        final ProgressBar currentProgressBar = (ProgressBar) progressBarContainer.getChildAt(currentProgressBarIndex);

        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((TOTAL_TIME_MS - millisUntilFinished) * 100 / TOTAL_TIME_MS); // 計算進度
                currentProgressBar.setProgress(progress); // 更新進度條進度
            }
            @Override
            public void onFinish() {
                currentProgressBar.setProgress(100);
                messageTemp = database.getNewMessage(1); // 取得資料庫中新訊息 isNew = 1
                if (messageTemp != null && messageTemp.length != 0) { // 若有新訊息則顯示
                    historyMessage.setVisibility(View.INVISIBLE); // 隱藏 "歷史訊息"
                    // 移除 progressBar
                    for (int i = 0; i < message.length; i++){
                        View progressBar = progressBarContainer.getChildAt(0);
                        progressBarContainer.removeView(progressBar);
                    }

                    message = messageTemp; // 將新訊息丟到message
                    unshow.setVisibility(View.VISIBLE); // 顯示未讀數量
                    unshow.setText("未讀"+message.length+"則"); // 設定未讀數量

                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();

                    int numberofProgressBar = message.length; // 新訊息數量
                    // 若新訊息數量大於1
                    if (numberofProgressBar != 1) {
                        addPrograssbar(view, numberofProgressBar); // 建立進度條
                        currentProgressBarIndex = 0; // 設定currentProgressBarIndex 為 0
                        seeing = 0;
                        showMessage(message); // 顯示訊息
                        startNextProgressBar_New(database, view); // 啟動新訊息之倒數
                    }
                    // 一則新訊息
                    else {
                        seeing = 0;
                        currentProgressBarIndex = 0;
                        showMessage(message);
                        oneMessageTimer(view);
                    }

                } else {
                    // 計算下一個index
                    currentProgressBarIndex = (currentProgressBarIndex + 1) % numberofProgressBar;
                    if (currentProgressBarIndex == 0) {
                        resetProgressBar();
                    }
                    showMessage(message);
                    startNextProgressBar(database, view);
                }

            }

        };
        countDownTimer.start();
    }

    // 結合兩陣列
    private String[][] combineMessage(String[][] array1, String[][] array2) {
        int len1;
        int len2;

        if (array1 == null){
             len1 = 0;
        }else {
            len1 = array1.length;
        }
        if (array2 == null) {
            len2 = 0;
        } else {
            len2 = array2.length;
        }

        String[][] NewArray = new String[len1 + len2][6]; // 建立新陣列
        int index = 0; // 使用index 變數紀錄 NewArray陣列
        // 遍歷兩陣列，將資料放入NewArray
        for (int i = 0; i < len1; i++){
            for (int j = 0; j < 6; j++){
                NewArray[index][j] = array1[i][j];
            }
            index++;
        }
        for (int i = 0; i < len2; i++){
            for (int j = 0; j < 6; j++){
                NewArray[index][j] = array2[i][j];
            }
            index++;
        }
        return NewArray; // 回傳新陣列

    }

    // 單筆歷史訊息
    private void oneMessageTimer_His(View view){
        unshow.setVisibility(View.INVISIBLE); // 隱藏未讀數量字串
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage)); // 設定背景顏色
        readText.setText("已讀"); // 設定已讀字串
        readText.setTextColor(ContextCompat.getColor(getContext(), R.color.read)); // 設定已讀顏色
        readfab.setVisibility(View.INVISIBLE); // 隱藏已讀按鈕
        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsUntileFinished = millisUntilFinished / 1000; // 紀錄剩餘時間
            }
            @Override
            public void onFinish() {
                messageTemp = database.getNewMessage(1); // 取得 isNew = 1 之新訊息

                if (messageTemp != null && messageTemp.length != 0) {
                    mediaPlayer = createAndStartMediaPlayer(getContext(), R.raw.marimba);

                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();

                    message = messageTemp; // 將新訊息更新到 message
                    unshow.setVisibility(View.VISIBLE); // unshow 顯示
                    unshow.setText("未讀"+message.length+"則"); // 設定數量
                    numberofProgressBar = message.length;
                    addPrograssbar(view,  numberofProgressBar);
                    setProgressBarToEnd(currentProgressBarIndex);

                    currentProgressBarIndex = (currentProgressBarIndex + 1) % numberofProgressBar;
                    if (currentProgressBarIndex == 0) {
                        resetProgressBar();
                    }

                    showMessage(message);
                    startNextProgressBar_New(database, view);
                }

                else {
                    oneMessageTimer_His(view);
                }
            }

        };
        //
        countDownTimer.start();

    }

    // 單筆新訊息
    private void oneMessageTimer(View view){
        unshow.setVisibility(View.VISIBLE);
        unshow.setText("未讀"+message.length+"則");
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage));
        database.editIsNew(message[currentProgressBarIndex][4], 3);

        // 判斷是否需聲音
        if (Objects.equals(message[currentProgressBarIndex][5], "1")) {
            mediaPlayer = createAndStartMediaPlayer(getContext(), R.raw.marimba);

            database.editSound(message[currentProgressBarIndex][4], 0);
            message[currentProgressBarIndex][5] = "0";

        }


        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsUntileFinished = millisUntilFinished / 1000;
            }
            @Override
            public void onFinish() {
                messageTemp = database.getNewMessage(1);

                // 有新訊息
                if (messageTemp != null) {

                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();

                    message = combineMessage(message, messageTemp);
                    unshow.setVisibility(View.VISIBLE);
                    unshow.setText("未讀"+message.length+"則");
                    numberofProgressBar = message.length;
                    addPrograssbar(view,  numberofProgressBar);
                    setProgressBarToEnd(currentProgressBarIndex);

                    currentProgressBarIndex = (currentProgressBarIndex + 1) % numberofProgressBar;
                    if (currentProgressBarIndex == 0) {
                        resetProgressBar();
                    }

                    showMessage(message);
                    startNextProgressBar_New(database, view);
                }

                else {
                    oneMessageTimer(view);
                }
            }

        };
        //
        countDownTimer.start();

    }

    // 將進度條歸零
    private void resetProgressBar() {
        int numberofProgressBar = message.length;
        for (int j = 0; j < numberofProgressBar; j++) {
            View childView = progressBarContainer.getChildAt(j); // 取得在 progressBarContainer 中之子元素
            if (childView instanceof ProgressBar) {
                ProgressBar progressBar = (ProgressBar) childView; // 將childView轉換成 ProgressBar 型態 丟到 progressBar 變數中
                progressBar.setProgress(0); // 設定進度 0
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 暫停倒數
        if (countDownTimer != null)
            countDownTimer.cancel();
//        Log.d("New_message", "Pause");
        handler.removeCallbacksAndMessages(null); // 移除 handler 的等待處理

    }

    // 新增進度條
    private void addPrograssbar(View view, int numberofProgressBar){
        float density = getResources().getDisplayMetrics().density; // 取得螢幕密度

        int max_len = (int) ((480 - numberofProgressBar * 10) / numberofProgressBar); // 計算一個進度條長度(dp) numberofProgressBar * 10 為間隔
        max_len *= density; // dp to px
        int hight = (int) (20 * density); // dp to px

        Log.d("ProgressBarLen", String.valueOf(max_len));
        // 建立進度條
        for (int i = 0; i < numberofProgressBar; i++) {

            ProgressBar progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal); // 設定進度條樣式
            progressBar.setMax(100); // 設定最大值 100
            progressBar.setTag(i); // 設定標籤
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.white))); // 設定進度條顏色為白色
            // 設定子元素大小
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    max_len,
                    hight
            );

            layoutParams.setMargins(5, 0, 5, 0); // 外邊距離 前後各 5 px
            // 檢查此進度條是否有父容器，若有則先刪除
            if (progressBar.getParent() != null) {
                ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            }
            // 加入到容器中
            progressBarContainer.addView(progressBar, layoutParams);
        }

    }


    // 取得未讀訊息
    private void getUnReadMessage(){
        message = database.getNewMessage(1); // 取得 isNew = 1 , getNewMessage 將 isNew = 1 之資料都改成 isNew = 2
        Log.d("getUnReadMessage", Arrays.deepToString(message));
        message = combineMessage(database.getNewMessage(2), database.getNewMessage(3)); // 再取得 isNew = 2 與 isNew = 3 之資料
    }

    // 將進度條填滿
    private void setProgressBarToEnd(int BarIndex){
        // 將在 BarIndex 以前的進度條都填滿
        for (int i = 0; i <= BarIndex; i++) {
            View childView = progressBarContainer.getChildAt(i);
            if (childView instanceof ProgressBar) {
                ProgressBar progressBar = (ProgressBar) childView;
                progressBar.setProgress(100);
            }
        }
    }


    private MediaPlayer createAndStartMediaPlayer(Context context, int resId) {
        MediaPlayer mp;
        if (resId != 0) {
            // Create MediaPlayer with local resource
            mp = MediaPlayer.create(context, resId);
        } else {
            throw new IllegalArgumentException("Either resId or uri must be provided");
        }

        if (mp != null) {
            // Set up a listener for handling playback completion
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Handle the end of the playback, if necessary
                    mp.release();
                }
            });

            // Set up an error listener
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // Handle the error and release the MediaPlayer
                    mp.release();
                    return true;
                }
            });

            // Start playback
            mp.start();
        }
        return mp;
    }


}

