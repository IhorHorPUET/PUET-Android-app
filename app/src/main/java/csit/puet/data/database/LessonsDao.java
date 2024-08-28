package csit.puet.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LessonsDao {

    @Query("SELECT * FROM lessons")
    List<LessonsEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(LessonsEntity lessons);

    @Update
    void update(LessonsEntity lessons);

    @Query("DELETE FROM lessons")
    void deleteAll();
}