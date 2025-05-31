package com.my.raido.ui.home.bottomsheet_fragments.ride_book

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.adapters.AddressRecyclerViewAdapter
import com.my.raido.adapters.RecentRidesRecyclerViewAdapter
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentPlanTripBinding
import com.my.raido.ui.home.bookRide.BookRideActivity
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.setLocationOnMap.SetLocationOnMapActivity
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.sdk.core.client.Platform
import com.ola.maps.sdk.core.config.PlatformConfig
import com.ola.maps.sdk.forwardgeocoding.client.ForwardGeocodingClient
import com.ola.maps.sdk.model.nearbysearch.request.NearbySearchRequest
import com.ola.maps.sdk.model.places.request.AutocompleteRequest
import com.ola.maps.sdk.model.places.response.Predictions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


@AndroidEntryPoint
class PlanTripFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Plan Trip Fragment"
    }

    private lateinit var binding: FragmentPlanTripBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var sharedPreference: SharedPrefManager

    private var sourceAddressText = ""
    private var sourceSubAddressText = ""

    private var currentAddressText = ""
    private var currentSubAddressText = ""

    private var destAddressText = ""
    private var destSubAddressText = ""

    private val cabViewModel: CabViewModel by viewModels()

    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    private lateinit var addressAdapter: AddressRecyclerViewAdapter

    private lateinit var recentRidesAdapter: RecentRidesRecyclerViewAdapter

    private lateinit var loader: AlertDialog

    private lateinit var myContext: Context

    private lateinit var placesClient: com.ola.maps.sdk.places.client.PlacesClient
    private lateinit var textSearchClient: ForwardGeocodingClient

    private lateinit var selectedField : AutoCompleteTextView

    private var isProgrammaticChange = false

    private var i = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanTripBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        selectedField = binding.destinationEdittext

        // Register the launcher
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "result launcher => ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                val latLng = result.data?.getParcelableExtra<LatLng>("latLng")

                val setFor = result.data?.getStringExtra("setFor")
                val addressText = result.data?.getStringExtra("addressText")
                val subAddressText = result.data?.getStringExtra("subAddressText")

                Log.d(TAG, "result launcher => ${addressText} and ${latLng}")

                if (latLng != null) {
                    if(setFor == "source"){
//                        binding.sourceEdittext.setText(addressText)
                        setTextProgrammatically(addressText.toString(), binding.sourceEdittext)
                        sourceAddressText = addressText.toString()
                        sourceSubAddressText = subAddressText.toString()
                        sharedPreference.putLatLng(Constants.PICKUP_LOCATION, latLng)
                    }else{
//                        binding.destinationEdittext.setText(addressText)
                        setTextProgrammatically(addressText.toString(), binding.destinationEdittext)
                        destAddressText = addressText.toString()
                        destSubAddressText = subAddressText.toString()
                        sharedPreference.putLatLng(Constants.DROP_LOCATION, latLng)
                    }
                    if(sourceAddressText.isNotEmpty() && destAddressText.isNotEmpty()){
                        sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText, $sourceSubAddressText")
                        sharedPreference.putString(Constants.DROP_ADDRESS, "$destAddressText, $destSubAddressText")
                        val intent = Intent(myContext, BookRideActivity::class.java)
                        intent.putExtra("drawPath", true)
                        startActivity(intent)
                    }
                }
            }
        }

//        cabViewModel.fetchRideList()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        sharedPreference.putString(Constants.PICKUP_ADDRESS, "")
        sharedPreference.putString(Constants.DROP_ADDRESS, "")

        requestFocus(binding.destinationEdittext)

        // Set a focus change listener
        binding.sourceEdittext.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if(binding.sourceEdittext.text.toString() == "Your current location"){
                    // Clear the text when focused
                    binding.sourceEdittext.text.clear()
                    val currentPickUp = sharedPreference.getLatLng(Constants.CURRENT_LATLNG)!!
                    if (currentPickUp != null) {
                        sharedPreference.putLatLng(Constants.PICKUP_LOCATION, currentPickUp)
                        sourceAddressText = currentAddressText
                        sourceSubAddressText = currentSubAddressText
                        sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText, $sourceSubAddressText")
                    }
                }
            }else{
                if(binding.sourceEdittext.text.toString().isEmpty()){
                    binding.sourceEdittext.setText("Your current location")
                    val currentPickUp = sharedPreference.getLatLng(Constants.CURRENT_LATLNG)!!
                    if (currentPickUp != null) {
                        sharedPreference.putLatLng(Constants.PICKUP_LOCATION, currentPickUp)
                        sourceAddressText = currentAddressText
                        sourceSubAddressText = currentSubAddressText
                    }
                    if(sourceAddressText.isNotEmpty() && destAddressText.isNotEmpty()){
                        sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText, $sourceSubAddressText")
                        sharedPreference.putString(Constants.DROP_ADDRESS, "$destAddressText, $destSubAddressText")
                        val intent = Intent(myContext, BookRideActivity::class.java)
                        intent.putExtra("drawPath", true)
                        startActivity(intent)
                    }
                }
            }
        }

