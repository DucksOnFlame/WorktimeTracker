package com.ducksonflame.worktimetracker.data.dialect;

import org.hibernate.dialect.SQLiteDialect;

import java.sql.Types;

public class MySQLiteDialect extends SQLiteDialect {

    public MySQLiteDialect() {
        super();

        registerColumnType(Types.NULL, "null");
        registerHibernateType(Types.NULL, "null");
    }
}
