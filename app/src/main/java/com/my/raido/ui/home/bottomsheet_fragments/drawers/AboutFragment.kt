package com.my.raido.ui.home.bottomsheet_fragments.drawers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.Utils.AppUtils
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "About Fragment"
    }

    private lateinit var binding: FragmentAboutBinding

    @Inject
    lateinit var sharedPreference: SharedPrefManager

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.privacyPolicyBtn.setOnClickListener {
            val privacyPolicyUrl = sharedPreference.getString(Constants.PRIVACY_POLICY)!! ?: ""
            AppUtils.sharedInstance.openUrlOnExternalBrowser(myContext, privacyPolicyUrl)
        }

        binding.termConditionBtn.setOnClickListener {
            val termConditionUrl = sharedPreference.getString(Constants.TERM_CONDITION)!! ?: ""
            AppUtils.sharedInstance.openUrlOnExternalBrowser(myContext, termConditionUrl)
        }

        binding.joinTeamBtn.setOnClickListener {
            val joinTeamUrl = sharedPreference.getString(Constants.JOIN_TERM)!! ?: ""
            AppUtils.sharedInstance.openUrlOnExternalBrowser(myContext, joinTeamUrl)
        }

        binding.blogBtn.setOnClickListener {
            val blogUrl = sharedPreference.getString(Constants.BLOG)!! ?: ""
            AppUtils.sharedInstance.openUrlOnExternalBrowser(myContext, blogUrl)
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