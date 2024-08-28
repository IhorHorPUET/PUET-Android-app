package csit.puet.data;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import csit.puet.MainActivity;
import csit.puet.data.model.Lesson;

public class GoogleCalendarHelper {
    private static final String TAG = "GoogleCalendarHelper";
    private final Calendar mService;
    private final Context context;

    public GoogleCalendarHelper(Context context, GoogleAccountCredential credential) {
        this.context = context;
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("PUET")
                .build();
    }

    public void addEvent(String summary, String location, String description, DateTime startDateTime, DateTime endDateTime) {
        Log.d(TAG, "Starting to add event...");
        if (summary == null || summary.isEmpty()) {
            Log.e(TAG, "Event summary is null or empty");
            return;
        }
        if (location == null || location.isEmpty()) {
            Log.w(TAG, "Event location is null or empty, setting to 'Unknown location'");
            location = "Unknown location";  // задаем значение по умолчанию
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
                            MainActivity.REQUEST_AUTHORIZATION);
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
                                    // Handle dialog click
                                }
                            })
                            .create();
                    dialog.show();
                }
            });
        }
    }

    public void addLessonsToCalendar(List<Lesson> lessons) {
        for (Lesson lesson : lessons) {
            String summary = lesson.getLesson();
            String location = lesson.getRoom();
            String description = "Тип: " + lesson.getLessonType() + "\nГруппа: " + lesson.getGroup() + "\nПреподаватель: " + lesson.getTeacher();

            // Преобразуем дату и время начала/окончания занятия
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

            // Задаем время начала и окончания занятий
            int hour = 8; // Пример, можно использовать соответствие с lesson.getNum()
            int minute = 0;
            if (lesson.getNum() == 2) {
                hour = 10;
            } else if (lesson.getNum() == 3) {
                hour = 12;
            }
            // Другие временные слоты могут быть добавлены здесь

            if (!isStart) {
                hour += 1; // Продолжительность урока, например, 1 час
            }

            LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.of(hour, minute));
            return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing date/time for lesson: " + lesson, e);
            return null;
        }
    }
}
