package cl.friendlypos.mypos

import androidx.appcompat.app.AppCompatActivity

class ViewPagerAdapter(activity: AppCompatActivity) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): androidx.fragment.app.Fragment = when (position) {
        0 -> SetupAccessFragment()
        else -> androidx.fragment.app.Fragment()
    }
}