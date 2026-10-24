package com.example.nbarandomizer.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nbarandomizer.ui.playerDetails.AttributesFragment
import com.example.nbarandomizer.ui.playerDetails.BadgesFragment

class PlayerDetailsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AttributesFragment()
            1 -> BadgesFragment()
            else -> error("Unknown page: $position")
        }
    }

}
