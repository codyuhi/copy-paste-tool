package online.codyuhi.copypastetool.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SectionEntity::class,
        ButtonEntity::class,
        OutboxEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun copyPasteDao(): CopyPasteDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "copypaste.db"
                ).build().also { INSTANCE = it }
            }
    }
}
