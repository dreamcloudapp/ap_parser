package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class NewsMonth {
    File directory;
    public int year;
    public int month;

    public NewsMonth(File directory, int year, int month) {
        this.directory = directory;
        this.year = year;
        this.month = month;
    }

    public ArrayList<NewsDay> getDays() {
        ArrayList<NewsDay> days = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> name.matches("[0-9]{2}");
        File[] dayFiles = directory.listFiles(filter);
        if (dayFiles != null) {
            for (File dayFile: dayFiles) {
                days.add(new NewsDay(dayFile, this.year, this.month, Integer.parseInt(dayFile.getName())));
            }
        }
        return days;
    }
}
