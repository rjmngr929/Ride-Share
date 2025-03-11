package com.my.raido.ui.home.bottomsheet_fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.base.exception.CFException
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.databinding.FragmentAddMoneyBinding
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddMoneyFragment : BottomSheetDialogFragment(), CFCheckoutResponseCallback {

    companion object{
        private const val TAG = "Add Money Fragment"
    }

    private lateinit var binding: FragmentAddMoneyBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val userViewModel: UserDataViewModel by activityViewModels()

    private lateinit var loader: AlertDialog

    private var createdOrderId: String = ""

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMoneyBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this)
        } catch (e: CFException) {
            e.printStackTrace()
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.changePaymentModeBtn.setOnClickListener {
            val bottomSheetFragment = PaymentOptionFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.addMoneyBtn.setOnClickListener {
//            userViewModel.updateOrderStatus(orderId = "order_104033342shTQCogRVrKVHfazlLUyAhvwBL")
            userViewModel.createOrder(binding.amountEditText.text.toString())
        }

//        ***************************************************
        binding.add100.setOnClickListener {
            binding.amountEditText.setText(if(binding.amountEditText.text.isNullOrEmpty()) "100" else "${binding.amountEditText.text.toString().toFloat() + 100}")
            binding.amountEditText.setSelection(binding.amountEditText.text.length)
        }

        binding.add300.setOnClickListener {
            binding.amountEditText.setText(if(binding.amountEditText.text.isNullOrEmpty()) "300" else "${binding.amountEditText.text.toString().toFloat() + 300}")
            binding.amountEditText.setSelection(binding.amountEditText.text.length)
        }

        binding.add500.setOnClickListener {
            binding.amountEditText.setText(if(binding.amountEditText.text.isNullOrEmpty()) "500" else "${binding.amountEditText.text.toString().toFloat() + 500}")
            binding.amountEditText.setSelection(binding.amountEditText.text.length)
        }

//       ***************************************************

    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 1.0).toInt() // 75% of screen height
            bottomSheet.layoutParams = layoutParams
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
                behavior.isDraggable = true

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                // Set the height to match parent (full-screen)
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        return dialog
    }

    override fun onResume() {
        super.onResume()
        createOrderObserver()

        updateStatusObserver()
    }

    override fun onPaymentVerify(orderID: String?) {
        Log.d(TAG, "onPaymentVerify: on payment verify => ${orderID}, $createdOrderId")
        Toast.makeText(myContext, "payment success!!...", Toast.LENGTH_SHORT).show()
        userViewModel.updateOrderStatus(orderId = createdOrderId.toString())
//        activity?.finish()
    }

    override fun onPaymentFailure(cfErrorResponse: CFErrorResponse?, orderID: String?) {
        Log.d(TAG, "onPaymentVerify: on payment verify failed => ${orderID} = ${cfErrorResponse?.message}")
        Toast.makeText(myContext, "payment failed!!...", Toast.LENGTH_SHORT).show()
        userViewModel.updateOrderStatus(orderId = createdOrderId.toString())
    }

    private fun doUPIIntentCheckoutPayment(orderID: String, sessionId: String, environment: String) {
        try {
            val cfSession = CFSession.CFSessionBuilder()
                .setEnvironment(CFSession.Environment.SANDBOX)
                .setPaymentSessionID(sessionId)
                .setOrderId(orderID)
                .build()
            val cfThemeWeb = CFWebCheckoutTheme.CFWebCheckoutThemeBuilder()
                .setNavigationBarBackgroundColor("#000000")
                .setNavigationBarTextColor("#FFFFFF")
                .build()
            val cfWebCheckoutPayment = CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                .setSession(cfSession)
                .setCFWebCheckoutUITheme(cfThemeWeb)
                .build()
            CFPaymentGatewayService.getInstance().doPayment(myContext, cfWebCheckoutPayment)
        } catch (exception: CFException) {
            exception.printStackTrace()
        }
    }

    fun createOrderObserver(){
        userViewModel.createOrderResponseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is NetworkResult.Success -> {
//                    hideLoader(myContext, loader)

                    val data = it.data

                    val cfOrderId = data?.cfOrderId.toString()
                    val sessionId = data?.paymentSessionId
                    val environment = "dummy"
                    createdOrderId = data?.orderId.toString()

                    if(!cfOrderId.isNullOrEmpty() && !sessionId.isNullOrEmpty() && !environment.isNullOrEmpty()){
                        doUPIIntentCheckoutPayment(orderID = cfOrderId, sessionId = sessionId, environment = environment)
                    }

                    userViewModel.clearOrderRes()

                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)
                    alertDialogService.alertDialogAnim(myContext, it.message.toString(), R.raw.failed)
                }
                is NetworkResult.Loading ->{
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(myContext, loader)
                }
            }
        })

    }

    fun updateStatusObserver(){
        userViewModel.updateOrderResponseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)

                    val data = it.data

                    Log.d(TAG, "updateStatusObserver: response data => $data")

                    if(data?.status == true){
                        dismissAllBottomSheets(requireActivity())
                    }

                    userViewModel.clearOrderStatusRes()
                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)
                    alertDialogService.alertDialogAnim(myContext, it.message.toString(), R.raw.failed)
                }
                is NetworkResult.Loading ->{
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(myContext, loader)
                }
            }
        })

    }

    fun dismissAllBottomSheets(activity: FragmentActivity) {
        val fragmentManager = activity.supportFragmentManager
        fragmentManager.fragments.forEach { fragment ->
            if (fragment is BottomSheetDialogFragment) {
                fragment.dismiss()
            }
        }
    }

}