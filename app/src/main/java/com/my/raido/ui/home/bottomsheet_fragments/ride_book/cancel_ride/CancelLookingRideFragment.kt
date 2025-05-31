package com.my.raido.ui.home.bottomsheet_fragments.ride_book.cancel_ride

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentCancelLookingRideBinding
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.CancelRideFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.LookingForRiderFragment
import com.my.raido.ui.viewmodels.MasterViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CancelLookingRideFragment : BottomSheetDialogFragment() {

    companion object{
        const val TAG = "Cancel Looking Ride Fragment"

        fun newInstance(): CancelLookingRideFragment {
            return CancelLookingRideFragment()
        }
    }

    private lateinit var binding: FragmentCancelLookingRideBinding

    private val masterViewModel: MasterViewModel by viewModels()

    @Inject
    lateinit var sharedPreference: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private var sourceAddress = ""
    private var destinationAddress = ""

    private lateinit var loader: AlertDialog

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCancelLookingRideBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext, R.raw.loader)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        sourceAddress = sharedPreference.getString(Constants.PICKUP_ADDRESS)!!
        destinationAddress = sharedPreference.getString(Constants.DROP_ADDRESS)!!


        val splitSourceAddress = sourceAddress.split("||")
        if(splitSourceAddress.isNotEmpty() && splitSourceAddress.size == 2){
            binding.pickupAddressText.text = splitSourceAddress[0]
            binding.pickupSubAddressText.text = splitSourceAddress[1]
        }

        val splitDestinationAddress = destinationAddress.split("||")
        if(splitDestinationAddress.isNotEmpty() && splitDestinationAddress.size == 2){
            binding.dropAddressText.text = splitDestinationAddress[0]
            binding.dropSubAddressText.text = splitDestinationAddress[1]
        }

        val fare = sharedPreference.getString(Constants.TOTAL_FARE, "")

        binding.totalFarePrice.text = String.format("₹%s", fare)

        // Capture the back button press using the OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                // Do nothing when we press back button
                dismiss()

                val bottomSheetFragment = LookingForRiderFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

        binding.cancelRideBackBtn.setOnSingleClickListener {
            val bottomSheetFragment = LookingForRiderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.cancelRideBtn.setOnSingleClickListener {

            masterViewModel.setCancelRideData(true)

            val bottomSheetFragment = CancelRideFragment()
            parentFragmentManager.beginTransaction()
                .add(R.id.fragment_container_bookride, bottomSheetFragment)
                .addToBackStack(null)
                .commit()
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