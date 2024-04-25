package com.hfad.swbs;




import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class timer {
    MyDatabaseHelper database;

    int[][] breakTime;
    int delaysec;
    int i = 0;
    int next = 0;
    Context context;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalDateTime currentTime;
    public timer (Context context) {
        this.context = context;

        database = new MyDatabaseHelper(context);
        getBreakTime();
        //Log.e("Break time", Integer.toString(breakTime[0][0]));
    }

    public int startClock() {
        currentTime = LocalDateTime.now();

        String timeString = currentTime.format(formatter);


        int hour = Integer.parseInt(timeString.split(":")[0]);
        int minute = Integer.parseInt(timeString.split(":")[1]);
        if (next > 8) {
            delaysec = (24 * 3600000) - (hour * 3600000 + minute * 60000);
            Log.d("DelaySec", String.valueOf(delaysec));
            return delaysec;
        }
//
        Log.d("NowHour", String.valueOf(hour));
        Log.d("NowMinute", String.valueOf(minute));
        Log.d("breakHour", String.valueOf(breakTime[next][0]));
        Log.d("breakStartMin", String.valueOf(breakTime[next][1]));
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


    public void getBreakTime() {
//        SQLiteDatabase readData = database.getReadableDatabase();
//        Cursor cursor = readData.query("initData", projection, null, null, null, null, null);
        breakTime = new int[27][3];

        i = 0;
        for (int j = 8; j < 16; j++) {  // 8 to 16 O'clock
            if (j == 8) {
                breakTime[i][0] = 8;
                breakTime[i][1] = 5;
                breakTime[i][2] = 9;
            }
            if (j == 12) {
                breakTime[i][0] = 12;
                breakTime[i][1] = 0;
                breakTime[i][2] = 30;
            }
            else if (j == 13){
                breakTime[i][0] = 13;
                breakTime[i][1] = 5;
                breakTime[i][2] = 9;
            }
            else if (j == 15){
                breakTime[i][0] = 15;
                breakTime[i][1] = 0;
                breakTime[i][2] = 14;
            }
            else {
                breakTime[i][0] = j;
                breakTime[i][1] = 0;
                breakTime[i][2] = 9;
            }
            i++;
    }


        //            for (int k = 0; k < 3;k++) {
//                if (k == 0) {
//                    breakTime[i][0] = j;
//                    breakTime[i][1] = 0;
//                    breakTime[i][2] = 10;
//                }
//                else if (k == 1) {
//                    breakTime[i][0] = j;
//                    breakTime[i][1] = 20;
//                    breakTime[i][2] = 30;
//                }
//                else if (k == 2) {
//                    breakTime[i][0] = j;
//                    breakTime[i][1] = 40;
//                    breakTime[i][2] = 50;
//                }
//                i++;
//            }
//        }


    }


    private void haveCountDown(boolean yeah){
        Intent intent = new Intent("haveCountDown");
        intent.putExtra("key", yeah);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
