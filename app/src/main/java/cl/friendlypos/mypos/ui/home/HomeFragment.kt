package cl.friendlypos.mypos.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.compose.screen.HomeScreen
import cl.friendlypos.mypos.compose.viewmodel.CashboxViewModel

class HomeFragment : Fragment() {

    private val cashboxViewModel: CashboxViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val role = SessionManager.getRole(requireContext())
                val currentSession by cashboxViewModel.currentSession.collectAsState()
                val hasLoaded by cashboxViewModel.hasInitialLoadCompleted.collectAsState()

                val isSessionOpen: Boolean? = when {
                    !hasLoaded -> null
                    currentSession?.status == "open" -> true
                    else -> false
                }

                HomeScreen(
                    role = role,
                    isSessionOpen = isSessionOpen,
                    onNavigateToNewSale = {
                        findNavController().navigate(R.id.action_home_to_sales_calc)
                    },
                    onNavigateToInventory = {
                        findNavController().navigate(R.id.action_home_to_inventory)
                    },
                    onNavigateToCustomers = {
                        findNavController().navigate(R.id.action_home_to_customers)
                    },
                    onNavigateToReports = {
                        findNavController().navigate(R.id.action_home_to_reports)
                    },
                    onNavigateToCashbox = {
                        findNavController().navigate(R.id.action_home_to_cashbox)
                    },
                    onNavigateToTickets = {
                        findNavController().navigate(R.id.action_home_to_tickets)
                    },
                    onNavigateToSettings = {
                        findNavController().navigate(R.id.action_home_to_settings)
                    }
                )
            }
        }
    }
}
