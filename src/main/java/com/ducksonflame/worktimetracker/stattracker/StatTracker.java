package com.ducksonflame.worktimetracker.stattracker;

import com.ducksonflame.worktimetracker.data.DatabaseCommandInvoker;
import com.ducksonflame.worktimetracker.data.QueryCommand;
import com.ducksonflame.worktimetracker.data.SQLQueryCommand;
import com.ducksonflame.worktimetracker.dto.LogDTO;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StatTracker {

    private BufferedReader bufferedReader;

    public StatTracker(BufferedReader br) {
        this.bufferedReader = br;
    }

    public void printFullStatistics() {
        System.out.println("\nHere are your statistics:");
        printDailyStatistics(false);
        printMonthlyStatistics();
        printQuarterlyStatistics();
    }

    // Model frame with middle indicated
    // ---------------------------------------|---------------------------------------

    public void printDailyStatistics(boolean isClockIn) {
        System.out.println("\n------------------------------  CLOCK IN  ------------------------------");
        System.out.println("You have arrived at " + getTimeArrivedString() + " today.");
        if (!isClockIn) {
            System.out.println("\nYou have been working for " + getTimeWorkingString() + " today.");
        }
        System.out.println("------------------------------------------------------------------------");
    }

    private void printMonthlyStatistics() {
        long balance = getMonthlyBalance();
        printBalanceStatistics(Utils.getTitleMonthString(String.valueOf(Utils.getCurrentMonth())), balance);
    }

    private void printQuarterlyStatistics() {
        long balance = getQuarterlyBalance();
        printBalanceStatistics("QUARTER", balance);
    }

    private void printBalanceStatistics(String title, long balance) {

        title = "  " + title.trim() + "  ";
        String titleDecorationHalf = "-----------------------------";
        String endDecoration = "------------------------------------------------------------------------\n";

        int missingChars = 14 - title.length();
        StringBuilder builder = new StringBuilder(titleDecorationHalf);
        for (int i = 0; i < missingChars / 2; i++) {
            builder.append("-");
        }
        String firstHalf = builder.toString();
        if (missingChars % 2 == 1) {
            builder.append("-");
        }
        String secondHalf = builder.toString();

        System.out.println("\n" + firstHalf + title + secondHalf);
        System.out.print("Balance: " + Utils.getHHMMSSFormattedString(Math.abs(balance)));

        if (balance > 0) {
            System.out.println(" OVER the expected worktime.");
        } else if (balance == 0) {
            System.out.println("\nYou brought balance to the Force!");
        } else {
            System.out.println(" BELOW the expected worktime.");
        }

        System.out.println("\n(This includes today's worktime.)");
        System.out.println(endDecoration);
    }

    private long getMonthlyBalance() {
        String today = Utils.getTodayString();
        String month = Utils.getCurrentMonthString();
        String year = Utils.getCurrentYearString();

        String sql = "SELECT IFNULL((SUM((IFNULL(TimeOut, 0) - TimeIn))) - (COUNT(TimeIn)*28800), 0)\n" +
                " + (SELECT (" + Utils.getSecondsToday() + " - TimeIn) - 28800 FROM WorktimeIn WHERE day LIKE '" + today + "')\n" +
                " - (SELECT IFNULL(SUM(IFNULL(Break.BreakEnd - Break.BreakBegin, 0)), 0) FROM Break INNER JOIN WorktimeIn ON Break.BreakDay = WorktimeIn.day WHERE BreakDay LIKE '" + year + "-" + month + "-__')\n" +
                "FROM WorktimeIn\n" +
                "INNER JOIN WorktimeOut\n" +
                "ON WorktimeIn.day = WorktimeOut.day\n" +
                "WHERE WorktimeIn.day LIKE '" + year + "-" + month + "-__';";

        return getWorktimeFromDatabase(sql);
    }

    private long getQuarterlyBalance() {

        String year = Utils.getCurrentYearString();

        String[] months = new String[3];
        int quarter = Utils.getCurrentQuarter();

        for (int i = 0; i < 3; i++) {
            months[i] = String.format("%02d", 3 * (quarter - 1) + i + 1);
        }

        String today = Utils.getTodayString();

        String sql = "SELECT IFNULL((SUM((IFNULL(WorktimeOut.TimeOut, 0) - WorktimeIn.TimeIn))) - (COUNT(WorktimeIn.TimeIn)*28800), 0)\n" +
                " + (SELECT (" + Utils.getSecondsToday() + " - WorktimeIn.TimeIn) - 28800 FROM WorktimeIn WHERE WorktimeIn.day LIKE '" + today + "')\n" +
                " - (SELECT IFNULL(SUM(IFNULL(Break.BreakEnd - Break.BreakBegin, 0)), 0) FROM Break INNER JOIN WorktimeIn ON Break.BreakDay = WorktimeIn.day WHERE BreakDay LIKE '" + year + "-" + months[0] + "-__' OR BreakDay LIKE '" + year + "-" + months[1] + "-__' OR BreakDay LIKE '" + year + "-" + months[2] + "-__')\n" +
                "FROM WorktimeIn\n" +
                "INNER JOIN WorktimeOut\n" +
                "ON WorktimeIn.day = WorktimeOut.day\n" +
                "WHERE WorktimeIn.day LIKE '" + year + "-" + months[0] + "-__' OR WorktimeIn.day LIKE '" + year + "-" + months[1] + "-__' OR WorktimeIn.day LIKE '" + year + "-" + months[2] + "-__';";

        return getWorktimeFromDatabase(sql);
    }

    public void printQuarterLogs(boolean specificQuarter) {

        if (!specificQuarter) {
            executePrintQuarterLogs();
            return;
        }

        Pattern pattern = Utils.getQuarterPattern();

        System.out.println("Please specify the quarter and year in which you are interested.");
        System.out.println("Please use the following format: Q-yyyy\n");

        String userInput;
        int quarter = 0;
        int year = 0;

        int maxYear = Utils.getCurrentYear();
        int maxQuarter = Utils.getCurrentQuarter();

        try {

            boolean isInputCorrect = false;

            while (!isInputCorrect) {

                userInput = bufferedReader.readLine();
                Matcher matcher = pattern.matcher(userInput);

                if (matcher.find()) {
                    userInput = matcher.group();

                    quarter = Integer.parseInt(userInput.substring(0, 1));
                    year = Integer.parseInt(userInput.substring(2, 6));

                    if (year < maxYear || (year == maxYear && quarter <= maxQuarter)) {
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
        executePrintQuarterLogs(Utils.getCurrentYear(), Utils.getCurrentQuarter());
    }


    private void executePrintQuarterLogs(int year, int quarter) {

        List<String> months = new ArrayList<>();

        int currentMonth = Utils.getCurrentMonth();

        for (int i = 0; i < 3; i++) {
            int monthNumber = 3 * (quarter - 1) + i + 1;

            if (monthNumber > currentMonth)
                break;

            months.add(String.format("%02d", monthNumber));
        }

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

        System.out.println("\n|----------------------------- LOGS FOR QUARTER " + quarter + " ----------------------------|\n");
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

            String hql = "FROM LogDTO WHERE day LIKE '" + year + "-" + month + "-__'";

            boolean logsExist = false;
            DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
            List logs = (List<LogDTO>) invoker.executeQueryCommand(new QueryCommand(hql));

            for (Object obj : logs) {

                LogDTO log = (LogDTO) obj;

                if (log != null) {
                    logsExist = true;
                    day = log.getDay();
                    timeIn = log.getTimeIn();
                    timeOut = log.getTimeOut();
                    breakTime = log.getBreakTime();

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

    private String getTimeArrivedString() {
        return Utils.getTimeFormattedString(getTimeInToday());
    }

    private String getTimeWorkingString() {
        long timeIn = getTimeInToday();
        long secondsPassed = Utils.getSecondsToday() - timeIn;

        return Utils.getHHMMSSFormattedString(secondsPassed);
    }

    private int getTimeInToday() {
        String hql = "SELECT timeIn FROM WorktimeInDTO WHERE day LIKE '" + Utils.getTodayString() + "'";
        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        List result = invoker.executeQueryCommand(new QueryCommand(hql));
        if (!result.isEmpty()) {
            return (int) result.get(0);
        } else {
            return -1;
        }
    }

    private long getWorktimeFromDatabase(String sql) {
        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        List result = invoker.executeQueryCommand(new SQLQueryCommand(sql));
        if (!result.isEmpty()) {
            return (int) result.get(0);
        } else {
            return -1;
        }
    }
}
