package com.ducksonflame.worktimetracker;

import com.ducksonflame.worktimetracker.database.DbConnectionManager;
import com.ducksonflame.worktimetracker.loggers.BreakLogger;
import com.ducksonflame.worktimetracker.loggers.InLogger;
import com.ducksonflame.worktimetracker.loggers.OutLogger;
import com.ducksonflame.worktimetracker.stattracker.StatTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WorktimeTracker {

    private DbConnectionManager dbConnManager;
    private BufferedReader bufferedReader;
    private StatTracker statTracker;
    private InLogger inLogger;
    private OutLogger outLogger;
    private BreakLogger breakLogger;

    public static void main(String[] args) {

        WorktimeTracker worktimeTracker = new WorktimeTracker();

        if (args != null && args.length > 0 && args[0].equals("shutdown")) {
            worktimeTracker.shutdownClockOut();
        } else {
            worktimeTracker.initConsoleUi();
        }
    }

    private WorktimeTracker() {

        dbConnManager = new DbConnectionManager();
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        statTracker = new StatTracker(dbConnManager, bufferedReader);
        inLogger = new InLogger(dbConnManager, statTracker, bufferedReader);
        outLogger = new OutLogger(dbConnManager, statTracker, bufferedReader);
        breakLogger = new BreakLogger(dbConnManager, bufferedReader);

    }

    private void initConsoleUi() {
        try {

            askForAction();
            exit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void askForAction() throws IOException {

        System.out.println("\n\n------------------------------------------------------------------------");
        System.out.println("What would you like to do?");
        printAvailableCommands();

        while (true) {

            switch (bufferedReader.readLine().toLowerCase()) {
                case "cin":
                    inLogger.updateClockIn();
                    break;

                case "cout":
                    outLogger.clockOut();
                    break;

                case "cout now":
                    outLogger.quickClockOut();
                    break;

                case "break":
                    breakLogger.logBreak();
                    break;

                case "clear":
                    breakLogger.clearBreaks();
                    break;

                case "stat":
                    statTracker.printFullStatistics();
                    break;

                case "logs":
                    statTracker.printQuarterLogs(false);
                    break;

                case "spec logs":
                    statTracker.printQuarterLogs(true);
                    break;

                case "quit":
                    System.out.println("Quitting...");
                    exit();
                    return;

                default:
                    System.out.println("Please enter a valid command");
                    printAvailableCommands();
                    continue;
            }

            askForAction();
        }
    }

    private void printAvailableCommands() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("cin       - Update/create a clock in log");
        System.out.println("cout      - Update/create a clock out log");
        System.out.println("cout now  - Clock out now");
        System.out.println("break     - Register a break");
        System.out.println("clear     - Clear existing breaks for a given day");
        System.out.println("stat      - Show quick, summarised statistics");
        System.out.println("logs      - Show all logs for this quarter");
        System.out.println("spec logs - Show logs from a specific quarter");
        System.out.println("quit      - Quit");
        System.out.println("------------------------------------------------------------------------\n");
    }

    private void shutdownClockOut() {
        outLogger.shutdownClockOut();
    }

    private void exit() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}