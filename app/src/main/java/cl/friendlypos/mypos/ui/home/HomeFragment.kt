package cl.friendlypos.mypos.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.compose.screen.HomeScreen

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeScreen(
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
                    }
                )
            }
        }
    }
}
