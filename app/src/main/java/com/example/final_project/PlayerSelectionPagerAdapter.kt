package com.example.final_project

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PlayerSelectionPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Home and Away

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomePlayersFragment()
            1 -> AwayPlayersFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}