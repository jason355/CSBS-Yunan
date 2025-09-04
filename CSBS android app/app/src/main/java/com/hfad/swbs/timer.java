package com.hfad.swbs;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class timer {
    MyDatabaseHelper database;

    int[][] breakTime;
    int delaysec;
    int i = 0;
    int next = 0;
    Context context;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalDateTime currentTime;


    private static final String TAG = "GitHubFetch";
    private static final String URL = "https://raw.githubusercontent.com/jason355/CSBS-Yunan/refs/heads/main/coursetable.json";

    Gson gson = new Gson();
    public timer (Context context) {
        this.context = context;
        database = new MyDatabaseHelper(context);
    }

    public void init(BreakTimeCallback callback) {
        fetchDataFromGitHub(data -> {
            this.breakTime = data;
            callback.onLoaded(data);
        });
    }
    public int startClock() {
        currentTime = LocalDateTime.now();

        String timeString = currentTime.format(formatter);


        int hour = Integer.parseInt(timeString.split(":")[0]);
        int minute = Integer.parseInt(timeString.split(":")[1]);
        if (next >= 8) {
            delaysec = (24 * 3600000) - (hour * 3600000 + minute * 60000);
            Log.d("DelaySec", String.valueOf(delaysec));
            return delaysec;
        }
//
//        Log.d("NowHour", String.valueOf(hour));
//        Log.d("NowMinute", String.valueOf(minute));
//        Log.d("breakHour", String.valueOf(breakTime[next][0]));
//        Log.d("breakStartMin", String.valueOf(breakTime[next][1]));
        if (hour > breakTime[next][0]) {
            Log.d("timer process", "hour > breakTime");
            next++;
            return -1;
        } else if (hour < breakTime[next][0]) {
            haveCountDown(false);
            database.editHaveCountDown(0);
            delaysec = (breakTime[next][0] - hour) * 3600;
            delaysec -= (minute - breakTime[next][1]) * 60;
            int seconds = currentTime.getSecond();
            if (seconds > 30) {
                delaysec -= seconds;
            }
            delaysec *= 1000;
            Log.d("timer process", "delay" + delaysec);
            return delaysec;

        } else if (minute > breakTime[next][2]) {
            Log.d("timer process", "minute >= breakTime");
            next++;
        } else if (minute < breakTime[next][1]) {
            database.editHaveCountDown(0);
            delaysec = (breakTime[next][1] - minute) * 60 * 1000;
            int seconds = currentTime.getSecond();
            if (seconds > 30) {
                delaysec -= seconds * 1000;
            }
            Log.d("timer process", "delay" + delaysec);
            return delaysec;

        } else if (((breakTime[next][2] - minute) > 5)) {
            Log.d("time process", "return 1");
            return 1;
        } else {
            if (breakTime[next][0] == 16 && (breakTime[next][2] - minute) > 3) {
                Log.d("time process", "return 1");
                return 1;
            } else if (breakTime[next][0] == 13) {
                Log.d("time process", "return 1");
                return 1;
            }
            Log.d("time process", "return 0");
            return 0;
        }
        return -1;
    }

    public interface BreakTimeCallback {
        void onLoaded(int[][] breakTime);
    }

    private void fetchDataFromGitHub(BreakTimeCallback callback) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Fetch failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    // 將 JSON 轉成二維 int array
                    breakTime = gson.fromJson(json, int[][].class);
                    callback.onLoaded(breakTime);

                }
            }
        });

    }


    private void haveCountDown(boolean yeah){
        Intent intent = new Intent("haveCountDown");
        intent.putExtra("key", yeah);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
