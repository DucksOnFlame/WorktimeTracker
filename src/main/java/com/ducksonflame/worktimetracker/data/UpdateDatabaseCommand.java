package com.ducksonflame.worktimetracker.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateDatabaseCommand extends AbstractDatabaseCommand {

    public UpdateDatabaseCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute() {
        try (Connection conn = DbConnectionManager.getInstance().getDbConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
