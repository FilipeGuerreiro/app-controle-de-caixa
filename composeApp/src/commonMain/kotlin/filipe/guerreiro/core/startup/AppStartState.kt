package filipe.guerreiro.core.startup

sealed class AppStartState {
    object Loading: AppStartState()
    object NeedsUserCreation: AppStartState()
    object NeedsUserSelection: AppStartState()
    object Ready : AppStartState()
}