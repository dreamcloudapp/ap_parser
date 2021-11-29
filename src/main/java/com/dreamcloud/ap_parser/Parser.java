package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    File directory;

    public Parser(File directory) {
        this.directory = directory;
    }

    public ArrayList<NewsYear> getYears() {
        ArrayList<NewsYear> years = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> name.matches("[0-9]{4}");
        for (File yearFile: Objects.requireNonNull(directory.listFiles(filter))) {
          years.add(new NewsYear(yearFile, Integer.parseInt(yearFile.getName())));
        }
        return years;
    }
}
