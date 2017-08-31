package com.ducksonflame.worktimetracker.database;

import java.io.File;
import java.io.IOException;
import java.sql.*;


public class DbConnectionManager {

    private static String dbFilePath = "db/WorktimeDB.db";

    public DbConnectionManager() {

        if (!checkIfDbExists()) {
            System.out.println("Creating a new database file...");
            createNewDatabase();
        }

        checkConnection();
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
            System.out.println(e.getMessage());
        }

        return conn;
    }

    private void createNewDatabase() {

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
            System.out.println(e.getMessage());
        }

        createTables();
    }

    private static void createTables() {

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

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
             Statement stmt = conn.createStatement()) {
            stmt.execute(inTableSql);
            stmt.execute(outTableSql);
            stmt.execute(breakTableSql);
            System.out.println("Fresh tables were created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
