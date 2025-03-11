package com.my.raido.ui.home.bottomsheet_fragments.drawers.claims

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.my.raido.R
import com.my.raido.Utils.gone
import com.my.raido.Utils.visible
import com.my.raido.adapters.ClaimViewPagerAdapter
import com.my.raido.databinding.FragmentClaimsBinding

class ClaimsFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Help Fragment"
    }

    private lateinit var binding: FragmentClaimsBinding

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClaimsBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        val tabLayout: TabLayout = binding.tabLayout
        val viewPager: ViewPager2 = binding.viewPager

        // Step 1: Set the adapter for ViewPager2
        val adapter = activity?.let { ClaimViewPagerAdapter(it) }
        viewPager.adapter = adapter

        // Ensure adapter is initialized before attaching TabLayoutMediator
        if (viewPager.adapter == null) {
            viewPager.adapter = activity?.let { ClaimViewPagerAdapter(it) }
        }


// Attach ViewPager with TabLayout (optional)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Inflate the custom view
            val tabView = LayoutInflater.from(myContext).inflate(R.layout.custom_tab_item, null)

            // Customize the tab view
            val tabIcon = tabView.findViewById<ImageView>(R.id.tab_icon)
            val tabText = tabView.findViewById<TextView>(R.id.tab_text)
            when (position) {
                0 -> {
                    tabIcon.setImageResource(R.drawable.active_tab)
                    tabText.text = "Bike"
                }
                1 -> {
                    tabIcon.setImageResource(R.drawable.active_tab)
                    tabText.text = "Auto"
                }
                2 -> {
                    tabIcon.setImageResource(R.drawable.active_tab)
                    tabText.text = "Cab"
                }
            }

            // Set the custom view to the tab
            tab.customView = tabView
        }.attach()

        tabLayout.getTabAt(0)?.let { setActiveTab(it) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                setActiveTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                setInactiveTab(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle reselection if needed
            }
        })

    }

    private fun setActiveTab(tab: TabLayout.Tab) {
        val customView = tab.customView
        customView?.findViewById<ImageView>(R.id.tab_icon)?.visible()
    }

    private fun setInactiveTab(tab: TabLayout.Tab) {
        val customView = tab.customView
        customView?.findViewById<ImageView>(R.id.tab_icon)?.gone()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

//            This is for avoid bottomsheet dismiss on dragging
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                // Disable dragging
                behavior.isDraggable = false
            }
        }


        return dialog
    }


    override fun onStart() {
        super.onStart()

        // Make the bottom sheet full screen
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = LayoutParams.MATCH_PARENT

            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = LayoutParams.MATCH_PARENT
        }


    }
}