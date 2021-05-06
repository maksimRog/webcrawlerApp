package com.mraha.webcrawlerapp;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {
    private FileWriter fileWriter;

    public CSVWriter() {
        try {
            fileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory(), "Data.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeFoundData(List<LinkHolder> data) {
        try {
            fileWriter.append("id");
            fileWriter.append(',');
            fileWriter.append("url");
            fileWriter.append(',');
            fileWriter.append("term counter");
            fileWriter.append(',');
            fileWriter.append('\n');
            for (LinkHolder linkHolder : data) {
                fileWriter.append(String.valueOf(linkHolder.getId()));
                fileWriter.append(',');
                fileWriter.append(linkHolder.getLink());
                fileWriter.append(',');
                fileWriter.append(String.valueOf(linkHolder.getTermCounter()));
                fileWriter.append(',');
                fileWriter.append('\n');
            }
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
