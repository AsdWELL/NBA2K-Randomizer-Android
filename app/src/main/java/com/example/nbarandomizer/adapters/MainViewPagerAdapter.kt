package com.example.nbarandomizer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nbarandomizer.ui.randomizer.RandomizerFragment
import com.example.nbarandomizer.ui.roster.RosterFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RandomizerFragment()
            1 -> RosterFragment()
            else -> error("Unknown page: $position")
        }
    }
}
