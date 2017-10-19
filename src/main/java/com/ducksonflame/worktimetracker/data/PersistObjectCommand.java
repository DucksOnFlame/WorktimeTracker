package com.ducksonflame.worktimetracker.data;

import org.hibernate.Session;

public class PersistObjectCommand extends AbstractDatabaseCommand {

    private Object objectToPersist;

    public PersistObjectCommand(Object objectToPersist) {
        this.objectToPersist = objectToPersist;
    }

    @Override
    public void executeUpdate() {
        Session session = DbConnectionManager.getSession();
        session.beginTransaction();
        session.save(objectToPersist);
        session.getTransaction().commit();
        session.close();
    }
}
