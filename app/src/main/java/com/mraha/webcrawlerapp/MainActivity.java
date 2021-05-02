package com.mraha.webcrawlerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private Button buttonView;
    private EditText keywordView;
    private EditText urlView;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keywordView = findViewById(R.id.keywordView);
        buttonView = findViewById(R.id.buttonView);
        resultView = findViewById(R.id.resultView);
        urlView = findViewById(R.id.urlView);
        buttonView.setOnClickListener(v -> startSearch());
    }

    private void startSearch() {
        String url = urlView.getText().toString();

        if (url.isEmpty() || !url.contains("http")) {
            Toast.makeText(this, "Wrong URL!", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                Document document = Jsoup.connect(url).get();
                String str = "result is " + (document.text().toLowerCase()
                        .split(keywordView.getText().toString().toLowerCase()).length - 1);
                runOnUiThread(() -> resultView.setText(str));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

}