//************************* OlaMapSdk **************************************************************
        val config: PlatformConfig = PlatformConfig.Builder()
            .apiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
            .maxRetryAttempts(3)
            .baseUrl("https://api.olamaps.io/")
            .build()

         placesClient = Platform.getPlacesClient(config)
         textSearchClient = Platform.getForwardGeocodingClient(config)

        // Set up the AutoCompleteTextView
        setupAutocomplete(binding.sourceEdittext)
        setupAutocomplete(binding.destinationEdittext)

//        ***********************************************************************************


//  *************************** Recent Rides *******************************************************************

//        binding.recyclerView.layoutManager = LinearLayoutManager(context)
//        recentRidesAdapter = RecentRidesRecyclerViewAdapter(ArrayList()){ clickedRide ->
//            // Handle item click here
//            // For example, navigate to a detail screen or show a Toast
//            val intent = Intent(myContext, RiderHistoryDetailActivity::class.java)
//            intent.putExtra("rideId", clickedRide.rideId)
//            startActivity(intent)
//        }
//
//        binding.recyclerView.adapter = recentRidesAdapter
//  *************************** Recent Rides *******************************************************************


//  **************************** Address RecyclerView Adapter *******************************************
        binding.searchAddressRecyclerView.layoutManager = LinearLayoutManager(context)

        addressAdapter = AddressRecyclerViewAdapter(ArrayList(), selectedField) { selectedAddress, autoCompleteTextView ->

            addressAdapter.updateAddressListItems(ArrayList(), autoCompleteTextView)

//            autoCompleteTextView.setText(selectedAddress.structuredFormatting?.mainText.toString())
//            autoCompleteTextView.setSelection(autoCompleteTextView.text.length)

//            selectedAddress.placeId

            setTextProgrammatically(selectedAddress.structuredFormatting?.mainText.toString(), autoCompleteTextView)

            if (autoCompleteTextView.id == binding.sourceEdittext.id) {
                sourceAddressText = selectedAddress.structuredFormatting?.mainText.toString()
                sourceSubAddressText =
                    selectedAddress.structuredFormatting?.secondaryText.toString()
                sharedPreference.putLatLng(
                    Constants.PICKUP_LOCATION,
                    LatLng(
                        selectedAddress.geometry?.location?.lat!!,
                        selectedAddress.geometry?.location?.lng!!
                    )
                )
            } else {
                destAddressText = selectedAddress.structuredFormatting?.mainText.toString()
                destSubAddressText = selectedAddress.structuredFormatting?.secondaryText.toString()
                sharedPreference.putLatLng(
                    Constants.DROP_LOCATION,
                    LatLng(
                        selectedAddress.geometry?.location?.lat!!,
                        selectedAddress.geometry?.location?.lng!!
                    )
                )
            }

            Log.d(TAG, "onViewCreated: source and dstination address => ${sourceAddressText} and ${destAddressText}")
            
            if (sourceAddressText.isNotEmpty() && destAddressText.isNotEmpty()) {
                Log.d(
                    TAG,
                    "showPredictionsInAutoComplete: source address => ${sourceAddressText}, $sourceSubAddressText"
                )
                sharedPreference.putString(
                    Constants.PICKUP_ADDRESS,
                    "$sourceAddressText || $sourceSubAddressText"
                )
                sharedPreference.putString(
                    Constants.DROP_ADDRESS,
                    "$destAddressText || $destSubAddressText"
                )
                val intent = Intent(myContext, BookRideActivity::class.java)
                intent.putExtra("drawPath", true)
                startActivity(intent)

            }

        }

        binding.searchAddressRecyclerView.adapter = addressAdapter

