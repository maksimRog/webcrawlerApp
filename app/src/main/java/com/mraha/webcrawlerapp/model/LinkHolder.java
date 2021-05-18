package com.mraha.webcrawlerapp.model;

public class LinkHolder {
    private String link;
    private int linkDepth;
    private int termCounter;
    private boolean isSuccess=true;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getTermCounter() {
        return termCounter;
    }

    public void setTermCounter(int termCounter) {
        this.termCounter = termCounter;
    }

    public LinkHolder(String link, int linkDepth) {

        this.link = link;
        this.linkDepth = linkDepth;
    }


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getLinkDepth() {
        return linkDepth;
    }

    public void setLinkDepth(int linkDepth) {
        this.linkDepth = linkDepth;
    }
}
