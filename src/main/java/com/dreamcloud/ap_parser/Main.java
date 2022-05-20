package com.dreamcloud.ap_parser;

import org.apache.commons.cli.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static boolean nonEmpty(String s) {
        return s != null && !s.equals("");
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option infoOption = new Option(null, "info", true, "news source dir -- Displays information about the news articles.");
        infoOption.setRequired(false);
        options.addOption(infoOption);

        Option writeOption = new Option(null, "write", true, "news source dir, output file -- Takes all of the news articles, formats them, and writes to a single JSON file.");
        writeOption.setRequired(false);
        writeOption.setArgs(2);
        options.addOption(writeOption);

        Option randomArticlesOption = new Option(null, "random-summary", true, "news source dir, count -- Prints a list of $count random news summaries.");
        randomArticlesOption.setRequired(false);
        randomArticlesOption.setArgs(2);
        options.addOption(randomArticlesOption);

        Option startDateOption = new Option(null, "start", true, "start date (YYYY-MM-DD) -- Start date for articles to include.");
        startDateOption.setRequired(false);
        options.addOption(startDateOption);

        Option endDateOption = new Option(null, "end", true, "end date (YYYY-MM-DD) -- End date for articles to include.");
        endDateOption.setRequired(false);
        options.addOption(endDateOption);

        Option categoryOption = new Option(null, "category", true, "category [category2, ...] -- A list of categories to include.");
        categoryOption.setRequired(false);
        categoryOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(categoryOption);

        Option textMatchOption = new Option(null, "text", true, "text1 [text2, ...] -- A list of words or phrases which must be in the article texts.");
        textMatchOption.setRequired(false);
        textMatchOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(textMatchOption);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);
            String infoDirectory = cmd.getOptionValue("info");
            String start = cmd.getOptionValue("start");
            String end = cmd.getOptionValue("end");
            String[] writeArgs = cmd.getOptionValues("write");
            String[] randomSummaryArgs = cmd.getOptionValues("random-summary");
            String[] categoryArgs = cmd.getOptionValues("category");
            String[] textMatches =  cmd.getOptionValues("text");

            LocalDate startDate = null;
            if (nonEmpty(start)) {
                startDate = LocalDate.parse(start);
            }

            LocalDate endDate = null;
            if (nonEmpty(end)) {
                endDate = LocalDate.parse(end);
            }

            if (nonEmpty(infoDirectory)) {
                displayInfo(infoDirectory, startDate, endDate, categoryArgs, textMatches);
            } else if (writeArgs != null && writeArgs.length == 2) {
                File newsDirectory = new File(writeArgs[0]);
                File outputFile = new File(writeArgs[1]);
                writeParsedJson(newsDirectory, outputFile, startDate, endDate, categoryArgs, textMatches);
            } else if (randomSummaryArgs != null && randomSummaryArgs.length == 2) {
                File newsDirectory = new File(randomSummaryArgs[0]);
                int count = Integer.parseInt(randomSummaryArgs[1]);
                printRandomSummaries(newsDirectory, count, startDate, endDate, categoryArgs, textMatches);
            } else {
                formatter.printHelp("ap_parser", options);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printRandomSummaries(File newsDirectory, int count, LocalDate startDate, LocalDate endDate, String[] categories, String[] textMatches) {
        Set<String> uniqueArticleIds = new HashSet<>();
        int articleCount = 0;
        int matchedCount = 0;
        Instant startTime = Instant.now();
        System.out.println("Gathering info for '" + newsDirectory + "'...\n");
        Parser parser = new Parser(newsDirectory);
        ArrayList<NewsYear> years = parser.getYears();
        for (NewsYear year: years) {
            ArrayList<NewsMonth> months = year.getMonths();
            for (NewsMonth month: months) {
                ArrayList<NewsDay> days = month.getDays();
                for (NewsDay day: days) {
                    ArrayList<NewsArticle> articles = day.getArticles();
                    for (NewsArticle article: articles) {
                        articleCount++;
                        if (!article.matches(startDate, endDate, categories, textMatches)) {
                            continue;
                        }
                        uniqueArticleIds.add(article.id);
                        matchedCount++;
                    }
                }
            }
        }

        Set<String> randomIds;
        Random random = new Random();
        if (uniqueArticleIds.size() <= count) {
            randomIds = uniqueArticleIds;
        } else {
            randomIds = new HashSet<>();
            String[] uniqueIds = uniqueArticleIds.toArray(String[]::new);
            for (int i=0; i<count; i++) {
                randomIds.add(uniqueIds[random.nextInt(uniqueIds.length)]);
            }
        }

        ArrayList<NewsArticle> randomArticles = new ArrayList<>();
        for (NewsYear year: years) {
            ArrayList<NewsMonth> months = year.getMonths();
            for (NewsMonth month: months) {
                ArrayList<NewsDay> days = month.getDays();
                for (NewsDay day: days) {
                    ArrayList<NewsArticle> articles = day.getArticles();
                    for (NewsArticle article: articles) {
                        if (randomIds.contains(article.id)) {
                            randomArticles.add(article);
                        }
                    }
                }
            }
        }

        System.out.println("Random articles: ");
        System.out.println("----------\n");
        for (NewsArticle article: randomArticles) {
            System.out.println(article.summary.replace("\n", " "));
        }
        System.out.println("----------\n");

        long secondsPassed = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        System.out.println("Processed " + articleCount + " articles in " + secondsPassed + " seconds.\n");
    }

    static void displayInfo(String newsDirectory, LocalDate startDate, LocalDate endDate, String[] categories, String[] textMatches) {
        Map<String, Integer> categoryMap = new HashMap<>();
        int articleCount = 0;
        int matchedCount = 0;
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
            int yearArticles = 0;
            ArrayList<NewsMonth> months = year.getMonths();
            for (NewsMonth month: months) {
                int monthArticles = 0;
                ArrayList<NewsDay> days = month.getDays();
                for (NewsDay day: days) {
                    ArrayList<NewsArticle> articles = day.getArticles();
                    int dayArticleCount = 0;
                    for (NewsArticle article: articles) {
                        articleCount++;
                        if (!article.matches(startDate, endDate, categories, textMatches)) {
                            continue;
                        }
                        matchedCount++;
                        monthArticles++;
                        yearArticles++;
                        //Build some category maps
                        for (String category: article.categories) {
                            int count = categoryMap.getOrDefault(category, 0);
                            categoryMap.put(category, ++count);
                        }
                    }
                }
                System.out.println(month.year + "-" + month.month + ": " + monthArticles);
            }
            System.out.println(year.year + ": " + yearArticles);
            System.out.println("----------\n");
        }
        long secondsPassed = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        System.out.println("Processed " + articleCount + " articles in " + secondsPassed + " seconds.\n");
        System.out.println("----------");
        System.out.println("Matched articles: " + matchedCount);
        System.out.println("Category information:");
        System.out.println("----------");
        for (Map.Entry<String, Integer> entry: categoryMap.entrySet()) {
            System.out.println(entry.getKey() + ":\t" + entry.getValue());
        }
        System.out.println("----------");
    }

    static void writeParsedJson(File newsDirectory, File outputFile, LocalDate startDate, LocalDate endDate, String[] categories, String[] textMatches) throws IOException, XMLStreamException {
        boolean writeXml = outputFile.getName().contains(".xml");

        int articleCount = 0;
        int writeCount = 0;
        Instant startTime = Instant.now();
        System.out.println("Parsing news sources from " + newsDirectory.getPath() + "...");
        Parser parser = new Parser(newsDirectory);
        OutputStream outputStream = new FileOutputStream(outputFile);
        outputStream = new BufferedOutputStream(outputStream);

        if (writeXml) {
            outputStream = new BZip2CompressorOutputStream(outputStream);
        }

        XMLStreamWriter xmlWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);

        if (writeXml) {
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement("articles");
        } else {
            //write CSV headers
            outputStream.write("NewsId,Date,Title,Categories,Headline,Extended Headline,Summary,Text\n".getBytes(StandardCharsets.UTF_8));
        }

        ArrayList<NewsYear> years = parser.getYears();
        for (NewsYear year: years) {
            ArrayList<NewsMonth> months = year.getMonths();
            for (NewsMonth month: months) {
                ArrayList<NewsDay> days = month.getDays();
                for (NewsDay day: days) {
                    ArrayList<NewsArticle> articles = day.getArticles();
                    for (NewsArticle article: articles) {
                        articleCount++;
                        if (!article.matches(startDate, endDate, categories, textMatches)) {
                            continue;
                        }
                        writeCount++;

                        if (writeXml) {
                            xmlWriter.writeStartElement("article");

                            xmlWriter.writeStartElement("id");
                            xmlWriter.writeCharacters(article.id);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("type");
                            xmlWriter.writeCharacters(article.type);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("version");
                            xmlWriter.writeCharacters(String.valueOf(article.version));
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("status");
                            xmlWriter.writeCharacters(article.status);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("date");
                            String monthString = String.valueOf(article.date.getMonthValue());
                            if (monthString.length() == 1) {
                                monthString = "0" + monthString;
                            }
                            String dayString = String.valueOf(article.date.getMonthValue());
                            if (dayString.length() == 1) {
                                dayString = "0" + dayString;
                            }
                            xmlWriter.writeCharacters(article.date.getYear() + "-" + monthString + "-" + dayString);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("role");
                            xmlWriter.writeCharacters(article.role);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("language");
                            xmlWriter.writeCharacters(article.language);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("title");
                            xmlWriter.writeCharacters(article.title);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("headline");
                            xmlWriter.writeCharacters(article.headline);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("extendedHeadline");
                            xmlWriter.writeCharacters(article.extendedHeadline);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("summary");
                            xmlWriter.writeCharacters(article.summary);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("article");
                            xmlWriter.writeCharacters(article.article);
                            xmlWriter.writeEndElement();

                            xmlWriter.writeStartElement("categories");
                            for (String category: article.categories) {
                                xmlWriter.writeStartElement("category");
                                xmlWriter.writeCharacters(category);
                                xmlWriter.writeEndElement();
                            }
                            xmlWriter.writeEndElement();

                            xmlWriter.writeEndElement();
                        } else {
                            //Write CSV
                            //NewsId,Date,Title,Categories,Headline,Extended Headline,Summary,Text
                            String safeId = article.id;
                            String safeDate = article.date.toString();
                            String safeTitle = article.title.replace('\n', ' ').replace('"', '\'');
                            String safeCategories = String.join(", ", article.categories);
                            String safeHeadline = article.headline.replace('\n', ' ').replace('"', '\'');
                            String safeExtendedHeadline = article.extendedHeadline.replace('\n', ' ').replace('"', '\'');
                            String safeSummary = article.summary.replace('\n', ' ').replace('"', '\'');

                            if (safeSummary.trim().equals("")) {
                                continue;
                            }

                            String safeText = article.article.replace('\n', ' ').replace('"', '\'');

                            String[] csvLine = new String[]{safeId, safeDate, safeTitle, safeCategories, safeHeadline, safeExtendedHeadline, safeSummary, safeText};

                            for (int fieldIdx = 0; fieldIdx < csvLine.length; fieldIdx++) {
                                String field = csvLine[fieldIdx];
                                if (field.contains(",") || field.contains(" ")) {
                                    field = "\"" + field + "\"";
                                }
                                csvLine[fieldIdx] = field;
                            }
                            outputStream.write(String.join(",", csvLine).getBytes(StandardCharsets.UTF_8));
                            outputStream.write('\n');
                        }
                    }
                }
            }
        }
        if (writeXml) {
            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.close();
        }
        outputStream.close();
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
