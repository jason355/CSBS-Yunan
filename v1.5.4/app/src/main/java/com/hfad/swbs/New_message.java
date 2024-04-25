package com.hfad.swbs;



import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;

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


    private String mParam2;
    int seeing = 0;

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
    String[][] message;
    String[][] messageTemp;

    private Handler handler;

    private CountDownTimer countDownTimer;
    private LinearLayout progressBarContainer;
    int currentProgressBarIndex = 0;


    Button readfab;

    MyDatabaseHelper database;

    private  int TOTAL_TIME_MS = 10000;  // 進度條總時間（毫秒）
    private static final int INTERVAL_MS = 20;
    int numberofProgressBar = 0;

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
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_new_message, container, false);
        view.setTag("New_message");

        database = new MyDatabaseHelper(requireContext());

        teacher = view.findViewById(R.id.teacher);
        fromWhere = view.findViewById(R.id.fromWhere);
        content = view.findViewById(R.id.content);
        sendtime = view.findViewById(R.id.sendtime);
        page_num = view.findViewById(R.id.page_num);
        historyMessage = view.findViewById(R.id.historyMessage);
        progressBarContainer = view.findViewById(R.id.progressBarContainer);
        class_num = view.findViewById(R.id.class_num);
        cardView = view.findViewById(R.id.cardView);
        readfab = view.findViewById(R.id.readbutton);
        readText = view.findViewById(R.id.readText);
        unshow = view.findViewById(R.id.unshow);
        nowtime = view.findViewById(R.id.nowtime);

        handler = new Handler();
        startUpdatingTime();

        if (database.countUnread() > 0){
            unshow.setText("未讀"+database.countUnread()+"則");
        }


        Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();

        readfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (message[currentProgressBarIndex].length == 6) {
//                    readfab.setVisibility(View.INVISIBLE);
                    database.editIsNew(message[currentProgressBarIndex][4], 0);
//                    readText.setText("已讀");
                    if (database.countUnread() > 0){
                        unshow.setText("未讀"+database.countUnread()+"則");
                    } else {
                        unshow.setVisibility(View.INVISIBLE);
                    }

                    readText.setTextColor(ContextCompat.getColor(getContext(), R.color.read));
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
                    if (messageTemp != null && messageTemp.length != 0) {

                        Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();
                        historyMessage.setVisibility(View.INVISIBLE);

                        message = combineMessage(message, messageTemp);

//                        Log.d("combineMessage", Arrays.deepToString(message));
                        if (message.length == 1) {
//                            Log.d("FabClick1", "New message Len = 1");
                            currentProgressBarIndex = 0;
                            seeing =0;
                            showMessage(message);
                            oneMessageTimer(view);
                        } else if (numberofProgressBar != 0) {
                            currentProgressBarIndex--;
                            if (database.checkIsNew(message[(currentProgressBarIndex + 1) % numberofProgressBar][4]) == 3) {
//                                Log.d("FabClick2", "Jump to unShown");
                                currentProgressBarIndex = messageLen;
                                seeing = messageLen;
//                            Log.d("FabClick2", Arrays.deepToString(message));
//                            Log.d("FabClick2", "currentBar: "+currentProgressBarIndex);
//                            Log.d("FabClick2", "seeing: "+seeing);
                                numberofProgressBar = message.length;
//                            Log.d("FabClick2", "Bar Len: "+ message.length);
                                addPrograssbar(view,  numberofProgressBar);
                                setProgressBarToEnd(currentProgressBarIndex-1);

                                showMessage(message);
                                startNextProgressBar_New(database, view);
                            } else {
//                                Log.d("FabClick3", "currentBar: "+currentProgressBarIndex);
//                                Log.d("FabClick3", "seeing: "+seeing);
                                numberofProgressBar = message.length;
//                                Log.d("FabClick3", "numberofProgressBar: "+numberofProgressBar);
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

                    } else if (message.length == 0) {
//                        Log.d("FabClick4", "message len = 0");
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


        class_num.setText(database.getClassNumber() + " 班");

        historyMessage.setVisibility(View.INVISIBLE);



        getUnReadMessage();
//        Log.d("getUnRead", Arrays.deepToString(message));
        if (message != null && message.length != 0) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage));
            database.editMessageStat(1);
            historyMessage.setVisibility(View.INVISIBLE);
            int numberofProgressBar = message.length;
            if (numberofProgressBar != 1) {
                addPrograssbar(view, numberofProgressBar);
                showMessage(message);
                startNextProgressBar_New(database, view);

            }
            else {
                historyMessage.setVisibility(View.INVISIBLE);
                showMessage(message);
                oneMessageTimer(view);
            }

        } else {
            isOpenBroadCast(3);
            database.editMessageStat(3);
            message = database.getMessage();
            cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage));

            if (message != null && message.length != 0) {

                int numberofProgressBar = message.length;
                historyMessage.setVisibility(View.VISIBLE);
                if (numberofProgressBar != 1) {
                    addPrograssbar(view, numberofProgressBar);
                    showMessage(message);
                    startNextProgressBar(database, view);
                }
                else {
                    historyMessage.setVisibility(View.VISIBLE);
                    showMessage(message);
                    oneMessageTimer_His(view);
                }
            } else {
                content.setText("無新訊息");
            }
        }
        return view;

    }


    private void startUpdatingTime() {
        // 创建一个 Runnable 来更新时间
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // 获取当前时间
                Date currentTime = new Date();

                // 格式化时间
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                String formattedTime = sdf.format(currentTime);

                // 设置给 TextView
                nowtime.setText(formattedTime);

                // 一段时间后再次执行此 Runnable
                handler.postDelayed(this, 1000); // 1000毫秒 = 1秒
            }
        };

        // 首次执行此 Runnable
        updateTimeRunnable.run();
    }



    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void showMessage(String[][] message) {
        int len = message.length;
        if (seeing >= len) {
            seeing = 0;
        }


//        Log.d("showMessage", message[seeing][2]);

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





    @SuppressLint("SetTextI18n")
    private void startNextProgressBar_New(MyDatabaseHelper database, View view) {
        numberofProgressBar = message.length;
        database.editIsNew(message[currentProgressBarIndex][4], 3);
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage));
        final ProgressBar currentProgressBar = (ProgressBar) progressBarContainer.getChildAt(currentProgressBarIndex);
        unshow.setVisibility(View.VISIBLE);
        unshow.setText("未讀"+message.length+"則");
        if (Objects.equals(message[currentProgressBarIndex][5], "1")) {
//            cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage));
//            currentProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.NewMessage)));
            MediaPlayer mediaPlayer = MediaPlayer.create(content.getContext(), R.raw.marimba);
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            message[currentProgressBarIndex][5] = "0";
            database.editSound(message[currentProgressBarIndex][4], 0);
        }
