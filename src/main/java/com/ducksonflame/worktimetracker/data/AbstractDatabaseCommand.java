package com.ducksonflame.worktimetracker.data;

public abstract class AbstractDatabaseCommand {

    protected String sql;

    protected AbstractDatabaseCommand(String sql) {
        this.sql = sql;
    }

    public void execute() {

    }

    public long queryForLong() {
        return 0;
    }

    public LogDTO queryForLog() {
        return null;
    }

    public boolean queryForExistence() {
        return false;
    }
}
