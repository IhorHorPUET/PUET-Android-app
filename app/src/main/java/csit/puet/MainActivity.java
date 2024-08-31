package csit.puet;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import csit.puet.data.DataCallback;
import csit.puet.data.DataCallbackAllLessons;
import csit.puet.data.DataManager;
import csit.puet.data.DataUtils;
import csit.puet.data.GoogleCalendarHelper;
import csit.puet.data.model.Classroom;
import csit.puet.data.model.Group;
import csit.puet.data.model.Lesson;
import csit.puet.data.model.Teacher;
import csit.puet.presentation.app_settings.ScheduleSync;
import csit.puet.presentation.app_settings.SettingsActivity;
import csit.puet.presentation.ui.AdapterClassroomName;
import csit.puet.presentation.ui.AdapterGroupName;
import csit.puet.presentation.ui.AdapterTeacherName;
import csit.puet.presentation.ui.CalendarManager;
import csit.puet.presentation.ui.PresentationUtils;
import csit.puet.presentation.ui.ViewPagerAdapter;
import csit.puet.presentation.widget.ScheduleWidgetProvider;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefData;

    private ProgressBar progressBar;
    private CalendarManager calendarManager;
    TextView startDateTextView;
    TextView endDateTextView;

    List<String> groupBands = new ArrayList<>();
    List<String> searchBands = new ArrayList<>();
    ArrayList<String> savedGroupBands;
    List<List<Lesson>> catalogSchedules = new ArrayList<>();
    String date_s = "";
    String date_e = "";
    String teacherName = "";
    String classroomName = "";
    String groupName = "";
    String teacherId = "";
    String classroomId = "";

    private boolean autoLoad = true;
    private boolean checkBox = true;

    private ActivityResultLauncher<Intent> accountPickerLauncher;
    public static final int REQUEST_AUTHORIZATION = 1001;
    private GoogleAccountCredential mCredential;
    private GoogleCalendarHelper calendarHelper;

    private ActivityResultLauncher<Intent> settingsActivityLauncher;
    SharedPreferences prefSet;
    private static final String KEY_UPDATE_ENABLED = "keyUpdateEnabled";
    private static final String KEY_UPDATE_INTERVAL = "keyUpdateInterval";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PresentationUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_interface);
        prefSet = this.getSharedPreferences("prefSettings", Context.MODE_PRIVATE);
        prefData = this.getSharedPreferences("prefData", Context.MODE_PRIVATE);

        mCredential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));

        accountPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                        String accountName = result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            mCredential.setSelectedAccountName(accountName);
                            SharedPreferences.Editor editor = prefData.edit();
                            editor.putString("googleAccountName", accountName);
                            editor.apply();
                            calendarHelper = new GoogleCalendarHelper(this, mCredential);
                        }
                    }
                }
        );

        String savedAccountName = prefData.getString("googleAccountName", null);
        boolean shouldPromptAccountSelection = prefSet.getBoolean(SettingsActivity.KEY_GOOGLE_ACCOUNT_SELECTION, true);

        if (shouldPromptAccountSelection) {
            accountPickerLauncher.launch(mCredential.newChooseAccountIntent());
        } else if (savedAccountName != null) {
            mCredential.setSelectedAccountName(savedAccountName);
            calendarHelper = new GoogleCalendarHelper(this, mCredential);
        } else {
            calendarHelper = null;
        }

        settingsActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        PresentationUtils.updateDatesFromPreferences(this, calendarManager, startDateTextView, endDateTextView);
                        startBackgroundUpdate();
                    }
                }
        );

        progressBar = findViewById(R.id.progressBar);
        touchableOn(progressBar);

        String savedTeacherName = prefData.getString("teacherName", "");
        String savedTeacherId = prefData.getString("teacherId", "");
        String savedClassroomName = prefData.getString("classroomName", "");
        String savedClassroomId = prefData.getString("classroomId", "");
        String savedGroupName = prefData.getString("groupName", "");

        Gson gson = new Gson();
        String jsonGroupBands = prefData.getString("groupBands", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        savedGroupBands = gson.fromJson(jsonGroupBands, type);

        DataManager dataManager = new DataManager(getApplicationContext());

        if (savedGroupBands == null) {
            savedGroupBands = new ArrayList<>();
        }

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            PresentationUtils.vibrate(MainActivity.this, 40);
            Intent intent = new Intent(view.getContext(), SettingsActivity.class);
            settingsActivityLauncher.launch(intent);
        });


