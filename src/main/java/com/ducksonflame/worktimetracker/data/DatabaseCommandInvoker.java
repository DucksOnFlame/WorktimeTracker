package com.ducksonflame.worktimetracker.data;

import java.util.List;

public class DatabaseCommandInvoker {

    public DatabaseCommandInvoker() {
    }

    public void executeUpdateCommand(AbstractDatabaseCommand command) {
        command.executeUpdate();
    }

    public List executeQueryCommand(AbstractDatabaseCommand command) {
        return command.executeQuery();
    }
}
