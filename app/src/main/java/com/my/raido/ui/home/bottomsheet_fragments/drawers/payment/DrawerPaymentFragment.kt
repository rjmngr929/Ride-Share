package com.my.raido.ui.home.bottomsheet_fragments.drawers.payment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.databinding.FragmentDrawerPaymentBinding
import com.my.raido.ui.home.bottomsheet_fragments.AddMoneyFragment
import com.my.raido.ui.home.bottomsheet_fragments.drawers.AboutFragment
import com.my.raido.ui.viewmodels.MasterViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DrawerPaymentFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Drawer Payment Fragment"
    }

    private lateinit var binding: FragmentDrawerPaymentBinding

    private val masterViewModel: MasterViewModel by viewModels()

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawerPaymentBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnSingleClickListener {
            dismiss()
        }

        masterViewModel.walletBalanceData.observe(this) { balance ->
            binding.balanceText.text = String.format("₹ %s", balance)
        }


        binding.addMoneyBtn.setOnSingleClickListener {
            val sheet = AddMoneyFragment()
            val existingSheet = childFragmentManager.findFragmentByTag(sheet.tag)
            if (existingSheet == null) {
                sheet.show(childFragmentManager, sheet.tag)
            }

//            val bottomSheetFragment = AddMoneyFragment()
//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.showPassbookBtn.setOnSingleClickListener {
            val sheet = PassbookFragment()
            val existingSheet = childFragmentManager.findFragmentByTag(sheet.tag)
            if (existingSheet == null) {
                sheet.show(childFragmentManager, sheet.tag)
            }

//            val bottomSheetFragment = PassbookFragment()
//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
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