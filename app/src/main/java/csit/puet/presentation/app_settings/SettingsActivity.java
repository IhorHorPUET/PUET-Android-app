package csit.puet.presentation.app_settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import csit.puet.AppConstants;
import csit.puet.R;
import csit.puet.presentation.ui.PresentationUtils;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences prefSet;

    private CheckBox chkBoxSystem;
    private CheckBox chkBoxLight;
    private CheckBox chkBoxDark;
    private int selectedTheme;

    private CheckBox widgetCheckbox;

    private LinearLayout googleCalendarSection;
    private CheckBox googleCalendarCheckbox;
    private TextView googleAccountTextView;
    private CheckBox googleAccountSelectionCheckbox;

    private LinearLayout dateRangeSection;
    CheckBox dateRangeCheckbox;
    private TextView dateRangeText;
    private SeekBar dateRangeSeekBar;

    private LinearLayout autoUpdateSection;
    CheckBox autoUpdateCheckbox;
    private TextView autoUpdateIntervalValue;
    private SeekBar autoUpdateIntervalSeekBar;


    private LinearLayout notificationOptionsSection;
    CheckBox notificationCheckbox;
    CheckBox textMessageCheckbox;
    CheckBox vibrationCheckbox;
    CheckBox soundCheckbox;
    private ActivityResultLauncher<Intent> ringtonePickerLauncher;
    private String soundUriString;
    private TextView notificationRepeatValue;
    private SeekBar notificationRepeatSeekBar;
    private TextView notificationIntervalValue;
    private SeekBar notificationIntervalSeekBar;


    private LinearLayout doNotDisturbOptionsSection;
    CheckBox doNotDisturbCheckbox;
    private TimePicker startTimePicker, endTimePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefSet = getSharedPreferences(AppConstants.PREF_SET, MODE_PRIVATE);

        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            soundUriString = uri.toString();
                        }
                    }
                }
        );

        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        chkBoxSystem = findViewById(R.id.chkBoxSystem);
        chkBoxLight = findViewById(R.id.chkBoxLight);
        chkBoxDark = findViewById(R.id.chkBoxDark);

        widgetCheckbox = findViewById(R.id.widgetCheckbox);

        googleCalendarSection = findViewById(R.id.googleCalendarSection);
        googleCalendarCheckbox = findViewById(R.id.googleCalendarCheckbox);
        googleAccountTextView = findViewById(R.id.googleAccountTextView);
        googleAccountSelectionCheckbox = findViewById(R.id.googleAccountSelectionCheckbox);

        dateRangeSection = findViewById(R.id.dateRangeSection);
        dateRangeCheckbox = findViewById(R.id.dateRangeCheckbox);
        dateRangeText = findViewById(R.id.dateRangeText);
        dateRangeSeekBar = findViewById(R.id.dateRangeSeekBar);

        autoUpdateSection = findViewById(R.id.updateIntervalSection);
        autoUpdateCheckbox = findViewById(R.id.autoUpdateCheckbox);
        autoUpdateIntervalSeekBar = findViewById(R.id.updateIntervalSeekBar);
        autoUpdateIntervalValue = findViewById(R.id.updateIntervalValue);

        notificationOptionsSection = findViewById(R.id.notificationOptionsSection);
        notificationCheckbox = findViewById(R.id.notificationCheckbox);
        textMessageCheckbox = findViewById(R.id.textMessageCheckbox);
        vibrationCheckbox = findViewById(R.id.vibrationCheckbox);
        soundCheckbox = findViewById(R.id.soundCheckbox);
        Button selectSoundButton = findViewById(R.id.selectSoundButton);
        notificationRepeatSeekBar = findViewById(R.id.notificationRepeatSeekBar);
        notificationRepeatValue = findViewById(R.id.notificationRepeatValue);
        notificationIntervalSeekBar = findViewById(R.id.notificationIntervalSeekBar);
        notificationIntervalValue = findViewById(R.id.notificationIntervalValue);
        notificationIntervalSeekBar.setMax(45);

        doNotDisturbOptionsSection = findViewById(R.id.doNotDisturbOptionsSection);
        doNotDisturbCheckbox = findViewById(R.id.doNotDisturbCheckbox);
        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);

        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        btnSave.setOnClickListener(v -> {
            PresentationUtils.vibrate(this, 40);
            saveSettings();
            setResult(RESULT_OK);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            PresentationUtils.vibrate(this, 40);
            setResult(RESULT_CANCELED);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        selectedTheme = prefSet.getInt(AppConstants.KEY_THEME, AppConstants.THEME_SYSTEM);

        boolean isWidgetEnabled = prefSet.getBoolean(AppConstants.KEY_WIDGET_ENABLED, true);
        widgetCheckbox.setChecked(isWidgetEnabled);

        boolean isGoogleCalendarEnabled = prefSet.getBoolean(AppConstants.KEY_GOOGLE_CALENDAR_ENABLED, false);
        String googleAccountName = prefSet.getString(AppConstants.KEY_GOOGLE_ACCOUNT_NAME, null);
        boolean isGoogleAccountSelectionEnabled = prefSet.getBoolean(AppConstants.KEY_GOOGLE_ACCOUNT_SELECTION, false);

        boolean dateRangeEnabled = prefSet.getBoolean(AppConstants.KEY_DATE_RANGE_ENABLED, false);
        int dateRange = prefSet.getInt(AppConstants.KEY_DATE_RANGE_INTERVAL, 7);

        boolean autoUpdate = prefSet.getBoolean(AppConstants.KEY_UPDATE_ENABLED, false);
        int updateInterval = prefSet.getInt(AppConstants.KEY_UPDATE_INTERVAL, 24);

        boolean notificationsEnabled = prefSet.getBoolean(AppConstants.KEY_NOTIFICATIONS_ENABLED, false);
        boolean isTextMessageEnabled = prefSet.getBoolean(AppConstants.KEY_TEXT_MESSAGE_ENABLED, false);
        boolean vibrationEnabled = prefSet.getBoolean(AppConstants.KEY_VIBRATION_ENABLED, false);
        boolean soundEnabled = prefSet.getBoolean(AppConstants.KEY_SOUND_ENABLED, false);
        soundUriString = prefSet.getString(AppConstants.KEY_NOTIFICATION_SOUND_URI, "android.resource://" + getPackageName() + "/" + R.raw.allert);
        int notificationRepeat = prefSet.getInt(AppConstants.KEY_NOTIFICATION_REPEAT, 2);
        int notificationInterval = prefSet.getInt(AppConstants.KEY_NOTIFICATION_INTERVAL, 15);

        boolean doNotDisturbEnabled = prefSet.getBoolean(AppConstants.KEY_DO_NOT_DISTURB_ENABLED, false);
        doNotDisturbCheckbox.setChecked(doNotDisturbEnabled);
        doNotDisturbOptionsSection.setVisibility(doNotDisturbEnabled ? View.VISIBLE : View.GONE);
        int startTime = prefSet.getInt(AppConstants.KEY_DO_NOT_DISTURB_START_TIME, 1320); // Default 22:00 (1320 minutes)
        int endTime = prefSet.getInt(AppConstants.KEY_DO_NOT_DISTURB_END_TIME, 480); // Default 08:00 (480 minutes)

        switch (selectedTheme) {
            case AppConstants.THEME_SYSTEM:
                chkBoxSystem.setChecked(true);
                break;
            case AppConstants.THEME_LIGHT:
                chkBoxLight.setChecked(true);
                break;
            case AppConstants.THEME_DARK:
                chkBoxDark.setChecked(true);
                break;
        }

        chkBoxSystem.setOnClickListener(v -> {
            if (!chkBoxSystem.isChecked()) {
                chkBoxSystem.setChecked(true);
            }
            selectedTheme = AppConstants.THEME_SYSTEM;
            chkBoxLight.setChecked(false);
            chkBoxDark.setChecked(false);
        });

        chkBoxLight.setOnClickListener(v -> {
            if (!chkBoxLight.isChecked()) {
                chkBoxLight.setChecked(true);
            }
            selectedTheme = AppConstants.THEME_LIGHT;
            chkBoxSystem.setChecked(false);
            chkBoxDark.setChecked(false);
        });

        chkBoxDark.setOnClickListener(v -> {
            if (!chkBoxDark.isChecked()) {
                chkBoxDark.setChecked(true);
            }
            selectedTheme = AppConstants.THEME_DARK;
            chkBoxSystem.setChecked(false);
            chkBoxLight.setChecked(false);
        });

        googleCalendarSection.setVisibility(isGoogleCalendarEnabled ? View.VISIBLE : View.GONE);
        googleCalendarCheckbox.setChecked(isGoogleCalendarEnabled);
        googleAccountSelectionCheckbox.setChecked(isGoogleAccountSelectionEnabled);

        if (googleAccountName != null) {
            googleAccountTextView.setText(getString(R.string.selected_account, googleAccountName));
        } else {
            googleAccountTextView.setText(R.string.no_account_selected);
        }

        googleCalendarCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            googleCalendarSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                googleAccountSelectionCheckbox.setChecked(false);
            }
        });

        dateRangeSection.setVisibility(dateRangeEnabled ? View.VISIBLE : View.GONE);
        dateRangeCheckbox.setChecked(dateRangeEnabled);
        dateRangeText.setText(getString(R.string.date_range, dateRange));
        dateRangeSeekBar.setProgress(dateRange - 1);

        dateRangeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                dateRangeSection.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        dateRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int days = progress + 1;
                dateRangeText.setText(getString(R.string.date_range, days));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        autoUpdateSection.setVisibility(autoUpdate ? View.VISIBLE : View.GONE);
        autoUpdateCheckbox.setChecked(autoUpdate);
        autoUpdateIntervalValue.setText(getString(R.string.update_interval, updateInterval));
        autoUpdateIntervalSeekBar.setProgress(updateInterval - 1);

        autoUpdateCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                autoUpdateSection.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        autoUpdateIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int hours = progress + 1;
                autoUpdateIntervalValue.setText(getString(R.string.update_interval, hours));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        notificationOptionsSection.setVisibility(notificationsEnabled ? View.VISIBLE : View.GONE);
        notificationCheckbox.setChecked(notificationsEnabled);
        textMessageCheckbox.setChecked(isTextMessageEnabled);
        vibrationCheckbox.setChecked(vibrationEnabled);
        soundCheckbox.setChecked(soundEnabled);
        notificationRepeatValue.setText(getString(R.string.notification_repeat, notificationRepeat));
        notificationRepeatSeekBar.setProgress(notificationRepeat - 1);
        notificationIntervalValue.setText(getString(R.string.notification_interval, notificationInterval));
        notificationIntervalSeekBar.setProgress(notificationInterval - 15);

        notificationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                notificationOptionsSection.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        notificationRepeatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int repeatCount = progress + 1;
                notificationRepeatValue.setText(getString(R.string.notification_repeat, repeatCount));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        notificationIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int intervalNotification = progress + 15;
                notificationIntervalValue.setText(getString(R.string.notification_interval, intervalNotification));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        selectSoundButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_notification_sound));

            Uri existingUri = soundUriString.equals("android.resource://" + getPackageName() + "/" + R.raw.allert)
                    ? Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.allert)
                    : Uri.parse(soundUriString);

            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri);
            ringtonePickerLauncher.launch(intent);
        });

        // Set the checkbox state and visibility
        doNotDisturbOptionsSection.setVisibility(doNotDisturbEnabled ? View.VISIBLE : View.GONE);
        doNotDisturbCheckbox.setChecked(doNotDisturbEnabled);

        // Handle checkbox changes
        doNotDisturbCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                doNotDisturbOptionsSection.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        int startHour = startTime / 60;
        int startMinute = startTime % 60;
        int endHour = endTime / 60;
        int endMinute = endTime % 60;

        startTimePicker.setHour(startHour);
        startTimePicker.setMinute(startMinute);
        endTimePicker.setHour(endHour);
        endTimePicker.setMinute(endMinute);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefSet.edit();

        editor.putInt(AppConstants.KEY_THEME, selectedTheme);

        boolean isWidgetEnabled = widgetCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_WIDGET_ENABLED, isWidgetEnabled);

        boolean isGoogleCalendarEnabled = googleCalendarCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_GOOGLE_CALENDAR_ENABLED, isGoogleCalendarEnabled);
        boolean isGoogleAccountSelectionEnabled = googleAccountSelectionCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_GOOGLE_ACCOUNT_SELECTION, isGoogleAccountSelectionEnabled);

        boolean isDateRangeEnabled = dateRangeCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_DATE_RANGE_ENABLED, isDateRangeEnabled);
        int dateRange = dateRangeSeekBar.getProgress() + 1;
        editor.putInt(AppConstants.KEY_DATE_RANGE_INTERVAL, dateRange);

        boolean isAutoUpdateEnabled = autoUpdateCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_UPDATE_ENABLED, isAutoUpdateEnabled);
        int updateInterval = autoUpdateIntervalSeekBar.getProgress() + 1;
        editor.putInt(AppConstants.KEY_UPDATE_INTERVAL, updateInterval);

        boolean isNotificationsEnabled = notificationCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_NOTIFICATIONS_ENABLED, isNotificationsEnabled);
        boolean isTextMessageEnabled = textMessageCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_TEXT_MESSAGE_ENABLED, isTextMessageEnabled);
        boolean isVibrationEnabled = vibrationCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_VIBRATION_ENABLED, isVibrationEnabled);
        boolean isSoundEnabled = soundCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_SOUND_ENABLED, isSoundEnabled);
        editor.putString(AppConstants.KEY_NOTIFICATION_SOUND_URI, soundUriString);

        int notificationRepeat = notificationRepeatSeekBar.getProgress() + 1;
        editor.putInt(AppConstants.KEY_NOTIFICATION_REPEAT, notificationRepeat);
        int notificationInterval = notificationIntervalSeekBar.getProgress() + 15;
        editor.putInt(AppConstants.KEY_NOTIFICATION_INTERVAL, notificationInterval);

        boolean isDoNotDisturbEnabled = doNotDisturbCheckbox.isChecked();
        editor.putBoolean(AppConstants.KEY_DO_NOT_DISTURB_ENABLED, isDoNotDisturbEnabled);
        int startTime = startTimePicker.getHour() * 60 + startTimePicker.getMinute();
        int endTime = endTimePicker.getHour() * 60 + endTimePicker.getMinute();
        editor.putInt(AppConstants.KEY_DO_NOT_DISTURB_START_TIME, startTime);
        editor.putInt(AppConstants.KEY_DO_NOT_DISTURB_END_TIME, endTime);

        editor.apply();
    }
}
