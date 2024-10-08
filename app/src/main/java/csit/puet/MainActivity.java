package csit.puet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import csit.puet.data.DataCallback;
import csit.puet.data.DataCallbackAllLessons;
import csit.puet.data.DataManager;
import csit.puet.data.DataUtils;
import csit.puet.data.model.Classroom;
import csit.puet.data.model.Group;
import csit.puet.data.model.Lesson;
import csit.puet.data.model.Teacher;
import csit.puet.presentation.app_settings.ScheduleSync;
import csit.puet.presentation.app_settings.SettingsActivity;
import csit.puet.presentation.google_calendar.GoogleCalendarHelper;
import csit.puet.presentation.google_calendar.GoogleCalendarUtils;
import csit.puet.presentation.ui.AdapterClassroomName;
import csit.puet.presentation.ui.AdapterGroupName;
import csit.puet.presentation.ui.AdapterTeacherName;
import csit.puet.presentation.ui.CalendarManager;
import csit.puet.presentation.ui.PresentationUtils;
import csit.puet.presentation.ui.ViewPagerAdapter;
import csit.puet.presentation.widget.WidgetUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefData;

    private ProgressBar progressBar;
    private CalendarManager calendarManager;
    TextView startDateTextView;
    TextView endDateTextView;
    AutoCompleteTextView teacherNameAutoComplete;
    AutoCompleteTextView classroomNameAutoComplete;
    AutoCompleteTextView groupNameAutoComplete;
    int maxVisibleZone = 8;

    List<String> groupBands = new ArrayList<>();
    List<String> searchBands = new ArrayList<>();
    ArrayList<String> savedGroupBands;
    List<List<Lesson>> catalogSchedules = new ArrayList<>();
    String start_date = "";
    String end_date = "";
    String teacherName = "";
    String classroomName = "";
    String groupName = "";
    String teacherId = "";
    String classroomId = "";

    private boolean autoLoadSchedules = true;
    private boolean searchParametersCheckBox = true;

    private ActivityResultLauncher<Intent> settingsActivityLauncher;
    SharedPreferences prefSet;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PresentationUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.root_layout);
        View focusCatcher = findViewById(R.id.focus_catcher);
        focusCatcher.requestFocus();

        rootView.setOnTouchListener((v, event) -> {
            PresentationUtils.hideKeyboard(MainActivity.this);
            teacherNameAutoComplete.clearFocus();
            classroomNameAutoComplete.clearFocus();
            groupNameAutoComplete.clearFocus();
            return false;
        });

        prefSet = this.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        prefData = this.getSharedPreferences(AppConstants.PREF_DATA, Context.MODE_PRIVATE);

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

        String savedTeacherName = prefData.getString(AppConstants.KEY_TEACHER_NAME, "");
        String savedTeacherId = prefData.getString(AppConstants.KEY_TEACHER_ID, "");
        String savedClassroomName = prefData.getString(AppConstants.KEY_CLASSROOM_NAME, "");
        String savedClassroomId = prefData.getString(AppConstants.KEY_CLASSROOM_ID, "");
        String savedGroupName = prefData.getString(AppConstants.KEY_GROUP_NAME, "");

        Gson gson = new Gson();
        String jsonGroupBands = prefData.getString(AppConstants.KEY_GROUP_BANDS, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        savedGroupBands = gson.fromJson(jsonGroupBands, type);

        DataManager dataManager = new DataManager(getApplicationContext());

        if (savedGroupBands == null) {
            savedGroupBands = new ArrayList<>();
        }

        ImageView puetLogo = findViewById(R.id.puet_logo);
        puetLogo.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("")
                    .setMessage("Перейти на сайт http://puet.edu.ua")
                    .setPositiveButton("Так", (dialog, which) -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://puet.edu.ua/"));
                        startActivity(browserIntent);
                    })
                    .setNegativeButton("Ні", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("")
                    .setMessage("Перейти на сайт https://puetapp.online")
                    .setPositiveButton("Так", (dialog, which) -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://puetapp.online/"));
                        startActivity(browserIntent);
                    })
                    .setNegativeButton("Ні", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        TextView privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyPolicy.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage("Ознайомитися з політикою конфіденційності?")
                    .setPositiveButton("Так", (dialog, which) -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://puetapp.online/privacy-ua.html"));
                        startActivity(browserIntent);
                    })
                    .setNegativeButton("Ні", (dialog, which) -> dialog.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        });

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            PresentationUtils.vibrate(MainActivity.this, 20);
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

                    teacherNameAutoComplete = findViewById(R.id.teachersNameAutoComplete);
                    teacherNameAutoComplete.setDropDownHeight(
                            PresentationUtils.calculateDropdownHeight(teacherNameAutoComplete, maxVisibleZone));
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

                    classroomNameAutoComplete = findViewById(R.id.classroomsNameAutoComplete);
                    classroomNameAutoComplete.setDropDownHeight(
                            PresentationUtils.calculateDropdownHeight(classroomNameAutoComplete, maxVisibleZone));
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

                    groupNameAutoComplete = findViewById(R.id.groupsNameAutoComplete);
                    groupNameAutoComplete.setDropDownHeight(
                            PresentationUtils.calculateDropdownHeight(groupNameAutoComplete, maxVisibleZone));
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

                    if (autoLoadSchedules) {
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
        rememberSelectionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> searchParametersCheckBox = isChecked);


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
    protected void onStart() {
        super.onStart();
        View focusCatcher = findViewById(R.id.focus_catcher);
        focusCatcher.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        WidgetUtils.updateWidget(this, prefSet);
        GoogleCalendarUtils.handleGoogleCalendarSettings(this, prefSet);
        View focusCatcher = findViewById(R.id.focus_catcher);
        focusCatcher.requestFocus();
    }

    private void applyTheme() {
        int theme = prefSet.getInt(AppConstants.KEY_THEME, AppConstants.THEME_SYSTEM);
        switch (theme) {
            case AppConstants.THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case AppConstants.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case AppConstants.THEME_DARK:
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
            start_date = "date_s=" + calendarManager.formatDate(selectedStartDate) + "&";
            end_date = "date_e=" + calendarManager.formatDate(selectedEndDate) + "&";
        }
    }

    private void generateSearchBands() {
        String prefix = start_date + end_date + teacherId + classroomId;
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
        PresentationUtils.vibrate(MainActivity.this, 40);

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
            if (searchParametersCheckBox) {
                savePreferences();
            }
            usingSelectedDates();
            generateSearchBands();
            DataUtils.saveSearchBands(this, searchBands);
            DataManager dataManager = new DataManager(getApplicationContext());

            dataManager.getLessonsList(searchBands, new DataCallbackAllLessons() {
                @Override
                public void onDataLoaded(List<List<Lesson>> data) {
                    catalogSchedules.clear();
                    catalogSchedules.addAll(data);
                    catalogSchedules = DataUtils.sortLessonsList(catalogSchedules);
                    processData();
                    touchableOn(progressBar);

                    if (catalogSchedules != null && !catalogSchedules.isEmpty() && !catalogSchedules.get(0).isEmpty()) {
                        SharedPreferences.Editor editor = prefSet.edit();
                        Gson gson = new Gson();
                        String newLessonsJson = gson.toJson(catalogSchedules.get(0));
                        editor.putString("newLessonsFirst", newLessonsJson);
                        editor.apply();

                        WidgetUtils.updateWidget(MainActivity.this, prefSet);
                        GoogleCalendarHelper calendarHelper = new GoogleCalendarHelper(MainActivity.this, prefSet);
                        calendarHelper.addLessonsToCalendar();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(MainActivity.this,
                            "Помилка: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    touchableOn(progressBar);
                }
            });
        }
        autoLoadSchedules = false;
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
    }

    public void touchableOn(View progressBar) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void touchableOff(View progressBar) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void startBackgroundUpdate() {
        boolean autoUpdate = prefSet.getBoolean(AppConstants.KEY_AUTO_UPDATE_ENABLED, false);
        int updateInterval = prefSet.getInt(AppConstants.KEY_AUTO_UPDATE_INTERVAL, 24); // Default to 24 hours if not set

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
//                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                    .setInitialDelay(20, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .addTag("scheduleSync")
                    .build();

            // Schedule the work
            WorkManager.getInstance(this).enqueue(dataSyncWorkRequest);
        }
    }
}
