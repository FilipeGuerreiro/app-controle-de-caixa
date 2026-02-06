package filipe.guerreiro.core.startup

import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.ui.start.AppStartScreen
import kotlinx.coroutines.delay

class StartupResolver(
    private val userPreferences: UserPreferences
) : AppStartupResolver {

    override suspend fun resolveStartState(): AppStartState {

        delay(5000)

        return if (userPreferences.isUserRegistered()) {
            AppStartState.Ready
        } else {
            AppStartState.NeedsOnboarding
        }
    }
}