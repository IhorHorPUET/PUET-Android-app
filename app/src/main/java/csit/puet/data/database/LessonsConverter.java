package csit.puet.data.database;

import androidx.room.TypeConverter;
import csit.puet.data.model.Lesson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class LessonsConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String lessonsListToString(List<List<Lesson>> allLessons) {
        synchronized (allLessons) {
            List<List<Lesson>> copyOfAllLessons = new ArrayList<>();
            for (List<Lesson> lessons : allLessons) {
                copyOfAllLessons.add(new ArrayList<>(lessons));
            }
            return gson.toJson(copyOfAllLessons);
        }
    }

    @TypeConverter
    public static List<List<Lesson>> stringToLessonsList(String value) {
        return gson.fromJson(value, new TypeToken<List<List<Lesson>>>() {}.getType());
    }
}
