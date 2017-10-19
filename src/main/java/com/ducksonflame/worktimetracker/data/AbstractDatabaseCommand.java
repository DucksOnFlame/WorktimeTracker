package com.ducksonflame.worktimetracker.data;

import java.util.List;

public abstract class AbstractDatabaseCommand {

    protected String hql;

    protected AbstractDatabaseCommand() {

    }

    protected AbstractDatabaseCommand(String hql) {
        this.hql = hql;
    }

    public List executeQuery() {
        return null;
    }

    public void executeUpdate() {

    }
}
