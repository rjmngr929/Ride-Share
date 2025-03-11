package com.my.raido.ui.home.bottomsheet_fragments

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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.constants.Constants
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentLookingForRiderBinding
import com.my.raido.services.BookingStatus
import com.my.raido.services.ManageBooking
import com.my.raido.ui.home.bottomsheet_fragments.driver.AssignDriverFragment
import com.my.raido.ui.viewmodels.BookRideViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
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

    private var manageBookingOperations: ManageBooking? = null

    private val bookRideViewModel: BookRideViewModel by activityViewModels()
    private val cabViewModel: CabViewModel by activityViewModels()

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

        loader = getLoadingDialog(myContext)

        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Capture the back button press using the OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    // Do nothing when we press back button
                dismiss()

                val bottomSheetFragment = SelectVehicleTypeFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

        // Perform some operation after a delay
//        lifecycleScope.launch {
//            delay(2000) // 2 seconds delay
//
//            val bottomSheetFragment = AssignDriverFragment()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                .addToBackStack(null)
//                .commit()
//
//        }


        val userId = sharedPreference.getInt(Constants.USER_ID)
        bookRideViewModel.acceptRide(userId = userId){status, driverId ->

            Log.d(TAG, "onViewCreated: rider accept status => ${status}")
            
            if(status == "accepted"){

                manageBookingOperations?.performOperation(BookingStatus.ACCEPTED)

                if (driverId != null) {
                    sharedPreference.putInt(Constants.DRIVER_ID, driverId)
                }

//                if (driverId != null) {
//                    cabViewModel.fetchDriverDetail(driverId)
//                }

                replaceFragment(LookingForRiderFragment(), AssignDriverFragment())

////                dismiss()
//                dismissAllowingStateLoss()
//                val bottomSheetFragment = AssignDriverFragment()
////                val bundle = Bundle()
////                bundle.putParcelable("sourcelatLng", sourceLatLng)
////                bundle.putParcelable("destlatLng", destinationLatLng)
////                bottomSheetFragment.arguments = bundle
//                parentFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                    .addToBackStack(null)
//                    .commitAllowingStateLoss()

            }
        }

    }

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(currentFragment: Fragment, newFragment: Fragment){
        // Check if the current fragment exists and is attached
//        val removeFragment = parentFragmentManager.findFragmentByTag(currentFragment.javaClass.simpleName) as? DialogFragment
//        removeFragment?.dismissAllowingStateLoss()
////        val bottomSheetFragment = AssignDriverFragment()
//        parentFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container_bookride, newFragment, newFragment.javaClass.simpleName)
//            .addToBackStack(null)
//            .commitAllowingStateLoss()



        val removeFragment = parentFragmentManager.findFragmentByTag(currentFragment.javaClass.simpleName) as? DialogFragment

        if (removeFragment != null && removeFragment.isAdded) {
            removeFragment.dismissAllowingStateLoss()
        }

        if (!parentFragmentManager.isStateSaved) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_bookride, newFragment, newFragment.javaClass.simpleName)
                .addToBackStack(null)
                .commitAllowingStateLoss()
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