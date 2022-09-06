package commands.utils;

public class ConversionUtils {
    private static int[] secondsToDHMS(long totalSecs) {
        int days = (int) ((totalSecs / 3600) / 24);
        int hours = (int) ((totalSecs / 3600) % 24);
        int minutes = (int) ((totalSecs % 3600) / 60);
        int seconds = (int) (totalSecs % 60);
        int[] ints = {days, hours, minutes, seconds};
        return ints;
    }

    public static int[] bytesToKMG(long totalByes) {
        int gb = (int) (((totalByes / 1024) / 1024) / 1024);
        int mb = (int) ((totalByes / 1024) / 1024);
        int kb = (int) (totalByes / 1024);
        int[] ints = {gb, mb, kb};
        return ints;
    }

    public static String secondsToTime(long totalSecs) {
        String time = "";
        int[] DHMS = secondsToDHMS(totalSecs);
        if (DHMS[0] == 0 && DHMS[1] == 0 && DHMS[2] == 0) {
            time = DHMS[3] + " second(s)";
        } else if (DHMS[0] == 0 && DHMS[1] == 0) {
            time = DHMS[2] + " minute(s) " + DHMS[3] + " second(s)";
        } else if (DHMS[0] == 0) {
            time = DHMS[1] + " hour(s) " + DHMS[2] + " minute(s) " + DHMS[3] + " second(s)";
        } else {
            time = DHMS[0] + " day(s) " + DHMS[1] + " hour(s) " + DHMS[2] + " minute(s) " + DHMS[3] + " second(s)";
        }
        return time;
    }
}