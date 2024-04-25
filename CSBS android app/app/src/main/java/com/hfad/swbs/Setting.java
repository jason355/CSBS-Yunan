package com.hfad.swbs;

import static com.google.gson.internal.GsonBuildConfig.VERSION;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class Setting extends AppCompatActivity {


    FloatingActionButton backfab;

    TextView ClassNumber_textView;
    TextView ClassName_textView;

    ImageView imageView3;
    MyDatabaseHelper database;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new MyDatabaseHelper(this);
        setContentView(R.layout.activity_setting);
        backfab = findViewById(R.id.backfab);
        ClassNumber_textView = findViewById(R.id.ClassNumber);
        ClassName_textView = findViewById(R.id.ClassName);
        imageView3 = findViewById(R.id.imageView3);



        String classNumber = database.getClassNumber(null);
        String className = database.getClassName();

        if (!"-1".equals(classNumber) && !"-1".equals(className)) {
            ClassNumber_textView.setText("教室代碼: " + classNumber);
            ClassName_textView.setText("教室名稱: " + className);
        }


        backfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Setting.this, messageActivity.class);
                startActivity(intent);
            }
        });

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Setting.this, messageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);


            }
        });


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(Setting.this, "對不起 請使用 home 鍵回覆。", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


    }
}