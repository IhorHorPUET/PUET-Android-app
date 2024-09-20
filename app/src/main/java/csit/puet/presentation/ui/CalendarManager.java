package csit.puet.presentation.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.DatePicker;
import android.widget.TextView;

import csit.puet.AppConstants;
import csit.puet.MainActivity;
import csit.puet.R;

import java.util.Calendar;

public class CalendarManager {

    private final Context context;
    private final TextView startDateTextView;
    private final TextView endDateTextView;
    private final Calendar selectedStartDate;
    private final Calendar selectedEndDate;

    public CalendarManager(Context context, TextView startDateTextView, TextView endDateTextView) {
        this.context = context;
        this.startDateTextView = startDateTextView;
        this.endDateTextView = endDateTextView;
        selectedStartDate = Calendar.getInstance();
        selectedEndDate = Calendar.getInstance();

        // Load the date range from SharedPreferences
        SharedPreferences prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        int dateRange = prefSet.getInt("keyDateRange", 7); // Default is 7 days

        // Set the end date based on the current date plus the date range
        selectedEndDate.add(Calendar.DAY_OF_MONTH, dateRange);
    }

    public void showStartDatePicker() {

        if (context instanceof Activity) {
            PresentationUtils.hideKeyboard((Activity) context);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        selectedStartDate.set(year, month, day);
                        startDateTextView.setText(formatDate(selectedStartDate));
                    }
                },
                selectedStartDate.get(Calendar.YEAR),
                selectedStartDate.get(Calendar.MONTH),
                selectedStartDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set the content description for accessibility
        datePickerDialog.getDatePicker().setContentDescription(context.getString(R.string.select_start_date));

        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        datePickerDialog.getDatePicker().setMaxDate(selectedEndDate.getTimeInMillis());
        datePickerDialog.show();
    }

    public void showEndDatePicker() {

        if (context instanceof Activity) {
            PresentationUtils.hideKeyboard((Activity) context);
        }

        // Load the date range from SharedPreferences
        SharedPreferences prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        int dateRange = prefSet.getInt("keyDateRange", 7); // Default is 7 days

        // Set the end date based on the current date plus the date range
        Calendar endDateForPicker = Calendar.getInstance();
        endDateForPicker.add(Calendar.DAY_OF_MONTH, dateRange);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        selectedEndDate.set(year, month, day);
                        endDateTextView.setText(formatDate(selectedEndDate));

                        // Save the new end date in SharedPreferences
                        SharedPreferences.Editor editor = prefSet.edit();
                        editor.putInt("keyDateRange", calculateDateRange(selectedStartDate, selectedEndDate));
                        editor.apply();
                    }
                },
                endDateForPicker.get(Calendar.YEAR),
                endDateForPicker.get(Calendar.MONTH),
                endDateForPicker.get(Calendar.DAY_OF_MONTH)
        );

        // Set the content description for accessibility
        datePickerDialog.getDatePicker().setContentDescription(context.getString(R.string.select_end_date));

        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        datePickerDialog.getDatePicker().setMinDate(selectedStartDate.getTimeInMillis());
        datePickerDialog.show();
    }

    public Calendar getSelectedStartDate() {
        return selectedStartDate;
    }

    public Calendar getSelectedEndDate() {
        return selectedEndDate;
    }

    @SuppressLint("DefaultLocale")
    public String formatDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%02d.%02d.%04d", day, month, year);
    }

    public int getDateRange() {
        long diff = selectedEndDate.getTimeInMillis() - selectedStartDate.getTimeInMillis();
        return (int) (diff / (24 * 60 * 60 * 1000)); // Return the number of days
    }

    private int calculateDateRange(Calendar startDate, Calendar endDate) {
        long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return (int) (diff / (24 * 60 * 60 * 1000));
    }
}
