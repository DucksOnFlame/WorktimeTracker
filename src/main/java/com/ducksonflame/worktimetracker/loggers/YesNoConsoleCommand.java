package com.ducksonflame.worktimetracker.loggers;

import java.io.BufferedReader;
import java.io.IOException;

public class YesNoConsoleCommand extends AbstractConsoleCommand {

    private String instruction;

    public YesNoConsoleCommand(String instruction, BufferedReader br) {
        super(br);
        this.instruction = instruction;
    }

    @Override
    public Object[] execute() {
        try {
            while (true) {
                if (instruction != null) {
                    System.out.println(instruction);
                }

                switch (br.readLine().toLowerCase()) {
                    case "n":
                        return new Boolean[]{Boolean.FALSE};

                    case "y":
                        return new Boolean[]{Boolean.TRUE};

                    default:
                        System.out.println("Please enter a valid command (Y or N)");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
