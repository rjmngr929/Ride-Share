package com.my.raido.ui.home.bottomsheet_fragments.driver

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.Helper
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.invisible
import com.my.raido.Utils.visible
import com.my.raido.constants.Constants
import com.my.raido.constants.SocketConstants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentAssignDriverBinding
import com.my.raido.services.BookingStatus
import com.my.raido.services.ManageBooking
import com.my.raido.socket.SocketManager
import com.my.raido.ui.home.HomeActivity
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.CancelRideFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.ChatSupportFragment
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
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

    private val masterViewModel: MasterViewModel by viewModels()

    private val cabViewModel: CabViewModel by activityViewModels()

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var socketManager: SocketManager

    private var manageBookingOperations: ManageBooking? = null

    private lateinit var loader: AlertDialog

    private var userId by Delegates.notNull<Int>()

    private var rideId = ""

    private var isExpanded = false
    private var bottomSheet: View? = null
    private var behavior: BottomSheetBehavior<View>? = null

    private var layoutHeight = 0

    private lateinit var myContext: Context

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

        val pinCode = sharedPrefManager.getString(Constants.RIDE_OTP)
        binding.pinText.text = String.format("Pin : %s", pinCode)

        rotateArrow(binding.dropdownBtn, 0f, 180f)

//        ********** clear previous activities *********************************
        HomeActivity.instance?.finish()
//        ********** clear previous activities *********************************


//        ****************************** Received from socket *************************************

        cabViewModel.rideData.observe(viewLifecycleOwner) { jsonString ->
            try {
                val jsonObject = JSONObject(jsonString) // Convert back to JSONObject
                // Use jsonObject as needed

                cabViewModel.rideData.removeObservers(viewLifecycleOwner)
                Log.d(TAG, "onViewCreated: assign driver info is on assign driver screen => ${jsonObject} ")

                val name = jsonObject.getString("name")
                val mobile = jsonObject.getString("phone")
                val profile = jsonObject.getString("profile_picture")
                val dropAddress = jsonObject.getString("dropaddress")
                val pickUpAddress = jsonObject.getString("pickupaddress")
                val price = jsonObject.getString("fare")
                val vehicleName = jsonObject.getString("vehicle_name")
                val vehicleType = jsonObject.getString("vehicle_type")

                val vehicleNumber = jsonObject.getString("vehicle_number")

                rideId = jsonObject?.getString("newRideId").toString()

                binding.driverNameText.text = name
                binding.vehicleModelText.text = vehicleName
                binding.vehicleNumberText.text = vehicleNumber
                binding.totalFarePrice.text = String.format("₹ %s", price)

                Glide.with(myContext)
                    .load("${Constants.IMAGE_BASE_URL}/${profile}")
                    .placeholder(R.drawable.dummyuser) // Placeholder image while loading
                    .error(R.drawable.dummyuser)
                    .transition(DrawableTransitionOptions.withCrossFade()) // Fade transition
                    .into(binding.profilePic)

                sharedPrefManager.putString(Constants.DRIVER_INFO, jsonString.toString())

                binding.callDriverBtn.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$mobile")
                    }
                    startActivity(intent)
                }

                binding.chatSupportBtn.setOnClickListener {

                    val sheet = ChatSupportFragment()
                    sheet.arguments = Bundle().apply {
                        putString("mobile_number",mobile)
                    }
                    val existingSheet = childFragmentManager.findFragmentByTag(sheet.tag)
                    if (existingSheet == null) {
                        sheet.show(childFragmentManager, sheet.tag)
                    }

//                    val bottomSheetFragment = ChatSupportFragment()
//                    bottomSheetFragment.arguments = Bundle().apply {
//                        putString("mobile_number",mobile)
//                    }
//                    parentFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                        .addToBackStack(null)
//                        .commit()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        arguments?.getString("json_data")?.let { jsonString ->
//            try {
//                val jsonObject = JSONObject(jsonString) // Convert back to JSONObject
//                // Use jsonObject as needed
//
//                Log.d(TAG, "onViewCreated: assign driver info is on assign driver screen => ${jsonObject} ")
//
//                val name = jsonObject.getString("name")
//                val mobile = jsonObject.getString("phone")
//                val profile = jsonObject.getString("profile_picture")
//                val dropAddress = jsonObject.getString("dropaddress")
//                val pickUpAddress = jsonObject.getString("pickupaddress")
//                val price = jsonObject.getString("fare")
//                val vehicleName = jsonObject.getString("vehicle_name")
//                val vehicleType = jsonObject.getString("vehicle_type")
//
//                val vehicleNumber = jsonObject.getString("vehicle_number")
//
//                rideId = jsonObject?.getString("newRideId").toString()
//
//                binding.driverNameText.text = name
//                binding.vehicleModelText.text = vehicleName
//                binding.vehicleNumberText.text = vehicleNumber
//                binding.totalFarePrice.text = String.format("₹ %s", price)
//
//                Glide.with(myContext)
//                    .load("${Constants.IMAGE_BASE_URL}/${profile}")
//                    .placeholder(R.drawable.dummyuser) // Placeholder image while loading
//                    .error(R.drawable.dummyuser)
//                    .transition(DrawableTransitionOptions.withCrossFade()) // Fade transition
//                    .into(binding.profilePic)
//
//                sharedPrefManager.putString(Constants.DRIVER_INFO, jsonString)
//
//                binding.callDriverBtn.setOnClickListener {
//                    val intent = Intent(Intent.ACTION_DIAL).apply {
//                        data = Uri.parse("tel:$mobile")
//                    }
//                    startActivity(intent)
//                }
//
//                binding.chatSupportBtn.setOnClickListener {
//
//                    val sheet = ChatSupportFragment()
//                    sheet.arguments = Bundle().apply {
//                        putString("mobile_number",mobile)
//                    }
//                    val existingSheet = childFragmentManager.findFragmentByTag(sheet.tag)
//                    if (existingSheet == null) {
//                        sheet.show(childFragmentManager, sheet.tag)
//                    }
//
////                    val bottomSheetFragment = ChatSupportFragment()
////                    bottomSheetFragment.arguments = Bundle().apply {
////                        putString("mobile_number",mobile)
////                    }
////                    parentFragmentManager.beginTransaction()
////                        .replace(R.id.fragment_container_bookride, bottomSheetFragment)
////                        .addToBackStack(null)
////                        .commit()
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        ****************************** Received from socket *************************************


        masterViewModel.dashboardData.observe(viewLifecycleOwner, Observer{driverData ->

            if(driverData != null) {
                rideId = driverData.id.toString()

                binding.driverNameText.text = driverData.driverName
                binding.vehicleModelText.text = driverData.vehicleName
                binding.vehicleNumberText.text = driverData.vehicleNumber
                binding.totalFarePrice.text =
                    String.format("₹ %s", driverData.finalFareAfterDiscount)

                Glide.with(myContext)
                    .load("${Constants.IMAGE_BASE_URL}/${driverData.driverImage}")
                    .placeholder(R.drawable.dummyuser) // Placeholder image while loading
                    .error(R.drawable.dummyuser)
                    .transition(DrawableTransitionOptions.withCrossFade()) // Fade transition
                    .into(binding.profilePic)

                binding.callDriverBtn.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${driverData.driverMobile}")
                    }
                    startActivity(intent)
                }
            }
        })


