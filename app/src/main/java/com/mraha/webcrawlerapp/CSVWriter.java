package com.mraha.webcrawlerapp;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
    private FileWriter fileWriter;

    public CSVWriter(Context context) {
        try {
            fileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory(), "Data.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeContent(String content) {
        try {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeWriter(){
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
