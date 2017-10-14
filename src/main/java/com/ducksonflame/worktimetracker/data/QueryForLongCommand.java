package com.ducksonflame.worktimetracker.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryForLongCommand extends AbstractDatabaseCommand {

    public QueryForLongCommand(String sql) {
        super(sql);
    }

    @Override
    public long queryForLong() {
        try (Connection conn = DbConnectionManager.getInstance().getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
