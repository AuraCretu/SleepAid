package com.example.sleepaid;

import com.example.sleepaid.Database.SleepData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHandler {
    public static List<Integer> getIntsFromString(String s) {
        Matcher matcher = Pattern.compile("\\d+").matcher(s);

        List<Integer> numbers = new ArrayList<>();

        while (matcher.find()) {
            numbers.add(Integer.valueOf(matcher.group()));
        }

        return numbers;
    }

    public static String getFormattedDate(Date date) {
        return new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date);
    }

    public static List<Double> getDoublesFromSleepDataValues(List<SleepData> sleepData) {
        List<Double> processedValues = new ArrayList<>();

        for (SleepData s : sleepData) {
            List<Integer> times = DataHandler.getIntsFromString(s.getValue());

            int hours = times.get(0);
            double minutes = times.size() > 1 ? times.get(1) / 60.0 : 0;

            processedValues.add(hours + minutes);
        }

        return processedValues;
    }
}
