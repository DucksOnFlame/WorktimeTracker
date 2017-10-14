package com.ducksonflame.worktimetracker.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryForLogCommand extends AbstractDatabaseCommand {

    public QueryForLogCommand(String sql) {
        super(sql);
    }

    @Override
    public LogDTO queryForLog() {
        try (Connection conn = DbConnectionManager.getInstance().getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                LogDTO log = new LogDTO();

                log.setDay(rs.getString(1));
                log.setTimeIn(rs.getLong(2));
                log.setTimeOut(rs.getLong(3));
                log.setBreakTime(rs.getLong(4));

                return log;
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
