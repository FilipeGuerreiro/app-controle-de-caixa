package filipe.guerreiro.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "app_preferences"
)

class AndroidUserPreferences (
    private val context: Context
) : UserPreferences {
    private val USER_REGISTERED = booleanPreferencesKey("user_registered")

    override suspend fun isUserRegistered(): Boolean {
        return context.dataStore.data
            .map { prefs -> prefs[USER_REGISTERED] ?: false }
            .first()
    }

    override suspend fun setUserRegistered(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USER_REGISTERED] = value
        }
    }
}