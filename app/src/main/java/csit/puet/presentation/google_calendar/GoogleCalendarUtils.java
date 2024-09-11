package csit.puet.presentation.google_calendar;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;

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
}

