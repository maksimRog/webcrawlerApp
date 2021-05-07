package com.mraha.webcrawlerapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private CSVWriter csvWriter;
    private int previousLinkHoldersStorageSize = 0;
    public final List<LinkHolder> linkHoldersStorage = new ArrayList<>(MAX_CAPACITY);
    public static final String URL_CONST = "https://www.tut.by/";
    public static final String SEARCH_CONST = "минск";
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkPermission()) {
            askPermissions();
        }
        initViews();
        initObjects();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (linkHoldersStorage.isEmpty()) {
            makeButtonInactive(generateSCVButton);
        }
    }

    private void initViews() {
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
        linkSizeView.setText(String.valueOf(MAX_CAPACITY));
        linkDepthView.setText(String.valueOf(MAX_LINK_DEPTH));
        generateSCVButton.setOnClickListener(v -> writeToCSVFile());
    }

    private void writeToCSVFile() {
        progressBarDialog.show();
        csvWriter.writeFoundData(linkHoldersStorage);
        progressBarDialog.dismiss();
        Toast.makeText(this, "Data was saved to file!", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission() {
        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    private void initObjects() {
        csvWriter = new CSVWriter();
        progressBarDialog = initDialog();
        context = this;
    }

    private void makeButtonInactive(Button button) {
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
        builder.setCancelable(false);
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
            tasks.add(() -> {
                int val = 0;
                try {
                    val = Jsoup.connect(linkHolder.getLink()).get().text().toLowerCase()
                            .split(keywordView.getText().toString().toLowerCase()).length - 1;
                    linkHolder.setTermCounter(val);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return linkHolder.getId() + " " + linkHolder.getLink() + " " + val + "\n";
            });
        }
        try {
            List<Future<String>> results = executorService.invokeAll(tasks);
            StringBuffer StringBuffer = new StringBuffer();
            for (Future<String> str : results) {
                StringBuffer.append(str.get());
            }
            runOnUiThread(() -> {
                progressBarDialog.dismiss();
                resultView.setText(StringBuffer.toString());
                makeButtonActive(generateSCVButton);
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressBarDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }


    private void findLinksInDocument() {
        try {
            List<LinkHolder> tempLinkHolderStorage = new ArrayList<>(MAX_CAPACITY);
            majorLoop:
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
                        break majorLoop;
                    }
                }
            }
            previousLinkHoldersStorageSize = linkHoldersStorage.size();
            linkHoldersStorage.addAll(tempLinkHolderStorage);
            if (!isStopConditionReached(linkHoldersStorage.get(linkHoldersStorage.size() - 1).getLinkDepth())) {
                findLinksInDocument();
            } else {
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
        csvWriter.closeWriter();
    }
}
