package com.my.raido.ui.home.bottomsheet_fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.databinding.FragmentPaymentOptionBinding
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentOptionFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Payment Option Fragment"
    }

    private lateinit var binding: FragmentPaymentOptionBinding

    private val cabViewModel: CabViewModel by activityViewModels()

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
        binding = FragmentPaymentOptionBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnSingleClickListener {
            dismiss()
        }

        arguments?.getString("ride_fare")?.let { rideFare ->
            try {
              binding.totalFareText.text = String.format("₹ %s", rideFare)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.walletBalanceText.text = String.format("₹ %s", masterViewModel.walletBalanceData.value)

        if(cabViewModel.selectedPaymentMode.value == "cash"){
            binding.selectCodRadioBtn.isChecked = true
        }else{
            binding.selectWalletRadioBtn.isChecked = true
        }

        binding.walletSelectBtn.setOnClickListener {
            cabViewModel.updatePaymentMode("wallet")
            binding.selectWalletRadioBtn.isChecked = true
            binding.selectCodRadioBtn.isChecked = false
//            Handler(Looper.getMainLooper()).postDelayed({
//                dismiss()
//            }, 300)
        }

        binding.selectWalletRadioBtn.setOnClickListener {
            cabViewModel.updatePaymentMode("wallet")
            binding.selectWalletRadioBtn.isChecked = true
            binding.selectCodRadioBtn.isChecked = false
//            Handler(Looper.getMainLooper()).postDelayed({
//                dismiss()
//            }, 300)
        }

        binding.bookRideOnPaymentBtn.setOnSingleClickListener {
            dismiss()
        }

//        binding.selectPhonepeRadioBtn.setOnClickListener {
//            dismiss()
//        }
//
//        binding.selectGpayRadioBtn.setOnClickListener {
//            dismiss()
//        }
//
//        binding.selectAddcardsRadioBtn.setOnClickListener {
//            dismiss()
//        }

        binding.cashSelectBtn.setOnClickListener {
            cabViewModel.updatePaymentMode("cash")
            binding.selectCodRadioBtn.isChecked = true
            binding.selectWalletRadioBtn.isChecked = false
//            Handler(Looper.getMainLooper()).postDelayed({
//                dismiss()
//            }, 300)
        }

        binding.selectCodRadioBtn.setOnClickListener {
            cabViewModel.updatePaymentMode("cash")
            binding.selectCodRadioBtn.isChecked = true
            binding.selectWalletRadioBtn.isChecked = false
//            Handler(Looper.getMainLooper()).postDelayed({
//                dismiss()
//            }, 300)
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