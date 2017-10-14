package com.ducksonflame.worktimetracker.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InitDatabaseCommand extends AbstractDatabaseCommand {

    private DbConnectionManager dbConnectionManager;

    public InitDatabaseCommand(String sql, DbConnectionManager dbConnectionManager) {
        super(sql);
        this.dbConnectionManager = dbConnectionManager;
    }

    @Override
    public void execute() {
        try (Connection conn = dbConnectionManager.getDbConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
