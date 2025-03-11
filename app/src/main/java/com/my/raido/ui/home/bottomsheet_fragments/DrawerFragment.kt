package com.my.raido.ui.home.bottomsheet_fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.Utils.TokenManager
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentDrawerBinding
import com.my.raido.ui.auth.AuthActivity
import com.my.raido.ui.home.bottomsheet_fragments.drawers.AboutFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.HelpFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.MyRewardsFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.NotificationFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.ProfileFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.RaidoCoinsFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.ReferEarnFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.RiderHistoryFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.claims.ClaimsFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.payment.DrawerPaymentFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.safety.SafetyFragment
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DrawerFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Assign Driver Fragment"
    }

    private lateinit var binding: FragmentDrawerBinding

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val userViewModel: UserDataViewModel by viewModels()

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.drawerProfileBtn.setOnClickListener {
            val bottomSheetFragment = ProfileFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerRideHistoryBtn.setOnClickListener {
            val bottomSheetFragment = RiderHistoryFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerReferEarnBtn.setOnClickListener {
            val bottomSheetFragment = ReferEarnFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerPaymentBtn.setOnClickListener {
            val bottomSheetFragment = DrawerPaymentFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
        binding.drawerSafetyBtn.setOnClickListener {
            val bottomSheetFragment = SafetyFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
        binding.drawerRewardBtn.setOnClickListener {
            val bottomSheetFragment = MyRewardsFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
        binding.drawerRaidoCoinBtn.setOnClickListener {
            val bottomSheetFragment = RaidoCoinsFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerNotificationBtn.setOnClickListener {
            val bottomSheetFragment = NotificationFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerClaimsBtn.setOnClickListener {
            val bottomSheetFragment = ClaimsFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerHelpBtn.setOnClickListener {
            val bottomSheetFragment = HelpFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerAboutBtn.setOnClickListener {
            val bottomSheetFragment = AboutFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.drawerLogoutBtn.setOnClickListener {
            userViewModel.logoutUser()
            tokenManager.removeToken()
            sharedPrefManager.removeAll()
            startActivity(Intent(myContext, AuthActivity::class.java))
            activity?.finish()
        }


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

}