package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.IOException;
import java.sql.*;


public class DbConnectionManager {

    private static String dbFilePath = "db/WorktimeDB.db";
    private static SessionFactory sessionFactory;

    public void init() {
        if (!checkIfDbExists()) {
            createNewDatabase();
            initLogView();
        }
        checkConnection();
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public static Session getSession() {
        return sessionFactory.openSession();
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
    }

    private void initLogView() {
        String sql = "CREATE VIEW Log AS\n" +
                "SELECT win.DayIn, win.TimeIn, TimeOut, (SELECT SUM(BreakEnd - BreakBegin) FROM Break WHERE Break.BreakDay = win.DayIn) BreakTime\n" +
                "FROM WorktimeIn win\n" +
                "INNER JOIN WorktimeOut ON win.DayIn = WorktimeOut.DayOut;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
            if (conn != null) {
                System.out.println("Connection to the SQLite Database has been established successfully.\n");
            } else {
                System.out.println("Could not connect to the database!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
