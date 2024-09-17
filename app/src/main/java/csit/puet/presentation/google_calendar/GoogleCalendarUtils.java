package csit.puet.presentation.google_calendar;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;

import android.util.Pair;

import com.google.api.client.util.DateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import csit.puet.R;
import csit.puet.AppConstants;

public class GoogleCalendarUtils {

    public static void handleGoogleCalendarSettings(Context context, SharedPreferences prefSet) {
        boolean isGoogleCalendarEnabled = prefSet.getBoolean(AppConstants.KEY_GOOGLE_CALENDAR_ENABLED, false);
        boolean isRevocationNotificationRequired = prefSet.getBoolean(AppConstants.KEY_CALENDAR_PERMISSION_REVOCATION_SHOWN, false);

        if (!isGoogleCalendarEnabled && isRevocationNotificationRequired) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.revocation_permission_title)
                    .setMessage(R.string.revocation_permission_message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();

            SharedPreferences.Editor editor = prefSet.edit();
            editor.putBoolean(AppConstants.KEY_CALENDAR_PERMISSION_REVOCATION_SHOWN, false);
            editor.apply();
        }
    }

    public static Pair<String, String> getLessonStartAndEndTime(int pairNumber) {
        String startTime;
        String endTime;
        switch (pairNumber) {
            case 1:
                startTime = "08:00";
                endTime = "09:20";
                break;
            case 2:
                startTime = "09:30";
                endTime = "10:50";
                break;
            case 3:
                startTime = "11:00";
                endTime = "12:20";
                break;
            case 4:
                startTime = "12:40";
                endTime = "14:00";
                break;
            case 5:
                startTime = "14:10";
                endTime = "15:30";
                break;
            case 6:
                startTime = "15:40";
                endTime = "17:00";
                break;
            case 7:
                startTime = "17:05";
                endTime = "18:25";
                break;
            case 8:
                startTime = "18:30";
                endTime = "19:50";
                break;
            case 9:
                startTime = "19:55";
                endTime = "21:15";
                break;
            case 10:
                startTime = "21:20";
                endTime = "22:40";
                break;
            default:
                startTime = "00:00";
                endTime = "00:00";
                break;
        }
        return new Pair<>(startTime, endTime);
    }

    public static DateTime convertToDateTime(String date, String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(date + " " + time, formatter);
        return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}

