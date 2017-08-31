package com.ducksonflame.worktimetracker.loggers;


import com.ducksonflame.worktimetracker.stattracker.StatTracker;
import com.ducksonflame.worktimetracker.database.DbConnectionManager;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutLogger {

    private DbConnectionManager dbConnectionManager;
    private StatTracker statTracker;
    private BufferedReader br;


    public OutLogger(DbConnectionManager dbConnectionManager, StatTracker statTracker, BufferedReader br) {

        this.dbConnectionManager = dbConnectionManager;
        this.statTracker = statTracker;
        this.br = br;
    }

    public void clockOut() {

        askForClockOut();

    }

    public void shutdownClockOut() {
        if (doesLogExist(null)) {
            handleSameDayClockOut(null, null);
        } else {
            executeClockOut(null, null);
        }
    }


    private void askForClockOut() {

        System.out.println("UPDATE/CREATE CLOCK OUT TIME");
        System.out.println("Please specify the clock out day and time. Type \"b\" to return to main menu.");
        System.out.println("Please use the following format: yyyy-MM-dd hh:mm:ss\n");

        try {

            Pattern dayPattern = Utils.getDayPattern();
            Pattern timePattern = Utils.getTimePattern();
            boolean isInputCorrect = false;
            String day = "";
            String time = "";

            while (!isInputCorrect) {

                String userInput = br.readLine();

                if (userInput.toLowerCase().equals("b")) {
                    System.out.println("Clock out log NOT created/updated.");
                    return;
                }

                Matcher matcher = dayPattern.matcher(userInput);

                if (matcher.find()) {
                    day = matcher.group();
                    isInputCorrect = true;
                }

                if (isInputCorrect) {
                    matcher = timePattern.matcher(userInput);

                    if (matcher.find()) {
                        time = matcher.group();
                    } else {
                        isInputCorrect = false;
                    }
                }

                if (!isInputCorrect) {
                    System.out.println("Please provide correct input! (yyyy-MM-dd hh:mm:ss or \"b\" to go back to main menu)\n");
                }
            }

            if (doesLogExist(day)) {
                handleSameDayClockOut(day, time);
            } else {
                executeClockOut(day, time);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean doesLogExist(String day) {

        String sqlSelect;

        if (day == null) {
            sqlSelect = "SELECT DayOut FROM WorktimeOut WHERE DayOut = date('now', 'localtime');";
        } else {
            sqlSelect = "SELECT DayOut FROM WorktimeOut WHERE DayOut = '" + day + "';";
        }

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlSelect)) {

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void quickClockOut() {
        if (!askForQuickClockOut()) {
            return;
        }

        if (doesLogExist(null)) {
            handleSameDayClockOut(null, null);
        } else {
            executeClockOut(null, null);
        }
    }

    private boolean askForQuickClockOut() {

        System.out.println("\nClock out for today? (Y/N)");

        try {

            while (true) {
                switch (br.readLine().toLowerCase()) {
                    case "n":
                        System.out.println("\nYou have not been clocked out.");
                        return false;

                    case "y":
                        executeClockOut(null, null);
                        return true;

                    default:
                        System.out.println("Please enter a valid command (Y or N)");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleSameDayClockOut(String day, String time) {

        System.out.println("Log for the day found. Do you want to override? (Y/N)");

        while (true) {
            try {
                switch (br.readLine().toLowerCase()) {
                    case "n":
                        return;

                    case "y":
                        updateClockOut(day, time);
                        return;

                    default:
                        System.out.println("Please enter a valid command (Y or N)");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateClockOut(String day, String time) {

        String sqlUpdate;

        if (day == null || time == null) {
            sqlUpdate = "UPDATE WorktimeOut SET TimeOut = " + statTracker.getSecondsToday() + " WHERE DayOut = date('now', 'localtime');";
        } else {
            int sqlTime = Utils.convertStringTimeToInt(time);
            sqlUpdate = "UPDATE WorktimeOut SET TimeOut = " + sqlTime + " WHERE DayOut = '" + day + "';";
        }

        try (Statement stmt = dbConnectionManager.getDbConnection().createStatement()) {
            stmt.execute(sqlUpdate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        if (day == null) {
            day = "today";
        }

        System.out.println("Clockout time for " + day + " overriden.");

    }

    private void executeClockOut(String day, String time) {

        String sqlInsert;

        if (day == null || time == null) {
            sqlInsert = "INSERT INTO WorktimeOut(DayOut, TimeOut) VALUES (date('now', 'localtime'), " + statTracker.getSecondsToday() + ");";
        } else {
            int sqlTime = Utils.convertStringTimeToInt(time);
            sqlInsert = "INSERT INTO WorktimeOut(DayOut, TimeOut) VALUES ('" + day + "', " + sqlTime + ");";
        }


        try (Statement stmt = dbConnectionManager.getDbConnection().createStatement()) {
            stmt.execute(sqlInsert);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Clocked out.");
    }

}
