package filipe.guerreiro.core.startup

import kotlinx.coroutines.delay

class MockStartupResolver : AppStartupResolver {

    override suspend fun resolveStartState(): AppStartState {
        delay(1500)

        val userAlreadyRegistered = true

        return if (userAlreadyRegistered) {
            AppStartState.Ready
        } else {
            AppStartState.NeedsOnboarding
        }
    }
}