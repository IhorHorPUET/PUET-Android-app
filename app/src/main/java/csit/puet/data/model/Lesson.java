package csit.puet.data.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Lesson implements Comparable<Lesson>, Parcelable {

    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private String date;
    private int num;
    private String lesson;
    private String lessonType;
    private String group;
    private String room;
    private String teacher;

    public Lesson(String date, int num, String lesson, String lessonType, String group, String room, String teacher) {
        this.date = date;
        this.num = num;
        this.lesson = lesson;
        this.lessonType = lessonType;
        this.group = group;
        this.room = room;
        this.teacher = teacher;
    }

    protected Lesson(Parcel in) {
        date = in.readString();
        num = in.readInt();
        lesson = in.readString();
        lessonType = in.readString();
        group = in.readString();
        room = in.readString();
        teacher = in.readString();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getLesson() {
        return lesson;
    }

    public void setLesson(String lesson) {
        this.lesson = lesson;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @Override
    public int compareTo(Lesson other) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        try {
            LocalDate thisDate = LocalDate.parse(this.date, formatter);
            LocalDate otherDate = LocalDate.parse(other.date, formatter);
            return thisDate.compareTo(otherDate);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeInt(num);
        dest.writeString(lesson);
        dest.writeString(lessonType);
        dest.writeString(group);
        dest.writeString(room);
        dest.writeString(teacher);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lesson lesson1 = (Lesson) o;
        return num == lesson1.num &&
                Objects.equals(date, lesson1.date) &&
                Objects.equals(lesson, lesson1.lesson) &&
                Objects.equals(lessonType, lesson1.lessonType) &&
                Objects.equals(group, lesson1.group) &&
                Objects.equals(room, lesson1.room) &&
                Objects.equals(teacher, lesson1.teacher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, num, lesson, lessonType, group, room, teacher);
    }
}
