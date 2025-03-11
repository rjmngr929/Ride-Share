package com.my.raido.ui.home.bottomsheet_fragments.driver

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.constants.Constants
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.invisible
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.visible
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentAssignDriverBinding
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class AssignDriverFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Assign Driver Fragment"
    }

    private lateinit var binding: FragmentAssignDriverBinding

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val cabViewModel: CabViewModel by activityViewModels()

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private lateinit var loader: AlertDialog

    private var userId by Delegates.notNull<Int>()

    private var isExpand = false

    private var isDriverDataFetched = false

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssignDriverBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        userId = sharedPrefManager.getInt(Constants.USER_ID, 0)

        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Capture the back button press using the OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

//        val driverDetails = cabViewModel.driverDetails.value
//
//        Log.d(TAG, "onViewCreated: driver details => $cabViewModel.")
//
//        if(driverDetails != null) {
//            val awayTime = sharedPrefManager.getString(Constants.AWAY_TIME, "")
//            val rideFare = sharedPrefManager.getString(Constants.TOTAL_FARE, "")
//
//            binding.pinText.text = String.format("Pin : %s", driverDetails.otp)
//            binding.vehicleModelText.text = driverDetails.vehicleName
//            binding.vehicleNumberText.text = driverDetails.vehicleNumber
//
//            binding.driverNameText.text = driverDetails.driverName
//
//            if(!awayTime.isNullOrEmpty())
//                binding.awayTimeText.text = String.format("%s min", awayTime)
//
//            if(!rideFare.isNullOrEmpty())
//                binding.totalFarePrice.text = String.format("₹ %s", rideFare)
//        }
//
//        cabViewModel.driverDetails.observe(viewLifecycleOwner) { driverDetail ->
//            Log.d(TAG, "onViewCreated: driver detail => ${driverDetail}")
//            if(driverDetail != null) {
//                val awayTime = sharedPrefManager.getString(Constants.AWAY_TIME, "")
//                val rideFare = sharedPrefManager.getString(Constants.TOTAL_FARE, "")
//
//                binding.pinText.text = String.format("Pin : %s", driverDetail.otp)
//                binding.vehicleModelText.text = driverDetail.vehicleName
//                binding.vehicleNumberText.text = driverDetail.vehicleNumber
//
//                binding.driverNameText.text = driverDetail.driverName
//
//                if(!awayTime.isNullOrEmpty())
//                    binding.awayTimeText.text = String.format("%s min", awayTime)
//
//                if(!rideFare.isNullOrEmpty())
//                    binding.totalFarePrice.text = String.format("₹ %s", rideFare)
//            }
//        }

        cabViewModel.fetchDataForDriver(userId = userId.toString()){ rideHistoryId ->
            Log.d(TAG, "onViewCreated: $rideHistoryId")
            if (!rideHistoryId.isNullOrEmpty() && !isDriverDataFetched){
                cabViewModel.fetchDriverDetail(rideHistoryId.toString())
            }

        }

        val pickUpFullAddress = sharedPrefManager.getString(Constants.PICKUP_ADDRESS).toString()
        val dropFullAddress = sharedPrefManager.getString(Constants.DROP_ADDRESS).toString()

        if(pickUpFullAddress.isNotEmpty() || pickUpFullAddress.contains("||")){
            val splitAddress = pickUpFullAddress.split("||")
            if(splitAddress.isNotEmpty() && splitAddress.size == 2){
                binding.collapsePickupAddressText.text = splitAddress[0]
                binding.pickupAddressText.text = splitAddress[0]
                binding.pickupSubAddressText.text = splitAddress[1]
            }
        }

        if(dropFullAddress.isNotEmpty() || dropFullAddress.contains("||")){
            val splitAddress = dropFullAddress.split("||")
            if(splitAddress.isNotEmpty() && splitAddress.size == 2) {
                binding.dropAddressText.text = splitAddress[0]
                binding.dropSubAddressText.text = splitAddress[1]
            }
        }

        binding.arrowIconBtn.setOnClickListener {
            if(isExpand){
                binding.expandLayout.gone()
                binding.collapsePickupAddressText.visible()
            }else{
                binding.expandLayout.visible()
                binding.collapsePickupAddressText.gone()
            }
            isExpand = !isExpand
        }


        binding.cancelRideBtn.setOnClickListener {
//            activity?.finish()
            val bottomSheetFragment = DriverInfoFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
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

    override fun onResume() {
        super.onResume()

        observer()
    }

    fun observer(){
        cabViewModel.driverDetailResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                        hideLoader(myContext, loader)
//                    binding.shimmerEffect.shimmerLayout.gone()
//                    binding.selectVehicleSection.visible()

                    isDriverDataFetched = true

                    Log.d(TAG, "bindObservers: response received => ${it}")

                    val responseData = it.data
                    
                    val driverDetail = responseData?.driverDetail

                    if(driverDetail != null) {
                        val awayTime = sharedPrefManager.getString(Constants.AWAY_TIME, "")
                        val rideFare = sharedPrefManager.getString(Constants.TOTAL_FARE, "")

                        binding.pinText.text = String.format("Pin : %s", driverDetail.otp)

                        if(!driverDetail.vehicleName.isNullOrEmpty() && !driverDetail.vehicleNumber.isNullOrEmpty()){
                            binding.vehicleDetailLayout.visible()
                            binding.vehicleModelText.text = driverDetail.vehicleName
                            binding.vehicleNumberText.text = driverDetail.vehicleNumber
                        }else{
                            binding.vehicleDetailLayout.invisible()
                        }


                        binding.driverNameText.text = driverDetail.driverName

                        if(!awayTime.isNullOrEmpty())
                            binding.awayTimeText.text = String.format("%s min", awayTime)

                        if(!rideFare.isNullOrEmpty())
                            binding.totalFarePrice.text = String.format("₹ %s", rideFare)
                    }

//                    Log.d(TAG, "observer: driver detail name => ${driverDetail?.driverName}")


                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)
                    alertDialogService.alertDialogAnim(
                        myContext,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                }
            }
        })
    }


    }