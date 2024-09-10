package csit.puet.presentation.google_calendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AlertDialog;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import csit.puet.AppConstants;
import csit.puet.data.model.Lesson;


public class GoogleCalendarHelper {
    private static final String TAG = "GoogleCalendarHelper";
    private Calendar mService = null;
    private final Context context;

    public GoogleCalendarHelper(Context context, GoogleAccountCredential credential) {
        this.context = context;
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("PUET")
                .build();
    }

    private void addEvent(String summary, String location, String description, DateTime startDateTime, DateTime endDateTime) {
        Log.d(TAG, "Starting to add event...");

        if (summary == null || summary.isEmpty()) {
            Log.e(TAG, "Event summary is null or empty");
            return;
        }
        if (location == null || location.isEmpty()) {
            Log.w(TAG, "Event location is null or empty, setting to 'Unknown location'");
            location = "Unknown location";
        }
        if (description == null) {
            Log.w(TAG, "Event description is null, setting to an empty string");
            description = "";
        }
        if (startDateTime == null) {
            Log.e(TAG, "Event startDateTime is null");
            return;
        }
        if (endDateTime == null) {
            Log.e(TAG, "Event endDateTime is null");
            return;
        }

        // Добавляем уникальный идентификатор в описание
        description += "\n" + AppConstants.PROGRAM_EVENT_DESCRIPTION;

        Log.d(TAG, "Summary: " + summary);
        Log.d(TAG, "Location: " + location);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "Start DateTime: " + startDateTime);
        Log.d(TAG, "End DateTime: " + endDateTime);

        new AddEventTask(summary, location, description, startDateTime, endDateTime).execute();
    }

    private class AddEventTask extends AsyncTask<Void, Void, Void> {
        private Exception mLastError = null;
        private final String summary;
        private final String location;
        private final String description;
        private final DateTime startDateTime;
        private final DateTime endDateTime;

        AddEventTask(String summary, String location, String description, DateTime startDateTime, DateTime endDateTime) {
            this.summary = summary;
            this.location = location;
            this.description = description;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Executing AddEventTask...");
            try {
                Event event = new Event()
                        .setSummary(summary)
                        .setLocation(location)
                        .setDescription(description);

                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone(TimeZone.getDefault().getID());
                event.setStart(start);

                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone(TimeZone.getDefault().getID());
                event.setEnd(end);

                Log.d(TAG, "Inserting event into calendar...");
                String calendarId = "primary";
                mService.events().insert(calendarId, event).execute();
                Log.d(TAG, "Event successfully added to calendar");
            } catch (Exception e) {
                mLastError = e;
                Log.e(TAG, "Error occurred while adding event: " + e.getMessage(), e);
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(context, "Event added to calendar", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(context, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }

        void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setMessage("Google Play Services is not available.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    dialog.show();
                }
            });
        }
    }

    public void addLessonsToCalendar(List<Lesson> lessons) {

        removeAllProgramEventsFromCalendar();
        //Toast.makeText(context, "Запуск фор addLessonsToCalendar"+lessons.size(), Toast.LENGTH_LONG).show();
        for (Lesson lesson : lessons) {

            String summary = lesson.getLesson();
            String location = lesson.getRoom();
            String description = "Тип: " + lesson.getLessonType() + "\nГрупа: " + lesson.getGroup() + "\nВикладач: " + lesson.getTeacher();

            DateTime startDateTime = convertLessonToDateTime(lesson, true);
            DateTime endDateTime = convertLessonToDateTime(lesson, false);

            if (startDateTime != null && endDateTime != null) {
                addEvent(summary, location, description, startDateTime, endDateTime);
            }
        }
    }

    private DateTime convertLessonToDateTime(Lesson lesson, boolean isStart) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate localDate = LocalDate.parse(lesson.getDate(), formatter);

            int hour = 8;
            int minute = 0;
            if (lesson.getNum() == 2) {
                hour = 10;
            } else if (lesson.getNum() == 3) {
                hour = 12;
            }

            if (!isStart) {
                hour += 1;
            }

            LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.of(hour, minute));
            return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing date/time for lesson: " + lesson, e);
            return null;
        }
    }

    public void removeAllProgramEventsFromCalendar() {
        ContentResolver cr = context.getContentResolver();
        Uri calendarUri = CalendarContract.Events.CONTENT_URI;

       // Toast.makeText(context, "Запуск removeAllProgramEventsFromCalendar", Toast.LENGTH_LONG).show();

        String selection = CalendarContract.Events.DESCRIPTION + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + AppConstants.PROGRAM_EVENT_DESCRIPTION + "%"};

        int rowsDeleted = cr.delete(calendarUri, selection, selectionArgs);

        if (rowsDeleted > 0) {
            Log.d("CalendarCleanup", "Deleted " + rowsDeleted + " events from the calendar.");
        } else {
            Log.d("CalendarCleanup", "No events found to delete or an error occurred.");
        }
    }

    public void updateLessonsFromPreferences(SharedPreferences prefSet) {
        // Получаем список новых уроков из SharedPreferences
        List<Lesson> newLessons = getLessonsFromPreferences(prefSet, AppConstants.KEY_NEW_LESSONS);

        // Удаляем старые события с тэгом
        removeAllProgramEventsFromCalendar();
        //Toast.makeText(context, "newLessons.size="+newLessons.size(), Toast.LENGTH_LONG).show();
        // Добавляем новые уроки в календарь
        addLessonsToCalendar(newLessons);
        //Toast.makeText(context, "Конец updateLessonsFromPreferences", Toast.LENGTH_LONG).show();
    }

    private List<Lesson> getLessonsFromPreferences(SharedPreferences prefSet, String key) {
        // Логика извлечения списка уроков из SharedPreferences
        // Пример: Сериализация и десериализация списка уроков в формате JSON
        String json = prefSet.getString(key, "");
        if (!json.isEmpty()) {
            return new Gson().fromJson(json, new TypeToken<List<Lesson>>() {
            }.getType());
        } else {
            return new ArrayList<>();
        }
    }
}
