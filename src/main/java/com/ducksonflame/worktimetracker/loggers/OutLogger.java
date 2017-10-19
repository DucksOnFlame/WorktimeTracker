package com.ducksonflame.worktimetracker.loggers;


import com.ducksonflame.worktimetracker.data.DatabaseCommandInvoker;
import com.ducksonflame.worktimetracker.data.PersistObjectCommand;
import com.ducksonflame.worktimetracker.data.QueryCommand;
import com.ducksonflame.worktimetracker.dto.WorktimeInDTO;
import com.ducksonflame.worktimetracker.dto.WorktimeOutDTO;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;

public class OutLogger {

    private BufferedReader bufferedReader;

    public OutLogger(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public void clockOut() {
        askForClockOut();
    }


    private void askForClockOut() {

        System.out.println("UPDATE/CREATE CLOCK OUT TIME");
        System.out.println("Please specify the clock out day and time. Type \"b\" to return to main menu.");
        System.out.println("Please use the following format: yyyy-MM-dd hh:mm:ss\n");

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        String[] input = (String[]) invoker.execute(new GetTimeAndDayConsoleCommand(bufferedReader));
        if (input == null || input.length < 2) {
            return;
        }
        String day = input[0];
        String time = input[1];

        if (doesLogExist(day)) {
            handleSameDayClockOut(day, time);
        } else {
            executeClockOut(day, time);
        }
    }

    private boolean doesLogExist(String day) {
        if (day == null) {
            day = Utils.getTodayString();
        }
        String hql = "FROM WorktimeOutDTO WHERE day = '" + day + "';";

        return !(new DatabaseCommandInvoker().executeQueryCommand(new QueryCommand(hql)).isEmpty());
    }


    public void shutdownClockOut() {
        if (doesLogExist(null)) {
            handleSameDayClockOut(null, null);
        } else {
            executeClockOut(null, null);
        }
    }

    public void quickClockOut() {
        String instruction = "\nClock out for today? (Y/N)";

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        Object[] result = invoker.execute(new YesNoConsoleCommand(instruction, bufferedReader));

        if (result != null) {
            Boolean decision = (Boolean) result[0];
            if (decision) {
                if (doesLogExist(Utils.getTodayString())) {
                    updateClockOut(null, null);
                } else {
                    executeClockOut(null, null);
                }
            } else {
                System.out.println("\nYou have not been clocked out.");
            }
        }
    }

    private void handleSameDayClockOut(String day, String time) {

        String instruction = "Log for the day found. Do you want to override? (Y/N)";

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        Object[] result = invoker.execute(new YesNoConsoleCommand(instruction, bufferedReader));

        if (result != null) {
            Boolean decision = (Boolean) result[0];
            if (decision) {
                updateClockOut(day, time);
            } else {
                System.out.println("\nYou have not been clocked out.");
            }
        }
    }

    private void updateClockOut(String day, String time) {

        int sqlTime;

        if (day == null) {
            day = Utils.getTodayString();
        }

        if (time == null) {
            sqlTime = Utils.getSecondsToday();
        } else {
            sqlTime = Utils.convertStringTimeToInt(time);
        }

        String hql = "FROM WorktimeOutDTO WHERE day LIKE '" + day + "';";

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        WorktimeOutDTO worktimeOutDTO = (WorktimeOutDTO) invoker.executeQueryCommand(new QueryCommand(hql)).get(0);
        worktimeOutDTO.setTimeOut(sqlTime);
        invoker.executeUpdateCommand(new PersistObjectCommand(worktimeOutDTO));

        System.out.println("Clockout time for " + day + " overridden.");
    }

    private void executeClockOut(String day, String time) {

        int sqlTime;

        if (day == null) {
            day = Utils.getTodayString();
        }

        if (time == null) {
            sqlTime = Utils.getSecondsToday();
        } else {
            sqlTime = Utils.convertStringTimeToInt(time);
        }

        WorktimeInDTO worktimeInDTO = new WorktimeInDTO();
        worktimeInDTO.setDay(day);
        worktimeInDTO.setTimeIn(sqlTime);
        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.executeUpdateCommand(new PersistObjectCommand(worktimeInDTO));

        System.out.println("Clocked out.");
    }
}
