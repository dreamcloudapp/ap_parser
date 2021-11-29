package com.dreamcloud.ap_parser;

import org.apache.commons.cli.*;
import org.json.JSONWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static boolean nonEmpty(String s) {
        return s != null && !s.equals("");
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option infoOption = new Option("info", "info", true, "news source dir -- Displays information about the news articles.");
        infoOption.setRequired(false);
        options.addOption(infoOption);

        Option writeOption = new Option("write", "write", true, "news source dir, output file -- Takes all of the news articles, formats them, and writes to a single JSON file.");
        writeOption.setRequired(false);
        writeOption.setArgs(2);
        options.addOption(writeOption);

        Option startDateOption = new Option("start", "start", true, "start date (YYYY-MM-DD) -- Start date for articles to include.");
        startDateOption.setRequired(false);
        options.addOption(startDateOption);

        Option endDateOption = new Option("end", "end", true, "end date (YYYY-MM-DD) -- End date for articles to include.");
        endDateOption.setRequired(false);
        options.addOption(endDateOption);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);
            String infoDirectory = cmd.getOptionValue("info");
            String start = cmd.getOptionValue("start");
            String end = cmd.getOptionValue("end");
            String[] writeArgs = cmd.getOptionValues("write");

            LocalDate startDate = null;
            if (nonEmpty(start)) {
                startDate = LocalDate.parse(start);
            }

            LocalDate endDate = null;
            if (nonEmpty(end)) {
                endDate = LocalDate.parse(end);
            }


            if (nonEmpty(infoDirectory)) {
                displayInfo(infoDirectory, startDate, endDate);
            } else if (writeArgs != null && writeArgs.length == 2) {
                File newsDirectory = new File(writeArgs[0]);
                File outputFile = new File(writeArgs[1]);
                writeParsedJson(newsDirectory, outputFile, startDate, endDate);
            } else {
                formatter.printHelp("ap_parser", options);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void displayInfo(String newsDirectory, LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> categoryMap = new HashMap<>();
        int articleCount = 0;
        Instant startTime = Instant.now();
        System.out.println("Gathering info for '" + newsDirectory + "'...\n");
        Parser parser = new Parser(new File(newsDirectory));
        ArrayList<NewsYear> years = parser.getYears();
        System.out.println("We found news articles for " + years.size() + " years:");
        System.out.println("----------");
        for (NewsYear year: years) {
            System.out.println(year.year);
        }
        System.out.println("----------\n");

        System.out.println("Here's a quick break-down of each year:");
        for (NewsYear year: years) {
            System.out.println("Year " + year.year + ":");
            System.out.println("----------");
            ArrayList<NewsMonth> months = year.getMonths();
            System.out.println("Months: " + months.size());
            for (NewsMonth month: months) {
                System.out.println("Month " + month.month + ":");
                System.out.println("----------");
                ArrayList<NewsDay> days = month.getDays();
                System.out.println("Days: " + days.size());
                for (NewsDay day: days) {
                    ArrayList<NewsArticle> articles = day.getArticles();
                    for (NewsArticle article: articles) {
                        //Build some category maps
                        for (String category: article.categories) {
                            int count = categoryMap.getOrDefault(category, 0);
                            categoryMap.put(category, ++count);
                        }
                    }
                    System.out.println("Day " + day.day + " articles: " + articles.size());
                    articleCount += articles.size();
                }
                System.out.println("----------\n");
            }
            System.out.println("----------\n");
        }
        long secondsPassed = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        System.out.println("Processed " + articleCount + " articles in " + secondsPassed + " seconds.\n");
        System.out.println("----------");
        System.out.println("Category information:");
        System.out.println("----------");
        for (Map.Entry<String, Integer> entry: categoryMap.entrySet()) {
            System.out.println(entry.getKey() + ":\t" + entry.getValue());
        }
        System.out.println("----------");
    }

    static void writeParsedJson(File newsDirectory, File outputFile, LocalDate startDate, LocalDate endDate) throws IOException {
        int articleCount = 0;
        int writeCount = 0;
        Instant startTime = Instant.now();
        System.out.println("Parsing news sources from " + newsDirectory.getPath() + "...");
        Parser parser = new Parser(newsDirectory);
        FileWriter fileWriter = new FileWriter(outputFile);
        JSONWriter jsonWriter = new JSONWriter(fileWriter);
        jsonWriter.array();
        ArrayList<NewsYear> years = parser.getYears();
        for (NewsYear year: years) {
            ArrayList<NewsMonth> months = year.getMonths();
            for (NewsMonth month: months) {
                ArrayList<NewsDay> days = month.getDays();
                for (NewsDay day: days) {
                    ArrayList<NewsArticle> articles = day.getArticles();
                    for (NewsArticle article: articles) {
                        articleCount++;
                        if (startDate != null && startDate.isAfter(article.date)) {
                            continue;
                        }
                        if (endDate != null && endDate.isBefore(article.date)) {
                            continue;
                        }
                        writeCount++;
                        jsonWriter.object();
                        jsonWriter.key("id");
                        jsonWriter.value(article.id);

                        jsonWriter.key("type");
                        jsonWriter.value(article.type);

                        jsonWriter.key("version");
                        jsonWriter.value(article.version);

                        jsonWriter.key("status");
                        jsonWriter.value(article.status);

                        jsonWriter.key("date");
                        String monthString = String.valueOf(article.date.getMonthValue());
                        if (monthString.length() == 1) {
                            monthString = "0" + monthString;
                        }
                        String dayString = String.valueOf(article.date.getMonthValue());
                        if (dayString.length() == 1) {
                            dayString = "0" + dayString;
                        }
                        jsonWriter.value(article.date.getYear() + "-" + monthString + "-" + dayString);

                        jsonWriter.key("role");
                        jsonWriter.value(article.role);

                        jsonWriter.key("language");
                        jsonWriter.value(article.language);

                        jsonWriter.key("title");
                        jsonWriter.value(article.title);

                        jsonWriter.key("headline");
                        jsonWriter.value(article.headline);

                        jsonWriter.key("extendedHeadline");
                        jsonWriter.value(article.extendedHeadline);

                        jsonWriter.key("summary");
                        jsonWriter.value(article.summary);

                        jsonWriter.key("article");
                        jsonWriter.value(article.article);

                        jsonWriter.key("categories");
                        jsonWriter.array();
                        for (String category: article.categories) {
                            jsonWriter.value(category);
                        }
                        jsonWriter.endArray();
                        jsonWriter.endObject();
                    }
                }
            }
        }
        jsonWriter.endArray();
        fileWriter.close();
        long secondsPassed = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        System.out.println("Write Statistics:");
        System.out.println("----------");
        System.out.println("Articles Parsed:\t" + articleCount);
        System.out.println("Articles Written:\t" + writeCount);
        DecimalFormat format = new DecimalFormat("#.00%");
        System.out.println("Acceptance Rate:\t" + format.format( (float) writeCount / articleCount));
        System.out.println("----------");
        System.out.println("Process completed in " + secondsPassed + " seconds.");
    }
}
