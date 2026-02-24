package filipe.guerreiro.session

import filipe.guerreiro.data.SessionPreferences
import platform.Foundation.NSUserDefaults

class IosSessionPreferences: SessionPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val KEY = "logged_user_id"

    override suspend fun getLoggedUserId(): Long? {
        return if (defaults.objectForKey(KEY) != null) {
            defaults.integerForKey(KEY)
        } else {
            null
        }
    }

    override suspend fun setLoggedUserId(userId: Long) {
        defaults.setInteger(userId, KEY)
    }

    private val GOAL_KEY = "default_daily_goal"

    override suspend fun getDefaultDailyGoal(): Long? {
        return if (defaults.objectForKey(GOAL_KEY) != null) {
            defaults.integerForKey(GOAL_KEY)
        } else {
            null
        }
    }

    override suspend fun setDefaultDailyGoal(amount: Long) {
        defaults.setInteger(amount, GOAL_KEY)
    }

    override suspend fun clearSession() {
        defaults.removeObjectForKey(KEY)
        defaults.removeObjectForKey(GOAL_KEY)
    }
}