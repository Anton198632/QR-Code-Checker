package com.builderlinebr.qr_codechecker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class ResultViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_view);


        String url = getIntent().getStringExtra("url");
        if (url.indexOf("https://www.gosuslugi.ru/") == 0){
            Uri page = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, page);
            startActivity(intent);
        }



    }
}