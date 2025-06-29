package cl.friendlypos.mypos.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import cl.friendlypos.mypos.compose.screen.NotificationsScreen

class NotificationsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            NotificationsScreen()
        }
    }
}