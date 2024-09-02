package csit.puet.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import csit.puet.AppConstants;
import csit.puet.data.database.ClassroomEntity;
import csit.puet.data.database.GroupDao;
import csit.puet.data.database.GroupEntity;
import csit.puet.data.database.LessonsConverter;
import csit.puet.data.database.LessonsDao;
import csit.puet.data.database.LessonsEntity;
import csit.puet.data.database.ClassroomDao;
import csit.puet.data.database.TeacherDao;
import csit.puet.data.database.TeacherEntity;
import csit.puet.data.model.Group;
import csit.puet.data.model.Lesson;
import csit.puet.data.model.Classroom;
import csit.puet.data.model.Teacher;
import csit.puet.data.network.ServerDataSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DataRepository {

    Context context;
    String timestamp;
    private final ServerDataSource serverDataSource;
    private final TeacherDao teacherDao;
    private final ClassroomDao classroomDao;
    private final GroupDao groupDao;
    private final LessonsDao lessonsDao;
    private final Executor executor;

    public DataRepository(Context context, ServerDataSource serverDataSource, TeacherDao teacherDao,
                          ClassroomDao classroomDao, GroupDao groupDao, LessonsDao lessonsDao) {
        this.context = context;
        this.serverDataSource = serverDataSource;
        this.teacherDao = teacherDao;
        this.classroomDao = classroomDao;
        this.groupDao = groupDao;
        this.lessonsDao = lessonsDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    Handler mainHandler = new Handler(Looper.getMainLooper());

    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }


    //============  getListFromDataRepository  ===============
    public void getTeachersListFromDataRepository(final DataCallback<List<Teacher>> callback) {
        serverDataSource.getTeachersServerList(new DataCallback<List<Teacher>>() {
            @Override
            public void onDataLoaded(List<Teacher> data) {
                executor.execute(() -> {
                    if (!data.isEmpty()) {
                        callback.onDataLoaded(data);
                        saveTeachersList(data);
                    } else {
                        List<TeacherEntity> teacherEntities = teacherDao.getAll();
                        List<Teacher> teachers = convertTeacherEntitiesToList(teacherEntities);
                        callback.onDataLoaded(teachers);
                        showToast("Список викладачів отриманий з локальної бази даних");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                showToast("Відсутні дані на сервері та в локальній базі даних");
            }
        });
    }

    public void getClassroomsListFromDataRepository(final DataCallback<List<Classroom>> callback) {
        serverDataSource.getClassroomsServerList(new DataCallback<List<Classroom>>() {
            @Override
            public void onDataLoaded(List<Classroom> data) {
                executor.execute(() -> {
                    if (!data.isEmpty()) {
                        callback.onDataLoaded(data);
                        saveRoomsList(data);
                    } else {
                        List<ClassroomEntity> roomEntities = classroomDao.getAll();
                        List<Classroom> classrooms = convertClassroomEntitiesToList(roomEntities);
                        callback.onDataLoaded(classrooms);
                        showToast("Список аудиторій отриманий з локальної бази даних");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                showToast("Відсутні дані на сервері та в локальній базі даних");
            }
        });
    }

    public void getGroupsListFromDataRepository(final DataCallback<List<Group>> callback) {
        serverDataSource.getGroupsServerList(new DataCallback<List<Group>>() {
            @Override
            public void onDataLoaded(List<Group> data) {
                executor.execute(() -> {
                    if (!data.isEmpty()) {
                        callback.onDataLoaded(data);
                        saveGroupsList(data);
                    } else {
                        List<GroupEntity> groupEntities = groupDao.getAll();
                        List<Group> groups = convertGroupEntitiesToList(groupEntities);
                        callback.onDataLoaded(groups);
                        showToast("Список груп отриманий з локальної бази даних");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                showToast("Відсутні дані на сервері та в локальній базі даних");
            }
        });
    }


    //============  saveListToDatabase  ===============
    public void saveTeachersList(List<Teacher> teacherList) {
        teacherDao.deleteAll();
        DataUtils.saveListToDatabase(executor, teacherList, teacher ->
                new TeacherEntity(teacher.getIdPrep(), teacher.getTeacherName()), teacherDao::insertAll);
    }

    public void saveRoomsList(List<Classroom> classroomList) {
        classroomDao.deleteAll();
        DataUtils.saveListToDatabase(executor, classroomList, room ->
                new ClassroomEntity(room.getId(), room.getClassroomName()), classroomDao::insertAll);
    }

    public void saveGroupsList(List<Group> groupList) {
        groupDao.deleteAll();
        DataUtils.saveListToDatabase(executor, groupList, group ->
                new GroupEntity(group.getId(), group.getCourse(), group.getSpecId(), group.getForma(),
                        group.getOwner(), group.getName(), group.getNum()), groupDao::insertAll);
    }

    //============  convertEntitiesFromDatabaseToList  ===============
    private List<Teacher> convertTeacherEntitiesToList(List<TeacherEntity> teacherEntities) {
        if (teacherEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Teacher> teacherList = new ArrayList<>();
        for (TeacherEntity entity : teacherEntities) {
            Teacher teacher = new Teacher();
            teacher.setIdPrep(entity.getIdPrep());
            teacher.setName(entity.getName());
            teacherList.add(teacher);
        }
        teacherList.sort(new Comparator<Teacher>() {
            @Override
            public int compare(Teacher t1, Teacher t2) {
                return t1.getTeacherName().compareToIgnoreCase(t2.getTeacherName());
            }
        });
        return teacherList;
    }

    private List<Classroom> convertClassroomEntitiesToList(List<ClassroomEntity> classroomEntities) {
        if (classroomEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Classroom> classroomList = new ArrayList<>();
        for (ClassroomEntity entity : classroomEntities) {
            Classroom classroom = new Classroom();
            classroom.setId(entity.getId());
            classroom.setName(entity.getName());
            classroomList.add(classroom);
        }
        classroomList.sort(new Comparator<Classroom>() {
            @Override
            public int compare(Classroom r1, Classroom r2) {
                return r1.getClassroomName().compareToIgnoreCase(r2.getClassroomName());
            }
        });
        return classroomList;
    }

    private List<Group> convertGroupEntitiesToList(List<GroupEntity> groupEntities) {
        if (groupEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Group> groupList = new ArrayList<>();
        for (GroupEntity entity : groupEntities) {
            Group group = new Group();
            group.setCourse(entity.getCourse());
            group.setSpecId(entity.getSpecId());
            group.setForma(entity.getForma());
            group.setOwner(entity.getOwner());
            group.setName(entity.getName());
            group.setNum(entity.getNum());
            groupList.add(group);
        }
        groupList.sort(new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.getName().compareToIgnoreCase(g2.getName());
            }
        });
        return groupList;
    }


    //============  getLessons  ===============
    public void getLessonsListFromDataRepository(List<String> groupBands, DataCallbackAllLessons callback) {
        List<List<Lesson>> allLessonsList = Collections.synchronizedList(new ArrayList<>());
        List<Lesson> allLessonsSum = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger counter = new AtomicInteger(groupBands.size());

        for (String band : groupBands) {
            serverDataSource.getLessonsServerList(band, new DataCallback<List<Lesson>>() {
                @Override
                public void onDataLoaded(List<Lesson> data) {
                    if (!data.isEmpty()) {
                        synchronized (allLessonsList) {
                            allLessonsList.add(data);
                            synchronized (allLessonsSum) {
                                allLessonsSum.addAll(data);
                            }
                        }
                    }

                    if (counter.decrementAndGet() == 0) {
                        if (allLessonsList.size() > 1) {
                            allLessonsList.add(0, allLessonsSum);
                        }
                        if (!allLessonsList.isEmpty()) {
                            callback.onDataLoaded(allLessonsList);
                            executor.execute(() -> saveLessonsList(DataUtils.sortLessonsList(allLessonsList)));
                            saveLastSyncTime();
                        } else {
                            executor.execute(() -> {
                                List<List<Lesson>> lessonsFromDb = convertLessonsEntitiesToList();
                                if (!lessonsFromDb.isEmpty()) {
                                    mainHandler.post(() -> {
                                        callback.onDataLoaded(lessonsFromDb);
                                        showToast("Розклад отриманий з локальної бази даних");
                                    });
                                } else {
                                    mainHandler.post(() -> {
                                        showToast("Відсутні дані на сервері та в локальній базі даних");
                                        callback.onDataLoaded(new ArrayList<>());
                                    });
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    showToast("Помилковий запит до сервера");
                    if (counter.decrementAndGet() == 0) {
                        executor.execute(() -> {
                            List<List<Lesson>> lessonsFromDb = convertLessonsEntitiesToList();
                            if (!lessonsFromDb.isEmpty()) {
                                mainHandler.post(() -> {
                                    callback.onDataLoaded(lessonsFromDb);
                                    showToast("Розклад отриманий з локальної бази даних");
                                });
                            } else {
                                mainHandler.post(() -> {
                                    showToast("Відсутні дані на сервері та в локальній базі даних");
                                    callback.onDataLoaded(new ArrayList<>());
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    void saveLessonsList(List<List<Lesson>> allLessonsList) {
        String lessonsJson = LessonsConverter.lessonsListToString(allLessonsList);
        LessonsEntity lessonsEntity = new LessonsEntity(lessonsJson);
        lessonsDao.deleteAll();
        lessonsDao.insertOrUpdate(lessonsEntity);
    }

    public List<List<Lesson>> convertLessonsEntitiesToList() {
        List<LessonsEntity> lessonsEntities = lessonsDao.getAll();
        if (lessonsEntities.isEmpty()) {
            return new ArrayList<>();
        }

        LessonsEntity lessonsEntity = lessonsEntities.get(0);
        if (lessonsEntity == null) {
            return new ArrayList<>();
        }

        timestamp = lessonsEntity.getTimestamp();
        String lessonsJson = LessonsConverter.lessonsListToString(lessonsEntity.getAllLessons());
        return LessonsConverter.stringToLessonsList(lessonsJson);
    }

    private void saveLastSyncTime() {
        SharedPreferences prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefSet.edit();

        String currentTime = new SimpleDateFormat(" HH:mm dd-MM-yyyy", Locale.getDefault()).format(new Date());
        editor.putString(AppConstants.KEY_LAST_SYNC_TIME, currentTime);
        editor.apply();
    }
}
