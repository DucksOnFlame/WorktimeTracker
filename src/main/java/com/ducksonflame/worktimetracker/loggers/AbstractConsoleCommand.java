package com.ducksonflame.worktimetracker.loggers;

import java.io.BufferedReader;

public abstract class AbstractConsoleCommand {
    protected BufferedReader br;

    protected AbstractConsoleCommand(BufferedReader br) {
        this.br = br;
    }

    public abstract Object[] execute();
}
