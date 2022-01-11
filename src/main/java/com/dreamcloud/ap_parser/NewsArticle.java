package com.dreamcloud.ap_parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class NewsArticle {
    public String id;
    public String type;
    public int version;
    public String status;
    public LocalDate date;
    public String role;
    public String language;
    public String headline;
    public String extendedHeadline;
    public String title;
    public String summary;
    public String article;
    public ArrayList<String> categories;

    public NewsArticle() {
        this.categories = new ArrayList<>();
    }

    public static NewsArticle parseFromJsonFile(File file) throws IOException, ParseException {
        NewsArticle article = new NewsArticle();
        String UTF8_BOM = "\uFEFF";

        //Read to JSON
        byte[] encoded = Files.readAllBytes(file.toPath());
        String jsonData = new String(encoded, StandardCharsets.UTF_8);
        //Detect BOM
        if (jsonData.startsWith(UTF8_BOM)) {
            jsonData = jsonData.substring(1);
        }
        JSONObject newsData = new JSONObject(jsonData);

        //ID
        JSONObject idData = newsData.getJSONObject("altids");
        article.id = idData.getString("itemid");

        //Type
        article.type = newsData.getString("type");

        //Version
        article.version = newsData.getInt("version");

        //Status
        article.status = newsData.getString("pubstatus");

        //Date
        String dateString = newsData.getString("firstcreated");
        Instant date = Instant.parse(dateString);
        article.date = LocalDate.ofInstant(date, ZoneId.systemDefault());

        //Role
        article.role = newsData.getString("editorialrole");

        //Language
        article.language = newsData.getString("language");

        //Title
        article.title = newsData.getString("title");

        //Headline
        article.headline = newsData.getString("headline");

        //Extended Headline
        if (newsData.has("headline_extended")) {
            article.extendedHeadline = newsData.getString("headline_extended");
        } else {
            article.extendedHeadline = article.headline;
        }

        //Summary
        if (newsData.has("description_summary")) {
            article.summary = newsData.getString("description_summary");
        }

        //News Article
        String rawArticle = newsData.getString("body_nitf");
        article.article = Jsoup.clean(rawArticle, "", Safelist.none());

        //Categories
        JSONArray subjects = newsData.getJSONArray("subject");
        for (int i=0; i<subjects.length(); i++) {
            JSONObject subject = subjects.getJSONObject(i);
            String creator = subject.getString("creator");
            if ("Editorial".equals(creator) && subject.has("rels")) {
                JSONArray rels = subject.getJSONArray("rels");
                for (int r = 0; r < rels.length(); r++) {
                    String rel = rels.getString(r);
                    if ("direct".equals(rel)) {
                        article.categories.add(subject.getString("name"));
                        break;
                    }
                }
            }
        }

        return article;
    }
}
