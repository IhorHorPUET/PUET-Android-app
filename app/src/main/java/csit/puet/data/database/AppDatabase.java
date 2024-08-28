package csit.puet.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TeacherEntity.class, ClassroomEntity.class, GroupEntity.class, LessonsEntity.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TeacherDao teacherDao();

    public abstract ClassroomDao classroomDao();

    public abstract GroupDao groupDao();

    public abstract LessonsDao lessonsDao();

    private static final String DATABASE = "puet-database";
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE
                            )
                            .fallbackToDestructiveMigration() // !!!!!   DestructiveMigration    !!!!!!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
