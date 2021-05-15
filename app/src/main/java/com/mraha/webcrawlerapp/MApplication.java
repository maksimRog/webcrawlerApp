package com.mraha.webcrawlerapp;

import android.app.Application;

import com.mraha.webcrawlerapp.model.Config;
import com.mraha.webcrawlerapp.model.LinkHolder;

import java.util.ArrayList;
import java.util.List;

public class MApplication extends Application {
    private static MApplication mApplication;
    private Config config;
    private  List<LinkHolder> linkHoldersStorage;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        config=new Config();
        linkHoldersStorage=new ArrayList<>(Integer.parseInt(config.getMaxLinkSize()));
    }

    public Config getConfig() {
        return config;
    }

    public List<LinkHolder> getLinkHoldersStorage() {
        return linkHoldersStorage;
    }

    public static MApplication getInstance() {
        return mApplication;
    }
}
