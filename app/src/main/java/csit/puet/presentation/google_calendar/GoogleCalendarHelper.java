package csit.puet.presentation.google_calendar;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;
import android.util.Pair;

import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.gson.Gson;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import csit.puet.AppConstants;
import csit.puet.data.model.Lesson;


public class GoogleCalendarHelper {
    private static final String TAG = "GoogleCalendarHelper";
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private GoogleAccountCredential credential;
    private boolean authorizationGranted = true;

    public GoogleCalendarHelper(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }

    private void initializeCredential() {
        if (credential == null) {
            String accountName = sharedPreferences.getString(AppConstants.KEY_GOOGLE_ACCOUNT_NAME, null);
            credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton("https://www.googleapis.com/auth/calendar.events"));
            credential.setSelectedAccountName(accountName);
        }
    }

    private void addEvent(String summary, String location, String description, DateTime startDateTime, DateTime endDateTime) {
        if (summary == null || summary.isEmpty()) {
            return;
        }
        if (location == null || location.isEmpty()) {
            location = "Unknown location";
        }
        if (description == null) {
            description = "";
        }
        if (startDateTime == null) {
            return;
        }
        if (endDateTime == null) {
            return;
        }

        description += "\n" + AppConstants.PROGRAM_EVENT_DESCRIPTION;

        // Логгирование перед добавлением события
        Log.d("GoogleCalendarHelper", "Adding event: " +
                "\nSummary: " + summary +
                "\nLocation: " + location +
                "\nDescription: " + description +
                "\nStart: " + startDateTime +
                "\nEnd: " + endDateTime);

        new AddEventTask(credential, summary, location, description, startDateTime, endDateTime).execute();
    }

    private class AddEventTask extends AsyncTask<Void, Void, Void> {
        private Exception mLastError = null;
        private final GoogleAccountCredential credential;
        private final String summary;
        private final String location;
        private final String description;
        private final DateTime startDateTime;
        private final DateTime endDateTime;

        AddEventTask(GoogleAccountCredential credential, String summary, String location, String description, DateTime startDateTime, DateTime endDateTime) {
            this.credential = credential;
            this.summary = summary;
            this.location = location;
            this.description = description;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HttpTransport transport = new NetHttpTransport();
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                Calendar service = new Calendar.Builder(transport, jsonFactory, credential)
                        .setApplicationName("PUET")
                        .build();

                Event event = new Event()
                        .setSummary(summary)
                        .setLocation(location)
                        .setDescription(description)
                        .setColorId("10");

                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone(TimeZone.getDefault().getID());
                event.setStart(start);

                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone(TimeZone.getDefault().getID());
                event.setEnd(end);

                String calendarId = "primary";
                service.events().insert(calendarId, event).execute();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    ((Activity) context).startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            AppConstants.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(context, "Помилка:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Запит скасовано.", Toast.LENGTH_LONG).show();
            }
        }

        void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
            ((Activity) context).runOnUiThread(() -> {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage("Сервіси Google Play недоступні.")
                        .setPositiveButton("OK", (dialog1, which) -> {
                        })
                        .create();
                dialog.show();
            });
        }
    }

    public void addLessonsToCalendar(String autor) {
        List<Lesson> newLessons = getLessonsFromPreferences(sharedPreferences);
        removeAllPuetEventsFromCalendar();
        int eventsAdded = 0;
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentDateTime.format(timeFormatter);

        if (authorizationGranted && newLessons != null && !newLessons.isEmpty()) {
            for (Lesson lesson : newLessons) {
                String summary =
//                        autor + " " + formattedTime + " " +
                        lesson.getLesson() + ". " +
                        lesson.getTeacher() + ". " +
                        lesson.getGroup();

                String description = "";
//                        "Час додавання: " + formattedTime + "   " + autor;

                Pair<String, String> startAndEndTime = GoogleCalendarUtils.getLessonStartAndEndTime(lesson.getNum());
                DateTime startDateTime = GoogleCalendarUtils.convertToDateTime(lesson.getDate(), startAndEndTime.first);
                DateTime endDateTime = GoogleCalendarUtils.convertToDateTime(lesson.getDate(), startAndEndTime.second);

                String location = lesson.getNum() + " пара " + "(" + lesson.getLessonType() + ") " +
                        ("дом_ПК".equals(lesson.getRoom()) ? "дистанційно" : lesson.getRoom());

                addEvent(summary, location, description, startDateTime, endDateTime);
                eventsAdded++;
            }
            Toast.makeText(context, "Додано подій до календаря: " + eventsAdded, Toast.LENGTH_LONG).show();
        }
    }

    public void removeAllPuetEventsFromCalendar() {
        boolean isGoogleCalendarEnabled = sharedPreferences.getBoolean(
                AppConstants.KEY_GOOGLE_CALENDAR_ENABLED, false);

        boolean hasCalendarPermissions = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;

        String accountName = sharedPreferences.getString(AppConstants.KEY_GOOGLE_ACCOUNT_NAME, null);
        boolean isAccountSelected = accountName != null && !accountName.isEmpty();

        if (!isGoogleCalendarEnabled || !hasCalendarPermissions || !isAccountSelected) {
            authorizationGranted = false;
//            Toast.makeText(context, "первая проверка - ошибка" + "   " + isGoogleCalendarEnabled + "   " + hasCalendarPermissions + "   " + isAccountSelected, Toast.LENGTH_LONG).show();
            return;
        } else {
//            Toast.makeText(context, "первая проверка - нормально", Toast.LENGTH_LONG).show();
            authorizationGranted = true;
        }

        initializeCredential();
        if (credential.getSelectedAccountName() == null) {
//            Toast.makeText(context, "вторая проверка - ошибка", Toast.LENGTH_LONG).show();
            authorizationGranted = false;
            return;
        } else {
//            Toast.makeText(context, "вторая проверка - нормально", Toast.LENGTH_LONG).show();
            authorizationGranted = true;
        }


        int rowsDeleted = 0;
        ContentResolver cr = context.getContentResolver();
        Uri calendarUri = CalendarContract.Events.CONTENT_URI;
        String selection = CalendarContract.Events.DESCRIPTION + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + AppConstants.PROGRAM_EVENT_DESCRIPTION + "%"};

        rowsDeleted = cr.delete(calendarUri, selection, selectionArgs);

        if (rowsDeleted > 0) {
            Toast.makeText(context, "Видалено подій з календаря: " + rowsDeleted, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Не знайдено подій для видалення або сталася помилка.", Toast.LENGTH_LONG).show();
        }
    }

    private List<Lesson> getLessonsFromPreferences(SharedPreferences prefSet) {
        String json = prefSet.getString(AppConstants.KEY_NEW_LESSONS, "");
        if (!json.isEmpty()) {
            return new Gson().fromJson(json, new TypeToken<List<Lesson>>() {
            }.getType());
        } else {
            return new ArrayList<>();
        }
    }
}
