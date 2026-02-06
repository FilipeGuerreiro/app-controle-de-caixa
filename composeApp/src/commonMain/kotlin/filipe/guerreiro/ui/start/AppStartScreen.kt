package filipe.guerreiro.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import filipe.guerreiro.core.navigation.NavigateOnce
import filipe.guerreiro.core.startup.AppStartState
import filipe.guerreiro.core.startup.AppViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppStartScreen(
    navController: NavController,
    viewModel: AppViewModel = koinViewModel()
) {
    val state by viewModel.startState.collectAsState()

    when (state) {
        AppStartState.Loading -> LoadingContent()
        AppStartState.Ready -> NavigateOnce(navController, "home")
        AppStartState.NeedsOnboarding -> NavigateOnce(navController, "register")
    }
}