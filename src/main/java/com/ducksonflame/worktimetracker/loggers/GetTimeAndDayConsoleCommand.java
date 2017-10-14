package com.ducksonflame.worktimetracker.loggers;

import com.ducksonflame.worktimetracker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetTimeAndDayConsoleCommand extends AbstractConsoleCommand {

    public GetTimeAndDayConsoleCommand(BufferedReader br) {
        super(br);
    }

    @Override
    public Object[] execute() {

        String day = "";
        String time = "";

        try {

            Pattern dayPattern = Utils.getDayPattern();
            Pattern timePattern = Utils.getTimePattern();
            boolean isInputCorrect = false;

            while (!isInputCorrect) {

                String userInput = br.readLine();

                if (userInput.toLowerCase().equals("b")) {
                    System.out.println("Log NOT created/updated.");
                    return null;
                }

                Matcher matcher = dayPattern.matcher(userInput);

                if (matcher.find()) {
                    day = matcher.group();
                    isInputCorrect = true;
                }

                if (isInputCorrect) {
                    matcher = timePattern.matcher(userInput);

                    if (matcher.find()) {
                        time = matcher.group();
                    } else {
                        isInputCorrect = false;
                    }
                } else {
                    System.out.println("Please provide correct input! (yyyy-MM-dd hh:mm:ss or \"b\" to go back to main menu)\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String[]{day, time};
    }
}
