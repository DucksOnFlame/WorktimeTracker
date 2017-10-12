package com.ducksonflame.worktimetracker.loggers;


import com.ducksonflame.worktimetracker.database.DbConnectionManager;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreakLogger {

    private DbConnectionManager dbConnectionManager;
    private BufferedReader bufferedReader;


    public BreakLogger(DbConnectionManager dbConnectionManager, BufferedReader bufferedReader) {

        this.dbConnectionManager = dbConnectionManager;
        this.bufferedReader = bufferedReader;

    }

    public void logBreak() {
        System.out.println("\nDo you want to register a break? (Y/N)");

        try {

            while (true) {
                switch (bufferedReader.readLine().toLowerCase()) {
                    case "n":
                        System.out.println("\nYou have not registered a break.");
                        return;

                    case "y":
                        String day = askForBreakDay();
                        int[] breakTimes = getBreakTimes();

                        registerBreak(day, breakTimes[0], breakTimes[1]);
                        return;

                    default:
                        System.out.println("Please enter a valid command (Y or N)");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String askForBreakDay() {

        Pattern dayPattern = Utils.getDayPattern();
        String day = "";

        System.out.println("BREAK DAY");
        System.out.println("Please provide the day of the break.");
        System.out.println("Please use the following format: yyyy-MM-dd\n");

        try {

            boolean isInputCorrect = false;

            while (!isInputCorrect) {

                String userInput = bufferedReader.readLine();
                Matcher matcher = dayPattern.matcher(userInput);

                if (matcher.find()) {
                    day = matcher.group();
                    isInputCorrect = true;
                }

                if (!isInputCorrect) {
                    System.out.println("Please provide correct input! (yyyy-MM-dd)\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return day;
    }

    private int[] getBreakTimes() {
        int breakBegin = askForBreakTime(true);
        int breakEnd = askForBreakTime(false);

        while (breakEnd <= breakBegin) {
            System.out.println("Breaks must begin BEFORE they end!");

            breakBegin = askForBreakTime(true);
            breakEnd = askForBreakTime(false);
        }

        return new int[]{breakBegin, breakEnd};
    }

    private int askForBreakTime(boolean beginning) {

        Pattern timePattern = Utils.getTimePattern();
        String time = "";

        if (beginning) {
            System.out.println("At what time did your break start?");
            System.out.println("Please use the following format: hh:mm:ss\n");
        } else {
            System.out.println("At what time did your break end?");
            System.out.println("Please use the following format: hh:mm:ss\n");
        }

        try {

            boolean isInputCorrect = false;

            while (!isInputCorrect) {

                String userInput = bufferedReader.readLine();
                Matcher matcher = timePattern.matcher(userInput);

                if (matcher.find()) {
                    time = matcher.group();
                    isInputCorrect = true;
                }

                if (!isInputCorrect) {
                    System.out.println("Please provide correct input! (hh:mm:ss)\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Utils.convertStringTimeToInt(time);
    }

    private void registerBreak(String day, int breakBegin, int breakEnd) {

        String sql = "INSERT INTO Break(BreakDay, BreakBegin, BreakEnd) VALUES ('" + day + "', " + breakBegin + ", " + breakEnd + ");";

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Break for: " + day + " registered between " + Utils.getTimeFormattedString(breakBegin) + " and " + Utils.getTimeFormattedString(breakEnd));
    }

    public void clearBreaks() {

        String day = askForBreakDay();
        System.out.println("Do you want to clear all breaks logged for " + day + "?");

        try {

            while (true) {
                switch (bufferedReader.readLine().toLowerCase()) {
                    case "n":
                        System.out.println("\nOperation cancelled.");
                        return;

                    case "y":
                        executeClearBreaks(day);
                        return;

                    default:
                        System.out.println("Please enter a valid command (Y or N)");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeClearBreaks(String day) {

        String sql = "DELETE FROM Break WHERE (Break.BreakDay = '" + day + "')";

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("All breaks for: " + day + " deleted.");
    }
}