//        else {
////            cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage));
////            currentProgressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
//
//        }
//        int words = message[currentProgressBarIndex][2].length();
//        if (words > 10 && words < 20) {
//            TOTAL_TIME_MS = (int) (words * 210);
//        }
//        else if (words > 30){
//            TOTAL_TIME_MS = (int) (words * 230);
//
//        } else {
//            TOTAL_TIME_MS = 10000;
//        }


        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {


            @Override
            public void onTick(long millisUntilFinished) {

                int progress = (int) ((TOTAL_TIME_MS - millisUntilFinished) * 100 / TOTAL_TIME_MS);
                currentProgressBar.setProgress(progress);

            }

            @Override
            public void onFinish() {
                currentProgressBar.setProgress(100);
                messageTemp = database.getNewMessage(1);
                if (messageTemp != null && messageTemp.length != 0){
                    unshow.setVisibility(View.VISIBLE);
                    unshow.setText("未讀"+database.countUnread()+"則");
//                    MediaPlayer mediaPlayer = MediaPlayer.create(content.getContext(), R.raw.marimba);
//                    mediaPlayer.seekTo(0);
//                    mediaPlayer.start();
                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();
                    historyMessage.setVisibility(View.INVISIBLE);
                    for (int i = 0; i < message.length; i++){
                        View progressBar = progressBarContainer.getChildAt(0);
                        progressBarContainer.removeView(progressBar);
                    }

                    if (numberofProgressBar != 0) {
                        if (database.checkIsNew(message[(currentProgressBarIndex + 1) % numberofProgressBar][4]) == 3) {
                            currentProgressBarIndex = message.length -1;
                            seeing = message.length;

                        }
                    }
                    message = combineMessage(message, messageTemp);
                    numberofProgressBar = message.length;
//                    Log.d("numberofProgressBar", String.valueOf(message.length));
                    addPrograssbar(view,  numberofProgressBar);
                    setProgressBarToEnd(currentProgressBarIndex);
                }
                currentProgressBarIndex = (currentProgressBarIndex + 1) % numberofProgressBar;
                if (currentProgressBarIndex == 0) {
                    resetProgressBar();
                }

                showMessage(message);
                startNextProgressBar_New(database, view);

            }

        };
        //
        countDownTimer.start();


    }

    private static String[][] removeMessage(String[][] array, int indexToRemove) {
//        if (array[0].length == 5) {
//            return array;
//        }

        String[][] newArray = new String[array.length - 1][6];
        int top = 0;
        for (int i = 0; i < array.length; i++) {
//            Log.d("removeMessage", String.valueOf(i));
            if (i != indexToRemove) {
                for (int j = 0; j < 6; j++) {
                    newArray[top][j] = array[i][j];
                }
                top++;
            }

        }
        return newArray;
    }
    private void startNextProgressBar(MyDatabaseHelper database, View view) {
        unshow.setVisibility(View.INVISIBLE);
        readfab.setVisibility(View.INVISIBLE);
        int numberofProgressBar = message.length;
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage));
        readText.setText("已讀");

        final ProgressBar currentProgressBar = (ProgressBar) progressBarContainer.getChildAt(currentProgressBarIndex);

        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {
//            ProgressBar currentProgressBar = (ProgressBar) progressBarContainer.getChildAt(currentProgressBarIndex);
            @Override
            public void onTick(long millisUntilFinished) {

                int progress = (int) ((TOTAL_TIME_MS - millisUntilFinished) * 100 / TOTAL_TIME_MS);
                currentProgressBar.setProgress(progress);

            }

            @Override
            public void onFinish() {
                currentProgressBar.setProgress(100);
                messageTemp = database.getNewMessage(1);
                if (messageTemp != null && messageTemp.length != 0) {
                    historyMessage.setVisibility(View.INVISIBLE);
                    for (int i = 0; i < message.length; i++){
                        View progressBar = progressBarContainer.getChildAt(0);
                        progressBarContainer.removeView(progressBar);
                    }
                    message = messageTemp;
                    unshow.setVisibility(View.VISIBLE);
                    unshow.setText("未讀"+message.length+"則");

                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();
                    int numberofProgressBar = message.length;
                    if (numberofProgressBar != 1) {

                        addPrograssbar(view, numberofProgressBar);
                        currentProgressBarIndex = 0;
                        seeing = 0;
                        showMessage(message);
                        startNextProgressBar_New(database, view);
                    }
                    else {
                        seeing = 0;
                        currentProgressBarIndex = 0;

                        showMessage(message);
                        oneMessageTimer(view);
                    }

                } else {
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
        String[][] NewArray = new String[len1 + len2][6];
        int index = 0;
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
        return NewArray;

    }


    private void oneMessageTimer_His(View view){

        unshow.setVisibility(View.INVISIBLE);
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.HistoryMessage));
        readText.setText("已讀");
        readfab.setVisibility(View.INVISIBLE);
        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsUntileFinished = millisUntilFinished / 1000;
            }

            @Override
            public void onFinish() {


                messageTemp = database.getNewMessage(1);

                if (messageTemp != null) {
                    MediaPlayer mediaPlayer = MediaPlayer.create(content.getContext(), R.raw.marimba);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    Toast.makeText(content.getContext(), "新訊息!", Toast.LENGTH_LONG).show();

                    message = messageTemp;
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
                    oneMessageTimer_His(view);
                }
            }

        };
        //
        countDownTimer.start();

    }


    private void oneMessageTimer(View view){
        unshow.setVisibility(View.VISIBLE);
        unshow.setText("未讀"+message.length+"則");
        cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.NewMessage));
        database.editIsNew(message[currentProgressBarIndex][4], 3);

        if (Objects.equals(message[currentProgressBarIndex][5], "1")) {
//            Log.d("Sound", "True");
            MediaPlayer mediaPlayer = MediaPlayer.create(content.getContext(), R.raw.marimba);
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            database.editSound(message[currentProgressBarIndex][4], 0);
            message[currentProgressBarIndex][5] = "0";

        }


