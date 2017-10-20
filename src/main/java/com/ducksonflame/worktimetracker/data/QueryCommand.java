package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class QueryCommand extends AbstractDatabaseCommand {

    public QueryCommand(String hql) {
        super(hql);
    }

    @Override
    public List executeQuery() {
        try {
            Session session = DbConnectionManager.getSession();
            session.beginTransaction();
            Query query = session.createQuery(hql);
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
