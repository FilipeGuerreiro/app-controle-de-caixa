package filipe.guerreiro.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val businessName: String,
    val remoteId: String? = null // chave para um possível id remoto
)