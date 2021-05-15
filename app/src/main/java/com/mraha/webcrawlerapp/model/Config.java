package com.mraha.webcrawlerapp.model;

public class Config {
    private String maxLinkDepth = "8";
    private String maxLinkSize = "1000";
    private String term = "Минск";
    private String seedPage = "https://www.tut.by/";

    public void setMaxLinkDepth(String maxLinkDepth) {
        this.maxLinkDepth = maxLinkDepth;
    }

    public void setMaxLinkSize(String maxLinkSize) {
        this.maxLinkSize = maxLinkSize;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setSeedPage(String seedPage) {
        this.seedPage = seedPage;
    }

    public String getMaxLinkDepth() {
        return maxLinkDepth;
    }

    public String getMaxLinkSize() {
        return maxLinkSize;
    }

    public String getTerm() {
        return term;
    }

    public String getSeedPage() {
        return seedPage;
    }
}
