package com.mraha.webcrawlerapp;


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

import moxy.InjectViewState;
import moxy.MvpPresenter;
@InjectViewState
public class MainPresenter extends MvpPresenter<MainView> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    public static int MAX_CAPACITY = 1000;
    public static final int CONNECTION_TIME_OUT = 2000;
    public List<LinkHolder> linkHoldersStorage;
    private int previousLinkHoldersStorageSize = 0;
    public static int MAX_LINK_DEPTH = 8;
    private Config config;
    private int progressCounter = 0;

    private boolean isUrlCorrect(String url) {
        return url.contains("http");
    }

    public void writeToCSVFile(String fileName) {
        CSVWriter csvWriter = new CSVWriter(fileName);
        csvWriter.writeFoundData(MApplication.getInstance().getLinkHoldersStorage());
        csvWriter.closeWriter();
        getViewState().showMessage("Saved!");
    }

    public void startSearch() {
        config = MApplication.getInstance().getConfig();
        linkHoldersStorage = MApplication.getInstance().getLinkHoldersStorage();
        if (!linkHoldersStorage.isEmpty()) {
            linkHoldersStorage.clear();
            getViewState().updateAdapter();
        }
        previousLinkHoldersStorageSize = 0;
        MAX_CAPACITY = Integer.parseInt(config.getMaxLinkSize());
        MAX_LINK_DEPTH = Integer.parseInt(config.getMaxLinkDepth());
        executorService.execute(() -> {
            progressCounter = 0;
            (getViewState()).showProgressBar(progressCounter);
            LinkHolder.idCounter = 1;
            linkHoldersStorage.add(new LinkHolder(config.getSeedPage(), 1));
            findLinksInDocument();
        });
    }

    private boolean isStopConditionReached(int currentLinkDepth) {
        return linkHoldersStorage.size() >= MAX_CAPACITY || currentLinkDepth == MAX_LINK_DEPTH;
    }

    private void findLinksInDocument() {
        List<LinkHolder> tempLinkHolderStorage = findLinksInStorage();
        previousLinkHoldersStorageSize = linkHoldersStorage.size();
        linkHoldersStorage.addAll(tempLinkHolderStorage);
        if (tempLinkHolderStorage.size() == 0 && !linkHoldersStorage.get(linkHoldersStorage.size() - 1).isSuccess()) {
            getViewState().hideProgressBar();
            getViewState().showMessage("No connection or server is not responding!");
        } else if (tempLinkHolderStorage.size() == 0 && linkHoldersStorage.get(linkHoldersStorage.size() - 1).isSuccess()) {
            processLinkStorage();
        } else if (!isStopConditionReached(linkHoldersStorage.get(linkHoldersStorage.size() - 1).getLinkDepth())) {
            findLinksInDocument();
        } else {
            processLinkStorage();
        }
    }

    private List<LinkHolder> findLinksInStorage() {
        List<LinkHolder> tempLinkHolderStorage = new ArrayList<>(MAX_CAPACITY);
        majorLoop:
        for (int i = previousLinkHoldersStorageSize; i < linkHoldersStorage.size(); i++) {
            LinkHolder linkHolder = linkHoldersStorage.get(i);
            try {
                Document document = Jsoup.connect(linkHolder.getLink()).timeout(CONNECTION_TIME_OUT).get();
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
            } catch (IOException e) {
                linkHolder.setSuccess(false);
                e.printStackTrace();
            }
        }
        return tempLinkHolderStorage;
    }

    private void processLinkStorage() {
        List<Callable<String>> tasks = new ArrayList<>(MAX_CAPACITY);
        for (LinkHolder linkHolder : linkHoldersStorage) {
            tasks.add(() -> {
                int val = 0;
                try {
                    val = Jsoup.connect(linkHolder.getLink())
                            .timeout(CONNECTION_TIME_OUT).get().text().toLowerCase()
                            .split(config.getTerm()).length - 1;
                    linkHolder.setTermCounter(val);
                    progressCounter++;
                    float progress = ((float) progressCounter / linkHoldersStorage.size()) * 100;
                    getViewState().updateProgressBar((int) progress);
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
            getViewState().updateAdapter();
            getViewState().hideProgressBar();
            getViewState().makeButtonActive(R.id.generateCSVView);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            getViewState().hideProgressBar();
            getViewState().showMessage(e.getMessage());
        }
    }

}
