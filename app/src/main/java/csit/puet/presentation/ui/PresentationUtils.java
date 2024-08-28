package csit.puet.presentation.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.content.SharedPreferences;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;

import csit.puet.data.model.Lesson;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.StyleSpan;
import android.text.Layout;

public class PresentationUtils {

    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_THEME = "keyTheme";
    private static final int THEME_SYSTEM = 0;
    private static final int THEME_LIGHT = 1;
    private static final int THEME_DARK = 2;

    public static void setupTextWatcher(final AutoCompleteTextView autoCompleteTextView, final ImageButton imageButton) {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    imageButton.setVisibility(View.GONE);
                } else {
                    imageButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public static <T> void setupAutoComplete(Activity activity, AutoCompleteTextView autoCompleteTextView,
                                             ArrayAdapter<T> adapter, ImageButton closeButton,
                                             OnItemSelected<T> onItemSelected) {
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                autoCompleteTextView.performClick();
                if (!autoCompleteTextView.isPopupShowing()) {
                    autoCompleteTextView.showDropDown();
                }
            }
            return false;
        });

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            T selectedItem = adapter.getItem(position);
            assert selectedItem != null;
            autoCompleteTextView.setText(selectedItem.toString());
            autoCompleteTextView.clearFocus();
            autoCompleteTextView.dismissDropDown();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
            onItemSelected.onItemSelect(selectedItem);
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    closeButton.setVisibility(View.GONE);
                } else {
                    closeButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public interface OnItemSelected<T> {
        void onItemSelect(T item);
    }

    public static void vibrate(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    public static void applySavedTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int theme = preferences.getInt(KEY_THEME, THEME_SYSTEM);

        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void updateDatesFromPreferences(Context context, CalendarManager calendarManager, TextView startDateTextView, TextView endDateTextView) {
        // Загрузите сохраненные данные о диапазоне дат из SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE);
        int dateRange = preferences.getInt("keyDateRange", 7);

        // Обновите конечную дату на основе текущей даты плюс диапазон дат
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.DAY_OF_MONTH, dateRange);

        // Установите начальную и конечную дату в CalendarManager
        startDateTextView.setText(calendarManager.formatDate(calendarManager.getSelectedStartDate()));
        endDateTextView.setText(calendarManager.formatDate(endCalendar));

        // Обновите данные в CalendarManager
        calendarManager.getSelectedEndDate().setTime(endCalendar.getTime());
    }

    public static String formatScheduleForWidget(Context context, List<Lesson> schedulesForWidget) {
        // Загрузка времени последней синхронизации из SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("ServerDataPrefs", Context.MODE_PRIVATE);
        final String KEY_LAST_SYNC_TIME = "keyLastSyncTime";
        String lastSyncTime = preferences.getString(KEY_LAST_SYNC_TIME, "Немає даних про останню синхронізацію");

        StringBuilder scheduleStringBuilder = new StringBuilder();

        // Добавляем строку с последним временем синхронизации
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

    public static SpannableStringBuilder formatScheduleForDisplay(List<Lesson> schedules) {
        SpannableStringBuilder scheduleStringBuilder = new SpannableStringBuilder();

        for (Lesson lesson : schedules) {
            scheduleStringBuilder.append("\n");

            // Center and bold the pair number and time
            String pairNumberAndTime = String.valueOf(LessonAdapter.getPairNumberAndTime(lesson.getNum()));
            SpannableString spannablePair = new SpannableString(pairNumberAndTime);
            spannablePair.setSpan(new StyleSpan(Typeface.BOLD), 0, pairNumberAndTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannablePair.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, pairNumberAndTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            scheduleStringBuilder.append(spannablePair).append("\n");

            // Add the rest of the lesson details
            scheduleStringBuilder.append(lesson.getLesson())
                    .append(" (")
                    .append(lesson.getLessonType())
                    .append(")")
                    .append("\n")
                    .append(lesson.getGroup())
                    .append("\n")
                    .append(lesson.getRoom().equals("дом_ПК") ? "дистанційно" : lesson.getRoom())
                    .append("\n")
                    .append(lesson.getTeacher())
                    .append("\n");
        }
        return scheduleStringBuilder;
    }

    public static String getFormattedDateWithDayOfWeek(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE - dd.MM.yyyy", new Locale("uk"));

        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // If parsing fails, return the original string
        }
    }
}
