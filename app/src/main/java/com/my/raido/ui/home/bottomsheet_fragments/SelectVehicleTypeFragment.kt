package com.my.raido.ui.home.bottomsheet_fragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.Helper.SelectVehicle
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.constants.Constants
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.gone
import com.my.raido.Utils.visible
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentSelectVehicleTypeBinding
import com.my.raido.ui.viewmodels.BookRideViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SelectVehicleTypeFragment : BottomSheetDialogFragment()  { //OnMapReadyCallback

    companion object{
        private const val TAG = "Select Vehicle Type Fragment"
    }

    private lateinit var binding: FragmentSelectVehicleTypeBinding

    private lateinit var myContext: Context

    lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val bookRideViewModel: BookRideViewModel by activityViewModels()

    private val cabViewModel: CabViewModel by activityViewModels()

    private lateinit var map: GoogleMap

    private lateinit var source: LatLng
    private lateinit var destination: LatLng

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

    private var fcmToken: String = ""

    private var userId: String = ""

    private var serviceCategory: Int = 0
    private var rideFare: String = ""
    private var selectedVehicleDuration: String = ""
    private var bookingId: String = ""
//    private fun getWindowHeight() = resources.displayMetrics.heightPixels
//
//    private val map_view_bundle_key = "My_Bundle"
//
//    private lateinit var mapView: MapView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
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

        source = LatLng(26.220376, 73.040453)
        destination = LatLng(26.276547, 73.037121)


        val sourceLatLng: LatLng = sharedPrefManager.getLatLng(Constants.PICKUP_LOCATION)!!
        val destLatLng: LatLng = sharedPrefManager.getLatLng(Constants.DROP_LOCATION)!!


        fcmToken = sharedPrefManager.getString(Constants.FCM_TOKEN, "fcm_xyz").toString()

        userId = sharedPrefManager.getInt(Constants.USER_ID).toString()

//        Log.d(TAG, "onViewCreated: source data => ${sourceLatLng}")
//        Log.d(TAG, "onViewCreated: source data => ${destLatLng}")

        binding.changePaymentModeBtn.setOnClickListener {
            val bottomSheetFragment = PaymentOptionFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.bikeRegularPriceText.paintFlags = binding.bikeRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.autoRegularPriceText.paintFlags = binding.autoRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.cabRegularPriceText.paintFlags = binding.cabRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.bikeliteRegularPriceText.paintFlags = binding.bikeliteRegularPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

//        mapView = binding.googleMap
//        var mapViewBundle: Bundle? = null
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(map_view_bundle_key)
//        }
//        mapView.onCreate(mapViewBundle)
////        val mapFragment = binding.googleMap as SupportMapFragment
//        mapView.getMapAsync(this)

        var selectedVehicle = sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)

        setActiveTab(selectedVehicle)

        binding.bikeCart.setOnClickListener {
            setActiveTab(SelectVehicle.BIKE)
            cabViewModel.updateVehicleType(SelectVehicle.BIKE)
        }

        binding.taxiCart.setOnClickListener {
            setActiveTab(SelectVehicle.AUTO)
            cabViewModel.updateVehicleType(SelectVehicle.AUTO)
        }

        binding.carCart.setOnClickListener {
            setActiveTab(SelectVehicle.CABS)
            cabViewModel.updateVehicleType(SelectVehicle.CABS)
        }

        binding.bikeliteCart.setOnClickListener {
            setActiveTab(SelectVehicle.BIKE_LITE)
            cabViewModel.updateVehicleType(SelectVehicle.BIKE_LITE)
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

            val sourceAddress = sharedPrefManager.getString(Constants.PICKUP_ADDRESS)!!
            val destinationAddress = sharedPrefManager.getString(Constants.DROP_ADDRESS)!!


             sharedPrefManager.putString(Constants.AWAY_TIME, selectedVehicleDuration)
             sharedPrefManager.putString(Constants.TOTAL_FARE, rideFare)


            bookRideViewModel.sendRideRequest(riderId = userId, pickupLat = sourceLatLng.latitude, pickupLng = sourceLatLng.longitude, dropLat = destLatLng.latitude, dropLng = destLatLng.longitude, paymentMode = binding.paymentTypeText.text.toString(), farePrice = rideFare, fcmToken = fcmToken, distance = distance, serviceCategory = serviceCategory, rideDuration = selectedVehicleDuration, selectedvehicleType = selectedMotor, bookingId = bookingId, sourceAddress, destinationAddress)

            dismiss()



            val bottomSheetFragment = LookingForRiderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                .addToBackStack(null)
                .commit()

        }


        cabViewModel.selectedPaymentMode.observe(this) { status ->
            val currentStatus = status ?: "wallet"
            binding.paymentTypeText.text = currentStatus
            paymentMode = currentStatus
        }


    }

    private fun setActiveTab(keyVal: SelectVehicle) {

        sharedPrefManager.saveEnum(Constants.VEHICLE_TYPE, keyVal)

        val primaryColor = ContextCompat.getColor(myContext, R.color.primary)
        val darkColor = ContextCompat.getColor(myContext, R.color.bg_dark_secondary)
        val whiteColor = ContextCompat.getColor(myContext, R.color.white)


        when (keyVal) {
            SelectVehicle.BIKE -> {
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

    override fun onResume() {
        super.onResume()
        bindObservers()
    }

    private fun bindObservers() {
        cabViewModel.rideFareDetailResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {

//                    binding.shimmerEffect.shimmerLayout.gone()
//                    binding.selectVehicleSection.visible()

                    Log.d(TAG, "bindObservers: response received => ${it}")

                    val responseData = it.data

                    binding.shimmerEffect.shimmerLayout.gone()
                    binding.selectVehicleSection.visible()

                    if(responseData?.status == true){
                        val fareRidersData = responseData.fareData

//                        val motordata = responseData.nearbyRiders
//*************************** Bike Detail ************************************************************
                        bikeFare = fareRidersData.bikeDetail.finalFare.toString()
                        binding.bikeTimeText.text = String.format("%s | ",
                            fareRidersData.bikeDetail.arrivalTime
                        )
                        binding.bikeAwayText.text = fareRidersData.bikeDetail.durationTime
                        bikeDuration = fareRidersData.bikeDetail.durationTime
//                        binding.bikeAwayText.text = motordata.nearbyBikes.averageDuration
                        binding.bikeRegularPriceText.text =  String.format("₹ %s", fareRidersData.bikeDetail.fare)
                        binding.bikePriceText.text =  String.format("₹ %s",bikeFare)
//*************************** Bike Detail ************************************************************



//****************************** Auto Detail **********************************************************
                        autoFare = fareRidersData.autoDetail.finalFare.toString()
                        binding.autoTimeText.text = String.format("%s | ", fareRidersData.autoDetail.arrivalTime)
                        binding.autoAwayText.text = fareRidersData.autoDetail.durationTime
                        autoDuration = fareRidersData.autoDetail.durationTime
//                        binding.autoAwayText.text = motordata.nearbyAutos.averageDuration
                        binding.autoRegularPriceText.text =  String.format("₹ %s",fareRidersData.autoDetail.fare)
                        binding.autoPriceText.text =  String.format("₹ %s",autoFare)
//****************************** Auto Detail **********************************************************



//******************************** Cab Detail ****************************************************************
                        cabFare = fareRidersData.cabDetail.finalFare.toString()
                        binding.cabTimeText.text = String.format("%s | ", fareRidersData.cabDetail.arrivalTime)
                        binding.cabAwayText.text = fareRidersData.cabDetail.durationTime
                        cabDuration = fareRidersData.cabDetail.durationTime
//                        binding.cabAwayText.text = motordata.nearbyCabs.averageDuration
                        binding.cabRegularPriceText.text =  String.format("₹ %s",fareRidersData.cabDetail.fare)
                        binding.cabPriceText.text =  String.format("₹ %s",cabFare)
//******************************** Cab Detail ****************************************************************


//  ************************************ Bike Lite ***********************************************************
                        bikeLiteFare = fareRidersData.bikeLiteDetail.finalFare.toString()
                        binding.bikeLiteTimeText.text = String.format("%s | ", fareRidersData.bikeLiteDetail.arrivalTime)
                        binding.bikeLiteAwayText.text = fareRidersData.bikeLiteDetail.durationTime
                        bikeLiteDuration = fareRidersData.bikeLiteDetail.durationTime
//                        binding.bikeLiteAwayText.text = motordata.nearbyBikeLite.averageDuration
                        binding.bikeliteRegularPriceText.text =  String.format("₹ %s",fareRidersData.bikeLiteDetail.fare)
                        binding.bikeLitePriceText.text =  String.format("₹ %s",bikeLiteFare)
//  ************************************ Bike Lite ***********************************************************

                        bookingId = responseData.bookingId

                        distance = responseData.Distance
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

}