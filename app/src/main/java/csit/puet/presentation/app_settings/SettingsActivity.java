package csit.puet.presentation.app_settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import csit.puet.R;
import csit.puet.presentation.ui.PresentationUtils;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences prefSet;

    public static final String KEY_THEME = "keyTheme";
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    public static final String KEY_WIDGET_ENABLED = "keyWidgetEnabled";

    public static final String KEY_GOOGLE_CALENDAR_ENABLED = "keyGoogleCalendarEnabled";
    public static final String KEY_GOOGLE_ACCOUNT_SELECTION = "keyGoogleAccountSelection";

    private static final String KEY_DATE_RANGE_ENABLED = "keyDateRangeEnabled";
    private static final String KEY_DATE_RANGE = "keyDateRange";

    private static final String KEY_UPDATE_ENABLED = "keyUpdateEnabled";
    private static final String KEY_UPDATE_INTERVAL = "keyUpdateInterval";

    private static final String KEY_NOTIFICATIONS_ENABLED = "keyNotificationsEnabled";
    private static final String KEY_TEXT_MESSAGE_ENABLED = "keyTextMessageEnabled";
    private static final String KEY_VIBRATION_ENABLED = "keyVibrationEnabled";
    private static final String KEY_SOUND_ENABLED = "keySoundEnabled";
    private static final String KEY_NOTIFICATION_REPEAT = "keyNotificationRepeat";
    private static final String KEY_NOTIFICATION_INTERVAL = "keyNotificationInterval";
    public static final String KEY_NOTIFICATION_SOUND_URI = "keyNotificationSoundUri";

    private static final String KEY_DO_NOT_DISTURB_ENABLED = "keyDoNotDisturbEnabled";
    private static final String KEY_DO_NOT_DISTURB_START_TIME = "keyDoNotDisturbStartTime";
    private static final String KEY_DO_NOT_DISTURB_END_TIME = "keyDoNotDisturbEndTime";

    private CheckBox chkBoxSystem;
    private CheckBox chkBoxLight;
    private CheckBox chkBoxDark;
    private int selectedTheme;

    private CheckBox widgetCheckbox;

    private LinearLayout googleCalendarSection;
    private CheckBox googleCalendarCheckbox;
    private CheckBox googleAccountSelectionCheckbox;

    private LinearLayout dateRangeSection;
    CheckBox dateRangeCheckbox;
    private SeekBar dateRangeSeekBar;
    private TextView dateRangeText;

    private LinearLayout updateIntervalSection;
    CheckBox autoUpdateCheckbox;
    private SeekBar updateIntervalSeekBar;
    private TextView updateIntervalValue;

    private ActivityResultLauncher<Intent> ringtonePickerLauncher;
    private LinearLayout notificationOptionsSection;
    CheckBox notificationCheckbox;
    CheckBox textMessageCheckbox;
    CheckBox vibrationCheckbox;
    CheckBox soundCheckbox;
    private String soundUriString;
    private SeekBar notificationRepeatSeekBar;
    private TextView notificationRepeatValue;
    private SeekBar notificationIntervalSeekBar;
    private TextView notificationIntervalValue;

    private LinearLayout doNotDisturbOptionsSection;
    CheckBox doNotDisturbCheckbox;
    private TimePicker startTimePicker, endTimePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefSet = getSharedPreferences("prefSettings", MODE_PRIVATE);

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
        googleAccountSelectionCheckbox = findViewById(R.id.googleAccountSelectionCheckbox);

        dateRangeSection = findViewById(R.id.dateRangeSection);
        dateRangeCheckbox = findViewById(R.id.dateRangeCheckbox);
        dateRangeSeekBar = findViewById(R.id.dateRangeSeekBar);
        dateRangeText = findViewById(R.id.dateRangeText);

        updateIntervalSection = findViewById(R.id.updateIntervalSection);
        autoUpdateCheckbox = findViewById(R.id.autoUpdateCheckbox);
        updateIntervalSeekBar = findViewById(R.id.updateIntervalSeekBar);
        updateIntervalValue = findViewById(R.id.updateIntervalValue);

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

        // Инициализация SharedPreferences

        selectedTheme = prefSet.getInt(KEY_THEME, THEME_SYSTEM);

        boolean isWidgetEnabled = prefSet.getBoolean(KEY_WIDGET_ENABLED, true);
        widgetCheckbox.setChecked(isWidgetEnabled);

        boolean isGoogleCalendarEnabled = prefSet.getBoolean(KEY_GOOGLE_CALENDAR_ENABLED, false);
        boolean isGoogleAccountSelectionEnabled = prefSet.getBoolean(KEY_GOOGLE_ACCOUNT_SELECTION, false);

        boolean dateRangeEnabled = prefSet.getBoolean(KEY_DATE_RANGE_ENABLED, false);
        int dateRange = prefSet.getInt(KEY_DATE_RANGE, 7);

        boolean autoUpdate = prefSet.getBoolean(KEY_UPDATE_ENABLED, false);
        int updateInterval = prefSet.getInt(KEY_UPDATE_INTERVAL, 24);

        boolean notificationsEnabled = prefSet.getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
        boolean isTextMessageEnabled = prefSet.getBoolean(KEY_TEXT_MESSAGE_ENABLED, false);
        boolean vibrationEnabled = prefSet.getBoolean(KEY_VIBRATION_ENABLED, false);
        boolean soundEnabled = prefSet.getBoolean(KEY_SOUND_ENABLED, false);
        soundUriString = prefSet.getString(KEY_NOTIFICATION_SOUND_URI, "android.resource://" + getPackageName() + "/" + R.raw.allert);
        int notificationRepeat = prefSet.getInt(KEY_NOTIFICATION_REPEAT, 2);
        int notificationInterval = prefSet.getInt(KEY_NOTIFICATION_INTERVAL, 15);

        boolean doNotDisturbEnabled = prefSet.getBoolean(KEY_DO_NOT_DISTURB_ENABLED, false);
        doNotDisturbCheckbox.setChecked(doNotDisturbEnabled);
        doNotDisturbOptionsSection.setVisibility(doNotDisturbEnabled ? View.VISIBLE : View.GONE);
        int startTime = prefSet.getInt(KEY_DO_NOT_DISTURB_START_TIME, 1320); // Default 22:00 (1320 minutes)
        int endTime = prefSet.getInt(KEY_DO_NOT_DISTURB_END_TIME, 480); // Default 08:00 (480 minutes)

        // Разбиваем минуты на часы и минуты
        int startHour = startTime / 60;
        int startMinute = startTime % 60;
        int endHour = endTime / 60;
        int endMinute = endTime % 60;

        // Установка времени для TimePicker'ов
        startTimePicker.setHour(startHour);
        startTimePicker.setMinute(startMinute);
        endTimePicker.setHour(endHour);
        endTimePicker.setMinute(endMinute);

        // Установим выбранный чекбокс в зависимости от сохраненной темы
        switch (selectedTheme) {
            case THEME_SYSTEM:
                chkBoxSystem.setChecked(true);
                break;
            case THEME_LIGHT:
                chkBoxLight.setChecked(true);
                break;
            case THEME_DARK:
                chkBoxDark.setChecked(true);
                break;
        }

        // Обработка выбора темы
        chkBoxSystem.setOnClickListener(v -> {
            if (!chkBoxSystem.isChecked()) {
                chkBoxSystem.setChecked(true);
            }
            selectedTheme = THEME_SYSTEM;
            chkBoxLight.setChecked(false);
            chkBoxDark.setChecked(false);
        });

        chkBoxLight.setOnClickListener(v -> {
            if (!chkBoxLight.isChecked()) {
                chkBoxLight.setChecked(true);
            }
            selectedTheme = THEME_LIGHT;
            chkBoxSystem.setChecked(false);
            chkBoxDark.setChecked(false);
        });

        chkBoxDark.setOnClickListener(v -> {
            if (!chkBoxDark.isChecked()) {
                chkBoxDark.setChecked(true);
            }
            selectedTheme = THEME_DARK;
            chkBoxSystem.setChecked(false);
            chkBoxLight.setChecked(false);
        });

        googleCalendarCheckbox.setChecked(isGoogleCalendarEnabled);
        googleCalendarSection.setVisibility(isGoogleCalendarEnabled ? View.VISIBLE : View.GONE);
        googleAccountSelectionCheckbox.setChecked(isGoogleAccountSelectionEnabled);

        googleCalendarCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                googleCalendarSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        // Установим диапазон дат по умолчанию и обработаем изменение ползунка
        dateRangeSeekBar.setProgress(dateRange - 1);
        dateRangeText.setText(getString(R.string.date_range, dateRange));
        dateRangeSection.setVisibility(dateRangeEnabled ? View.VISIBLE : View.GONE);
        dateRangeCheckbox.setChecked(dateRangeEnabled);

        dateRangeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dateRangeSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

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

        // Установим состояние чекбокса и значение ползунка для автообновления
        autoUpdateCheckbox.setChecked(autoUpdate);
        updateIntervalSeekBar.setProgress(updateInterval - 1);
        updateIntervalValue.setText(getString(R.string.update_interval, updateInterval));

        updateIntervalSection.setVisibility(autoUpdate ? View.VISIBLE : View.GONE);

        autoUpdateCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateIntervalSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        updateIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int hours = progress + 1;
                updateIntervalValue.setText(getString(R.string.update_interval, hours));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Установим состояние чекбоксов и сикбара для уведомлений
        notificationCheckbox.setChecked(notificationsEnabled);
        textMessageCheckbox.setChecked(isTextMessageEnabled);
        vibrationCheckbox.setChecked(vibrationEnabled);
        soundCheckbox.setChecked(soundEnabled);
        notificationRepeatSeekBar.setProgress(notificationRepeat - 1);
        notificationRepeatValue.setText(getString(R.string.notification_repeat, notificationRepeat));
        notificationIntervalSeekBar.setProgress(notificationInterval - 15);
        notificationIntervalValue.setText(getString(R.string.notification_interval, notificationInterval));

        notificationOptionsSection.setVisibility(notificationsEnabled ? View.VISIBLE : View.GONE);

        notificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                notificationOptionsSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

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
                int interval = progress + 15;
                notificationIntervalValue.setText(getString(R.string.notification_interval, interval));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Устанавливаем обработчик нажатия на кнопку
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
        doNotDisturbCheckbox.setChecked(doNotDisturbEnabled);
        doNotDisturbOptionsSection.setVisibility(doNotDisturbEnabled ? View.VISIBLE : View.GONE);

        // Handle checkbox changes
        doNotDisturbCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                doNotDisturbOptionsSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefSet.edit();

        editor.putInt(KEY_THEME, selectedTheme);

        boolean isWidgetEnabled = widgetCheckbox.isChecked();
        editor.putBoolean(KEY_WIDGET_ENABLED, isWidgetEnabled);

        boolean isGoogleCalendarEnabled = googleCalendarCheckbox.isChecked();
        editor.putBoolean(KEY_GOOGLE_CALENDAR_ENABLED, isGoogleCalendarEnabled);
        boolean isGoogleAccountSelectionEnabled = googleAccountSelectionCheckbox.isChecked();
        editor.putBoolean(KEY_GOOGLE_ACCOUNT_SELECTION, isGoogleAccountSelectionEnabled);

        boolean isDateRangeEnabled = dateRangeCheckbox.isChecked();
        editor.putBoolean(KEY_DATE_RANGE_ENABLED, isDateRangeEnabled);
        int dateRange = dateRangeSeekBar.getProgress() + 1;
        editor.putInt(KEY_DATE_RANGE, dateRange);

        boolean isAutoUpdateEnabled = autoUpdateCheckbox.isChecked();
        editor.putBoolean(KEY_UPDATE_ENABLED, isAutoUpdateEnabled);
        int updateInterval = updateIntervalSeekBar.getProgress() + 1;
        editor.putInt(KEY_UPDATE_INTERVAL, updateInterval);

        boolean isNotificationsEnabled = notificationCheckbox.isChecked();
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isNotificationsEnabled);
        boolean isTextMessageEnabled = textMessageCheckbox.isChecked();
        editor.putBoolean(KEY_TEXT_MESSAGE_ENABLED, isTextMessageEnabled);
        boolean isVibrationEnabled = vibrationCheckbox.isChecked();
        editor.putBoolean(KEY_VIBRATION_ENABLED, isVibrationEnabled);
        boolean isSoundEnabled = soundCheckbox.isChecked();
        editor.putBoolean(KEY_SOUND_ENABLED, isSoundEnabled);
        editor.putString(KEY_NOTIFICATION_SOUND_URI, soundUriString);

        int notificationRepeat = notificationRepeatSeekBar.getProgress() + 1;
        editor.putInt(KEY_NOTIFICATION_REPEAT, notificationRepeat);
        int notificationInterval = notificationIntervalSeekBar.getProgress() + 15;
        editor.putInt(KEY_NOTIFICATION_INTERVAL, notificationInterval);

        boolean isDoNotDisturbEnabled = doNotDisturbCheckbox.isChecked();
        editor.putBoolean(KEY_DO_NOT_DISTURB_ENABLED, isDoNotDisturbEnabled);
        int startTime = startTimePicker.getHour() * 60 + startTimePicker.getMinute();
        int endTime = endTimePicker.getHour() * 60 + endTimePicker.getMinute();
        editor.putInt(KEY_DO_NOT_DISTURB_START_TIME, startTime);
        editor.putInt(KEY_DO_NOT_DISTURB_END_TIME, endTime);

        editor.apply();
    }
}
