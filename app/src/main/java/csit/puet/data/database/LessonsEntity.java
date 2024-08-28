package csit.puet.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import csit.puet.data.model.Lesson;

import java.util.List;
import androidx.room.ColumnInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity(tableName = "lessons")
public class LessonsEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @TypeConverters(LessonsConverter.class)
    private List<List<Lesson>> allLessons;

    public LessonsEntity(List<List<Lesson>> allLessons) {
        this.allLessons = allLessons;
        this.timestamp = generateTimestamp();
    }

    public LessonsEntity(String lessonsJson) {
        this.allLessons = LessonsConverter.stringToLessonsList(lessonsJson);
        this.timestamp = generateTimestamp();
    }

    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@NonNull String timestamp) {
        this.timestamp = timestamp;
    }

    public List<List<Lesson>> getAllLessons() {
        return allLessons;
    }

    public void setAllLessons(List<List<Lesson>> allLessons) {
        this.allLessons = allLessons;
    }

    private String generateTimestamp() {
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC+2"));
        return currentTime.toString();
    }
}
