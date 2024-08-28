package csit.puet.data;

import android.content.Context;

import csit.puet.data.database.AppDatabase;
import csit.puet.data.database.GroupDao;
import csit.puet.data.database.LessonsDao;
import csit.puet.data.database.ClassroomDao;
import csit.puet.data.database.TeacherDao;
import csit.puet.data.model.Group;
import csit.puet.data.model.Classroom;
import csit.puet.data.model.Teacher;
import csit.puet.data.model.Lesson;
import csit.puet.data.network.ServerDataSource;

import java.util.List;

public class DataManager {
    private DataRepository dataRepository;
    private final Context context;

    public DataManager(Context context) {
        this.context = context;
        initRepository();
    }

    private void initRepository() {
        ServerDataSource serverDataSource = new ServerDataSource();
        TeacherDao teacherDao = AppDatabase.getInstance(context).teacherDao();
        ClassroomDao classroomDao = AppDatabase.getInstance(context).classroomDao();
        GroupDao groupDao = AppDatabase.getInstance(context).groupDao();
        LessonsDao lessonsDao = AppDatabase.getInstance(context).lessonsDao();
        dataRepository = new DataRepository(context, serverDataSource, teacherDao, classroomDao, groupDao, lessonsDao);
    }

    public void getTeachersList(DataCallback<List<Teacher>> callback) {
        dataRepository.getTeachersListFromDataRepository(callback);
    }

    public void getRoomsList(DataCallback<List<Classroom>> callback) {
        dataRepository.getClassroomsListFromDataRepository(callback);
    }

    public void getGroupsList(DataCallback<List<Group>> callback) {
        dataRepository.getGroupsListFromDataRepository(callback);
    }

    public void getLessonsList(List<String> groupBands, DataCallbackAllLessons callback) {
        dataRepository.getLessonsListFromDataRepository(groupBands, callback);
    }

    public List<List<Lesson>> getLessonsListFromDatabase() {
        return dataRepository.convertLessonsEntitiesToList();
    }

    public void saveLessonsListToDatabase(List<List<Lesson>> lessonsList) {
        if (lessonsList == null || lessonsList.isEmpty()) {
            return;
        }
        dataRepository.saveLessonsList(lessonsList);
    }
}