//  **************************** Address RecyclerView Adapter *******************************************

        binding.currentStateBtn.setOnClickListener {
//            startActivity(Intent(myContext, BookRideActivity::class.java))
//            dismiss()
        }

        binding.setOnMapBtn.setOnClickListener {
            val intent = Intent(myContext, SetLocationOnMapActivity::class.java)
            intent.putExtra("focusFrom", if(binding.sourceEdittext.isFocused) "source" else "dest")
            resultLauncher?.launch(intent)
//            dismiss()
        }

        binding.addStopBtn.setOnClickListener {
//            val bottomSheetFragment = DriverInfoFragment()
//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }



    }

    private fun setTextProgrammatically(text: String, fieldText: AutoCompleteTextView) {
        isProgrammaticChange = true  // Set flag to true before setting text
        fieldText.setText(text)
        fieldText.setSelection(text.length)  // Move cursor to end
        isProgrammaticChange = false  // Reset flag after setting text
    }

    private fun requestFocus(view: AutoCompleteTextView){
        // Request focus on AutoCompleteTextView
        view.requestFocus()

        // Show the keyboard
        val inputMethodManager = myContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT)
    }

//    override fun onStart() {
//        super.onStart()
//
//        // Set the height of the bottom sheet to cover 3/4 of the screen
//        val dialog = dialog
//        dialog?.let {
//            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            val layoutParams = bottomSheet.layoutParams
//            layoutParams.height = (resources.displayMetrics.heightPixels * 0.90).toInt() // 75% of screen height
//            bottomSheet.layoutParams = layoutParams
//        }
//
//    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 1.0).toInt() // 100% of screen height
            bottomSheet.layoutParams = layoutParams
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

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = false
                // Set the height to match parent (full-screen)
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        return dialog
    }


    override fun onResume() {
        super.onResume()

        fetchAddress()
//        bindObservers()

    }

    override fun onDestroy() {
        super.onDestroy()
        resultLauncher = null
    }

//    private fun bindObservers() {
//        cabViewModel.recentRidesResponseLiveData.observe(viewLifecycleOwner, Observer {
//            Log.d(TAG, "bindObservers: response received  => ${it}")
//            when (it) {
//                is NetworkResult.Success -> {
//                    hideLoader(myContext, loader)
//                    Log.d(TAG, "bindObservers: response received => ${it}")
//
//                    val riderList = it.data?.rideList ?: ArrayList<RidesData>()
//
//                    Log.d(TAG, "bindObservers: response received  => ${riderList}")
//
//                    if(riderList.isNullOrEmpty()){
//                        binding.emptylist.emptyData.visible()
//                    }else{
//                        binding.emptylist.emptyData.gone()
//                    }
//
//                    Log.d(TAG, "bindObservers: riderList data => ${it.data?.status}, ${it.data?.message} and ${it.data?.rideList}")
//
//                    val aryData = riderList.asReversed()
//                    // Update the adapter with new data
//                    recentRidesAdapter.updateItems(aryData)
//
//                    cabViewModel.clearRes()
//
//                }
//                is NetworkResult.Error -> {
//                    hideLoader(myContext, loader)
//
//                    alertDialogService.alertDialogAnim(
//                        myContext,
//                        it.message.toString(),
//                        R.raw.failed
//                    )
//                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
//                }
//                is NetworkResult.Loading ->{
//                    showLoader(myContext, loader)
//                }
//                is NetworkResult.Empty -> {
//                    hideLoader(myContext, loader)
//                }
//            }
//        })
//    }

    private fun fetchAddress()  {

        val latLng = sharedPreference.getLatLng(Constants.CURRENT_LATLNG)
        latLng?.let {
            sharedPreference.putLatLng(Constants.PICKUP_LOCATION,
                it
            )
        }

            if(latLng != null){
                lifecycleScope.launch {
                    try {
                        val address = withContext(Dispatchers.IO) {
                            val config: PlatformConfig = PlatformConfig.Builder()
                                .apiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
                                .maxRetryAttempts(3)
                                .baseUrl("https://api.olamaps.io/")
                                .build()
                            val nearbySearchClient = Platform.getNearbySearchClient(config)

                            val nearbySearchRequest = NearbySearchRequest.Builder()
                                .limit(3)
                                .radius(1000)
                                .location("${latLng.latitude}, ${latLng.longitude}")
                                .build()

                            nearbySearchClient.nearbySearch(nearbySearchRequest)
                        }
                        address.let {
                            if (it.predictions.isNotEmpty()) {
                                Log.d(TAG, "fetchAddress: address data is ${it.predictions}")
                                currentAddressText = it.predictions[0].structuredFormatting?.mainText.toString()
                                currentSubAddressText = it.predictions[0].structuredFormatting?.secondaryText.toString()
                                sourceAddressText = currentAddressText
                                sourceSubAddressText = currentSubAddressText
                            }
                        }
                    }catch (err: Exception){
                        Log.d(TAG, "fetchAddress: error occor is ${err}")
                    }
                }
            }

        
            

    }

