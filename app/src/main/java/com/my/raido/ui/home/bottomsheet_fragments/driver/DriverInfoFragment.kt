package com.my.raido.ui.home.bottomsheet_fragments.driver

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentDriverInfoBinding
import com.my.raido.ui.home.HomeActivity
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class DriverInfoFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Driver Info Fragment"
    }

    private lateinit var binding: FragmentDriverInfoBinding

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val cabViewModel: CabViewModel by activityViewModels()

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private lateinit var loader: AlertDialog

    private lateinit var myContext: Context

    private var rideId: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriverInfoBinding.inflate(layoutInflater)

        loader = getLoadingDialog(myContext)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dialog?.dismiss()
        }

        cabViewModel.rideData.observe(viewLifecycleOwner) {jsonString ->
            try {
//                val jsonString = sharedPrefManager.getString(Constants.DRIVER_INFO)

                if(!jsonString.isNullOrEmpty()) {
                    val jsonObject = jsonString?.let { JSONObject(it) } // Convert back to JSONObject
                    // Use jsonObject as needed

                    Log.d(
                        TAG,
                        "onViewCreated: assign driver info is on assign driver screen => ${jsonObject} "
                    )

                    val name = jsonObject?.getString("name")
                    val mobile = jsonObject?.getString("phone")
                    val dropAddress = jsonObject?.getString("dropaddress")
                    val pickUpAddress = jsonObject?.getString("pickupaddress")
                    val price = jsonObject?.getString("fare")
                    val profile = jsonObject?.getString("profile_picture")
                    val tripId = jsonObject?.getString("ride_id")
                    val date = jsonObject?.getString("date")
                    val vehicleName = jsonObject?.getString("vehicle_name")
                    val vehicleType = jsonObject?.getString("vehicle_type")
                    val vehicleNumber = jsonObject?.getString("vehicle_number")

                    rideId = jsonObject?.getInt("newRideId").toString()

                    binding.driverNameText.text = name

                    binding.tripId.text = tripId

                    binding.dateText.text = date

                    Glide.with(myContext)
                        .load("${Constants.IMAGE_BASE_URL}/${profile}")
                        .placeholder(R.drawable.dummyuser) // Placeholder image while loading
                        .error(R.drawable.dummyuser)
                        .transition(DrawableTransitionOptions.withCrossFade()) // Fade transition
                        .into(binding.profilePic)

                    try {
                        binding.pickupLocationText.text = pickUpAddress?.split("||")?.get(0)
                    }catch (err: Exception){
                        binding.pickupLocationText.text = pickUpAddress
                    }

                    try {
                        binding.dropLocationText.text = dropAddress?.split("||")?.get(0)
                    }catch (err: Exception){
                        binding.dropLocationText.text = dropAddress
                    }

                    binding.vehicleNameText.text = vehicleName
                    binding.vehicleNumberText.text = vehicleNumber

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        binding.backHomeBtn.setOnClickListener {
            Log.d(TAG, "onViewCreated: back to home pressed $rideId")
            val ratingCount = binding.ratingEdit.rating.roundToInt()
            if(ratingCount > 0) {
                if (!rideId.isNullOrEmpty()) {
                    cabViewModel.rateDriverApi(
                        rideId = rideId.toString(),
                        rating = ratingCount.toString()
                    )
                }
            }else{
                startActivity(Intent(myContext, HomeActivity::class.java))
                activity?.finish()
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
//                behavior.isDraggable = false
            }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.9).toInt() // 75% of screen height
            bottomSheet.layoutParams = layoutParams
        }

    }

    override fun onResume() {
        super.onResume()
        bindObservers()
    }

    private fun bindObservers() {
        cabViewModel.rateDriverResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)

                    val responseData = it.data

                    if(responseData?.status == true){

                        startActivity(Intent(myContext, HomeActivity::class.java))
                        activity?.finish()

                        cabViewModel.rateDriverResponseLiveData.removeObservers(viewLifecycleOwner)
                        cabViewModel.clearRateDriverRes()


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
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                }
            }
        })
    }

}