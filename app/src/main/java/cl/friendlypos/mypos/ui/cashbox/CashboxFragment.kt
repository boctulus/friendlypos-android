package cl.friendlypos.mypos.ui.cashbox

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
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.compose.screen.CashboxScreen
import cl.friendlypos.mypos.compose.viewmodel.CashboxViewModel

class CashboxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val viewModel: CashboxViewModel = viewModel()
                val currentSession by viewModel.currentSession.collectAsState()
                val stores by viewModel.stores.collectAsState()
                val cashboxes by viewModel.cashboxes.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val successMessage by viewModel.successMessage.collectAsState()

                val storeId = SessionManager.get(requireContext())?.storeId ?: ""

                CashboxScreen(
                    currentSession = currentSession,
                    stores = stores,
                    cashboxes = cashboxes,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    storeId = storeId,
                    onLoadCashboxes = { id -> viewModel.loadCashboxesForStore(id) },
                    onOpenSession = { sid, num, amt, notes ->
                        viewModel.openSession(sid, num, amt, notes)
                    },
                    onSessionOpened = {},
                    onCloseSession = { sessionId, amount, notes ->
                        viewModel.closeSession(sessionId, amount, notes)
                    },
                    onClearMessages = { viewModel.clearMessages() }
                )
            }
        }
    }
}
