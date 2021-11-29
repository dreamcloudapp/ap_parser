package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Objects;

public class NewsYear {
    File directory;
    public int year;

    public NewsYear(File directory, int year) {
        this.directory = directory;
        this.year = year;
    }

    public ArrayList<NewsMonth> getMonths() {
        ArrayList<NewsMonth> months = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> name.matches("[0-9]{2}");
        for (File monthFile: Objects.requireNonNull(directory.listFiles(filter))) {
            months.add(new NewsMonth(monthFile, Integer.parseInt(monthFile.getName())));
        }
        return months;
    }
}
