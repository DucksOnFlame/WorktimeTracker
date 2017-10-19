package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class SQLQueryCommand extends AbstractDatabaseCommand {

    private String sql;

    public SQLQueryCommand(String sql) {
        this.sql = sql;
    }

    @Override
    public List executeQuery() {
        Session session = DbConnectionManager.getSession();
        session.beginTransaction();
        Query query = session.createNativeQuery(sql);
        List results = query.list();
        session.getTransaction().commit();
        session.close();
        return results;
    }
}
