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

        if (scheduleData != null && !scheduleData.isEmpty()) {
            views.setTextViewText(R.id.widget_text_view, Html.fromHtml(scheduleData));
        } else {
            String defaultText = "Зараз відсутня інформація про розклад. Оберіть параметри для пошуку та натисніть кнопку \"Пошук\" у додатку";
            views.setTextViewText(R.id.widget_text_view, defaultText);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.hasExtra("SCHEDULE_DATA")) {
            String scheduleData = intent.getStringExtra("SCHEDULE_DATA");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            if (appWidgetIds != null) {
                for (int appWidgetId : appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, scheduleData);
                }
            }
        }
    }
}