//  ***************************** Socket Listener **************************************************

//        cabViewModel.rideCompleteData.observe(viewLifecycleOwner) {data->
//
//            Log.d(TAG, "onViewCreated: data updated success! $data")
//
//            if(!data.isNullOrBlank()){
//                if (data.getBoolean("status")) {
//                    if (data.getString("message") == "Ride Complete") {
//
//                        masterViewModel.setWalletBalanceData(
//                            data.getDouble("wallet_balance").toString()
//                        )
//
//                        replaceFragment(AssignDriverFragment(), DriverInfoFragment())
//                    }
//                }
//            }
//        }

//        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
//            override fun onConnected() {

                socketManager.listenToEvent("welcomeMessage1") { data ->
                    Log.d(TAG, "onViewCreated: welcome message 1")
                }


                socketManager.listenToEvent(SocketConstants.RIDER_RIDE_START) { data ->
                    Log.d(TAG, "onViewCreated: ride started success")
                    manageBookingOperations?.performOperation(BookingStatus.RIDE_STARTED)
                }

                socketManager.listenToEvent(SocketConstants.USER_INFO_RIDE_COMPLETE) { data ->
                    if (data.getBoolean("status")) {
                        if (data.getString("message") == "Ride Complete") {

                            manageBookingOperations?.performOperation(BookingStatus.RIDE_COMPLETED)

                            masterViewModel.setWalletBalanceData(
                                data.getDouble("wallet_balance").toString()
                            )

                            replaceFragment(AssignDriverFragment(), DriverInfoFragment())
                        }
                    }

                }

                socketManager.listenToEvent("receive_message_by_user") { data ->
                    Log.d(TAG, "onViewCreated: driver message receive to user => $data")
                }
//            }
//        })
//  ***************************** Socket Listener **************************************************


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


//        val toggleButton = findViewById<Button>(R.id.toggleButton)
//        val expandableView = findViewById<View>(R.id.expandableView)

        binding.arrowIconBtn.setOnClickListener {
//            if(isExpand){
//                binding.expandLayout.gone()
//                binding.collapsePickupAddressText.visible()
//            }else{
//                binding.expandLayout.visible()
//                binding.collapsePickupAddressText.gone()
//            }
//            isExpand = !isExpand

            if (binding.expandLayout.visibility == View.VISIBLE) {
                Helper.collapseView(binding.expandLayout, binding.arrowIconBtn)
                binding.collapsePickupAddressText.visible()
            } else {
                Helper.expandView(binding.expandLayout, binding.arrowIconBtn)
                binding.collapsePickupAddressText.invisible()
            }
        }


        binding.cancelRideBtn.setOnClickListener {
//            activity?.finish()
            val bottomSheetFragment = CancelRideFragment()
            bottomSheetFragment.arguments = Bundle().apply {
                putString("ride_id", rideId)
            }

            val existingSheet = childFragmentManager.findFragmentByTag(bottomSheetFragment.tag)
            if (existingSheet == null) {
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }

//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

        }

        val myLayout = binding.rootLayout

