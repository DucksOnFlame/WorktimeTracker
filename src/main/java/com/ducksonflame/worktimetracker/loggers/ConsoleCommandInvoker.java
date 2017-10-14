package com.ducksonflame.worktimetracker.loggers;

public class ConsoleCommandInvoker {
    public Object[] execute(AbstractConsoleCommand command) {
        return command.execute();
    }
}
