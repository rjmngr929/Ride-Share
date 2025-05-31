package com.my.raido.ui.home.bottomsheet_fragments.ride_book

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moengage.core.internal.utils.isNullOrBlank
import com.my.raido.R
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.showSnack
import com.my.raido.constants.Constants
import com.my.raido.constants.SocketConstants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentCancelRideBinding
import com.my.raido.socket.SocketManager
import com.my.raido.ui.home.HomeActivity
import com.my.raido.ui.viewmodels.MasterViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class CancelRideFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Cancel Ride Fragment"
    }

    private lateinit var binding: FragmentCancelRideBinding

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

//    private var manageBookingOperations: ManageBooking? = null

    private val masterViewModel: MasterViewModel by viewModels()

    private lateinit var cancelReason : String

    private var rideId = ""

    private lateinit var loader: AlertDialog

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
//        if (context is ManageBooking) {
//            manageBookingOperations = context
//        } else {
//            throw ClassCastException("$context must implement MainActivityOperations")
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCancelRideBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.getString("ride_id")?.let {
            rideId = it
        }

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

// ****************************** Reason Select Spinner **************************

        val reasonList: Array<String> = resources.getStringArray(R.array.cancel_reasons_array)

        var groupSelectAdapter = ArrayAdapter(
            myContext, android.R.layout.simple_spinner_dropdown_item, reasonList
        )

        binding.typesFilter.setAdapter(groupSelectAdapter)

        if(reasonList.size > 5)
            binding.typesFilter.dropDownHeight = 500

        binding.typesFilter.setDropDownBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_border,
                null
            )
        )

        binding.typesFilter.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, id->
                Log.d(TAG, "onViewCreated: selected reason is ${reasonList[position]}")
                cancelReason = reasonList[position]
                if(cancelReason == "Other"){
                    binding.cancelReasonTextInput.isEnabled = true
                }else{
                    binding.cancelReasonTextInput.isEnabled = false
                    binding.cancelReasonTextInput.text?.clear()
                }
            }

//   ****************************** Reason Select Spinner **************************


        binding.cancelRideBtn.setOnClickListener {
            if(::cancelReason.isInitialized) {
                showLoader(myContext, loader)
                if (cancelReason == "Other") {
                    cancelReason = binding.cancelReasonTextInput.text.toString()
                }


                if(masterViewModel.cancelRideData.value == true){
                    showLoader(myContext, loader)
                    val bookingId = sharedPrefManager.getString(Constants.TEMP_BOOKING_ID)
                    val cancelRide = JSONObject().apply {
                        put("booking_id ", bookingId)
                        put("UsersocketId ", socketManager.getSocketId())
                    }
                    socketManager.sendData("cancelBooking", cancelRide) { status ->
                        hideLoader(myContext, loader)
                        //                      showToast(myContext, "ride cancel call success ${socketManager.getSocketId()}")
                    }

                    socketManager.listenToEvent("UsercancelBookingRequest") { data ->
                        if (!data.isNullOrBlank()) {
                            Log.d(TAG, "UsercancelBookingRequest: cancel ride data is ${data}")
                            val status = data.getBoolean("status")
                            if (status) {
                                sharedPrefManager.putString(Constants.TEMP_BOOKING_ID, "")
                                masterViewModel.setCancelRideData(false)
                                if (isAdded && context != null) {
                                    dismiss()
                                    val bottomSheetFragment = SelectVehicleTypeFragment()
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                        }
                    }

                }else{
                    val cancelRide = JSONObject().apply {
                        put("ride_id", rideId)
                        put("cancel_reason", cancelReason)
                        put("type", "user")
                    }

                    socketManager.sendData(SocketConstants.RIDE_CANCEL, cancelRide) { status ->
                        hideLoader(myContext, loader)
//                    showToast(myContext, "ride cancel call success ${socketManager.getSocketId()}")
                    }

                    socketManager.listenToEvent(SocketConstants.RIDE_CANCEL_SUCCESS) { data ->
                        Log.d(TAG, "onViewCreated: cancel ride data is ${data}")
                        sharedPrefManager.putString(Constants.TEMP_BOOKING_ID, "")
                        if (isAdded && context != null) {
                            startActivity(Intent(myContext, HomeActivity::class.java))
                            activity?.finish()
                        }
                    }

                }

//                masterViewModel.cancelRideData.observe(viewLifecycleOwner, Observer{
//                    Log.d(TAG, "onViewCreated: cancel from looking rider status $it")
//                    if(it){
//
//                        val bookingId = sharedPrefManager.getString(Constants.TEMP_BOOKING_ID)
//                        val cancelRide = JSONObject().apply {
//                            put("booking_id ", bookingId)
//                            put("UsersocketId ", socketManager.getSocketId())
//                        }
//                        masterViewModel.cancelRideData.removeObservers(viewLifecycleOwner)
//                        socketManager.sendData("cancelBooking", cancelRide) { status ->
//                            hideLoader(myContext, loader)
//    //                      showToast(myContext, "ride cancel call success ${socketManager.getSocketId()}")
//                        }
//                    }else{
//                        val cancelRide = JSONObject().apply {
//                            put("ride_id", rideId)
//                            put("cancel_reason", cancelReason)
//                            put("type", "user")
//                        }
//
//                        socketManager.sendData(SocketConstants.RIDE_CANCEL, cancelRide) { status ->
//                            hideLoader(myContext, loader)
////                    showToast(myContext, "ride cancel call success ${socketManager.getSocketId()}")
//                        }
//                    }
//                })




            }else{
                view.showSnack(message = "Please select a reason", textColor = R.color.white, bgColor = R.color.failed)
            }
        }

//        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
//            override fun onConnected() {
//                socketManager.listenToEvent(SocketConstants.RIDE_CANCEL_SUCCESS) { data ->
//                    Log.d(TAG, "onViewCreated: cancel ride data is ${data}")
//                    sharedPrefManager.putString(Constants.TEMP_BOOKING_ID, "")
//                    if (isAdded && context != null) {
//                        startActivity(Intent(myContext, HomeActivity::class.java))
//                        activity?.finish()
//                    }
//                }
//
//                socketManager.listenToEvent("UsercancelBookingRequest") { data ->
//                    if (!data.isNullOrBlank()) {
//                        Log.d(TAG, "UsercancelBookingRequest: cancel ride data is ${data}")
//                        val status = data.getBoolean("status")
//                        if (status) {
//                            sharedPrefManager.putString(Constants.TEMP_BOOKING_ID, "")
//                            masterViewModel.setCancelRideData(false)
//                            if (isAdded && context != null) {
//                                dismiss()
//                                val bottomSheetFragment = SelectVehicleTypeFragment()
//                                parentFragmentManager.beginTransaction()
//                                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                                    .addToBackStack(null)
//                                    .commit()
//                            }
//                        }
//                    }
//                }
//            }
//        })

    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            val behavior = BottomSheetBehavior.from(bottomSheet)
            // Disable dragging
            behavior.isDraggable = false

            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.60).toInt() // 75% of screen height
            bottomSheet.layoutParams = layoutParams
        }

    }

}