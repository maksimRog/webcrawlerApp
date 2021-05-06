package com.mraha.webcrawlerapp;

public class LinkHolder {
    private static int idCounter = 0;
    private String link;
    private int linkDepth;
    private final int id;


    public LinkHolder(String link, int linkDepth) {
        id = idCounter;
        idCounter++;
        this.link = link;
        this.linkDepth = linkDepth;
    }

    public int getId() {
        return id;
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
