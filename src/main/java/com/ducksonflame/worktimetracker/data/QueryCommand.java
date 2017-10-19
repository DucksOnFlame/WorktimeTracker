package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class QueryCommand extends AbstractDatabaseCommand {

    public QueryCommand(String hql) {
        super(hql);
    }

    @Override
    public List executeQuery() {
        Session session = DbConnectionManager.getSession();
        session.beginTransaction();
        Query query = session.createQuery(hql);
        List results = query.list();
        session.getTransaction().commit();
        session.close();
        return results;
    }
}
