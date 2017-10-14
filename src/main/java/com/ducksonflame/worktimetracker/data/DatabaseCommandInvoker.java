package com.ducksonflame.worktimetracker.data;

import java.util.ArrayList;
import java.util.List;

public class DatabaseCommandInvoker {
    private List<AbstractDatabaseCommand> commands;

    public DatabaseCommandInvoker() {
        commands = new ArrayList<>();
    }

    public void addCommand(AbstractDatabaseCommand command) {
        commands.add(command);
    }

    public void executeCommands() {
        for (AbstractDatabaseCommand command : commands) {
            command.execute();
        }
    }

    public long executeQueryForLongCommand(AbstractDatabaseCommand command) {
        return command.queryForLong();
    }

    public LogDTO executeQueryForLogCommand(AbstractDatabaseCommand command) {
        return command.queryForLog();
    }

    public boolean executeQueryForExistenceCommand(AbstractDatabaseCommand command) {
        return command.queryForExistence();
    }
}
