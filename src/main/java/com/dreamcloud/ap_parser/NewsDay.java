package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class NewsDay {
    File directory;
    public int year;
    public int month;
    public int day;

    public NewsDay(File directory, int year, int month, int day) {
        this.directory = directory;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public ArrayList<NewsArticle> getArticles() {
        ArrayList<NewsArticle> articles = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> name.matches("[0-9a-f]+\\.json");
        File[] articleFiles = directory.listFiles(filter);
        if (articleFiles != null) {
            for (File articleFile: articleFiles) {
                try{
                    NewsArticle article = NewsArticle.parseFromJsonFile(articleFile);
                    //Override based on the file structure
                    String monthString = String.valueOf(month);
                    if (monthString.length() == 1) {
                        monthString = "0" + monthString;
                    }
                    String dayString = String.valueOf(day);
                    if (dayString.length() == 1) {
                        dayString = "0" + dayString;
                    }
                    article.date = LocalDate.parse(year + "-" + monthString + "-" + dayString);
                    articles.add(article);
                } catch (IOException | java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return articles;
    }
}
