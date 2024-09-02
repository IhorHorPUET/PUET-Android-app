package csit.puet;

public class AppConstants {

    // Keys for SharedPreferences
    public static final String PREF_SET = "prefSet";
    public static final String PREF_DATA = "prefData";

    // Theme keys
    public static final String KEY_THEME = "keyTheme";
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    // Widget keys
    public static final String KEY_WIDGET_ENABLED = "keyWidgetEnabled";

    // Google Calendar keys
    public static final int REQUEST_CALENDAR_PERMISSION = 1;
    public static final String KEY_GOOGLE_CALENDAR_ENABLED = "keyGoogleCalendarEnabled";
    public static final String KEY_GOOGLE_ACCOUNT_NAME = "googleAccountName";
    public static final String KEY_CALENDAR_PERMISSION_REVOCATION_SHOWN = "calendarPermissionRevocationShown";

    // Date range keys
    public static final String KEY_DATE_RANGE_ENABLED = "keyDateRangeEnabled";
    public static final String KEY_DATE_RANGE_INTERVAL = "keyDateRange";

    // Update interval keys
    public static final String KEY_UPDATE_ENABLED = "keyUpdateEnabled";
    public static final String KEY_UPDATE_INTERVAL = "keyUpdateInterval";

    // Notification keys
    public static final String KEY_NOTIFICATIONS_ENABLED = "keyNotificationsEnabled";
    public static final String KEY_TEXT_MESSAGE_ENABLED = "keyTextMessageEnabled";
    public static final String KEY_VIBRATION_ENABLED = "keyVibrationEnabled";
    public static final String KEY_SOUND_ENABLED = "keySoundEnabled";
    public static final String KEY_NOTIFICATION_REPEAT = "keyNotificationRepeat";
    public static final String KEY_NOTIFICATION_INTERVAL = "keyNotificationInterval";
    public static final String KEY_NOTIFICATION_SOUND_URI = "keyNotificationSoundUri";

    // Do not disturb keys
    public static final String KEY_DO_NOT_DISTURB_ENABLED = "keyDoNotDisturbEnabled";
    public static final String KEY_DO_NOT_DISTURB_START_TIME = "keyDoNotDisturbStartTime";
    public static final String KEY_DO_NOT_DISTURB_END_TIME = "keyDoNotDisturbEndTime";

    // Permission Request Codes
    public static final int PERMISSIONS_REQUEST_CODE = 100;
    public static final int REQUEST_AUTHORIZATION = 1001;

    // Other Constants
    public static final String PROGRAM_EVENT_DESCRIPTION = "Created by PUET App";
    public static final String KEY_DATES_WITH_DIFFERENCES = "datesWithDifferences";
    public static final String KEY_NEW_LESSONS = "newLessonsFirst";
    public static final String KEY_OLD_LESSONS = "oldLessonsFirst";
    public static final String KEY_LAST_SYNC_TIME = "keyLastSyncTime";

    // Keys for teacher, classroom, and group data
    public static final String KEY_TEACHER_NAME = "teacherName";
    public static final String KEY_TEACHER_ID = "teacherId";
    public static final String KEY_CLASSROOM_NAME = "classroomName";
    public static final String KEY_CLASSROOM_ID = "classroomId";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_GROUP_BANDS = "groupBands";
}
