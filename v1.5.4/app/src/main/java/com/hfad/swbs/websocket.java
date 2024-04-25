package com.hfad.swbs;



import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class websocket extends WebSocketListener {
    private WebSocket webSocket;
    private final MyDatabaseHelper database;
    Gson gson = new Gson();
    private final Context context;

    private MediaPlayer mediaPlayer;
    String ClassNum;
    int retryAttempts = 0;
    int returnKey;
//    int MAX_RETRY_ATTEMPTS = 40;
    int RETRY_DELAY_MS = 15000;
    public websocket(Context context) {
        this.context = context;
        database = new MyDatabaseHelper(context);
    }
    public void startWebSocket() {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://192.168.56.1:8000")   //    140.119.99.17:80   192.168.56.1:8000
                .build();

        webSocket = client.newWebSocket(request, this);


    }



    public void close() {
        webSocket.close(10001, "WebSocket closed");
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
//        SQLiteDatabase db = database.getReadableDatabase();
        ClassNum = database.getClassNumber();

        if (!Objects.equals(ClassNum, "-1")) {
            webSocket.send(ClassNum);
            broadcastConnectionStatus(true);
        } else {
            Log.d("ClassNumError", "There is no class number");
            Intent intent1 = new Intent(context, MainActivity.class);
            context.startActivity(intent1);
        }

    }

    Type listType = new TypeToken<List<Message>>(){}.getType();
//    ContentValues values = new ContentValues();

    List<Message> messages = new ArrayList<>();
    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        SQLiteDatabase db = database.getReadableDatabase();
        ContentValues values = new ContentValues();
        Log.d("Get", "Get message"+ text);

        messages = gson.fromJson(text, listType);
        for (Message message: messages) {
            returnKey = message.getprimaryKey();
            webSocket.send(String.valueOf(returnKey));
            values.put("onServerID", returnKey);
            values.put("teacher", message.getTeacher());
            values.put("fromWhere", message.getFromWhere());
            values.put("content", message.getContent());
            values.put("sendtime", message.getSendTime());
            values.put("isNew", message.getIsNew());
            values.put("finishDate", message.getFinish_date());
            values.put("sound", message.getSound());
            db.insert("mytable", null, values);

            db.close();

        }

//
//        Intent intent = new Intent(context, CountDown.class);
//        context.startService(intent);

////        //intent.putExtra("fragmentTag", "History_message");

//        Intent intent1 = new Intent(context, messageActivity.class);
//        intent1.putExtra("fragmentTag", "New_message");
//        context.startActivity(intent1);

    }



    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        startWebSocket();
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        Log.d("onClosed", "reason");
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
        Log.e("Error", "some thing went wrong " + t);
        broadcastConnectionStatus(false);

        Log.d("Websocket", "Retrying... Attempt: " + retryAttempts);



//
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

            }
            });


            // 使用 Handler 進行重試
            // 重新啟動 WebSocket 連接
            new Handler(Looper.getMainLooper()).postDelayed(
                    this::startWebSocket, RETRY_DELAY_MS);

    }

    private void broadcastConnectionStatus(boolean isConnected) {
        Intent intent = new Intent("websocket_connection_status");
        intent.putExtra("is_connected", isConnected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    public static class Message {
        private int id;

        private String name;
        private String office;
        private String content;
        private String time;
        private int is_new;
        private String finish_date;
        private  int sound;

        public int getprimaryKey(){ return id;}
        public String getTeacher() {
            return name;
        }
        public String getFromWhere() {
            return office;
        }
        public String getContent() {
            return content;
        }
        public String getSendTime() {
            return time;
        }
        public String getFinish_date(){
            return finish_date;
        }
        public int getIsNew() {
            return is_new;
        }
        public int getSound(){return sound;}
    }
}
