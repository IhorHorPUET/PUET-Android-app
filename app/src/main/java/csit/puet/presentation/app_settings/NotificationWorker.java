package csit.puet.presentation.app_settings;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;

import csit.puet.AppConstants;
import csit.puet.R;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.net.Uri;

public class NotificationWorker extends Worker {

    SharedPreferences prefSet;
    private static final String CHANNEL_ID = "schedule_sync_channel";

    private final boolean isTextMessageEnabled;
    private final boolean isVibrationEnabled;
    private final boolean isSoundEnabled;
    private final String soundUriString;
    private final int notificationRepeat;
    private final int notificationInterval;
    private final boolean isDoNotDisturbEnabled;
    private final int startTime;
    private final int endTime;
    private final String scheduleDifferences;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);

        // Reading from SharedPreferences
        prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);

        isTextMessageEnabled = prefSet.getBoolean(AppConstants.KEY_TEXT_MESSAGE_ENABLED, false);
        isVibrationEnabled = prefSet.getBoolean(AppConstants.KEY_VIBRATION_ENABLED, false);
        isSoundEnabled = prefSet.getBoolean(AppConstants.KEY_SOUND_ENABLED, false);
        soundUriString = prefSet.getString(AppConstants.KEY_NOTIFICATION_SOUND_URI, String.valueOf(R.raw.allert));
        notificationRepeat = prefSet.getInt(AppConstants.KEY_NOTIFICATION_REPEAT, 2);
        notificationInterval = prefSet.getInt(AppConstants.KEY_NOTIFICATION_INTERVAL, 15); // in minutes
        isDoNotDisturbEnabled = prefSet.getBoolean(AppConstants.KEY_DO_NOT_DISTURB_ENABLED, false);
        startTime = prefSet.getInt(AppConstants.KEY_DO_NOT_DISTURB_START_TIME, 1320); // Default 22:00 (1320 minutes)
        endTime = prefSet.getInt(AppConstants.KEY_DO_NOT_DISTURB_END_TIME, 480); // Default 08:00 (480 minutes)
        scheduleDifferences = prefSet.getString(AppConstants.KEY_DATES_WITH_DIFFERENCES, "Розклад змінився");
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        Calendar calendar = Calendar.getInstance();
        int currentTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        for (int i = 0; i < notificationRepeat; i++) {
            // Check if current time is within Do Not Disturb period
            if (isDoNotDisturbEnabled && isWithinDoNotDisturbPeriod(currentTime, startTime, endTime)) {
                int delay = calculateDelayUntilEndOfDoNotDisturbPeriod(currentTime, endTime);
                try {
                    Thread.sleep((long) delay * 60 * 1000);
                } catch (InterruptedException e) {
                    return Result.failure();
                }
            }

            // Create notification
            if (isTextMessageEnabled) {
                createNotification(context, scheduleDifferences);
            }

            triggerVibrationAndSound(context, isVibrationEnabled, isSoundEnabled);

            if (i < notificationRepeat - 1) {
                try {
                    Thread.sleep((long) notificationInterval * 60 * 1000);
                } catch (InterruptedException e) {
                    return Result.failure();
                }
            }
        }
        return Result.success();
    }

    private void createNotification(Context context, String scheduleDifferences) {
        Intent intent = new Intent(context, ScheduleComparisonActivity.class);
        intent.putExtra("scheduleDifferences", scheduleDifferences);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Schedule Sync", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.puet_logo)
                .setContentTitle("Розклад змінився на")
                .setContentText(scheduleDifferences)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(scheduleDifferences))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, notificationBuilder.build());
    }

    private void triggerVibrationAndSound(Context context, boolean isVibrationEnabled, boolean isSoundEnabled) {
        if (isVibrationEnabled) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] vibrationPattern = {0, 100, 200, 500};
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1));
            }
        }

        if (isSoundEnabled) {
            Uri soundUri;
            if (soundUriString.startsWith("android.resource://")) {
                soundUri = Uri.parse(soundUriString);
            } else {
                soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + soundUriString);
            }

            MediaPlayer mediaPlayer = MediaPlayer.create(context, soundUri);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        }
    }

    private boolean isWithinDoNotDisturbPeriod(int currentTime, int startTime, int endTime) {
        if (startTime < endTime) {
            return currentTime >= startTime && currentTime < endTime;
        } else {
            return currentTime >= startTime || currentTime < endTime;
        }
    }

    private int calculateDelayUntilEndOfDoNotDisturbPeriod(int currentTime, int endTime) {
        if (currentTime <= endTime) {
            return endTime - currentTime;
        } else {
            return 24 * 60 - currentTime + endTime;
        }
    }
}
