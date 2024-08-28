package csit.puet.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class GroupEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int course;
    private int specId;
    private int forma;
    private int owner;
    private String name;
    private int num;

    public GroupEntity(int id, int course, int specId, int forma, int owner, String name, int num) {
        this.id = id;
        this.course = course;
        this.specId = specId;
        this.forma = forma;
        this.owner = owner;
        this.name = name;
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public int getSpecId() {
        return specId;
    }

    public void setSpecId(int specId) {
        this.specId = specId;
    }

    public int getForma() {
        return forma;
    }

    public void setForma(int forma) {
        this.forma = forma;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @NonNull
    @Override
    public String toString() {
        return "GroupEntity{" +
                "course=" + course +
                ", specId=" + specId +
                ", forma=" + forma +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                ", num=" + num +
                '}';
    }
}