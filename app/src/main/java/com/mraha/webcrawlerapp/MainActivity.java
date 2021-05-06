package com.mraha.webcrawlerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button startSearchButton;
    private Button generateSCVButton;
    private EditText keywordView;
    private EditText urlView;
    private EditText linkSizeView;
    private EditText linkDepthView;
    private TextView resultView;
    private Context context;
    public static int MAX_LINK_DEPTH = 8;
    public static int MAX_CAPACITY = 10000;
    public AlertDialog progressBarDialog;
    private FileWriter fileWriter;
    private int previousLinkHoldersStorageSize = 0;
    public final List<LinkHolder> linkHoldersStorage = new ArrayList<>(MAX_CAPACITY);
    public static final String URL_CONST = "https://www.tut.by/";
    public static final String SEARCH_CONST = "минск";
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keywordView = findViewById(R.id.keywordView);
        startSearchButton = findViewById(R.id.startSearchView);
        generateSCVButton = findViewById(R.id.generateCSVView);
        resultView = findViewById(R.id.resultView);
        linkSizeView = findViewById(R.id.linkSizeView);
        linkDepthView = findViewById(R.id.linkDepthView);
        urlView = findViewById(R.id.urlView);
        urlView.setText(URL_CONST);
        keywordView.setText(SEARCH_CONST);
        startSearchButton.setOnClickListener(v -> startSearch());
        context = this;
        progressBarDialog = initDialog();
        linkSizeView.setText(String.valueOf(MAX_CAPACITY));
        linkDepthView.setText(String.valueOf(MAX_LINK_DEPTH));
        setButtonInactive(generateSCVButton);

    }

    private void setButtonInactive(Button button) {
        button.setBackgroundColor(getResources().getColor(R.color.inactive));
        button.setClickable(false);
    }

    private void makeButtonActive(Button button) {
        button.setBackgroundColor(getResources().getColor(R.color.purple_700));
        button.setClickable(true);
    }

    private AlertDialog initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(new ProgressBar(this));
        return builder.create();
    }


    private boolean isUrlCorrect(String url) {
        return url.contains("http");
    }

    private void startSearch() {
        String url = urlView.getText().toString();
        if (!isUrlCorrect(url)) {
            Toast.makeText(this, "Wrong URL!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!linkHoldersStorage.isEmpty()) {
            linkHoldersStorage.clear();
        }
        previousLinkHoldersStorageSize = 0;
        resultView.setText("");
        String capStr = linkSizeView.getText().toString();
        String depthStr = linkDepthView.getText().toString();
        if (capStr.isEmpty() || depthStr.isEmpty() || keywordView.getText().toString().isEmpty()) {
            Toast.makeText(this, "Fill all values!", Toast.LENGTH_SHORT).show();
            return;
        }
        MAX_CAPACITY = Integer.parseInt(capStr);
        MAX_LINK_DEPTH = Integer.parseInt(depthStr);

        linkHoldersStorage.add(new LinkHolder(url, 1));
        progressBarDialog.show();
        executorService.execute(() -> {
            findLinksInDocument();
        });
    }

    private boolean isStopConditionReached(int currentLinkDepth) {
        return linkHoldersStorage.size() >= MAX_CAPACITY || currentLinkDepth == MAX_LINK_DEPTH;
    }

    private void processLinkStorage() {

        List<Callable<String>> tasks = new ArrayList<>();

        for (LinkHolder linkHolder : linkHoldersStorage) {
            tasks.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    int val = Jsoup.connect(linkHolder.getLink()).get().text().toLowerCase()
                            .split(keywordView.getText().toString().toLowerCase()).length - 1;
                    return linkHolder.getId() + " "+linkHolder.getLink() + " " + val + "\n";
                }
            });
        }
        try {
            List<Future<String>> results = executorService.invokeAll(tasks);
            StringBuilder stringBuilder = new StringBuilder();
            for (Future<String> str : results) {
                stringBuilder.append(str.get());
            }
            runOnUiThread(() -> {
                resultView.setText(stringBuilder.toString());
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

 /*           executorService.execute(() -> {
                try {
                    int val = Jsoup.connect(linkHolder.getLink()).get().text().toLowerCase()
                            .split(keywordView.getText().toString().toLowerCase()).length - 1;
                    runOnUiThread(() -> {
                        resultView.append(linkHolder.getLink() + " " + val + "\n");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });*/

    }


    private void findLinksInDocument() {
        try {
            List<LinkHolder> tempLinkHolderStorage = new ArrayList<>(MAX_CAPACITY);
            for (int i = previousLinkHoldersStorageSize; i < linkHoldersStorage.size(); i++) {
                LinkHolder linkHolder = linkHoldersStorage.get(i);
                Document document = Jsoup.connect(linkHolder.getLink()).get();
                Elements elements = document.select("a[href]");
                for (Element element : elements) {
                    String link = element.attr("href");
                    if (isUrlCorrect(link)) {
                        tempLinkHolderStorage.add(new LinkHolder(link, linkHolder.getLinkDepth() + 1));
                    }
                    if (tempLinkHolderStorage.size() + linkHoldersStorage.size() >= MAX_CAPACITY) {
                        break;
                    }
                }
                if (tempLinkHolderStorage.size() + linkHoldersStorage.size() >= MAX_CAPACITY) {
                    break;
                }

            }
            previousLinkHoldersStorageSize = linkHoldersStorage.size();
            linkHoldersStorage.addAll(tempLinkHolderStorage);
            if (!isStopConditionReached(linkHoldersStorage.get(linkHoldersStorage.size() - 1).getLinkDepth())) {
                findLinksInDocument();
            } else {
                runOnUiThread(() -> {
                    progressBarDialog.dismiss();
                    Toast.makeText(context, linkHoldersStorage.size() + " links were found!", Toast.LENGTH_LONG).show();
                });
                processLinkStorage();
            }

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressBarDialog.dismiss();
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
