package com.ducksonflame.worktimetracker.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryForExistenceCommand extends AbstractDatabaseCommand {
    public QueryForExistenceCommand(String sql) {
        super(sql);
    }

    @Override
    public boolean queryForExistence() {
        try (Connection conn = DbConnectionManager.getInstance().getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
