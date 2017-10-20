package com.ducksonflame.worktimetracker.loggers;

import com.ducksonflame.worktimetracker.data.DatabaseCommandInvoker;
import com.ducksonflame.worktimetracker.data.PersistObjectCommand;
import com.ducksonflame.worktimetracker.data.QueryCommand;
import com.ducksonflame.worktimetracker.dto.WorktimeInDTO;
import com.ducksonflame.worktimetracker.stattracker.StatTracker;
import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.util.List;


public class InLogger {

    private BufferedReader bufferedReader;

    public InLogger(StatTracker statTracker, BufferedReader br) {
        this.bufferedReader = br;

        if (checkIfAlreadyLoggedToday()) {
            System.out.println("Found a \"Clock in\" log for today.");
            statTracker.printDailyStatistics(false);
        } else {
            System.out.println("Clocking you in...");
            clockIn();
            statTracker.printDailyStatistics(true);
        }
    }

    private boolean checkIfAlreadyLoggedToday() {
        String hql = "FROM WorktimeInDTO WHERE day = " + Utils.getTodayString();
        return new DatabaseCommandInvoker().executeQueryCommand(new QueryCommand(hql)).isEmpty();
    }

    private void clockIn() {
        WorktimeInDTO worktimeInDTO = new WorktimeInDTO();
        worktimeInDTO.setDay(Utils.getTodayString());
        worktimeInDTO.setTimeIn(Utils.getSecondsToday());

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.executeUpdateCommand(new PersistObjectCommand(worktimeInDTO));

        System.out.println("Log for: " + Utils.getTodayString() + " created.");
    }

    public void updateClockIn() {

        System.out.println("UPDATE CLOCK IN TIME");
        System.out.println("Please specify day to be updated and the new time. Type \"b\" to return to main menu.");
        System.out.println("Please use the following format: yyyy-MM-dd hh:mm:ss\n");

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        String[] input = (String[]) invoker.execute(new GetTimeAndDayConsoleCommand(bufferedReader));
        if (input == null || input.length < 2) {
            return;
        }
        String day = input[0];
        String time = input[1];

        updateClockInLog(day, time);
    }

    private void updateClockInLog(String day, String time) {
        String hql = "FROM WorktimeInDTO WHERE day LIKE '" + day + "'";

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        List result = invoker.executeQueryCommand(new QueryCommand(hql));

        if (!result.isEmpty()) {
            executeUpdateClockInLog(time, (WorktimeInDTO) result.get(0));
        } else {
            handleNoLogFound(day, time);
        }
    }

    private void executeUpdateClockInLog(String time, WorktimeInDTO worktimeInDTO) {
        int sqlTime = Utils.convertStringTimeToInt(time);

        worktimeInDTO.setTimeIn(sqlTime);

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.executeUpdateCommand(new PersistObjectCommand(worktimeInDTO));
    }

    private void handleNoLogFound(String day, String time) {

        String instruction = "No log for " + day + " found.\n" +
                "Do you want to create it? (Y/N)\n";

        ConsoleCommandInvoker invoker = new ConsoleCommandInvoker();
        Object[] result = invoker.execute(new YesNoConsoleCommand(instruction, bufferedReader));

        if (result != null) {
            Boolean decision = (Boolean) result[0];
            if (decision) {
                executeCreateNewClockInLog(day, time);
            } else {
                System.out.println("\nNo log created.");
            }
        }
    }

    private void executeCreateNewClockInLog(String day, String time) {
        int sqlTime = Utils.convertStringTimeToInt(time);

        WorktimeInDTO worktimeInDTO = new WorktimeInDTO();
        worktimeInDTO.setDay(day);
        worktimeInDTO.setTimeIn(sqlTime);

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.executeUpdateCommand(new PersistObjectCommand(worktimeInDTO));

        System.out.println("Log for: " + day + " created.");
    }
}