//***********************************************************************************
    private fun setupAutocomplete(locationType: AutoCompleteTextView) {
    val autocompleteTextView = locationType

    selectedField = locationType

//    locationType.setSingleLine(true)
//    locationType.maxLines = 1
//    locationType.setHorizontallyScrolling(false)
//    locationType.ellipsize = TextUtils.TruncateAt.END

    // Listen for text changes
    autocompleteTextView.addTextChangedListener(object : TextWatcher {
        private var timer = Timer()
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
            if (!isProgrammaticChange) {

                if (query.isNullOrEmpty()) return

                timer.cancel()

                if (query.toString().length > 2) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            fetchPlaceSuggestions(query.toString(), locationType)
                        }
                    }, 500)
                } else {
                    addressAdapter.updateAddressListItems(ArrayList(), locationType)
                }
            }
        }
    })
}

    private fun fetchPlaceSuggestions(query: String, locationType: AutoCompleteTextView) {
        
        val currentLatLng = sharedPreference.getLatLng(Constants.CURRENT_LATLNG)

        try {

            val autocompleteRequest: AutocompleteRequest = AutocompleteRequest.Builder()
                .queryText(query)
                .build()


            placesClient.autocomplete(autocompleteRequest).let { response ->

                requireActivity().runOnUiThread{
                    if(isAdded)
                        showPredictionsInAutoComplete(response.predictions, locationType)
                }

            }
            
        }catch (err: Exception){
            Log.d(TAG, "fetchPlaceSuggestions: error occur => $err")
        }

    }

    private fun showPredictionsInAutoComplete(predictions: ArrayList<Predictions>, locationType: AutoCompleteTextView) {
        // Create an array adapter for the suggestions
//        val suggestionList = predictions.map { it.getFullText(null).toString() }
        val suggestionList = predictions.map { "\"${ it.description.toString()}\"" }

        Log.d(TAG, "showPredictionsInAutoComplete: search list data => ${suggestionList}")


        selectedField = locationType
        addressAdapter.updateAddressListItems(predictions, locationType)



//        val adapter = CustomAutocompleteAdapter(myContext, predictions)
//        locationType.setAdapter(adapter)
//
//        locationType.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                // Set dropdown width to match the screen width
//                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
//                locationType.dropDownWidth = screenWidth
//
//                // Remove the listener after setting the width
//                locationType.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
//
//        locationType.setOnItemClickListener {  parent, _, position, _ ->
////            val selectedItem = adapter.getItem(position)
//            val selectedItem = parent.getItemAtPosition(position) as Predictions
//            locationType.setText(selectedItem.structuredFormatting?.mainText.toString())
//            locationType.setSelection(locationType.text.length)
//
//            selectedItem.placeId
//
////            fetchPlaceDetails(placeId, locationType)
//
//            if(locationType.id == binding.sourceEdittext.id){
//                sourceAddressText = selectedItem.structuredFormatting?.mainText.toString()
//                sourceSubAddressText = selectedItem.structuredFormatting?.secondaryText.toString()
//                sharedPreference.putLatLng(Constants.PICKUP_LOCATION, LatLng(selectedItem.geometry?.location?.lat!!, selectedItem.geometry?.location?.lng!!))
//            }else{
//                destAddressText = selectedItem.structuredFormatting?.mainText.toString()
//                destSubAddressText = selectedItem.structuredFormatting?.secondaryText.toString()
//                sharedPreference.putLatLng(Constants.DROP_LOCATION, LatLng(selectedItem.geometry?.location?.lat!!, selectedItem.geometry?.location?.lng!!))
//            }
//
//            if(sourceAddressText.isNotEmpty() && destAddressText.isNotEmpty()){
//                Log.d(TAG, "showPredictionsInAutoComplete: source address => ${sourceAddressText}, $sourceSubAddressText")
//                sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText || $sourceSubAddressText")
//                sharedPreference.putString(Constants.DROP_ADDRESS, "$destAddressText || $destSubAddressText")
//                startActivity(Intent(myContext, BookRideActivity::class.java))
//
//            }
//
//
//        }



    }


}