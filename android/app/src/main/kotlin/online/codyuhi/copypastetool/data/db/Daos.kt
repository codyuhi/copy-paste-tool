package online.codyuhi.copypastetool.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CopyPasteDao {
    @Query("SELECT * FROM sections ORDER BY sortOrder ASC, id ASC")
    fun observeSections(): Flow<List<SectionEntity>>

    @Query("SELECT * FROM buttons ORDER BY sortOrder ASC, id ASC")
    fun observeButtons(): Flow<List<ButtonEntity>>

    @Query("SELECT * FROM buttons WHERE sectionId = :sectionId ORDER BY sortOrder ASC, id ASC")
    fun observeButtonsForSection(sectionId: Long): Flow<List<ButtonEntity>>

    @Query("SELECT * FROM buttons WHERE isFavorite = 1 ORDER BY sortOrder ASC, id ASC")
    fun observeFavorites(): Flow<List<ButtonEntity>>

    @Query("SELECT * FROM sections ORDER BY sortOrder ASC, id ASC")
    suspend fun getSections(): List<SectionEntity>

    @Query("SELECT * FROM buttons ORDER BY sortOrder ASC, id ASC")
    suspend fun getButtons(): List<ButtonEntity>

    @Query("SELECT * FROM sections WHERE id = :id LIMIT 1")
    suspend fun getSectionById(id: Long): SectionEntity?

    @Query("SELECT * FROM buttons WHERE id = :id LIMIT 1")
    suspend fun getButtonById(id: Long): ButtonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>): List<Long>

    @Update
    suspend fun updateSection(section: SectionEntity)

    @Query("DELETE FROM sections WHERE id = :sectionId")
    suspend fun deleteSection(sectionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: ButtonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButtons(buttons: List<ButtonEntity>): List<Long>

    @Update
    suspend fun updateButton(button: ButtonEntity)

    @Query("DELETE FROM buttons WHERE id = :buttonId")
    suspend fun deleteButton(buttonId: Long)

    @Query("UPDATE buttons SET isFavorite = :isFavorite WHERE id = :buttonId")
    suspend fun setFavorite(buttonId: Long, isFavorite: Boolean)

    @Query("DELETE FROM sections")
    suspend fun clearSections()

    @Query("DELETE FROM buttons")
    suspend fun clearButtons()

    @Transaction
    suspend fun replaceAll(sectionsWithButtons: List<Pair<SectionEntity, List<ButtonEntity>>>) {
        clearButtons()
        clearSections()
        for ((sec, btns) in sectionsWithButtons) {
            val secId = insertSection(sec)
            val updatedBtns = btns.map { it.copy(sectionId = secId) }
            insertButtons(updatedBtns)
        }
    }
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    fun observePending(): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    suspend fun getPending(): List<OutboxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outbox: OutboxEntity): Long

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM outbox")
    suspend fun clearAll()
}
