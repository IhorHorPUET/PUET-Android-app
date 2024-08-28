package csit.puet.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "teachers")
public class TeacherEntity {

    @PrimaryKey
    private int idPrep;
    private String name;

    public TeacherEntity(int idPrep, String name) {
        this.idPrep = idPrep;
        this.name = name;
    }

    public int getIdPrep() {
        return idPrep;
    }

    public void setIdPrep(int idPrep) {
        this.idPrep = idPrep;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}