package csit.puet.presentation.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import csit.puet.AppConstants;
import csit.puet.data.model.Lesson;
import csit.puet.presentation.ui.LessonAdapter;

public class WidgetUtils {

    public static String formatScheduleForWidget(Context context, List<Lesson> schedulesForWidget) {
        SharedPreferences prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        String lastSyncTime = prefSet.getString(AppConstants.KEY_LAST_SYNC_TIME,
                "Немає даних про останню синхронізацію");

        StringBuilder scheduleStringBuilder = new StringBuilder();
        scheduleStringBuilder.append("<div style='text-align:center;'>")
                .append("<font color='#FF9800'>")
                .append("<b>")
                .append("Розклад оновлено: ").append(lastSyncTime)
                .append("</b>")
                .append("</div>");

        String lastDate = "";
        for (Lesson lesson : schedulesForWidget) {
            if (!lesson.getDate().equals(lastDate)) {
                scheduleStringBuilder.append("<div style='text-align:center;'>")
                        .append("<font color='#2196F3'>")
                        .append("<b>")
                        .append(LessonAdapter.formatToCustomFormat(lesson.getDate()))
                        .append("</b>")
                        .append("</div>");
                lastDate = lesson.getDate();
            }
            scheduleStringBuilder.append("<b>")
                    .append(LessonAdapter.getPairNumberAndTime(lesson.getNum()))
                    .append("</b>")
                    .append("<br>")
                    .append(lesson.getLesson())
                    .append(" (")
                    .append(lesson.getLessonType())
                    .append(")")
                    .append("<br>")
                    .append(lesson.getGroup())
                    .append("<br>")
                    .append(lesson.getRoom().equals("дом_ПК") ? "дистанційно" : lesson.getRoom())
                    .append("<br>")
                    .append(lesson.getTeacher())
                    .append("<br>");
        }
        return scheduleStringBuilder.toString();
    }

    public static void updateWidget(Context context, SharedPreferences prefSet) {
        boolean isWidgetEnabled = prefSet.getBoolean(AppConstants.KEY_WIDGET_ENABLED, false);
        String savedLessonsJson = prefSet.getString("newLessonsFirst", null);

        if (isWidgetEnabled && savedLessonsJson != null) {
            Gson gson = new Gson();
            List<Lesson> savedLessons = gson.fromJson(savedLessonsJson, new TypeToken<List<Lesson>>() {
            }.getType());

            if (savedLessons != null && !savedLessons.isEmpty()) {
                String scheduleDataForWidget = formatScheduleForWidget(context, savedLessons);
                sendScheduleDataToWidget(context, scheduleDataForWidget, prefSet);
            } else {
                removeWidget(context);
            }
        } else {
            removeWidget(context);
        }
    }

    public static void removeWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, ScheduleWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, null);
        }
    }

    public static void sendScheduleDataToWidget
            (Context context, String scheduleData, SharedPreferences prefSet) {
        boolean isWidgetEnabled = prefSet.getBoolean(AppConstants.KEY_WIDGET_ENABLED, false);

        if (isWidgetEnabled) {
            Intent intent = new Intent(context, ScheduleWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra("SCHEDULE_DATA", scheduleData);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName
                    (context, ScheduleWidgetProvider.class));

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(intent);
        }
    }
}
