package csit.puet.presentation.app_settings;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import csit.puet.AppConstants;
import csit.puet.data.DataCallbackAllLessons;
import csit.puet.data.DataManager;
import csit.puet.data.model.Lesson;
import csit.puet.data.DataUtils;
import csit.puet.presentation.ui.PresentationUtils;
import csit.puet.presentation.widget.ScheduleWidgetProvider;

public class ScheduleSync extends Worker {

    SharedPreferences prefSet;

    private int dateRange;
    private boolean isNotificationsEnabled;

    List<List<Lesson>> newLessons = new ArrayList<>();
    List<List<Lesson>> oldLessons = new ArrayList<>();
    List<Lesson> newLessonsFirst = new ArrayList<>();
    List<Lesson> oldLessonsFirst = new ArrayList<>();
    private static final String TAG = "scheduleSync";
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean success = true;
    StringBuilder datesWithDifferences = new StringBuilder();

    public ScheduleSync(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        loadPreferences(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        DataManager dataManager = new DataManager(context);

        List<String> searchBands = DataUtils.loadSearchBands(context);

        if (searchBands.isEmpty()) {
            return Result.failure();
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        searchBands.replaceAll(s -> s.replaceFirst("date_s=\\d{4}-\\d{2}-\\d{2}", "date_s=" + currentDate));

        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(System.currentTimeMillis() + dateRange * 24L * 60 * 60 * 1000));
        searchBands.replaceAll(s -> s.replaceFirst("date_e=\\d{4}-\\d{2}-\\d{2}", "date_e=" + endDate));

        dataManager.getLessonsList(searchBands, new DataCallbackAllLessons() {
            @Override
            public void onDataLoaded(List<List<Lesson>> data) {
                newLessons.clear();
                newLessons.addAll(data);
                newLessons = DataUtils.sortLessonsList(newLessons);

                Future<?> future = executor.submit(() -> {
                    dataManager.saveLessonsListToDatabase(newLessons);
                    oldLessons = dataManager.getLessonsListFromDatabase();
                });

                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    success = false;
                }

                if (newLessons != null && !newLessons.isEmpty() && !newLessons.get(0).isEmpty()) {
                    newLessonsFirst = newLessons.get(0);
                }

                if (oldLessons != null && !oldLessons.isEmpty() && !oldLessons.get(0).isEmpty()) {
                    oldLessonsFirst = oldLessons.get(0);
                }

                if (isNotificationsEnabled) {
                    if (areSchedulesDifferent(newLessonsFirst, oldLessonsFirst, context)) {
                        triggerNotificationWorker(context);
                    }
                }
                updateWidget(context, newLessonsFirst);
                executor.shutdown();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Error fetching new lessons from server", throwable);
                success = false;
            }
        });
        return success ? Result.success() : Result.failure();
    }

    private void triggerNotificationWorker(Context context) {
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .addTag("notification_task")
                .build();

        WorkManager.getInstance(context).enqueue(notificationWorkRequest);
    }

    private boolean areSchedulesDifferent(List<Lesson> newLessons, List<Lesson> oldLessons, Context context) {
        if (newLessons.isEmpty() && oldLessons.isEmpty()) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < dateRange; i++) {
            String dateToCheck = currentDate.format(formatter);

            List<Lesson> newDailyLessons = findLessonsByDate(newLessons, dateToCheck, "newDailyLessons");
            List<Lesson> oldDailyLessons = findLessonsByDate(oldLessons, dateToCheck, "oldDailyLessons");

            if (newDailyLessons == null && oldDailyLessons == null) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            if (!Objects.equals(newDailyLessons, oldDailyLessons)) {
                if (datesWithDifferences.length() > 0) {
                    datesWithDifferences.append(", ");
                }
                datesWithDifferences.append(dateToCheck);
            }
            currentDate = currentDate.plusDays(1);
        }

        if (datesWithDifferences.length() > 0) {
            SharedPreferences.Editor editor = prefSet.edit();
            editor.putString("datesWithDifferences", datesWithDifferences.toString());
            Gson gson = new Gson();
            if (!newLessonsFirst.isEmpty()) {
                String newLessonsJson = gson.toJson(newLessons);
                editor.putString("newLessonsFirst", newLessonsJson);
            }
            if (!oldLessonsFirst.isEmpty()) {
                String oldLessonsJson = gson.toJson(oldLessons);
                editor.putString("oldLessonsFirst", oldLessonsJson);
            }
            editor.apply();
            return true;
        }
        return false;
    }

    private List<Lesson> findLessonsByDate(List<Lesson> lessons, String date, String lessonsName) {
        List<Lesson> lessonsForDate = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Lesson lesson : lessons) {
            if (lesson.getDate().equals(date)) {
                lessonsForDate.add(lesson);
            }
        }
        return lessonsForDate.isEmpty() ? null : lessonsForDate;
    }

    private void loadPreferences(Context context) {
        dateRange = prefSet.getInt("keyDateRange", 7); // Default to 7 days
        isNotificationsEnabled = prefSet.getBoolean("keyNotificationsEnabled", false);
    }

    private void updateWidget(Context context, List<Lesson> newLessonsFirst) {
        String scheduleData = null;
        if (newLessonsFirst != null && !newLessonsFirst.isEmpty()) {
            scheduleData = PresentationUtils.formatScheduleForWidget(context, newLessonsFirst);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, ScheduleWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        Intent intent = new Intent(context, ScheduleWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("SCHEDULE_DATA", scheduleData);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        context.sendBroadcast(intent);
    }
}
