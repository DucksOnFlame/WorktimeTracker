package com.ducksonflame.worktimetracker.loggers;

import com.ducksonflame.worktimetracker.stattracker.StatTracker;
import com.ducksonflame.worktimetracker.database.DbConnectionManager;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InLogger {

    private DbConnectionManager dbConnectionManager;
    private StatTracker statTracker;
    private BufferedReader br;

    public InLogger(DbConnectionManager dbConnectionManager, StatTracker statTracker, BufferedReader br) {

        this.dbConnectionManager = dbConnectionManager;
        this.statTracker = statTracker;
        this.br = br;

        if (checkIfAlreadyLoggedToday()) {

            System.out.println("Found a \"Clock in\" log for today.");
            statTracker.printTodaysStatistics();

        } else {

            System.out.println("Clocking you in...");
            clockIn();
            statTracker.printClockInStatistics();

        }

    }

    private boolean checkIfAlreadyLoggedToday() {

        String sql = "SELECT DayIn FROM WorktimeIn WHERE DayIn = date('now', 'localtime');";

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }

    private void clockIn() {

        String sql = "INSERT INTO WorktimeIn(DayIn, TimeIn) VALUES (date('now', 'localtime'), " + statTracker.getSecondsToday() + ");";

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(date);
        System.out.println("Log for: " + today + " created.");

    }

    public void updateClockIn() {

        System.out.println("UPDATE CLOCK IN TIME");
        System.out.println("Please specify day to be updated and the new time. Type \"b\" to return to main menu.");
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

            updateClockInLog(day, time);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateClockInLog(String day, String time) {

        String sqlSelect = "SELECT TimeIn FROM WorktimeIn WHERE DayIn LIKE '" + day + "';";

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlSelect)) {

            if (rs.next()) {
                executeUpdateClockInLog(conn, day, time);
            } else {
                handleIfNoLogFound(conn, day, time);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeUpdateClockInLog(Connection conn, String day, String time) {

        int sqlTime = Utils.convertStringTimeToInt(time);

        String sqlUpdate = "UPDATE WorktimeIn SET TimeIn = " + sqlTime + " WHERE DayIn LIKE '" + day + "';";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUpdate);
            System.out.println("Changed the clock in time on " + day + " to " + time);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleIfNoLogFound(Connection conn, String day, String time) {

        System.out.println("No log for " + day + " found.");
        System.out.println("Do you want to create it? (Y/N)\n");

        try {

            while (true) {
                switch (br.readLine().toLowerCase()) {
                    case "n":
                        System.out.println("\nNo log created.");
                        return;

                    case "y":
                        executeCreateNewClockInLog(day, time, conn);
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

    private void executeCreateNewClockInLog(String day, String time, Connection conn) {

        int sqlTime = Utils.convertStringTimeToInt(time);

        String sql = "INSERT INTO WorktimeIn(DayIn, TimeIn) VALUES ('" + day + "', " + sqlTime + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Log for: " + day + " created.");

    }
}
