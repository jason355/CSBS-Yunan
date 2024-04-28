package com.hfad.swbs;



import static androidx.core.content.ContextCompat.startActivity;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    String ip = "ws://140.119.99.17:80";
    String ClassCode;
    String ClassName;
    int retryAttempts = 0;
    int returnKey;
    int RETRY_DELAY_MS = 5000;
    public websocket(Context context) {
        this.context = context;
        database = new MyDatabaseHelper(context);
    }
    public void startWebSocket() {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(ip)   //    140.119.99.17:80   192.168.56.1:8000
                .build();

        webSocket = client.newWebSocket(request, this);


    }



    public void close() {
        webSocket.close(10001, "WebSocket closed");
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
//        SQLiteDatabase db = database.getReadableDatabase();
        ClassCode = database.getClassNumber(null);
        ClassName = database.getClassName();
        Class_format Class = new Class_format();
        Class.setHeader("A0");
        Class.setCode(ClassCode);
        Class.setClassName(ClassName);

         // 設定資料標頭為何種目的 A0 首次登入


        // 使用 Gson 库将对象转换为 JSON 字符串
        String json = gson.toJson(Class);

        // 打印 JSON 字符串
        System.out.println(json);
        if (!Objects.equals(ClassCode, "-1")) {
            webSocket.send(json); // 傳送教室代碼與名稱
            broadcastConnectionStatus(true);
//            Intent intent = new Intent(context, messageActivity.class);
//            intent.putExtra("fragmentTag", "New_message");
//            context.startActivity(intent);
        } else {
            Log.d("ClassNumError", "There is no class number");
            Intent intent1 = new Intent(context, MainActivity.class);
            context.startActivity(intent1);
        }

    }


    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        SQLiteDatabase db = database.getReadableDatabase();
        ContentValues values = new ContentValues();
        Log.d("Get", "Get message"+ text);

        JsonObject jsonObject = gson.fromJson(text, JsonObject.class);
        // 確認是否有 header
        if (jsonObject.has("header")) {
            String type = jsonObject.get("header").getAsString(); // 取得 資料類型
            if ("S0".equals(type)) {
                boolean result = jsonObject.getAsJsonObject().get("result").getAsBoolean();
                Log.d("Server back result", String.valueOf(result));
            }
            else if ("S1".equals(type)) {

                JsonObject message = jsonObject.get("message").getAsJsonObject();

                Class_format return_to_serer = new Class_format(); // 建立訊息回傳確認實例
                returnKey = message.get("id").getAsInt();
                return_to_serer.setHeader("A2"); // 回傳廣播 ID
                return_to_serer.setReturnKey(String.valueOf(returnKey));
                String json = gson.toJson(return_to_serer);
                webSocket.send(json);

                values.put("onServerID", returnKey);
                values.put("teacher", message.get("name").getAsString());
                values.put("fromWhere", message.get("office").getAsString());
                values.put("content", message.get("content").getAsString());
                values.put("sendtime", message.get("time").getAsString());
                values.put("isNew", message.get("is_new").getAsString());
                values.put("finishDate", message.get("finish_date").getAsString());
                values.put("sound", message.get("sound").getAsString());
                db.insert("mytable", null, values);

                db.close();
            }





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

        if ("ws://140.119.99.17:80".equals(ip)) {
            ip = "ws://192.168.56.1:8000";
        } else {
            ip = "ws://140.119.99.17:80";
        }

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

    static class Class_format {
        private  String header;
        private String classCode;
        private String className;

        private String id;

        public String getHeader() {
            return header;
        }
        public void setHeader(String header) {
            this.header = header;

        }
        public String getCode() {
            return classCode;
        }

        public void setCode(String code) {
            this.classCode = code;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getReturnKey() {
            return id;
        }
        public void setReturnKey(String returnKey) {
            this.id = returnKey;
        }
    }
    public static class Message {

        private String header;
        private int id;
        private String name;
        private String office;
        private String content;
        private String time;
        private int is_new;
        private String finish_date;
        private  int sound;

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

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
