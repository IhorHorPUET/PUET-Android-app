package csit.puet.presentation.app_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import csit.puet.AppConstants;
import csit.puet.R;
import csit.puet.data.model.Lesson;
import csit.puet.presentation.ui.PresentationUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ScheduleComparisonActivity extends AppCompatActivity {

    SharedPreferences prefSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_comparison);
        prefSet = getSharedPreferences(AppConstants.PREF_SET, MODE_PRIVATE);

        WorkManager.getInstance(this).cancelAllWorkByTag("notification_task");

        // Get the container for dynamically adding schedule blocks
        LinearLayout scheduleContainer = findViewById(R.id.scheduleContainer);

        // Retrieve data from Intent
        String scheduleDifferences = getIntent().getStringExtra("scheduleDifferences");

        assert scheduleDifferences != null;
        String[] dates = scheduleDifferences.split(",");

        for (String date : dates) {
            // Add schedule blocks for each date
            addScheduleBlock(scheduleContainer, date.trim());
        }
    }

    private void addScheduleBlock(LinearLayout container, String date) {
        Context context = container.getContext();

        // Create a header with the date
        TextView dateHeader = createDateHeader(context, date);
        container.addView(dateHeader);

        // Retrieve new and old schedules for the given date
        List<Lesson> newSchedule = getNewScheduleForDate(date);
        List<Lesson> oldSchedule = getOldScheduleForDate(date);

        // Create and add blocks for the new schedule
        TextView newScheduleLabel = createLabel(context, getString(R.string.new_lessons), "#C8E6C9");
        container.addView(newScheduleLabel);
        TextView newScheduleText = createScheduleTextView(context, newSchedule, "#C8E6C9");
        container.addView(newScheduleText);

        // Create and add blocks for the old schedule
        TextView oldScheduleLabel = createLabel(context, getString(R.string.old_lessons), "#F8BBD0");
        container.addView(oldScheduleLabel);
        TextView oldScheduleText = createScheduleTextView(context, oldSchedule, "#F8BBD0");
        container.addView(oldScheduleText);
    }

    private TextView createDateHeader(Context context, String date) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setBackgroundColor(context.getResources().getColor(R.color.blue)); // Blue background
        textView.setPadding(8, 8, 8, 8);
        textView.setText(PresentationUtils.getFormattedDateWithDayOfWeek(date));
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(context.getResources().getColor(android.R.color.black));
        textView.setTextSize(20);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private TextView createLabel(Context context, String text, String backgroundColor) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setBackgroundColor(Color.parseColor(backgroundColor));
        textView.setPadding(8, 8, 8, 8);
        textView.setText(text);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(context.getResources().getColor(android.R.color.black));
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private TextView createScheduleTextView(Context context, List<Lesson> schedule, String backgroundColor) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setBackgroundColor(Color.parseColor(backgroundColor));
        textView.setPadding(8, 4, 8, 4);

        if (schedule.isEmpty()) {
            textView.setText("На цей день занять не заплановано.");
        } else {
            // Format schedule with bold and centered pair number and time
            textView.setText(PresentationUtils.formatScheduleForDisplay(schedule));
        }

        textView.setTextColor(context.getResources().getColor(android.R.color.black));
        textView.setTextSize(16);
        return textView;
    }

    private List<Lesson> getNewScheduleForDate(String date) {
        String newLessonsJson = prefSet.getString(AppConstants.KEY_NEW_LESSONS, null);

        if (newLessonsJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Lesson>>() {
        }.getType();
        List<Lesson> newLessons = gson.fromJson(newLessonsJson, type);

        return filterScheduleByDate(newLessons, date);
    }

    private List<Lesson> getOldScheduleForDate(String date) {
        String oldLessonsJson = prefSet.getString(AppConstants.KEY_OLD_LESSONS, null);

        if (oldLessonsJson == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Lesson>>() {
        }.getType();
        List<Lesson> oldLessons = gson.fromJson(oldLessonsJson, type);

        return filterScheduleByDate(oldLessons, date);
    }

    private List<Lesson> filterScheduleByDate(List<Lesson> schedule, String date) {
        List<Lesson> filteredSchedule = new ArrayList<>();
        for (Lesson lesson : schedule) {
            if (lesson.getDate().equals(date)) {
                filteredSchedule.add(lesson);
            }
        }
        return filteredSchedule;
    }
}
