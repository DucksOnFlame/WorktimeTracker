package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class SQLQueryCommand extends AbstractDatabaseCommand {

    private String sql;

    public SQLQueryCommand(String sql) {
        this.sql = sql;
    }

    @Override
    public List executeQuery() {

        try {
            Session session = DbConnectionManager.getSession();
            session.beginTransaction();
            Query query = session.createNativeQuery(sql);
            List results = new ArrayList();
            if (query != null) {
                results = query.list();
            }
            session.getTransaction().commit();
            session.close();
            return results;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }
}
