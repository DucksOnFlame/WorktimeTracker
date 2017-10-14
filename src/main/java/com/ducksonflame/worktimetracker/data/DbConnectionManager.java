package com.ducksonflame.worktimetracker.data;

import java.io.File;
import java.io.IOException;
import java.sql.*;


public class DbConnectionManager {

    private static String dbFilePath = "db/WorktimeDB.db";
    private static DbConnectionManager sInstance;

    private DbConnectionManager() {

        if (!checkIfDbExists()) {
            createNewDatabase();
        }
        checkConnection();
    }

    public static DbConnectionManager getInstance() {
        if (sInstance == null) {
            synchronized (DbConnectionManager.class) {
                if (sInstance == null) {
                    sInstance = new DbConnectionManager();
                }
            }
        }
        return sInstance;
    }

    private void createNewDatabase() {

        System.out.println("Creating a new database file...");

        try {
            new File("db").mkdirs();
            new File(dbFilePath).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("New database created.");
            } else {
                System.out.println("Could not establish connection.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        createTables();
    }

    private boolean checkIfDbExists() {

        File f = new File(dbFilePath);

        if (f.exists() && !f.isDirectory()) {
            System.out.println("Database file found.");
            return true;
        }

        System.out.println("Database file not found.");
        return false;
    }

    private void checkConnection() {
        try (Connection conn = getDbConnection()) {
            if (conn != null) {
                System.out.println("Connection to the SQLite Database has been established successfully.\n");
            } else {
                System.out.println("Could not connect to the database!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getDbConnection() {

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    private void createTables() {

        String inTableSql = "CREATE TABLE IF NOT EXISTS WorktimeIn (\n" +
                "    Id      INTEGER PRIMARY KEY\n" +
                "    UNIQUE\n" +
                "    NOT NULL,\n" +
                "    DayIn     TEXT    UNIQUE\n" +
                "    NOT NULL,\n" +
                "    TimeIn INTEGER    NOT NULL\n" +
                "    );";

        String outTableSql = "CREATE TABLE IF NOT EXISTS WorktimeOut (\n" +
                "    Id      INTEGER PRIMARY KEY\n" +
                "    UNIQUE\n" +
                "    NOT NULL,\n" +
                "    DayOut     TEXT    UNIQUE\n" +
                "    NOT NULL,\n" +
                "    TimeOut INTEGER    NOT NULL\n" +
                "    );";

        String breakTableSql = "CREATE TABLE IF NOT EXISTS Break (\n" +
                "    Id      INTEGER PRIMARY KEY\n" +
                "    UNIQUE\n" +
                "    NOT NULL,\n" +
                "    BreakDay     TEXT\n" +
                "    NOT NULL,\n" +
                "    BreakBegin INTEGER    NOT NULL,\n" +
                "    BreakEnd INTEGER    NOT NULL\n" +
                "    );";

        DatabaseCommandInvoker invoker = new DatabaseCommandInvoker();
        invoker.addCommand(new InitDatabaseCommand(inTableSql, this));
        invoker.addCommand(new InitDatabaseCommand(outTableSql, this));
        invoker.addCommand(new InitDatabaseCommand(breakTableSql, this));
        invoker.executeCommands();

        System.out.println("Fresh tables were created.");
    }
}