//        int words = message[currentProgressBarIndex][2].length();
//        if (words > 10 && words < 30) {
//            TOTAL_TIME_MS = (int) (words * 200);
//        }
//        else if (words > 30){
//            TOTAL_TIME_MS = (int) (words * 250);
//
//        } else {
//            TOTAL_TIME_MS = 8000;
//        }


        countDownTimer = new CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {

            @Override
            public void onTick(long millisUntilFinished) {
                long secondsUntileFinished = millisUntilFinished / 1000;
            }

            @Override
            public void onFinish() {

//                database.editIsNew(message[currentProgressBarIndex][4], 3);

                messageTemp = database.getNewMessage(1);

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

    private void resetProgressBar() {
        int numberofProgressBar = message.length;
        for (int j = 0; j < numberofProgressBar; j++) {
            View childView = progressBarContainer.getChildAt(j);
            if (childView instanceof ProgressBar) {
                ProgressBar progressBar = (ProgressBar) childView;
                progressBar.setProgress(0);
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
        if (countDownTimer != null)
            countDownTimer.cancel();
//        Log.d("New_message", "Pause");
        handler.removeCallbacksAndMessages(null);
    }

    private void addPrograssbar(View view, int numberofProgressBar){
        float density = getResources().getDisplayMetrics().density;

        int max_len = (int) ((480 - numberofProgressBar * 10) / numberofProgressBar);
        max_len *= density;
        int hight = (int) (20 * density);

        Log.d("ProgressBarLen", String.valueOf(max_len));
        for (int i = 0; i < numberofProgressBar; i++) {

            ProgressBar progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setTag(i);
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    max_len,
                    hight
            );

            layoutParams.setMargins(5, 0, 5, 0);
            if (progressBar.getParent() != null) {
                ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            }
            progressBarContainer.addView(progressBar, layoutParams);
//            Log.d("ProgressBar", "ProgressBar added at index:" + i);
        }

    }


    // Get unread message
    private void getUnReadMessage(){
        message = database.getNewMessage(1);
        Log.d("getUnReadMessage", Arrays.deepToString(message));
        message = combineMessage(database.getNewMessage(2), database.getNewMessage(3));


    }


    private void setProgressBarToEnd(int BarIndex){
        for (int i = 0; i <= BarIndex; i++) {
            View childView = progressBarContainer.getChildAt(i);
            if (childView instanceof ProgressBar) {
                ProgressBar progressBar = (ProgressBar) childView;
                progressBar.setProgress(100);
            }
        }
    }

    private void isOpenBroadCast(int isOpen) {
        Intent MessageOpen = new Intent("MessageActivity_Open");
        MessageOpen.putExtra("is_open", isOpen);
        LocalBroadcastManager.getInstance(content.getContext()).sendBroadcast(MessageOpen);
    }
}

