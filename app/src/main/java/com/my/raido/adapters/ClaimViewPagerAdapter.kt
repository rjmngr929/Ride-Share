package com.my.raido.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.my.raido.ui.home.bottomsheet_fragments.drawers.claims.fragments.ClaimAutoFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.claims.fragments.ClaimBikeFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.claims.fragments.ClaimCabFragment

class ClaimViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3 // Number of fragments/pages

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ClaimBikeFragment()
            1 -> ClaimAutoFragment()
            2 -> ClaimCabFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}