package com.ducksonflame.worktimetracker.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {

    }

    public static String getHHMMSSFormattedString(long timeInSeconds) {
        return String.format("%02dh %02dm %02ds", timeInSeconds / 3600, timeInSeconds / 60 % 60, timeInSeconds % 60);
    }

    public static String getHHMMSSFormattedStringFixedSpace(long timeInSeconds) {
        return String.format("%1$2sh %2$2sm %3$2ss", timeInSeconds / 3600, timeInSeconds / 60 % 60, timeInSeconds % 60);
    }

    public static String getHHMMSSFormattedStringFixedSpaceSignSafe(long timeInSeconds) {
        if (timeInSeconds < 0) {
            timeInSeconds = Math.abs(timeInSeconds);
            return String.format("-%1$2sh %2$2sm %3$2ss", timeInSeconds / 3600, timeInSeconds / 60 % 60, timeInSeconds % 60);
        } else {
            return String.format("+%1$2sh %2$2sm %3$2ss", timeInSeconds / 3600, timeInSeconds / 60 % 60, timeInSeconds % 60);
        }
    }

    public static int convertStringTimeToInt(String time) {
        int hours = Integer.parseInt(time.substring(0, 2));
        int minutes = Integer.parseInt(time.substring(3, 5));
        int seconds = Integer.parseInt(time.substring(6, 8));

        return hours * 3600 + minutes * 60 + seconds;
    }

    public static String getTimeFormattedString(long timeInSeconds) {
        return String.format("%02d:%02d:%02d", timeInSeconds / 3600, timeInSeconds / 60 % 60, timeInSeconds % 60);
    }

    public static int getCurrentYear() {
        return Integer.parseInt(getCurrentYearString());
    }

    public static String getCurrentYearString() {
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    public static int getCurrentQuarter() {
        Date date = new Date();
        int quarter = (Integer.parseInt(new SimpleDateFormat("MM").format(date)) / 3);

        if (Integer.parseInt(new SimpleDateFormat("MM").format(date)) % 3 != 0) {
            quarter++;
        }

        return quarter;
    }

    public static int getCurrentMonth() {
        return Integer.parseInt(getCurrentMonthString());
    }

    public static String getCurrentMonthString() {
        return new SimpleDateFormat("MM").format(new Date());
    }

    public static String getTitleMonthString(String month) {
        String monthName = Month.of(Integer.parseInt(month)).name();
        int length = monthName.length();
        int maxNameLength = 9;

        int leftPadding = (maxNameLength - length) / 2;
        int rightPadding = (maxNameLength - length) / 2 + (maxNameLength - length) % 2;

        StringBuilder formattedMonth = new StringBuilder();

        for (int i = 0; i <= leftPadding; i++) {
            formattedMonth.append(" ");
        }

        formattedMonth.append(monthName);

        for (int i = 0; i <= rightPadding; i++) {
            formattedMonth.append(" ");
        }

        return formattedMonth.toString();
    }

    public static String getTodayString() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static int getSecondsToday() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime midnight = now.truncatedTo(ChronoUnit.DAYS);
        Duration duration = Duration.between(midnight, now);
        return (int) duration.getSeconds();
    }

    public static Pattern getDayPattern() {
        return Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
    }

    public static Pattern getTimePattern() {
        return Pattern.compile("\\d{1,2}:\\d{1,2}:\\d{1,2}");
    }

    public static Pattern getQuarterPattern() {
        return Pattern.compile("\\d-\\d{4}");
    }
}
