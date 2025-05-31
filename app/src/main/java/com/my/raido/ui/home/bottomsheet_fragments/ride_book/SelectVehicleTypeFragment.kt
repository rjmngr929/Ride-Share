package com.my.raido.ui.home.bottomsheet_fragments.ride_book


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.my.raido.Helper.SelectVehicle
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.capitalizeEachWord
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showSnack
import com.my.raido.Utils.visible
import com.my.raido.constants.Constants
import com.my.raido.constants.SocketConstants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentSelectVehicleTypeBinding
import com.my.raido.models.cab.DistrictFareData
import com.my.raido.services.ManageBooking
import com.my.raido.socket.SocketManager
import com.my.raido.ui.home.bottomsheet_fragments.PaymentOptionFragment
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class SelectVehicleTypeFragment : BottomSheetDialogFragment()  { //OnMapReadyCallback

    companion object{
        internal const val TAG = "Select Vehicle Type Fragment"
    }

    private lateinit var binding: FragmentSelectVehicleTypeBinding

    private lateinit var myContext: Context

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var socketManager: SocketManager

    private var manageBookingOperations: ManageBooking? = null

    private val cabViewModel: CabViewModel by activityViewModels()

    private val masterViewModel: MasterViewModel by viewModels()

    private var distance: String = ""

    private var paymentMode: String = "cash"

    private var bikeFare: String = ""
    private var bikeLiteFare: String = ""
    private var autoFare: String = ""
    private var cabFare: String = ""

    private var bikeDuration: String = ""
    private var bikeLiteDuration: String = ""
    private var autoDuration: String = ""
    private var cabDuration: String = ""

    private lateinit var loader: AlertDialog

    private var fcmToken: String = ""

    private var userId: String = ""

    private var serviceCategory: Int = 0
    private var rideFare: String = ""
    private var selectedVehicleDuration: String = ""
    private var bookingId: String = ""


    private var bikeId = 0
    private var cabId = 0
    private var autoId = 0
    private var bikeLiteId = 0

    private var districtFareData: ArrayList<DistrictFareData> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
        if (context is ManageBooking) {
            manageBookingOperations = context
        } else {
            throw ClassCastException("$context must implement MainActivityOperations")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectVehicleTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fcmToken = sharedPrefManager.getString(Constants.FCM_TOKEN, "fcm_xyz").toString()

        loader = getLoadingDialog(myContext)

        userId = sharedPrefManager.getInt(Constants.USER_ID).toString()

        bikeId = sharedPrefManager.getInt(Constants.BIKE_ID, 0)
        cabId = sharedPrefManager.getInt(Constants.CAB_ID, 0)
        autoId = sharedPrefManager.getInt(Constants.AUTO_ID, 0)
        bikeLiteId = sharedPrefManager.getInt(Constants.BIKE_LITE_ID, 0)

        binding.changePaymentModeBtn.setOnSingleClickListener {
            val bottomSheetFragment = PaymentOptionFragment()
            bottomSheetFragment.arguments = Bundle().apply {
                putString("ride_fare", rideFare)
            }

            val existingSheet = childFragmentManager.findFragmentByTag(bottomSheetFragment.tag)
            if (existingSheet == null) {
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }

//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)



//            parentFragmentManager.beginTransaction()
//                .add(R.id.fragment_container_bookride, bottomSheetFragment)
//                .addToBackStack(null)
//                .commit()
        }

        binding.bikeRegularPriceText.paintFlags = binding.bikeRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.autoRegularPriceText.paintFlags = binding.autoRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.cabRegularPriceText.paintFlags = binding.cabRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.bikeliteRegularPriceText.paintFlags = binding.bikeliteRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        binding.bikeCart.setOnClickListener {
            setActiveTab(SelectVehicle.BIKE, true)
        }

        binding.taxiCart.setOnClickListener {
            setActiveTab(SelectVehicle.AUTO, true)
        }

        binding.carCart.setOnClickListener {
            setActiveTab(SelectVehicle.CABS, true)
        }

        binding.bikeliteCart.setOnClickListener {
            setActiveTab(SelectVehicle.BIKE_LITE, true)
        }

        // Capture the back button press using the OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        binding.bookRideBtn.setOnClickListener {

            val selectedMotor = sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)

             when(selectedMotor){
                SelectVehicle.BIKE -> {
                    rideFare = bikeFare
                    selectedVehicleDuration = bikeDuration
                }
                SelectVehicle.AUTO -> {
                    rideFare = autoFare
                    selectedVehicleDuration = autoDuration
                }
                SelectVehicle.CABS -> {
                    rideFare = cabFare
                    selectedVehicleDuration = cabDuration
                }
                SelectVehicle.BIKE_LITE -> {
                    rideFare = bikeLiteFare
                    selectedVehicleDuration = bikeLiteDuration
                }
                SelectVehicle.PARCEL -> {
                    rideFare = bikeFare
                    selectedVehicleDuration = bikeDuration
                }
                else -> {
                    rideFare = bikeFare
                    selectedVehicleDuration = bikeDuration
                }
            }

             sharedPrefManager.putString(Constants.AWAY_TIME, selectedVehicleDuration)
             sharedPrefManager.putString(Constants.TOTAL_FARE, rideFare)

                val walletBalance = masterViewModel.walletBalanceData.value?.toDouble() ?: 0.0

//                Log.d(TAG, "onViewCreated: wallet balance for book ride is ${walletBalance}, ${rideFare} and ${walletBalance > rideFare.toDouble()}")

                if(paymentMode == "wallet" && rideFare.isNotEmpty()){
                    if(walletBalance < rideFare.toDouble()){
                        view.showSnack(message = "insufficient fund", textColor = R.color.white, bgColor = R.color.failed )
                        return@setOnClickListener
                    }
                }



                val bookingRideData = JSONObject().apply {
                    put("booking_id", bookingId)
                    put("user_id", userId)
                    put("socketId", socketManager.getSocketId())
                    put("service_categories_id", serviceCategory)
                    put("paymentMode", paymentMode)
                }

            cabViewModel.setCreateBookingData(bookingRideData)

            val bottomSheetFragment = LookingForRiderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                .addToBackStack(null)
                .commit()

//                socketManager.sendData(SocketConstants.CREATE_BOOKING, bookingRideData) { status ->
//                    Log.d(TAG, "onViewCreated: createBooking emit status => ${status} and ${selectedMotor.name}")
//                    if (status) {
//                        manageBookingOperations?.performOperation(BookingStatus.LOOKING_DRIVER)
//                        val bottomSheetFragment = LookingForRiderFragment()
//                        parentFragmentManager.beginTransaction()
//                            .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                            .addToBackStack(null)
//                            .commit()
//                    }
//                }

                dismiss()

        }

        cabViewModel.selectedPaymentMode.observe(viewLifecycleOwner) { status ->
            val currentStatus = status ?: "wallet"
            binding.paymentTypeText.text = currentStatus
            paymentMode = currentStatus
        }

    }

    private fun setActiveTab(keyVal: SelectVehicle, checkDoubleClick: Boolean = false) {

        val previousSelectedVehicle = sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java, SelectVehicle.BIKE)

        if(checkDoubleClick && previousSelectedVehicle.equals(keyVal)){
            showBottomSheetDialog(keyVal)
            return
        }

        sharedPrefManager.saveEnum(Constants.VEHICLE_TYPE, keyVal)

        val primaryColor = ContextCompat.getColor(myContext, R.color.primary)
        val darkColor = ContextCompat.getColor(myContext, R.color.bg_dark_secondary)
        val whiteColor = ContextCompat.getColor(myContext, R.color.white)

        val currentLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG)

        val json = JSONObject().apply {
            put("latitude", currentLatLng?.latitude)
            put("longitude", currentLatLng?.longitude)
            put("vehicle_type", keyVal.name)
            put("socketId", socketManager.getSocketId())
        }

        socketManager.sendData(SocketConstants.GET_NEAR_BY_RIDERS, json){status ->

        }

        when (keyVal) {
            SelectVehicle.BIKE -> {
                rideFare = bikeFare
                binding.bikeCart.backgroundTintList = null
                binding.leftSectionSelectBike.backgroundTintList =
                    ColorStateList.valueOf(primaryColor)

                binding.taxiCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectTaxi.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.carCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectCar.backgroundTintList = ColorStateList.valueOf(whiteColor)

                binding.bikeliteCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBikeLite.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                serviceCategory = sharedPrefManager.getInt(Constants.BIKE_ID)

            }

            SelectVehicle.AUTO -> {
                rideFare = autoFare
                binding.bikeCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBike.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.taxiCart.backgroundTintList = null
                binding.leftSectionSelectTaxi.backgroundTintList =
                    ColorStateList.valueOf(primaryColor)

                binding.carCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectCar.backgroundTintList = ColorStateList.valueOf(whiteColor)

                binding.bikeliteCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBikeLite.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                serviceCategory = sharedPrefManager.getInt(Constants.AUTO_ID)
            }

            SelectVehicle.CABS -> {
                rideFare = cabFare
                binding.bikeCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBike.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.taxiCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectTaxi.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.carCart.backgroundTintList = null
                binding.leftSectionSelectCar.backgroundTintList = ColorStateList.valueOf(primaryColor)

                binding.bikeliteCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBikeLite.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                serviceCategory = sharedPrefManager.getInt(Constants.CAB_ID)
            }

            SelectVehicle.BIKE_LITE -> {
                rideFare = bikeLiteFare
                binding.bikeCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBike.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.taxiCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectTaxi.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.carCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectCar.backgroundTintList = ColorStateList.valueOf(whiteColor)

                binding.bikeliteCart.backgroundTintList = null
                binding.leftSectionSelectBikeLite.backgroundTintList =
                    ColorStateList.valueOf(primaryColor)

                serviceCategory = sharedPrefManager.getInt(Constants.BIKE_LITE_ID)
            }

            SelectVehicle.PARCEL -> {
                rideFare = bikeFare
                binding.bikeCart.backgroundTintList = null
                binding.leftSectionSelectBike.backgroundTintList =
                    ColorStateList.valueOf(primaryColor)

                binding.taxiCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectTaxi.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                binding.carCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectCar.backgroundTintList = ColorStateList.valueOf(whiteColor)

                binding.bikeliteCart.backgroundTintList = ColorStateList.valueOf(darkColor)
                binding.leftSectionSelectBikeLite.backgroundTintList =
                    ColorStateList.valueOf(whiteColor)

                serviceCategory = sharedPrefManager.getInt(Constants.BIKE_ID)
            }
        }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Remove background blur
        dialog.window?.setDimAmount(0f)

        // Transparent background
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // 👇 Allow touch to pass through
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        dialog.setCanceledOnTouchOutside(false)
        isCancelable = false
        return dialog
    }

    override fun onStart() {
        super.onStart()

        // ✅ Remove blur/dim background
        dialog?.window?.setDimAmount(0f)

        // ✅ Set height to WRAP_CONTENT
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        bottomSheet?.requestLayout()
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        dialog.setOnShowListener {
//            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
//
////            This is for avoid bottomsheet dismiss on dragging
//            if (bottomSheet != null) {
//                val behavior = BottomSheetBehavior.from(bottomSheet)
//                // Disable dragging
//                behavior.isDraggable = false
//            }
//        }
//
//
//        return dialog
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        // Make the bottom sheet full screen
//        dialog?.let {
//            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.layoutParams?.height = LayoutParams.MATCH_PARENT
//            val behavior = BottomSheetBehavior.from(bottomSheet!!)
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
//            behavior.peekHeight = LayoutParams.MATCH_PARENT
//
//        }
//
//    }


    override fun onResume() {
        super.onResume()
        bindObservers()
    }

    private fun bindObservers() {
        cabViewModel.rideFareDetailResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {

                    val responseData = it.data

                    binding.shimmerEffect.shimmerLayout.gone()
                    binding.selectVehicleSection.visible()

                    if(responseData?.status == true){
                        val fareRidersData = responseData.fareData

                        bookingId = responseData.bookingId
                        sharedPrefManager.putString(Constants.TEMP_BOOKING_ID, bookingId)

                        districtFareData.clear()
                        districtFareData.addAll(responseData.districtFareData)
                        Log.d(TAG, "bindObservers: get detail size => ${responseData.districtFareData.get(0).serviceCategoryId}")

//*************************** Bike Detail ************************************************************

                        if(!fareRidersData.bikeDetail.arrivalTime.isNullOrEmpty() && fareRidersData.bikeDetail.arrivalTime != "null") {
                            binding.bikeCart.visible()

                            bikeFare = fareRidersData.bikeDetail.finalFare.toString()
                            binding.bikeTimeText.text = String.format(
                                "%s | ",
                                fareRidersData.bikeDetail.arrivalTime
                            )
                            bikeDuration = fareRidersData.bikeDetail.durationTime
                            binding.bikeAwayText.text = String.format("%s away", bikeDuration)

//                        binding.bikeAwayText.text = motordata.nearbyBikes.averageDuration
                            binding.bikeRegularPriceText.text =
                                String.format("₹ %s", fareRidersData.bikeDetail.fare)
                            binding.bikePriceText.text = String.format("₹ %s", bikeFare)
                        }else{
                            binding.bikeCart.gone()
                        }
//*************************** Bike Detail ************************************************************


//****************************** Auto Detail **********************************************************
                        if(!fareRidersData.autoDetail.arrivalTime.isNullOrEmpty() && fareRidersData.autoDetail.arrivalTime != "null") {
                            binding.taxiCart.visible()

                            autoFare = fareRidersData.autoDetail.finalFare.toString()
                            binding.autoTimeText.text =
                                String.format("%s | ", fareRidersData.autoDetail.arrivalTime)
                            autoDuration = fareRidersData.autoDetail.durationTime
                            binding.autoAwayText.text = String.format("%s away", autoDuration)
//                        binding.autoAwayText.text = motordata.nearbyAutos.averageDuration
                            binding.autoRegularPriceText.text =
                                String.format("₹ %s", fareRidersData.autoDetail.fare)
                            binding.autoPriceText.text = String.format("₹ %s", autoFare)
                        }else{
                            binding.taxiCart.gone()
                        }
//****************************** Auto Detail **********************************************************


//******************************** Cab Detail ****************************************************************
                        if(!fareRidersData.cabDetail.arrivalTime.isNullOrEmpty() && fareRidersData.cabDetail.arrivalTime != "null") {
                            binding.carCart.visible()

                            cabFare = fareRidersData.cabDetail.finalFare.toString()
                            binding.cabTimeText.text =
                                String.format("%s | ", fareRidersData.cabDetail.arrivalTime)
                            cabDuration = fareRidersData.cabDetail.durationTime
                            binding.cabAwayText.text = String.format("%s away", cabDuration)

//                        binding.cabAwayText.text = motordata.nearbyCabs.averageDuration
                            binding.cabRegularPriceText.text =
                                String.format("₹ %s", fareRidersData.cabDetail.fare)
                            binding.cabPriceText.text = String.format("₹ %s", cabFare)
                        }else{
                            binding.carCart.gone()
                        }
//******************************** Cab Detail ****************************************************************


//  ************************************ Bike Lite ***********************************************************
                            if(!fareRidersData.bikeLiteDetail.arrivalTime.isNullOrEmpty() && fareRidersData.bikeLiteDetail.arrivalTime != "null") {
                                binding.bikeliteCart.visible()

                                bikeLiteFare = fareRidersData.bikeLiteDetail.finalFare.toString()
                                binding.bikeLiteTimeText.text = String.format(
                                    "%s | ",
                                    fareRidersData.bikeLiteDetail.arrivalTime
                                )
                                bikeLiteDuration = fareRidersData.bikeLiteDetail.durationTime
                                binding.bikeLiteAwayText.text = String.format("%s away", bikeLiteDuration)

//                        binding.bikeLiteAwayText.text = motordata.nearbyBikeLite.averageDuration
                                binding.bikeliteRegularPriceText.text =
                                    String.format("₹ %s", fareRidersData.bikeLiteDetail.fare)
                                binding.bikeLitePriceText.text = String.format("₹ %s", bikeLiteFare)
                            }else{
                                binding.bikeliteCart.gone()
                            }
//  ************************************ Bike Lite ***********************************************************

                        distance = responseData.Distance

                        var selectedVehicle = sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)

                        setActiveTab(selectedVehicle)

                    }

                }
                is NetworkResult.Error -> {

//                    alertDialogService.alertDialogAnim(
//                        myContext,
//                        it.message.toString(),
//                        R.raw.failed
//                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
//                    binding.shimmerEffect.shimmerLayout.visible()
//                    binding.selectVehicleSection.gone()
                }
                is NetworkResult.Empty -> {
                }
            }
        })
    }

    private fun showBottomSheetDialog(vehicleType: SelectVehicle ) {
        val dialog = BottomSheetDialog(myContext)
        val view=layoutInflater.inflate(R.layout.vehicle_fare_bottomsheet, null)

        val fareTitle = view.findViewById<TextView>(R.id.fare_title_text)
        val totalFare = view.findViewById<TextView>(R.id.total_fare_text)
        val fareValue = view.findViewById<TextView>(R.id.fare_text)
        val distanceDesc = view.findViewById<TextView>(R.id.distance_desc_text)
        val waitingTime = view.findViewById<TextView>(R.id.waiting_text)

        val gotItBtn = view.findViewById<MaterialButton>(R.id.got_it_btn)

        districtFareData.filter { it.serviceCategoryId == serviceCategory }

        fareTitle.text = String.format("%s Fare Details", vehicleType.name.capitalizeEachWord())
        totalFare.text = String.format("₹%s*", rideFare)
        fareValue.text = String.format("₹%s", rideFare)
        distanceDesc.text = String.format("Rs %1s Base Fare. From %2s to %3s km - %4s Rs/km, From %5s to %6s km - %7s Rs/km Night Charges - Rs%8s", districtFareData.get(0).baseFare.toString().trim(), districtFareData.get(0).distanceFareMinFirst.toString().trim(), districtFareData.get(0).distanceFareMaxFirst.toString().trim(), districtFareData.get(0).distanceFareRateFirst.toString().trim(), districtFareData.get(0).distanceFareMinSecond.toString().trim(), districtFareData.get(0).distanceFareMaxSecond.toString().trim(), districtFareData.get(0).distanceFareRateSecond.toString().trim(), districtFareData.get(0).nightFare.toString().trim() )
        waitingTime.text = String.format("Waiting charges after %1s mins of captain arrival is ₹%2s/min", districtFareData.get(0).waitTimeMax, districtFareData.get(0).waitTimeCharge)

        gotItBtn.setOnSingleClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()

    }



}