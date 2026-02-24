package filipe.guerreiro.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import filipe.guerreiro.data.SessionPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "session_preferences"
)

class AndroidSessionPreferences(
    private val context: Context
) : SessionPreferences {

    private val LOGGED_USER_ID = longPreferencesKey("logged_user_id")
    private val DEFAULT_DAILY_GOAL = longPreferencesKey("default_daily_goal")

    override suspend fun getLoggedUserId(): Long? {
        val result = context.dataStore.data
            .map { prefs -> prefs[LOGGED_USER_ID] }
            .first()
        return result
    }

    override suspend fun setLoggedUserId(userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_USER_ID] = userId
        }
    }

    override suspend fun getDefaultDailyGoal(): Long? {
        return context.dataStore.data
            .map { prefs -> prefs[DEFAULT_DAILY_GOAL] }
            .first()
    }

    override suspend fun setDefaultDailyGoal(amount: Long) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_DAILY_GOAL] = amount
        }
    }

    override suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(LOGGED_USER_ID)
            prefs.remove(DEFAULT_DAILY_GOAL)
        }
    }
}