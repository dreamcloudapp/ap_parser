package com.dreamcloud.ap_parser;

import java.io.File;

public class NewsDay {
    File directory;
    public int day;

    public NewsDay(File directory, int day) {
        this.directory = directory;
        this.day = day;
    }
}
