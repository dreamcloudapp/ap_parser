package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

public class NewsDay {
    File directory;
    public int day;

    public NewsDay(File directory, int day) {
        this.directory = directory;
        this.day = day;
    }

    public ArrayList<NewsArticle> getArticles() throws IOException, ParseException {
        ArrayList<NewsArticle> articles = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> name.matches("[0-9a-f]+\\.json");
        for (File articleFile: Objects.requireNonNull(directory.listFiles(filter))) {
            articles.add(NewsArticle.parseFromJsonFile(articleFile));
        }
        return articles;
    }
}
