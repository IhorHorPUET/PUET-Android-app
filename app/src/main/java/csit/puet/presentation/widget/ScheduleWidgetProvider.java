package csit.puet.presentation.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.RemoteViews;

import csit.puet.R;

public class ScheduleWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String scheduleData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);

        // Обновление текста в TextView виджета
        views.setTextViewText(R.id.widget_text_view, Html.fromHtml(scheduleData));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Проверка, содержит ли intent данные расписания
        if (intent.hasExtra("SCHEDULE_DATA")) {
            String scheduleData = intent.getStringExtra("SCHEDULE_DATA");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            assert appWidgetIds != null;
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, scheduleData);
            }
        }
    }
}
