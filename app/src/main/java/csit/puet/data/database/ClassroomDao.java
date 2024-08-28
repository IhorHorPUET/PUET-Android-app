package csit.puet.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClassroomDao {
    @Query("SELECT * FROM classrooms")
    List<ClassroomEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ClassroomEntity> classrooms);

    @Query("DELETE FROM classrooms")
    void deleteAll();
}