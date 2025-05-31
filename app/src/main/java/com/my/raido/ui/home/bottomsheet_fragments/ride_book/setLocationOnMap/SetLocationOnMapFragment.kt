package com.my.raido.ui.home.bottomsheet_fragments.ride_book.setLocationOnMap

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.showSnack
import com.my.raido.databinding.FragmentSetLocationOnMapBinding
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.sdk.core.client.Platform
import com.ola.maps.sdk.core.config.PlatformConfig
import com.ola.maps.sdk.model.nearbysearch.request.NearbySearchRequest
import com.ola.maps.sdk.model.nearbysearch.response.NearbySearchResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SetLocationOnMapFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Set Location On Map Fragment"
    }

    private lateinit var binding: FragmentSetLocationOnMapBinding

    private lateinit var myContext: Context

    private val cabViewModel: CabViewModel by activityViewModels()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var setLocationFor = "dest"

    private lateinit var latLngVal: LatLng
    private  var addressText: String = ""
    private  var subAddressText: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    val config: PlatformConfig = PlatformConfig.Builder()
        .apiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
        .maxRetryAttempts(3)
        .baseUrl("https://api.olamaps.io/")
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetLocationOnMapBinding.inflate(inflater, container, false)

        // Retrieve ViewModel using ViewModelProvider
//        sharedViewModel = ViewModelProvider(requireActivity())[MapSharedViewModel::class.java]

        cabViewModel.markerLocation.observe(viewLifecycleOwner) { latlng ->
            latLngVal = latlng
            fetchAddressInCoroutine(latlng)
        }

        setLocationFor = cabViewModel.setLocationFor.value.toString()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Capture the back button press using the OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })


        binding.alertdialogCloseBtn.setOnClickListener {
            requireActivity().finish()
//            dismiss()
        }

        binding.selectAddressBtn.setOnClickListener {
            val intent = Intent()
            intent.putExtra("setFor", setLocationFor)
            intent.putExtra("latLng", latLngVal)
            intent.putExtra("addressText", addressText)
            intent.putExtra("subAddressText", subAddressText)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()

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


    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel the coroutine when the activity is destroyed
    }


    // Coroutine function to fetch address from LatLng
    private fun fetchAddressInCoroutine(latLng: LatLng) {
        lifecycleScope.launch {
            val address = withContext(Dispatchers.IO) {
//                getAddressFromLatLng(latLng)
                fetchAddress(latLng)
            }

            if(address != null){
                address.let {
                    Log.d(TAG, "fetchAddressInCoroutine: fetch address data => ${it.predictions.size}")
                    if (it.predictions.isNotEmpty()) {
                        //                    Log.d(TAG, "fetchAddressInCoroutine: fetch address data => ${it.predictions[0].structuredFormatting?.mainText}")
                        //                    Log.d(TAG, "fetchAddressInCoroutine: fetch address data => ${it.predictions[0].structuredFormatting?.secondaryText}")
                        addressText = it.predictions[0].structuredFormatting?.mainText.toString()
                        subAddressText =
                            it.predictions[0].structuredFormatting?.secondaryText.toString()
                        binding.addressText.text = addressText
                        binding.localityText.text = subAddressText
                    }
                }
            }else{
                view?.showSnack(message = "Unable to fetch address", textColor = R.color.white, bgColor = R.color.failed)
            }

            
        }
    }

    private fun fetchAddress(latLng: LatLng) : NearbySearchResponse? {
        try {
            val nearbySearchClient = Platform.getNearbySearchClient(config)

            val nearbySearchRequest = NearbySearchRequest.Builder()
                .limit(1)
                .radius(100)
                .location("${latLng.latitude}, ${latLng.longitude}")
                .withCentroid(withCentroid = true)
                .strictBounds(strictBounds = true)
                .build()

            val nearbySearchResponse = nearbySearchClient.nearbySearch(nearbySearchRequest)

            return nearbySearchResponse
        }catch (err: Exception){
            return null
        }

    }



}