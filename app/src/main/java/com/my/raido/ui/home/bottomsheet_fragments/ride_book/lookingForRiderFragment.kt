package com.my.raido.ui.home.bottomsheet_fragments.ride_book

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.observeOnce
import com.my.raido.constants.SocketConstants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentLookingForRiderBinding
import com.my.raido.services.BookingStatus
import com.my.raido.services.ManageBooking
import com.my.raido.socket.SocketManager
import com.my.raido.ui.home.bottomsheet_fragments.driver.AssignDriverFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.cancel_ride.CancelLookingRideFragment
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class LookingForRiderFragment : BottomSheetDialogFragment() {

    companion object{
        const val TAG = "Looking For Rider Fragment"

        fun newInstance(): LookingForRiderFragment {
            return LookingForRiderFragment()
        }
    }

    private lateinit var binding: FragmentLookingForRiderBinding

    @Inject
    lateinit var sharedPreference: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var socketManager: SocketManager

    private var manageBookingOperations: ManageBooking? = null

    private val cabViewModel: CabViewModel by activityViewModels()

    private lateinit var jsonResData : JSONObject

    private lateinit var loader: AlertDialog

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
        binding = FragmentLookingForRiderBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext, R.raw.loader)

        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cabViewModel.createBookingData.observeOnce (viewLifecycleOwner){data ->

            socketManager.sendData(SocketConstants.CREATE_BOOKING, data) { status ->
                Log.d(TAG, "onViewCreated: createBooking emit status => ${status} ")
                manageBookingOperations?.performOperation(BookingStatus.LOOKING_DRIVER)
            }

            socketManager.listenToEvent(SocketConstants.RIDE_ACCEPT_RESPONSE) { response ->
                Log.d(TAG, "onViewCreated: assign driver info is $response")
                jsonResData = response
                val status = response.getBoolean("status")
                if (status) {
                    hideLoader(myContext, loader)
                    manageBookingOperations?.performOperation(BookingStatus.RIDE_ACCEPTED)

//                        if (isAdded) {
//                            replaceFragment(
//                                LookingForRiderFragment(),
//                                AssignDriverFragment(),
//                                response
//                            )
//                        }

                    cabViewModel.setRiderData(response.toString())



//                            val bottomSheetFragment = AssignDriverFragment()
//                            parentFragmentManager.beginTransaction()
//                                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                                .addToBackStack(null)
//                                .commit()

                    Handler(Looper.getMainLooper()).post {
                        replaceFragmentSafely(this, AssignDriverFragment())
                    }


//
                }
            }

            socketManager.listenToEvent(SocketConstants.RIDE_ACCEPT_ERROR) { response ->
                hideLoader(myContext, loader)
                Log.d(TAG, "onViewCreated: assign driver info is $response")
            }


        }

//        showLoader(myContext, loader)
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            hideLoader(myContext, loader)
//
//            view?.showSnack("Rider is available for accept ride.")
//
//            dismiss()
//
//            val bottomSheetFragment = SelectVehicleTypeFragment()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                .addToBackStack(null)
//                .commit()
//        }, 12000)

        // Capture the back button press using the OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    // Do nothing when we press back button
                dismiss()

//                val bottomSheetFragment = SelectVehicleTypeFragment()
                val bottomSheetFragment = CancelLookingRideFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

//*********************** Socket Listener *********************************************************
//        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
//            override fun onConnected() {
//
//                socketManager.listenToEvent(SocketConstants.RIDE_ACCEPT_RESPONSE) { response ->
//                    Log.d(TAG, "onViewCreated: assign driver info is $response")
//                    jsonResData = response
//                    val status = response.getBoolean("status")
//                    if (status) {
//                        hideLoader(myContext, loader)
//                        manageBookingOperations?.performOperation(BookingStatus.RIDE_ACCEPTED)
//
////                        if (isAdded) {
////                            replaceFragment(
////                                LookingForRiderFragment(),
////                                AssignDriverFragment(),
////                                response
////                            )
////                        }
//
//                        cabViewModel.setRiderData(response.toString())
//
////                        if(isAdded) {
//                            val bottomSheetFragment = AssignDriverFragment()
//                            parentFragmentManager.beginTransaction()
//                                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                                .addToBackStack(null)
//                                .commit()
////                        }
//
////                        try {
////                            replaceFragment(
////                                LookingForRiderFragment(),
////                                AssignDriverFragment(),
////                                response
////                            )
////                        }catch (err: Exception){
////                            Log.d(TAG, "onConnected: looking for rider to assign driver screen. $err")
////                        }
////
//                    }
//                }
//
//                socketManager.listenToEvent(SocketConstants.RIDE_ACCEPT_ERROR) { response ->
//                    hideLoader(myContext, loader)
//                    Log.d(TAG, "onViewCreated: assign driver info is $response")
//                }
//            }
//        })
//*********************** Socket Listener *********************************************************


    }

    private fun replaceFragmentSafely(currentFragment: Fragment, newFragment: Fragment) {
        val fragmentManager = currentFragment.parentFragmentManager

        // Prevent crash if fragment is not attached
        if (!currentFragment.isAdded || fragmentManager.isStateSaved) {
            Log.w("FragmentTransaction", "Fragment not attached or state saved — skipping replace")
            return
        }

        // Dismiss current DialogFragment if showing
        (currentFragment as? DialogFragment)?.dismissAllowingStateLoss()

        try {
            fragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_bookride, newFragment, newFragment::class.java.simpleName)
                addToBackStack(null)
                commitAllowingStateLoss()
            }
        } catch (e: IllegalStateException) {
            Log.e("FragmentTransaction", "Commit failed: ${e.localizedMessage}", e)
        }
    }


    override fun onResume() {
        super.onResume()

//        if(::jsonResData.isInitialized) {
//            val status = jsonResData.getBoolean("status")
//            if (status) {
//                manageBookingOperations?.performOperation(BookingStatus.ACCEPTED)
//
//                if (isAdded) {
//                    replaceFragment(LookingForRiderFragment(), AssignDriverFragment(), jsonResData)
//                }
//
//            }
//        }

    }

    private fun replaceFragment(currentFragment: Fragment, newFragment: Fragment) {
        val fragmentManager = parentFragmentManager

        // Find and dismiss the current fragment if it's a DialogFragment
        (fragmentManager.findFragmentByTag(currentFragment.javaClass.simpleName) as? DialogFragment)?.let { dialogFragment ->
            if (dialogFragment.isAdded) {
                dialogFragment.dismissAllowingStateLoss()
            }
        }

        // Check if fragment manager is in a valid state
        if (!fragmentManager.isStateSaved) {
            // Perform the fragment transaction safely
            try {
                fragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_bookride, newFragment, newFragment.javaClass.simpleName)
                    addToBackStack(null) // Optional: Adds transaction to back stack
                    commit() // Use commit() for safety
                }
            } catch (e: Exception) {
                Log.e("FragmentTransaction", "Error replacing fragment: ${e.localizedMessage}", e)
            }
        } else {
            Log.w("FragmentTransaction", "Cannot replace fragment, state is already saved")
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