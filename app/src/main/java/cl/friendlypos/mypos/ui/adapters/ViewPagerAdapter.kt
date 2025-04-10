package cl.friendlypos.mypos.ui.adapters

import androidx.appcompat.app.AppCompatActivity
import cl.friendlypos.mypos.ui.setup.SetupAccessFragment

class ViewPagerAdapter(activity: AppCompatActivity) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): androidx.fragment.app.Fragment = when (position) {
        0 -> SetupAccessFragment()
        else -> androidx.fragment.app.Fragment()
    }
}