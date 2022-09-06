package commands.normalCommands.usr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Birthday {
    private int month;
    private int day;

    public Birthday() {}

    private Birthday(int month, int day) {
        this.month = month;
        this.day = day;
    }

    public static Birthday parseBirthday(String birthday){
        LocalDate date = isThisDateValid(birthday.trim(), new String[]{"M/d/y", "M-d-y", "d/M/y", "d-M-y", "d.M.y", "M.d.y"});
        if(date != null){
            return new Birthday(date.getMonthValue(), date.getDayOfMonth());
        }
        else{
            return null;
        }
    }

    public static LocalDate isThisDateValid(String dateToValidate, String[] format){
        if(dateToValidate == null){
            return null;
        }

        for(String f : format) {
            try {
                return LocalDate.parse(dateToValidate, DateTimeFormatter.ofPattern(f));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public int getMonth() {
        return this.month;
    }

    public int getDay() {
        return this.day;
    }

    public void setMonth(String month) {
        this.month = Integer.parseInt(month);
    }

    public void setDay(int day) {
        this.day = day;
    }

    public static String numToMonth(int month){
        switch(month){
            case 1 : return "January";
            case 2 : return "February";
            case 3 : return "March";
            case 4 : return "April";
            case 5 : return "May";
            case 6 : return "June";
            case 7 : return "July";
            case 8 : return "August";
            case 9 : return "September";
            case 10 : return "October";
            case 11 : return "November";
            case 12 : return "December";
            default : return "";
        }
    }

    public static int monthToNum(String month){
        switch (month.toLowerCase().trim()){
            case "january" : return 1;
            case "february" : return 2;
            case "march" : return 3;
            case "april" : return 4;
            case "may" : return 5;
            case "june" : return 6;
            case "july" : return 7;
            case "august" : return 8;
            case "september" : return 9;
            case "october" : return 10;
            case "november" : return 11;
            case "december" : return 12;
            default : return -1;
        }
    }
}