//        myLayout.viewTreeObserver.addOnGlobalLayoutListener {
//            val height = myLayout.height
//            Log.d("BottomSheet", "Layout height: $height")
//            // Remove the listener so it runs only once
//            myLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
//        }

        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layoutHeight = myLayout.height
                // Remove the listener
                myLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        myLayout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)


        binding.dropdownBtn.setOnClickListener {
//            if (binding.assignDriverScrollLayout.visibility == View.VISIBLE) {
//                Helper.collapseView(binding.assignDriverScrollLayout, binding.dropdownBtn)
//            } else {
//                Helper.expandView(binding.assignDriverScrollLayout, binding.dropdownBtn)
//            }

            if (binding.assignDriverScrollLayout.isGone) {
                animateVisibility(binding.assignDriverScrollLayout, true)
            } else {
                animateVisibility(binding.assignDriverScrollLayout, false)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog


        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            Log.d(TAG, "onCreateDialog: height => ${ViewGroup.LayoutParams.MATCH_PARENT}")


//            This is for avoid bottomsheet dismiss on dragging
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                // Disable dragging
                behavior.isDraggable = false
            }
        }

        return dialog


    }



    private fun updateHeight() {
        val newHeight = if (isExpanded) {
            // 90% of screen
            (Resources.getSystem().displayMetrics.heightPixels * 0.9).toInt()
        } else {
            // 40% of screen
            (Resources.getSystem().displayMetrics.heightPixels * 0.4).toInt()
        }

        Log.d(TAG, "updateHeight: data is ${newHeight}, $isExpanded and ${binding.root == null}")

        binding.root?.let {
            it.layoutParams.height = newHeight
            it.requestLayout()
        }
    }

    private fun animateVisibility(view: View, show: Boolean) {

        if (show) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )
//            val targetHeight = view.measuredHeight
            val targetHeight = layoutHeight

            view.layoutParams.height = 0
            view.visibility = View.VISIBLE



            val animator = ValueAnimator.ofInt(0, targetHeight)
            animator.addUpdateListener {
                view.layoutParams.height = it.animatedValue as Int
                view.requestLayout()
            }
            animator.duration = 300
            animator.start()

            // Rotate arrow to point downwards (180 to 0 degrees)
            rotateArrow(binding.dropdownBtn, 0f, 180f)
        } else {
            val initialHeight = view.measuredHeight
            val animator = ValueAnimator.ofInt(initialHeight, 0)
            animator.addUpdateListener {
                view.layoutParams.height = it.animatedValue as Int
                view.requestLayout()
            }
            animator.duration = 300
            animator.doOnEnd {
                view.visibility = View.GONE
            }
            animator.start()

            // Rotate arrow to point downwards (180 to 0 degrees)
            rotateArrow(binding.dropdownBtn, 180f, 0f)
        }
    }

    // Rotate the arrow ImageButton smoothly
    private fun rotateArrow(arrow: ImageButton, fromDegree: Float, toDegree: Float) {
        ObjectAnimator.ofFloat(arrow, "rotation", fromDegree, toDegree).apply {
            duration = 300
            start()
        }
    }


    private fun replaceFragment(currentFragment: Fragment, newFragment: Fragment) {

        try {
            val fragmentManager = parentFragmentManager

            // Find and dismiss the current fragment if it's a DialogFragment
            (fragmentManager.findFragmentByTag(currentFragment.javaClass.simpleName) as? DialogFragment)?.let { dialogFragment ->
                if (dialogFragment.isAdded) {
                    dialogFragment.dismissAllowingStateLoss()
                }
            }

            // Check if fragment manager is in a valid state
            if (!fragmentManager.isStateSaved) {

                fragmentManager.beginTransaction().apply {
                    replace(
                        R.id.fragment_container_bookride,
                        newFragment,
                        newFragment.javaClass.simpleName
                    )
                    addToBackStack(null) // Optional: Adds transaction to back stack
                    commit() // Use commit() for safety
                }

            } else {
                Log.w("FragmentTransaction", "Cannot replace fragment, state is already saved")
            }
        }catch (err: Exception){
            Log.d(TAG, "replaceFragment: error when we switch from assign driver to driver info $err")
            if(isAdded) {
                val context = requireContext()
                startActivity(Intent(context, HomeActivity::class.java))
                activity?.finish()
            }
        }

    }

}