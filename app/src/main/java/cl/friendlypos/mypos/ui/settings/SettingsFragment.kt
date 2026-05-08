package cl.friendlypos.mypos.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.friendlypos.mypos.compose.screen.SettingsScreen
import cl.friendlypos.mypos.compose.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val viewModel: SettingsViewModel = viewModel()
                val isLoading by viewModel.isLoading.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val successMessage by viewModel.successMessage.collectAsState()

                SettingsScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    onResetPassword = { email, password -> viewModel.resetPassword(email, password) },
                    onClearMessages = { viewModel.clearMessages() }
                )
            }
        }
    }
}
