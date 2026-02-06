package filipe.guerreiro.data

import platform.Foundation.NSUserDefaults

class IosUserPreferences : UserPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun isUserRegistered(): Boolean {
        return defaults.boolForKey("user_registered")
    }

    override suspend fun setUserRegistered(value: Boolean) {
        defaults.setBool(value, "user_registered")
    }
}