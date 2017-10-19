package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;
import org.hibernate.query.Query;

public class UpdateDatabaseCommand extends AbstractDatabaseCommand {

    public UpdateDatabaseCommand(String hql) {
        super(hql);
    }

    @Override
    public void executeUpdate() {
        Session session = DbConnectionManager.getSession();
        session.beginTransaction();
        Query query = session.createQuery(hql);
        query.executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
}
