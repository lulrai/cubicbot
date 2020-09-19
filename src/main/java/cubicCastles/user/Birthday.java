package cubicCastles.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Birthday {
    private String month;
    private String day;

    public Birthday() {}

    private Birthday(String month, String day) {
        this.month = month;
        this.day = day;
    }

    public static Birthday parseBirthday(String birthday){
        if(isThisDateValid(birthday.trim(), "MM/dd") || isThisDateValid(birthday.trim(), "MM-dd")){
            String[] date = birthday.split("[-/]");
            return new Birthday(numToMonth(date[0]), date[1]);
        }
        else{
            return null;
        }
    }

    public static boolean isThisDateValid(String dateToValidate, String format){
        if(dateToValidate == null){
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);

        try {
            Date date = sdf.parse(dateToValidate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getMonth() {
        return month;
    }

    public int getMonthNum() {
        return monthToNum(this.month);
    }

    public String getDay() {
        return day;
    }

    public int getDayNum() {
        return Integer.parseInt(day.trim());
    }

    public void setMonth(String month) {
        this.month = numToMonth(month);
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setDay(int day) {
        this.day = String.valueOf(day);
    }

    private static String numToMonth(String month){
        switch(Integer.parseInt(month.trim())){
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

    private int monthToNum(String month){
        switch (month.trim()){
            case "January" : return 1;
            case "February" : return 2;
            case "March" : return 3;
            case "April" : return 4;
            case "May" : return 5;
            case "June" : return 6;
            case "July" : return 7;
            case "August" : return 8;
            case "September" : return 9;
            case "October" : return 10;
            case "November" : return 11;
            case "December" : return 12;
            default : return -1;
        }
    }
}
