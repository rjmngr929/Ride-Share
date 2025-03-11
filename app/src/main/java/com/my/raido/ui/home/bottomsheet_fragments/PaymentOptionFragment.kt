package com.my.raido.ui.home.bottomsheet_fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.databinding.FragmentPaymentOptionBinding
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentOptionFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Payment Option Fragment"
    }

    private lateinit var binding: FragmentPaymentOptionBinding

    private val cabViewModel: CabViewModel by activityViewModels()

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

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        if(cabViewModel.selectedPaymentMode.value == "cash"){
            binding.selectCodRadioBtn.isChecked = true
        }else{
            binding.selectWalletRadioBtn.isChecked = true
        }

        binding.walletSelectBtn.setOnClickListener {
            cabViewModel.updatePaymentMode("wallet")
            binding.selectWalletRadioBtn.isChecked = true
            binding.selectCodRadioBtn.isChecked = false
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 500)
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
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 500)
        }

    }

}