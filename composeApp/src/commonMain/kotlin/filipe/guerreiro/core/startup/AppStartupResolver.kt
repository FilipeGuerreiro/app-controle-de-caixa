package filipe.guerreiro.core.startup

interface AppStartupResolver {
    suspend fun resolveStartState(): AppStartState
}