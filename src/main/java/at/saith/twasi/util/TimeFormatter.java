package at.saith.twasi.util;

public class TimeFormatter {
    public static String formatTime(long timeInSeconds){
        int days = (int) (timeInSeconds/(60*60*24));
        int hours = (int) (timeInSeconds/(60*60))%60;
        int minutes = (int) (timeInSeconds/(60))%60;
        int seconds = (int) timeInSeconds%60;
        StringBuilder formattedTime = new StringBuilder();
        if(days != 0){
            formattedTime.append(days+"d");
        }
        if(hours != 0){
            formattedTime.append(hours+"h");
        }
        if(minutes != 0){
            formattedTime.append(minutes+"m");
        }
        if(seconds != 0){
            formattedTime.append(seconds+"s");
        }
        return formattedTime.toString();
    }
}