//============  Calendar  ===============
        startDateTextView = findViewById(R.id.startDate);
        endDateTextView = findViewById(R.id.endDate);
        calendarManager = new CalendarManager(this, startDateTextView, endDateTextView);
        Calendar currentCalendar = Calendar.getInstance();

        int dateRange = prefSet.getInt("keyDateRange", 7);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.DAY_OF_MONTH, dateRange);

        startDateTextView.setText(calendarManager.formatDate(currentCalendar));
        endDateTextView.setText(calendarManager.formatDate(endCalendar));
        startDateTextView.setOnClickListener(v -> calendarManager.showStartDatePicker());

        endDateTextView.setOnClickListener(v -> calendarManager.showEndDatePicker());

//============  Teachers  AutoComplete  ===============
        dataManager.getTeachersList(new DataCallback<List<Teacher>>() {
            final List<Teacher> listOfTeachers = new ArrayList<>();

            @Override
            public void onDataLoaded(List<Teacher> teachersList) {
                runOnUiThread(() -> {
                    listOfTeachers.clear();
                    listOfTeachers.addAll(teachersList);

                    AutoCompleteTextView teacherNameAutoComplete = findViewById(R.id.teachersNameAutoComplete);
                    teacherNameAutoComplete.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    teacherNameAutoComplete.setSingleLine(false);
                    teacherNameAutoComplete.setMaxLines(3);
                    ImageButton clearButtonTeacherName = findViewById(R.id.clearButtonTeacherName);
                    teacherName = savedTeacherName;
                    teacherId = savedTeacherId;
                    if (!savedTeacherName.isEmpty()) {
                        clearButtonTeacherName.setVisibility(View.VISIBLE);
                        teacherNameAutoComplete.setText(savedTeacherName);
                    }
                    teacherNameAutoComplete.setThreshold(1);
                    AdapterTeacherName adapterTeacherName = new AdapterTeacherName(MainActivity.this, listOfTeachers);
                    PresentationUtils.setupAutoComplete(MainActivity.this,
                            teacherNameAutoComplete, adapterTeacherName, clearButtonTeacherName, selectedTeacher -> {
                                if (selectedTeacher != null) {
                                    teacherName = selectedTeacher.getTeacherName();
                                    teacherId = "teacher=" + selectedTeacher.getIdPrep() + "&";
                                    teacherNameAutoComplete.setText(teacherName);
                                }
                            });

                    clearButtonTeacherName.setOnClickListener(v -> {
                        PresentationUtils.vibrate(MainActivity.this, 60);
                        teacherNameAutoComplete.setText("");
                        teacherName = "";
                        teacherId = "";
                        clearButtonTeacherName.setVisibility(View.GONE);
                    });

                    PresentationUtils.setupTextWatcher(teacherNameAutoComplete, clearButtonTeacherName);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Помилка отримання списку викладачів: " + throwable.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });


//============  Classrooms  AutoComplete  ===============
        dataManager.getRoomsList(new DataCallback<List<Classroom>>() {
            final List<Classroom> listOfClassrooms = new ArrayList<>();

            @Override
            public void onDataLoaded(List<Classroom> classroomsList) {
                runOnUiThread(() -> {
                    listOfClassrooms.clear();
                    listOfClassrooms.addAll(classroomsList);

                    AutoCompleteTextView classroomNameAutoComplete = findViewById(R.id.classroomsNameAutoComplete);
                    classroomNameAutoComplete.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    classroomNameAutoComplete.setSingleLine(false);
                    classroomNameAutoComplete.setMaxLines(3);
                    ImageButton clearButtonClassroomName = findViewById(R.id.clearButtonClassroomName);
                    classroomName = savedClassroomName;
                    classroomId = savedClassroomId;
                    if (!savedClassroomName.isEmpty()) {
                        clearButtonClassroomName.setVisibility(View.VISIBLE);
                        classroomNameAutoComplete.setText(savedClassroomName);
                    }
                    classroomNameAutoComplete.setThreshold(1);
                    AdapterClassroomName adapterClassroomName = new AdapterClassroomName
                            (MainActivity.this, listOfClassrooms);
                    PresentationUtils.setupAutoComplete(MainActivity.this,
                            classroomNameAutoComplete, adapterClassroomName, clearButtonClassroomName, selectedClassroom -> {
                                if (selectedClassroom != null) {
                                    classroomName = selectedClassroom.getClassroomName();
                                    classroomId = "room=" + selectedClassroom.getId() + "&";
                                    classroomNameAutoComplete.setText(classroomName);
                                }
                            });

                    clearButtonClassroomName.setOnClickListener(v -> {
                        PresentationUtils.vibrate(MainActivity.this, 60);
                        classroomNameAutoComplete.setText("");
                        classroomName = "";
                        classroomId = "";
                        clearButtonClassroomName.setVisibility(View.GONE);
                    });

                    PresentationUtils.setupTextWatcher(classroomNameAutoComplete, clearButtonClassroomName);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Помилка отримання списку аудиторій", Toast.LENGTH_SHORT).show());
            }
        });


//============  Groups  AutoComplete  ===============
        dataManager.getGroupsList(new DataCallback<List<Group>>() {
            final List<Group> listOfGroups = new ArrayList<>();

            @Override
            public void onDataLoaded(List<Group> groupsList) {
                runOnUiThread(() -> {
                    listOfGroups.clear();
                    listOfGroups.addAll(groupsList);

                    AutoCompleteTextView groupNameAutoComplete = findViewById(R.id.groupsNameAutoComplete);
                    groupNameAutoComplete.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    groupNameAutoComplete.setSingleLine(false);
                    groupNameAutoComplete.setMaxLines(3);
                    ImageButton clearButtonGroupName = findViewById(R.id.clearButtonGroupName);
                    groupName = savedGroupName;
                    groupBands = savedGroupBands;
                    if (!savedGroupName.isEmpty()) {
                        clearButtonGroupName.setVisibility(View.VISIBLE);
                        groupNameAutoComplete.setText(savedGroupName);
                    }

                    if (autoLoad) {
                        search();
                    }
                    groupNameAutoComplete.setThreshold(1);
                    AdapterGroupName adapterGroupName = new AdapterGroupName(MainActivity.this, listOfGroups);
                    PresentationUtils.setupAutoComplete(MainActivity.this,
                            groupNameAutoComplete, adapterGroupName, clearButtonGroupName, selectedGroup -> {
                            });

                    groupNameAutoComplete.setOnDismissListener(() -> {
                        groupBands.clear();
                        List<Group> selectedGroups = adapterGroupName.getSelectedGroups();
                        StringBuilder selectedGroupsName = new StringBuilder();
                        for (Group group : selectedGroups) {
                            if (selectedGroupsName.length() > 0) selectedGroupsName.append(", ");
                            selectedGroupsName.append(group.getName());
                            groupBands.add(group.getGroupBand());
                        }
                        groupName = selectedGroupsName.toString();
                        groupNameAutoComplete.setText(groupName);
                    });

                    clearButtonGroupName.setOnClickListener(v -> {
                        PresentationUtils.vibrate(MainActivity.this, 60);
                        groupNameAutoComplete.setText("");
                        adapterGroupName.clearSelectedGroups();
                        groupBands.clear();
                        groupName = "";
                        clearButtonGroupName.setVisibility(View.GONE);
                    });

                    PresentationUtils.setupTextWatcher(groupNameAutoComplete, clearButtonGroupName);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Помилка отримання списку груп", Toast.LENGTH_SHORT).show());
            }
        });


//============  Checkbox  ===============
        CheckBox rememberSelectionCheckbox = findViewById(R.id.checkBox);
        rememberSelectionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> checkBox = isChecked);


//============  Search  ===============
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> search());


//============  Info Block  ===============
        TextView textView = findViewById(R.id.infoBlock);
        String formattedText = getString(R.string.Info);
        textView.setText(Html.fromHtml(formattedText, Html.FROM_HTML_MODE_COMPACT));

//============  End of onCreate  ==================================================================
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();

        boolean isWidgetEnabled = prefSet.getBoolean(SettingsActivity.KEY_WIDGET_ENABLED, false);

        if (isWidgetEnabled) {
            updateWidgetAndCalendar();
        } else {
            removeWidget();
        }
    }

    private void applyTheme() {
        int theme = prefSet.getInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_SYSTEM);
        switch (theme) {
            case SettingsActivity.THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case SettingsActivity.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case SettingsActivity.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefData.edit();

        editor.putString("teacherName", teacherName);
        editor.putString("teacherId", teacherId);
        editor.putString("classroomName", classroomName);
        editor.putString("classroomId", classroomId);
        editor.putString("groupName", groupName);

        Gson gson = new Gson();
        String jsonGroupBands = gson.toJson(groupBands);
        editor.putString("groupBands", jsonGroupBands);

        editor.apply();
    }

    public void usingSelectedDates() {
        if (calendarManager.getSelectedStartDate() != null && calendarManager.getSelectedEndDate() != null) {
            Calendar selectedStartDate = calendarManager.getSelectedStartDate();
            Calendar selectedEndDate = calendarManager.getSelectedEndDate();
            date_s = "date_s=" + calendarManager.formatDate(selectedStartDate) + "&";
            date_e = "date_e=" + calendarManager.formatDate(selectedEndDate) + "&";
        }
    }

    private void generateSearchBands() {
        String prefix = date_s + date_e + teacherId + classroomId;
        searchBands.clear();
        if (!groupBands.isEmpty()) {
            for (String band : groupBands) {
                searchBands.add(prefix + band);
            }
        } else if (!teacherId.isEmpty() || !classroomId.isEmpty()) {
            searchBands.add(prefix);
        }
    }

    public void search() {
        touchableOff(progressBar);
        PresentationUtils.vibrate(MainActivity.this, 80);

        if (Objects.equals(teacherId, "") & Objects.equals(classroomId, "") & Objects.equals(groupName, "")) {
            runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Увага")
                    .setMessage("Для отримання необхідної Вам інформації" +
                            " заповніть хоча б одне з полів «Викладач», «Аудиторія» або «Група».")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .setIcon(R.drawable.search_alert)
                    .show());
            touchableOn(progressBar);
        } else {
            if (checkBox) {
                savePreferences();
            }
        }

        usingSelectedDates();
        generateSearchBands();
        DataUtils.saveSearchBands(this, searchBands);

        DataManager dataManager = new DataManager(getApplicationContext());
        catalogSchedules.clear();

        dataManager.getLessonsList(searchBands, new DataCallbackAllLessons() {
            @Override
            public void onDataLoaded(List<List<Lesson>> data) {
                catalogSchedules.clear();
                catalogSchedules.addAll(data);
                catalogSchedules = DataUtils.sortLessonsList(catalogSchedules);
                processData();
                touchableOn(progressBar);
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(MainActivity.this,
                        "Помилка: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                touchableOn(progressBar);
            }
        });
        autoLoad = false;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void processData() {
        int tabTextColor = ContextCompat.getColor(this, R.color.blue_darker);
        int tabTextSelectedColor = ContextCompat.getColor(this, R.color.black);

        boolean isDarkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkTheme) {
            tabTextColor = ContextCompat.getColor(this, R.color.blue_darker);
        }
        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        tabLayout.setVisibility(View.GONE);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(MainActivity.this, catalogSchedules);
        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();

        List<String> groupsNameTab = new ArrayList<>();
        if (catalogSchedules.size() > 1) {
            for (int i = 1; i < catalogSchedules.size(); i++) {
                List<Lesson> schedule = catalogSchedules.get(i);
                for (Lesson lesson : schedule) {
                    groupsNameTab.add(lesson.getGroup());
                    break;
                }
            }
            groupsNameTab.add(0, "ВСІ ГРУПИ");
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(groupsNameTab.get(position))).attach();
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setTabTextColors(tabTextColor, tabTextSelectedColor);
        }

        viewPager.setVisibility(View.VISIBLE);
        updateWidgetAndCalendar();
    }

    public void touchableOff(View progressBar) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void touchableOn(View progressBar) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void startBackgroundUpdate() {
        boolean autoUpdate = prefSet.getBoolean(KEY_UPDATE_ENABLED, false);
        int updateInterval = prefSet.getInt(KEY_UPDATE_INTERVAL, 24); // Default to 24 hours if not set

        WorkManager.getInstance(this).cancelAllWorkByTag("scheduleSync");
        WorkManager.getInstance(this).cancelAllWorkByTag("csit.puet.presentation.app_settings.ScheduleSync");

        if (autoUpdate) {
            // Define constraints for your work
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Require internet connection
                    .build();

            // Create a periodic work request
            WorkRequest dataSyncWorkRequest = new PeriodicWorkRequest.Builder(
                    ScheduleSync.class, updateInterval, TimeUnit.HOURS)
//                    ScheduleSync.class, 15, TimeUnit.MINUTES)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
//                    .setInitialDelay(10, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .addTag("scheduleSync")
                    .build();

            // Schedule the work
            WorkManager.getInstance(this).enqueue(dataSyncWorkRequest);
        }
    }

    private void updateWidgetAndCalendar() {
        if (catalogSchedules != null && !catalogSchedules.isEmpty() && !catalogSchedules.get(0).isEmpty()) {
            String scheduleDataForWidget = PresentationUtils.formatScheduleForWidget(this, catalogSchedules.get(0));
            sendScheduleDataToWidget(scheduleDataForWidget);

            GoogleCalendarHelper calendarHelper = new GoogleCalendarHelper(this, mCredential);
            calendarHelper.addLessonsToCalendar(catalogSchedules.get(0));
        }
    }

    private void removeWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this, ScheduleWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, null);
        }
    }

    public void sendScheduleDataToWidget(String scheduleData) {
        boolean isWidgetEnabled = prefSet.getBoolean(SettingsActivity.KEY_WIDGET_ENABLED, false);

        if (isWidgetEnabled) {
            Intent intent = new Intent(this, ScheduleWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra("SCHEDULE_DATA", scheduleData);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ScheduleWidgetProvider.class));

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            sendBroadcast(intent);
        }
    }
}
