package com.dreamcloud.ap_parser;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class Main {
    public static boolean nonEmpty(String s) {
        return s != null && !s.equals("");
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option infoOption = new Option("info", "info", true, "news source dir -- Displays information about the news articles.");
        infoOption.setRequired(false);
        options.addOption(infoOption);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);
            String infoDirectory = cmd.getOptionValue("info");

            if (nonEmpty(infoDirectory)) {
                int articleCount = 0;
                Instant startTime = Instant.now();
                System.out.println("Gathering info for '" + infoDirectory + "'...\n");
                Parser parser = new Parser(new File(infoDirectory));
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
                            System.out.println("Day " + day.day + " articles: " + articles.size());
                            articleCount += articles.size();
                        }
                        System.out.println("----------\n");
                    }
                    System.out.println("----------\n");
                }
                long secondsPassed = Instant.now().getEpochSecond() - startTime.getEpochSecond();
                System.out.println("Processed " + articleCount + " in " + secondsPassed + " seconds.");
            } else {
                formatter.printHelp("ap_parser", options);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
