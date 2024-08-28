package csit.puet.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM groups")
    List<GroupEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GroupEntity> groups);

    @Query("DELETE FROM groups")
    void deleteAll();
}
