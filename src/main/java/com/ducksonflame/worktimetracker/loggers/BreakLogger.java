package com.ducksonflame.worktimetracker.loggers;


import com.ducksonflame.worktimetracker.data.DatabaseCommandInvoker;
import com.ducksonflame.worktimetracker.data.PersistObjectCommand;
import com.ducksonflame.worktimetracker.data.UpdateDatabaseCommand;
import com.ducksonflame.worktimetracker.dto.BreakDTO;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreakLogger {

    private BufferedReader bufferedReader;

    public BreakLogger(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public void logBreak() {
        String instruction = "\nDo you want to register a break? (Y/N)";

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        Object[] result = invoker.execute(new YesNoConsoleCommand(instruction, bufferedReader));

        if (result != null) {
            Boolean decision = (Boolean) result[0];
            if (decision) {
                String day = askForBreakDay();
                int[] breakTimes = getBreakTimes();

                registerBreak(day, breakTimes[0], breakTimes[1]);
            } else {
                System.out.println("\nYou have not registered a break.");
            }
        }
    }

    public void clearBreaks() {
        String day = askForBreakDay();
        String instruction = "Do you want to clear all breaks logged for " + day + "?";

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        Object[] result = invoker.execute(new YesNoConsoleCommand(instruction, bufferedReader));

        if (result != null) {
            Boolean decision = (Boolean) result[0];
            if (decision) {
                executeClearBreaks(day);
            } else {
                System.out.println("\nOperation cancelled.");
            }
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
        int breakBegin, breakEnd;

        while (true) {
            breakBegin = askForBreakTime(true);
            breakEnd = askForBreakTime(false);

            if (breakEnd > breakBegin) {
                break;
            } else {
                System.out.println("Breaks must begin BEFORE they end!");
            }
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

        BreakDTO breakDTO = new BreakDTO();
        breakDTO.setBreakDay(day);
        breakDTO.setBreakBegin(breakBegin);
        breakDTO.setBreakEnd(breakEnd);

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.executeUpdateCommand(new PersistObjectCommand(breakDTO));

        System.out.println("Break for: " + day + " registered between " + Utils.getTimeFormattedString(breakBegin) + " and " + Utils.getTimeFormattedString(breakEnd));
    }

    private void executeClearBreaks(String day) {
        String hql = "DELETE Break WHERE breakDay = '" + day + "')";

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.executeUpdateCommand(new UpdateDatabaseCommand(hql));

        System.out.println("All breaks for: " + day + " deleted.");
    }
}
