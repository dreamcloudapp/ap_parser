package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Objects;

public class NewsMonth {
    File directory;
    public int month;

    public NewsMonth(File directory, int month) {
        this.directory = directory;
        this.month = month;
    }

    public ArrayList<NewsDay> getDays() {
        ArrayList<NewsDay> days = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> name.matches("[0-9]{2}");
        for (File dayFile: Objects.requireNonNull(directory.listFiles(filter))) {
            days.add(new NewsDay(dayFile, Integer.parseInt(dayFile.getName())));
        }
        return days;
    }
}
