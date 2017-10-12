package com.ducksonflame.worktimetracker.stattracker;

import com.ducksonflame.worktimetracker.database.DbConnectionManager;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StatTracker {

    private DbConnectionManager dbConnectionManager;
    private BufferedReader bufferedReader;

    public StatTracker(DbConnectionManager dbConnectionManager, BufferedReader br) {

        this.dbConnectionManager = dbConnectionManager;
        this.bufferedReader = br;

    }

    public void printFullStatistics() {

        System.out.println("Here are your statistics:");
        printTodaysStatistics();
        printMonthlyStatistics();
        printQuarterlyStatistics();

    }

    // Model frame with middle indicated
    // ---------------------------------------|---------------------------------------

    public void printTodaysStatistics() {
        System.out.println("\n---------------------------------TODAY----------------------------------");
        System.out.println("You have arrived at " + getTimeArrivedString() + " today.");
        System.out.println("\nYou have been working for " + getTimeWorkingString() + " today.");
        System.out.println("------------------------------------------------------------------------\n");
    }

    public void printClockInStatistics() {
        System.out.println("\n--------------------------------CLOCK IN--------------------------------");
        System.out.println("You have arrived at " + getTimeArrivedString() + " today.");
        System.out.println("------------------------------------------------------------------------");
    }

    private void printMonthlyStatistics() {

        Calendar mCalendar = Calendar.getInstance();
        String month = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

        long balance = getMonthlyBalance();

        System.out.println("\n---------------------------------MONTH----------------------------------");
        System.out.print("Here is your balance for " + month + ": \n" + Utils.getHHMMSSFormattedString(Math.abs(balance)));

        printBalanceComment(balance);

        System.out.println("\n(This includes today's worktime.)");
        System.out.println("------------------------------------------------------------------------\n");

    }

    private void printQuarterlyStatistics() {

        long balance = getQuarterlyBalance();

        System.out.println("\n--------------------------------QUARTER---------------------------------");
        System.out.print("Here is your balance for this quarter: \n" + Utils.getHHMMSSFormattedString(Math.abs(balance)));

        printBalanceComment(balance);

        System.out.println("\n(This includes today's worktime.)");
        System.out.println("------------------------------------------------------------------------\n");

    }

    private void printBalanceComment(long balance) {
        if (balance > 0) {
            System.out.println(" OVER the expected worktime.");
        } else if (balance == 0) {
            System.out.println("\nYou brought balance to the Force!");
        } else {
            System.out.println(" BELOW the expected worktime.");
        }
    }

    private long getWorktimeToday() {

        String sql = "SELECT TimeIn FROM WorktimeIn WHERE DayIn = date('now', 'localtime');";

        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            rs.next();
            return rs.getInt("TimeIn");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }

    }

    private String getTimeArrivedString() {
        long timeArrived = getWorktimeToday();
        return String.format("%02d:%02d:%02d", timeArrived / 3600, timeArrived / 60 % 60, timeArrived % 60);
    }

    private String getTimeWorkingString() {

        long timeArrived = getWorktimeToday();
        long secondsPassed = getSecondsToday() - timeArrived;

        return Utils.getHHMMSSFormattedString(secondsPassed);
    }

    public long getSecondsToday() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime midnight = now.truncatedTo(ChronoUnit.DAYS);
        Duration duration = Duration.between(midnight, now);
        return duration.getSeconds();
    }

    private long getMonthlyBalance() {

        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String year = new SimpleDateFormat("yyyy").format(date);
        String month = new SimpleDateFormat("MM").format(date);

        String sql = "SELECT IFNULL((SUM((IFNULL(TimeOut, 0) - TimeIn))) - (COUNT(TimeIn)*28800), 0)\n" +
                " + (SELECT (" + getSecondsToday() + " - TimeIn) - 28800 FROM WorktimeIn WHERE DayIn LIKE '" + today + "')\n" +
                " - (SELECT IFNULL(SUM(IFNULL(Break.BreakEnd - Break.BreakBegin, 0)), 0) FROM Break INNER JOIN WorktimeIn ON Break.BreakDay = WorktimeIn.DayIn WHERE BreakDay LIKE '" + year + "-" + month + "-__')\n" +
                "FROM WorktimeIn\n" +
                "INNER JOIN WorktimeOut\n" +
                "ON DayIn = WorktimeOut.DayOut\n" +
                "WHERE DayIn LIKE '" + year + "-" + month + "-__';";

        return getWorktimeFromDatabase(sql);
    }

    private long getQuarterlyBalance() {

        Date date = new Date();
        String year = Integer.toString(getCurrentYear());

        String[] months = new String[3];
        int quarter = getCurrentQuarter();

        for (int i = 0; i < 3; i++) {
            months[i] = String.format("%02d", 3 * (quarter - 1) + i + 1);
        }

        String today = new SimpleDateFormat("yyyy-MM-dd").format(date);

        String sql = "SELECT IFNULL((SUM((IFNULL(WorktimeOut.TimeOut, 0) - WorktimeIn.TimeIn))) - (COUNT(WorktimeIn.TimeIn)*28800), 0)\n" +
                " + (SELECT (" + getSecondsToday() + " - WorktimeIn.TimeIn) - 28800 FROM WorktimeIn WHERE WorktimeIn.DayIn LIKE '" + today + "')\n" +
                " - (SELECT IFNULL(SUM(IFNULL(Break.BreakEnd - Break.BreakBegin, 0)), 0) FROM Break INNER JOIN WorktimeIn ON Break.BreakDay = WorktimeIn.DayIn WHERE BreakDay LIKE '" + year + "-" + months[0] + "-__' OR BreakDay LIKE '" + year + "-" + months[1] + "-__' OR BreakDay LIKE '" + year + "-" + months[2] + "-__')\n" +
                "FROM WorktimeIn\n" +
                "INNER JOIN WorktimeOut\n" +
                "ON WorktimeIn.DayIn = WorktimeOut.DayOut\n" +
                "WHERE WorktimeIn.DayIn LIKE '" + year + "-" + months[0] + "-__' OR WorktimeIn.DayIn LIKE '" + year + "-" + months[1] + "-__' OR WorktimeIn.DayIn LIKE '" + year + "-" + months[2] + "-__';";

        return getWorktimeFromDatabase(sql);
    }

    private long getWorktimeFromDatabase(String sql) {
        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public void printQuarterLogs(boolean specificQuarter) {

        if (!specificQuarter) {
            executePrintQuarterLogs();
            return;
        }

        Pattern pattern = Utils.getQuarterPatern();

        System.out.println("Please specify the quarter and year in which you are interested.");
        System.out.println("Please use the following format: Q-yyyy\n");

        String userInput;
        int quarter = 0;
        int year = 0;

        Date date = new Date();
        int minYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(date));
        int minQuarter = (Integer.parseInt(new SimpleDateFormat("MM").format(date)) / 3) + 1;

        try {

            boolean isInputCorrect = false;

            while (!isInputCorrect) {

                userInput = bufferedReader.readLine();
                Matcher matcher = pattern.matcher(userInput);

                if (matcher.find()) {
                    userInput = matcher.group();

                    quarter = Integer.parseInt(userInput.substring(0, 1));
                    year = Integer.parseInt(userInput.substring(2, 6));

                    if (year < minYear || (year == minYear && quarter <= minQuarter)) {
                        isInputCorrect = true;
                    } else {
                        System.out.println("I am not a fortune teller!\n");
                        System.out.println("Please do not ask me about the future!\n");
                        continue;
                    }
                }

                if (!isInputCorrect) {
                    System.out.println("Please provide correct input! (Q-yyyy)\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        executePrintQuarterLogs(year, quarter);
    }

    private void executePrintQuarterLogs() {
        executePrintQuarterLogs(getCurrentYear(), getCurrentQuarter());
    }


    private void executePrintQuarterLogs(int year, int quarter) {

        System.out.println();

        List<String> months = new ArrayList<>();

        Date date = new Date();
        int currentMonth = Integer.parseInt(new SimpleDateFormat("MM").format(date));

        for (int i = 0; i < 3; i++) {
            int monthNumber = 3 * (quarter - 1) + i + 1;

            if (monthNumber > currentMonth)
                break;

            months.add(String.format("%02d", monthNumber));
        }

        String sql;
        String day;

        String timeInString;
        String timeOutString;
        String breakTimeString;
        String workTimeString;
        String balanceString;
        String monthlyBalanceString;
        String totalBalanceString;
        long timeIn;
        long timeOut;
        long breakTime;
        long workTime;
        long balance;
        long monthlyBalance;
        long totalBalance = 0;

        System.out.println("|----------------------------- LOGS FOR QUARTER " + quarter + " ----------------------------|\n");
        if (months.isEmpty()) {
            System.out.println("|-----------------------------------------------------------------------------|");
            System.out.println("|                                 NO LOGS YET                                 |");
            System.out.println("|-----------------------------------------------------------------------------|");
            return;
        }


        for (String month : months) {
            monthlyBalance = 0;
            String monthString = Utils.getTitleMonthString(month);
            System.out.println("|---------------------------------" + monthString + "---------------------------------|");
            System.out.println("|-----------------------------------------------------------------------------|");
            System.out.println("|     DAY    | TIME IN  | TIME OUT | BREAK TIME  |  WORKTIME   |   BALANCE    |");
            System.out.println("|-----------------------------------------------------------------------------|");

            boolean logsExist = false;

            for (int j = 1; j < 32; j++) {

                sql = "SELECT win.DayIn, win.TimeIn, TimeOut, (SELECT SUM(BreakEnd - BreakBegin) FROM Break WHERE Break.BreakDay = win.DayIn) BreakTime\n" +
                        "FROM WorktimeIn win\n" +
                        "INNER JOIN WorktimeOut ON win.DayIn = WorktimeOut.DayOut\n" +
                        "WHERE DayIn = \"" + year + "-" + month + "-" + String.format("%02d", j) + "\"";

                try (Connection conn = dbConnectionManager.getDbConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    if (rs.next()) {
                        logsExist = true;
                        day = rs.getString(1);
                        timeIn = rs.getLong(2);
                        timeOut = rs.getLong(3);
                        breakTime = rs.getLong(4);

                        workTime = timeOut - timeIn - breakTime;
                        balance = workTime - 28800;
                        monthlyBalance += balance;

                        timeInString = Utils.getTimeFormattedString(timeIn);
                        timeOutString = Utils.getTimeFormattedString(timeOut);
                        breakTimeString = Utils.getHHMMSSFormattedStringFixedSpace(breakTime);
                        workTimeString = Utils.getHHMMSSFormattedStringFixedSpace(workTime);
                        balanceString = Utils.getHHMMSSFormattedStringFixedSpaceSignSafe(balance);

                        System.out.println("| " + day + " | " + timeInString + " | " + timeOutString + " | " + breakTimeString + " | " + workTimeString + " | " + balanceString + " |");
                    }

                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (!logsExist) {
                System.out.println("|                                   NO LOGS                                   |");
            }

            monthlyBalanceString = Utils.getHHMMSSFormattedStringFixedSpaceSignSafe(monthlyBalance);
            System.out.println("|---------------------------------------------|-------------------------------|");
            System.out.println("                                              | MONTHLY BALANCE: " + monthlyBalanceString + " |");
            System.out.println("                                              |-------------------------------|\n");
            totalBalance += monthlyBalance;
        }

        totalBalanceString = Utils.getHHMMSSFormattedStringFixedSpaceSignSafe(totalBalance);

        System.out.println("                                            |---------------------------------|");
        System.out.println("                                            | QUARTERLY BALANCE: " + totalBalanceString + " |");
        System.out.println("                                            |---------------------------------|");
    }

    private int getCurrentYear() {
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
    }

    private int getCurrentQuarter() {
        Date date = new Date();
        int quarter = (Integer.parseInt(new SimpleDateFormat("MM").format(date)) / 3);

        if (Integer.parseInt(new SimpleDateFormat("MM").format(date)) % 3 != 0) {
            quarter++;
        }

        return quarter;
    }

    private int getCurrentMonth() {
        return Integer.parseInt(new SimpleDateFormat("MM").format(new Date()));
    }
}