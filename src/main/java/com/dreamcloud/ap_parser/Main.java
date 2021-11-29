package com.dreamcloud.ap_parser;

import org.apache.commons.cli.*;

public class Main {
    public static boolean nonEmpty(String s) {
        return s != null && !s.equals("");
    }

    public static void main(String[] args) {
        System.out.println("Hello, AP news!");
        Options options = new Options();
        Option infoOption = new Option("info", "info", true, "news source dir -- Displays information about the news articles.");
        infoOption.setRequired(false);
        options.addOption(infoOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            String infoDirectory = cmd.getOptionValue("info");

            if (nonEmpty(infoDirectory)) {
                System.out.println("Gathering info for '" + infoDirectory + "'...");
            } else {
                formatter.printHelp("ap_parser", options);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
