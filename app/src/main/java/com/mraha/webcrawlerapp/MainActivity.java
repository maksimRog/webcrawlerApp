package com.mraha.webcrawlerapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mraha.webcrawlerapp.model.Config;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;

public class MainActivity extends MvpAppCompatActivity implements MainView {
    private Button startSearchButton;
    private Button generateSCVButton;
    private EditText termView;
    private EditText seedPageView;
    private EditText linkSizeView;
    private EditText linkDepthView;
    private ProgressBar progressBar;
    public AlertDialog progressBarDialog;
    private RecyclerView recyclerView;
    @InjectPresenter
    public MainPresenter mainPresenter;
    private MApplication mApplication;
    public static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkPermission()) {
            askPermissions();
        } else {
            initObjects();
            initViews();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mApplication.getLinkHoldersStorage().isEmpty()) {
            makeButtonInactive(generateSCVButton);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (!isPermissionGranted(grantResults)) {
                showResolveDialog();
            }else{
                initObjects();
                initViews();
            }
        }
    }

    private void showResolveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need storage permission!").setMessage("Try again?")
                .setPositiveButton("ok", (dialog, which) -> {
                    askPermissions();
                }).create().show();
    }

    private boolean isPermissionGranted(int[] grantResults) {
        boolean res = true;
        for (int i : grantResults) {
            if (i != PackageManager.PERMISSION_GRANTED) {
                res = false;
                break;
            }
        }
        return res;
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DataAdapter());
        termView = findViewById(R.id.termView);
        startSearchButton = findViewById(R.id.startSearchView);
        generateSCVButton = findViewById(R.id.generateCSVView);
        linkSizeView = findViewById(R.id.linkSizeView);
        linkDepthView = findViewById(R.id.linkDepthView);
        seedPageView = findViewById(R.id.seedPageView);
        seedPageView.setText(mApplication.getConfig().getSeedPage());
        termView.setText(mApplication.getConfig().getTerm());
        startSearchButton.setOnClickListener(v -> {
            if (isUserInputCorrect()) {
                makeButtonInactive(generateSCVButton);
                updateConfig();
                mainPresenter.startSearch();
            }
        });
        linkSizeView.setText(mApplication.getConfig().getMaxLinkSize());
        linkDepthView.setText(String.valueOf(mApplication.getConfig().getMaxLinkDepth()));
        generateSCVButton.setOnClickListener(v -> showSaveToCSVDialog());
    }


    private boolean isUserInputCorrect() {
        String url = seedPageView.getText().toString();
        if (!isUrlCorrect(url)) {
            Toast.makeText(this, "Wrong URL!", Toast.LENGTH_SHORT).show();
            return false;
        }
        String capStr = linkSizeView.getText().toString();
        String depthStr = linkDepthView.getText().toString();
        if (capStr.isEmpty() || depthStr.isEmpty() || termView.getText().toString().isEmpty()) {
            Toast.makeText(this, "Fill all values!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkPermission() {
        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    public void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    private void initObjects() {
        mApplication = MApplication.getInstance();
        progressBarDialog = initProgressDialog();
    }

    private void makeButtonInactive(Button button) {
        button.setBackgroundColor(getResources().getColor(R.color.inactive));
        button.setClickable(false);
    }


    private AlertDialog initProgressDialog() {
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setPadding(5, 10, 5, 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(progressBar);
        builder.setCancelable(false);
        return builder.create();
    }


    private boolean isUrlCorrect(String url) {
        return url.contains("http");
    }


    public void updateConfig() {
        Config config = MApplication.getInstance().getConfig();
        config.setMaxLinkDepth(linkDepthView.getText().toString());
        config.setTerm(termView.getText().toString());
        config.setMaxLinkSize(linkSizeView.getText().toString());
        config.setSeedPage(seedPageView.getText().toString());
    }

    public void showSaveToCSVDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText editText = new EditText(this);
        AlertDialog alertDialog = builder.setView(editText).setMessage("Files with same name will be overwritten.")
                .setPositiveButton("ok", null).setTitle("Type file name.").create();
        alertDialog.setOnShowListener(dialog -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String fileName = editText.getText().toString();
                if (!fileName.isEmpty()) {
                    dialog.dismiss();
                    mainPresenter.writeToCSVFile(fileName);
                }
            });
        });
        alertDialog.show();
    }

    @Override
    public void showProgressBar(int progress) {
        runOnUiThread(() -> {
            progressBar.setProgress(0);
            progressBarDialog.show();
        });
    }

    @Override
    public void hideProgressBar() {
        runOnUiThread(() -> progressBarDialog.dismiss());
    }

    @Override
    public void makeButtonActive(int ButtonID) {
        runOnUiThread(() -> {
            Button button = findViewById(ButtonID);
            button.setBackgroundColor(getResources().getColor(R.color.purple_700));
            button.setClickable(true);
        });
    }


    @Override
    public void showMessage(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void updateProgressBar(int progress) {
        runOnUiThread(() -> progressBar.setProgress(progress));

    }

    @Override
    public void updateAdapter() {
        runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
    }

}
