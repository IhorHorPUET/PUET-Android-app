package csit.puet.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TeacherDao {
    @Query("SELECT * FROM teachers")
    List<TeacherEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TeacherEntity> teachers);

    @Query("DELETE FROM teachers")
    void deleteAll();
}
