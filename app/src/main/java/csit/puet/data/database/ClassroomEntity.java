package csit.puet.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "classrooms")
public class ClassroomEntity {
    @PrimaryKey
    private int id;
    private String name;

    public ClassroomEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}