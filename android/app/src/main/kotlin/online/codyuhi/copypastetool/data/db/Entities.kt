package online.codyuhi.copypastetool.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sections",
    indices = [Index(value = ["name"], unique = false)]
)
data class SectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "buttons",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sectionId"])]
)
data class ButtonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sectionId: Long,
    val buttonName: String,
    val pasteValue: String,
    val sortOrder: Int = 0,
    val isFavorite: Boolean = false,
)

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String, // "PUT_DATA"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
)
