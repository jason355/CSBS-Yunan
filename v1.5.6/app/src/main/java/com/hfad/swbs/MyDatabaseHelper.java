package com.hfad.swbs;




import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static int DATABASE_VERSION = 3;

//    private final String classNumber = getClassNumber();

    String[][] message;
    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建立資料表
        String createTableQuery = "CREATE TABLE if not exists " +
                " mytable (id INTEGER PRIMARY KEY AUTOINCREMENT, onServerID INTEGER,teacher varchar(15), " +
                "fromWhere varchar(15), content TEXT, " +
                "sendtime DATETIME, isNew int, finishDate varchar(10), sound int)";
        db.execSQL(createTableQuery);


        createTableQuery = "CREATE TABLE if not exists " +
                "initData (id INTEGER PRIMARY KEY AUTOINCREMENT, classNumber varchar(5) UNIQUE, className varchar(10) UNIQUE, messageStat int DEFAULT 0, haveCountDown int DEFAULT 0, counting int DEFAULT 0, canUpgrade int DEFAULT -1)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 更新資料庫
        if (oldVersion == 3) {
            String dropTableQuery = "DROP TABLE IF EXISTS mytable";
            db.execSQL(dropTableQuery);
            dropTableQuery = "DROP TABLE IF EXISTS initData";
            db.execSQL(dropTableQuery);
            onCreate(db);
        } else {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE initData ADD COLUMN canUpgrade int DEFAULT -1");
            }
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE initData ADD COLUMN className varchar(10)");
                String defaultValues = getClassNumber(db);
                ContentValues values = new ContentValues();
                values.put("className", defaultValues + " 班");
                db.update("initData", values, null, null);
            }
        }
    }


    public void editUpdate(int data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("canUpgrade", data);
        db.update("initData", values, null, null);
        db.close();
    }
    public int checkUpdate() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT canUpgrade FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("canUpgrade");
                int status = cursor.getInt(index);
                cursor.close();
                return status;
            }
            cursor.close();
        }

        return 0;
    }

    public void editHaveCountDown(int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("haveCountDown", status);
        db.update("initData", values, null, null);
        db.close();
    }

    public int checkHaveCountDown() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT haveCountDown FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("haveCountDown");
                int status = cursor.getInt(index);
                cursor.close();
                return status;
            }
            cursor.close();
        }

        return 0;
    }
    public void editCounting(int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("counting", status);
        db.update("initData", values, null, null);
        db.close();
    }

    public int checkCounting() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT counting FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("counting");
                int status = cursor.getInt(index);
                cursor.close();

                return status;
            }
            cursor.close();

        }
        return 0;
    }
    public void editMessageStat(int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageStat", status);
        db.update("initData", values, null, null);
        db.close();
    }

    public int checkConnection() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT connection FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("connection");
                int status = cursor.getInt(index);
                cursor.close();

                return status;
            }
            cursor.close();

        }
        return 0;
    }

    public void upDateConnection(int connection) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("connection", connection);
        db.update("initData", values, null, null);
        db.close();
    }
    public int checkMessageStat() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT MessageStat FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("messageStat");
                int messageStat = cursor.getInt(index);
                cursor.close();

                return messageStat;
            }
            cursor.close();
        }
        return 0;
    }
    public String getClassNumber(SQLiteDatabase db) {
        if (db == null) {
            db = getReadableDatabase();
        }
        String query = "SELECT classNumber FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            int index = cursor.getColumnIndex("classNumber");
            String classNumber = cursor.getString(index);
            if (classNumber != null) {
                cursor.close();
                return classNumber;
            }
        }
        return "-1";
    }

    public String getClassName() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT className FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            int index = cursor.getColumnIndex("className");
            String className = cursor.getString(index);
            if (className != null) {
                cursor.close();
                return className;
            }
        }
        return "-1";
    }


    public Boolean isFirstTime() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT firstTime FROM initData";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            int index = cursor.getColumnIndex("firstTime");
            String className = cursor.getString(index);
            if (Objects.equals(className, "1")) {
                cursor.close();
                return true;
            }
        }
        return false;
    }

    public int countUnread() {
        SQLiteDatabase db = getReadableDatabase();
        int length = 0;
        try {
            db = getWritableDatabase();
            String search = "SELECT * FROM mytable WHERE isNew != 0";
            try (Cursor cursor = db.rawQuery(search, null)) {
                length = cursor.getCount();
                return length;
            }
        } catch (SQLiteException e) {
            Log.e("Database Error", "Error accessing database or executing queries", e);
            return -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void messageDateCheck() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String search = "SELECT * FROM mytable WHERE isNew = 0";
            try (Cursor cursor = db.rawQuery(search, null)) {
                while (cursor.moveToNext()) {
                    int finishDateIndex = cursor.getColumnIndex("finishDate");
                    String datetime = cursor.getString(finishDateIndex);

                    LocalDate finishDate = LocalDate.parse(datetime, formatter);
                    LocalDate today = LocalDate.now();
                    long daysDifference = ChronoUnit.DAYS.between(today, finishDate);
                    Log.d("Database task", "finishDate - today: " + daysDifference);
                    if (daysDifference < 0) {
                        int idIndex = cursor.getColumnIndex("id");
                        int id = cursor.getInt(idIndex);
                        db.delete("mytable", "id = ?", new String[]{String.valueOf(id)});
                    }
                }
            }
        } catch (SQLiteException e) {
            Log.e("Database Error", "Error accessing database or executing queries", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }

    }

    //    public String[][] getMessageTemp(){
//
//    }
    public String[][] getMessage() {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String search = "SELECT onServerID, teacher, fromWhere, content, sendtime FROM mytable where isNew = 3 ORDER BY sendtime DESC";
        Cursor cursor = db.rawQuery(search, null);
        String search2 = "SELECT onServerID, teacher, fromWhere, content, sendtime FROM mytable where isNew = 0 ORDER BY sendtime DESC";
        Cursor cursor2 = db.rawQuery(search2, null);
        message = new String[cursor.getCount() + cursor2.getCount()][5];
        if (cursor.moveToFirst()) {
            do {
                int onServerIDIndex = cursor.getColumnIndex("onServerID");
                int teacherIndex = cursor.getColumnIndex("teacher");
                int fromWhereIndex = cursor.getColumnIndex("fromWhere");
                int contentIndex = cursor.getColumnIndex("content");
                int sendtimeIndex = cursor.getColumnIndex("sendtime");
                String onServerID = cursor.getString(onServerIDIndex);
                String teacher = cursor.getString(teacherIndex);
                String fromWhere = cursor.getString(fromWhereIndex);
                String content = cursor.getString(contentIndex);
                String sendtime = cursor.getString(sendtimeIndex);
                message[count][4] = onServerID;
                message[count][0] = teacher;
                message[count][1] = fromWhere;
                message[count][2] = content;
                message[count][3] = sendtime;
                count++;
            } while (cursor.moveToNext());
            cursor.close();
            Log.d("getMessage1", Arrays.deepToString(message));

        } else {
            cursor.close();
        }

        if (cursor2.moveToFirst()) {
            do {
                int onServerIDIndex = cursor2.getColumnIndex("onServerID");
                int teacherIndex = cursor2.getColumnIndex("teacher");
                int fromWhereIndex = cursor2.getColumnIndex("fromWhere");
                int contentIndex = cursor2.getColumnIndex("content");
                int sendtimeIndex = cursor2.getColumnIndex("sendtime");
                String onServerID = cursor2.getString(onServerIDIndex);
                String teacher = cursor2.getString(teacherIndex);
                String fromWhere = cursor2.getString(fromWhereIndex);
                String content = cursor2.getString(contentIndex);
                String sendtime = cursor2.getString(sendtimeIndex);
                message[count][4] = onServerID;
                message[count][0] = teacher;
                message[count][1] = fromWhere;
                message[count][2] = content;
                message[count][3] = sendtime;
                count++;
            } while (cursor2.moveToNext());
            cursor2.close();
            Log.d("getMessage2", Arrays.deepToString(message));
            return message;
        } else {
            cursor2.close();
            if (message != null) {
                return message;
            }
            else {
                return  null;
            }
        }
    }


    public boolean checkNewMessage(int isNewNum) {
        SQLiteDatabase db = getReadableDatabase();
        String search = "SELECT isNew FROM mytable WHERE isNew = " + isNewNum;
        Cursor cursor = db.rawQuery(search, null);
        if (cursor.moveToFirst() && cursor.getCount() != 0) {
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }
    public String[][] getNewMessage(int isNewNum) {
        int count = 0;
        String[][] empty = new String[0][6];
        SQLiteDatabase db = getReadableDatabase();
        String search = "SELECT onServerID, teacher, fromWhere, content, sendtime, sound FROM mytable where isNew = " + isNewNum;
        Cursor cursor = db.rawQuery(search, null);
        if (cursor.getCount() != 0) {
            message = new String[cursor.getCount()][6];
            if (cursor.moveToFirst()) {
                do {

                    int onServerIDIndex = cursor.getColumnIndex("onServerID");
                    int teacherIndex = cursor.getColumnIndex("teacher");
                    int fromWhereIndex = cursor.getColumnIndex("fromWhere");
                    int contentIndex = cursor.getColumnIndex("content");
                    int sendtimeIndex = cursor.getColumnIndex("sendtime");
                    int soundIndex = cursor.getColumnIndex("sound");

                    String onServerID = cursor.getString(onServerIDIndex);
                    String teacher = cursor.getString(teacherIndex);
                    String fromWhere = cursor.getString(fromWhereIndex);
                    String content = cursor.getString(contentIndex);
                    String sendtime = cursor.getString(sendtimeIndex);
                    String sound = Integer.toString(cursor.getInt(soundIndex));


                    message[count][0] = teacher;
                    message[count][1] = fromWhere;
                    message[count][2] = content;
                    message[count][3] = sendtime;
                    message[count][4] = onServerID;
                    message[count][5] = sound;

                    if (isNewNum == 1) {
                        editIsNew(onServerID, 2);
                    }



                    count++;


                } while (cursor.moveToNext());
                cursor.close();
                Log.d("MyDatabase message", Arrays.deepToString(message));
                return message;
            } else {
                cursor.close();
                return empty;
            }
        } else {
            return null;
        }

    }

    public int checkIsNew(String ID){
        SQLiteDatabase db = getReadableDatabase();
        String search = "SELECT isNew FROM mytable where onServerID = "+ID;
        Cursor cursor = db.rawQuery(search, null);
        if (cursor.moveToFirst()){
            int index = cursor.getColumnIndex("isNew");
            return cursor.getInt(index);
        }
        return -1;
    }


    public void editSound(String onServerID, int editTo){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sound", Integer.toString(editTo));
        db.update("mytable", values, "onServerID = ?",new String[]{onServerID});
    }
    public void editIsNew(String onServerID, int isNewNum) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isNew", isNewNum);
        db.update("mytable", values, "onServerID = ?",new String[]{onServerID});
//        db.setTransactionSuccessful(); // 提交事務
//        db.endTransaction(); // 結束事務
        db.close();
    }


}
