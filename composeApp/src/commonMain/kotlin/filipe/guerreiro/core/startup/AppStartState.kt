package filipe.guerreiro.core.startup

sealed class AppStartState {
    object Loading: AppStartState()
    object NeedsOnboarding: AppStartState()
    object Ready : AppStartState()
}