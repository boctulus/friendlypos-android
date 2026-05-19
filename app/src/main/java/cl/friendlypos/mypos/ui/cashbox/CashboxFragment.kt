package cl.friendlypos.mypos.ui.cashbox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.LoginActivity
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.compose.screen.CashboxScreen
import cl.friendlypos.mypos.compose.viewmodel.CashboxViewModel

class CashboxFragment : Fragment() {

    private val cashboxViewModel: CashboxViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val currentSession by cashboxViewModel.currentSession.collectAsState()
                val availability by cashboxViewModel.availability.collectAsState()
                val isLoading by cashboxViewModel.isLoading.collectAsState()
                val isLoadingAvailability by cashboxViewModel.isLoadingAvailability.collectAsState()
                val isLoadingMovementTypes by cashboxViewModel.isLoadingMovementTypes.collectAsState()
                val movementTypes by cashboxViewModel.movementTypes.collectAsState()
                val errorMessage by cashboxViewModel.errorMessage.collectAsState()
                val successMessage by cashboxViewModel.successMessage.collectAsState()

                val storeId = SessionManager.get(requireContext())?.storeId ?: ""
                val role = SessionManager.getRole(requireContext())
                val context = requireContext()

                LaunchedEffect(storeId) {
                    if (storeId.isNotBlank()) {
                        cashboxViewModel.loadAvailability(storeId)
                    }
                }

                LaunchedEffect(successMessage) {
                    if (successMessage != null) {
                        when (role) {
                            "supermarket" -> {
                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                cashboxViewModel.clearMessages()
                                if (storeId.isNotBlank()) cashboxViewModel.loadAvailability(storeId)
                            }
                        }
                    }
                }

                CashboxScreen(
                    role = role,
                    currentSession = currentSession,
                    availability = availability,
                    isLoading = isLoading,
                    isLoadingAvailability = isLoadingAvailability,
                    isLoadingMovementTypes = isLoadingMovementTypes,
                    movementTypes = movementTypes,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    onOpenSession = { cashboxId, cashboxLabel, amount, notes ->
                        cashboxViewModel.openSession(storeId, cashboxId, cashboxLabel, amount, notes)
                    },
                    onCloseSession = { sessionId, amount, notes ->
                        cashboxViewModel.closeSession(sessionId, amount, notes)
                    },
                    onRegisterMovement = { movementCode, amount, description, paymentMethod ->
                        val sessionId = currentSession?.id ?: return@CashboxScreen
                        cashboxViewModel.registerMovement(
                            sessionId = sessionId,
                            movementCode = movementCode,
                            amount = amount,
                            description = description,
                            paymentMethod = paymentMethod,
                            onSuccess = {
                                Toast.makeText(context, "Movimiento registrado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onLoadMovementTypes = { cashboxViewModel.loadMovementTypes() },
                    onClearMessages = { cashboxViewModel.clearMessages() },
                    onSaveAndLogout = { doLogout() },
                    onContinue = { cashboxViewModel.clearMessages() },
                    onLogout = { doLogout() }
                )
            }
        }
    }

    private fun doLogout() {
        SessionManager.clear(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
