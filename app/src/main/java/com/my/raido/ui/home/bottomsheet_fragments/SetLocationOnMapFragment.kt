package com.my.raido.ui.home.bottomsheet_fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.databinding.FragmentSetLocationOnMapBinding
import com.my.raido.ui.home.MapSharedViewModel
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
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class SetLocationOnMapFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Set Location On Map Fragment"
    }

    private lateinit var binding: FragmentSetLocationOnMapBinding

    private lateinit var myContext: Context

//    @Inject
//    lateinit var sharedViewModel: MapSharedViewModel

    private lateinit var sharedViewModel: MapSharedViewModel

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
        .apiKey("1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S")
        .maxRetryAttempts(3)
        .baseUrl("https://api.olamaps.io/")
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetLocationOnMapBinding.inflate(inflater, container, false)

        // Retrieve ViewModel using ViewModelProvider
        sharedViewModel = ViewModelProvider(requireActivity())[MapSharedViewModel::class.java]

        sharedViewModel.markerLocation.observe(viewLifecycleOwner) { latlng ->
            latLngVal = latlng
            fetchAddressInCoroutine(latlng)
        }

        setLocationFor = sharedViewModel.setLocationFor.value.toString()

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
//            startActivity(Intent(myContext, BookRideActivity::class.java))
//            requireActivity().finish()

            val intent = Intent()
            intent.putExtra("setFor", setLocationFor)
            intent.putExtra("latLng", latLngVal)
            intent.putExtra("addressText", addressText)
            intent.putExtra("subAddressText", subAddressText)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()

//            myContext.showToast("latlng val => ${latLngVal}")

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
            
            address.let {
//                Log.d(TAG, "fetchAddressInCoroutine: fetch address data => ${it.predictions.size}")
                if(it.predictions.isNotEmpty()){
//                    Log.d(TAG, "fetchAddressInCoroutine: fetch address data => ${it.predictions[0].structuredFormatting?.mainText}")
//                    Log.d(TAG, "fetchAddressInCoroutine: fetch address data => ${it.predictions[0].structuredFormatting?.secondaryText}")
                    addressText = it.predictions[0].structuredFormatting?.mainText.toString()
                    subAddressText = it.predictions[0].structuredFormatting?.secondaryText.toString()
                    binding.addressText.text = addressText
                    binding.localityText.text = subAddressText
                }

            }
            
            
            
            
            
            
//            address?.let {
//
//                val fullAddress = it.getAddressLine(0) // Full address
//                val district = it.locality ?: "N/A" // District
//                val state = it.adminArea ?: "N/A" // State
//                val pincode = it.postalCode ?: "N/A" // Pincode
//                val country = it.countryName ?: "N/A" // Pincode
//
//                binding.addressText.text = removeComponents(fullAddress, listOf(district, state, pincode, country))
//                binding.localityText.text = String.format("%1s, %2s, %3s, %4s", district, state, pincode, country)
//
//                addressText = fullAddress
//
//            } ?: run {
//                Log.d("MapsActivity", "No address found")
//            }
            
            
            
            
            
        }
    }

    // Function to get address from LatLng
    private fun getAddressFromLatLng(latLng: LatLng): Address? {

        val geocoder = Geocoder(myContext, Locale.getDefault())
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                address

            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun fetchAddress(latLng: LatLng) : NearbySearchResponse {
        val nearbySearchClient = Platform.getNearbySearchClient(config)

        val nearbySearchRequest = NearbySearchRequest.Builder()
            .limit(1)
            .radius(500)
            .location("${latLng.latitude}, ${latLng.longitude}")
            .build()

        val nearbySearchResponse = nearbySearchClient.nearbySearch(nearbySearchRequest)
        
        return nearbySearchResponse
    }


    private fun removeComponents(fullAddress: String, components: List<String>): String {
        var modifiedAddress = fullAddress

        // Remove specified components
        for (component in components) {
            // Replace the component with an empty string and handle leading/trailing spaces
            modifiedAddress = modifiedAddress.replace(", $component", "")
                .replace("$component,", "")
                .replace(component, "")
                .trim()
        }

        // Remove any remaining commas and clean up spaces
        modifiedAddress = modifiedAddress.replace(Regex(",\\s*,"), ",") // Remove consecutive commas
            .replace(Regex(",\\s*"), ",") // Clean up spaces after commas
            .replace(Regex("\\s+"), " ") // Clean up multiple spaces
            .replace(Regex("^\\s*|\\s*$"), "") // Trim leading and trailing spaces

        // Handle cases where the string starts or ends with a comma
        modifiedAddress = modifiedAddress.replace(Regex("^,\\s*|\\s*,\\s*$"), "")

        return modifiedAddress
    }

}