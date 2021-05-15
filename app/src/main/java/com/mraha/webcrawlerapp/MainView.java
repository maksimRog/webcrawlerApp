package com.mraha.webcrawlerapp;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface MainView extends MvpView {
    void showProgressBar(int progress);

    void hideProgressBar();
    void makeButtonActive(int ButtonID);
    void showMessage(String message);

    void updateProgressBar(int progress);

    void updateAdapter();

}
