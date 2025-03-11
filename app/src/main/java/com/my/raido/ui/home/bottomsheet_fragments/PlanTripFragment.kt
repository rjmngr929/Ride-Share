package com.my.raido.ui.home.bottomsheet_fragments

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.constants.Constants
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.visible
import com.my.raido.adapters.CustomAutocompleteAdapter
import com.my.raido.adapters.RecentRidesRecyclerViewAdapter
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentPlanTripBinding
import com.my.raido.models.RidesData
import com.my.raido.ui.RiderHistoryDetailActivity
import com.my.raido.ui.home.SetLocationOnMapActivity
import com.my.raido.ui.home.bookRide.BookRideActivity
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.sdk.core.client.Platform
import com.ola.maps.sdk.core.config.PlatformConfig
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

    private lateinit var pendingIntent: PendingIntent

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

    private lateinit var recentRidesAdapter: RecentRidesRecyclerViewAdapter

//    private lateinit var placesClient: PlacesClient

    private lateinit var loader: AlertDialog

    private lateinit var myContext: Context

    private lateinit var placesClient: com.ola.maps.sdk.places.client.PlacesClient

    private var i = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    val token = AutocompleteSessionToken.newInstance()

    // Register the launcher
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "result launcher => ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val latLng = result.data?.getParcelableExtra<LatLng>("latLng")

            val setFor = result.data?.getStringExtra("setFor")
            val addressText = result.data?.getStringExtra("addressText")
            val subAddressText = result.data?.getStringExtra("subAddressText")

            Log.d(TAG, "result launcher => ${addressText} and ${latLng}")

            if (latLng != null) {
               if(setFor == "source"){
                   binding.sourceEdittext.setText(addressText)
                   sourceAddressText = addressText.toString()
                   sourceSubAddressText = subAddressText.toString()
                   sharedPreference.putLatLng(Constants.PICKUP_LOCATION, latLng)
               }else{
                   binding.destinationEdittext.setText(addressText)
                   destAddressText = addressText.toString()
                   destSubAddressText = subAddressText.toString()
                   sharedPreference.putLatLng(Constants.DROP_LOCATION, latLng)
               }
                if(binding.sourceEdittext.text.isNotEmpty() && binding.destinationEdittext.text.isNotEmpty()){
                    sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText, $sourceSubAddressText")
                    sharedPreference.putString(Constants.DROP_ADDRESS, "$destAddressText, $destSubAddressText")
                    startActivity(Intent(myContext, BookRideActivity::class.java))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanTripBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        cabViewModel.fetchRideList()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }


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
                    if(binding.sourceEdittext.text.isNotEmpty() && binding.destinationEdittext.text.isNotEmpty()){
                        sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText, $sourceSubAddressText")
                        sharedPreference.putString(Constants.DROP_ADDRESS, "$destAddressText, $destSubAddressText")
                        startActivity(Intent(myContext, BookRideActivity::class.java))
                    }
                }
            }
        }

//************************* OlaMapSdk **************************************************************
        val config: PlatformConfig = PlatformConfig.Builder()
            .apiKey("1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S")
            .maxRetryAttempts(3)
            .baseUrl("https://api.olamaps.io/")
            .build()

         placesClient = Platform.getPlacesClient(config)


//        try {
//            val autocompleteRequest: AutocompleteRequest = AutocompleteRequest.Builder()
//                .queryText("Jalori gate")
//                .build()
//
//            Log.d(TAG, "fetchPlaceSuggestions: autocomplete response => ${autocompleteRequest.responseCode} ")
//
//
//            placesClient.autocomplete(autocompleteRequest).let { response ->
//
//                Log.d(TAG, "fetchPlaceSuggestions: autocomplete response => ${response.responseCode} ")
//                response.predictions?.forEach { prediction ->
//                    // Process each prediction
//                    println("Place Name: ${prediction.structuredFormatting?.secondaryText}")
//                    println("Place Address: ${prediction.placeId}")
//
//
//                }
//            }
//        }catch (err: Exception){
//            Log.d(TAG, "fetchPlaceSuggestions: autocomplete response => catch error = $err ")
//        }



//************************* OlaMapSdk **************************************************************


//        ***********************************************************************************
        // Initialize the Places SDK
//        if (!Places.isInitialized()) {
//            Places.initialize(myContext, resources.getString(R.string.google_maps_key))
//        }
//        // Initialize Places client
//        placesClient = Places.createClient(myContext)

        // Set up the AutoCompleteTextView
        setupAutocomplete(binding.sourceEdittext)
        setupAutocomplete(binding.destinationEdittext)

//        ***********************************************************************************


//  *************************** Recent Rides *******************************************************************

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        recentRidesAdapter = RecentRidesRecyclerViewAdapter(ArrayList()){ clickedRide ->
            // Handle item click here
            // For example, navigate to a detail screen or show a Toast
            val intent = Intent(myContext, RiderHistoryDetailActivity::class.java)
            intent.putExtra("rideId", clickedRide.rideId)
            startActivity(intent)
        }

        binding.recyclerView.adapter = recentRidesAdapter
//  *************************** Recent Rides *******************************************************************

        binding.currentStateBtn.setOnClickListener {
//            startActivity(Intent(myContext, BookRideActivity::class.java))
//            dismiss()
        }

        binding.setOnMapBtn.setOnClickListener {
            val intent = Intent(myContext, SetLocationOnMapActivity::class.java)
            intent.putExtra("focusFrom", if(binding.sourceEdittext.isFocused) "source" else "dest")
            resultLauncher.launch(intent)
//            dismiss()
        }

        binding.addStopBtn.setOnClickListener {
//            val bottomSheetFragment = DriverInfoFragment()
//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }



    }

    private fun requestFocus(view: AutoCompleteTextView){
        // Request focus on AutoCompleteTextView
        view.requestFocus()

        // Show the keyboard
        val inputMethodManager = myContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.90).toInt() // 75% of screen height
            bottomSheet.layoutParams = layoutParams
        }

    }

    override fun onResume() {
        super.onResume()

        fetchAddress()
        bindObservers()

    }


    private fun bindObservers() {
        cabViewModel.recentRidesResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)
                    Log.d(TAG, "bindObservers: response received => ${it}")

                    val riderList = it.data?.rideList ?: ArrayList<RidesData>()

                    if(riderList.isNullOrEmpty()){
                        binding.emptylist.emptyData.visible()
                    }else{
                        binding.emptylist.emptyData.gone()
                    }

                    Log.d(TAG, "bindObservers: riderList data => ${it.data?.status}, ${it.data?.message} and ${it.data?.rideList}")

                    // Update the adapter with new data
                    recentRidesAdapter.updateItems(riderList)

                    cabViewModel.clearRes()

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
                    hideLoader(myContext, loader)
                }
            }
        })
    }

    private fun fetchAddress()  {

        val latLng = sharedPreference.getLatLng(Constants.CURRENT_LATLNG)
            if(latLng != null){
                lifecycleScope.launch {
                    val address = withContext(Dispatchers.IO) {
                        val config: PlatformConfig = PlatformConfig.Builder()
                            .apiKey("1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S")
                            .maxRetryAttempts(3)
                            .baseUrl("https://api.olamaps.io/")
                            .build()
                        val nearbySearchClient = Platform.getNearbySearchClient(config)

                        val nearbySearchRequest = NearbySearchRequest.Builder()
                            .limit(1)
                            .radius(500)
                            .location("${latLng?.latitude}, ${latLng?.longitude}")
                            .build()

                        nearbySearchClient.nearbySearch(nearbySearchRequest)
                    }

                    address.let {
                        if (it.predictions.isNotEmpty()) {
                            currentAddressText = it.predictions[0].structuredFormatting?.mainText.toString()
                            currentSubAddressText = it.predictions[0].structuredFormatting?.secondaryText.toString()
                            sourceAddressText = currentAddressText
                            sourceSubAddressText = currentSubAddressText
                        }
                    }
                }
            }

    }

//***********************************************************************************
    private fun setupAutocomplete(locationType: AutoCompleteTextView) {
    val autocompleteTextView = locationType

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
            if (query.isNullOrEmpty()) return

            timer.cancel()

            if(query.toString().length > 3) {
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {

                                    fetchPlaceSuggestions(query.toString(), locationType)
//                        i++
//                        Log.d(TAG, "onTextChanged: query search count data => $i")
                    }
                }, 500)
            }
        }
    })
}

    private fun fetchPlaceSuggestions(query: String, locationType: AutoCompleteTextView) {

        val autocompleteRequest: AutocompleteRequest = AutocompleteRequest.Builder()
            .queryText(query)
            .build()

        Log.d(TAG, "fetchPlaceSuggestions: autocomplete response => ${autocompleteRequest.responseCode} ")

        placesClient.autocomplete(autocompleteRequest).let { response ->

            Log.d(TAG, "fetchPlaceSuggestions: autocomplete response => ${response.predictions}")
//            response.predictions?.forEach { prediction ->
//                // Process each prediction
//                println("Place Name: ${prediction.structuredFormatting?.secondaryText}")
//                println("Place Address: ${prediction.placeId}")
//
//            }
            requireActivity().runOnUiThread{
                if(isAdded)
                    showPredictionsInAutoComplete(response.predictions, locationType)
            }


        }

//        val autocompleteResponse: AutocompleteResponse =
//            placesClient.autocomplete(autocompleteRequest)




//        val request = FindAutocompletePredictionsRequest.builder()
//            .setQuery(query)
//            .setSessionToken(token)
//            .setTypeFilter(TypeFilter.ADDRESS) // Optional: Filter by place type
//            .setCountries("IN")  // Optional: Set country filter (e.g., "US")
//            .build()
//
//        placesClient.findAutocompletePredictions(request)
//            .addOnSuccessListener { response ->
//                val predictions = response.autocompletePredictions
//                Log.d("Autocomplete", "Place not found: $predictions")
//
//                showPredictionsInAutoComplete(predictions, locationType)
//            }
//            .addOnFailureListener { exception ->
//                Log.e("Autocomplete", "Place not found: ${exception.message}")
//            }
    }

    private fun showPredictionsInAutoComplete(predictions: ArrayList<Predictions>, locationType: AutoCompleteTextView) {
        // Create an array adapter for the suggestions
//        val suggestionList = predictions.map { it.getFullText(null).toString() }
        val suggestionList = predictions.map { "\"${ it.description.toString()}\"" }

        val arrayList = listOf("hello", "How are you", "Kese ho", "hshv ddd")

        Log.d(TAG, "showPredictionsInAutoComplete: search list data => ${suggestionList}")

//        val adapter = ArrayAdapter(myContext, android.R.layout.simple_dropdown_item_1line, suggestionList)
//
//        val autocompleteTextView = locationType
//
//        autocompleteTextView.post {
//            autocompleteTextView.setAdapter(adapter)
//        }
//
////      autocompleteTextView.setAdapter(adapter)
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
//        locationType.setOnItemClickListener {  parent, view, position, id  ->
//
//            // Fetch the placeId of the selected item
//            val selectedPrediction = predictions[position]
//            val placeId = selectedPrediction.placeId
//            Log.d("Place ID", "Selected Place ID: $placeId")
//
//            // Fetch detailed information of the selected place
////            fetchPlaceDetails(placeId, locationType)  // Fetch place details such as LatLng
//
//        }



        val adapter = CustomAutocompleteAdapter(myContext, predictions)
        locationType.setAdapter(adapter)

        locationType.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Set dropdown width to match the screen width
                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                locationType.dropDownWidth = screenWidth

                // Remove the listener after setting the width
                locationType.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        locationType.setOnItemClickListener {  parent, _, position, _ ->
//            val selectedItem = adapter.getItem(position)
            val selectedItem = parent.getItemAtPosition(position) as Predictions
            locationType.setText(selectedItem.structuredFormatting?.mainText.toString())
            locationType.setSelection(locationType.text.length)

            selectedItem.placeId

//            fetchPlaceDetails(placeId, locationType)

            if(locationType.id == binding.sourceEdittext.id){
                sourceAddressText = selectedItem.structuredFormatting?.mainText.toString()
                sourceSubAddressText = selectedItem.structuredFormatting?.secondaryText.toString()
                sharedPreference.putLatLng(Constants.PICKUP_LOCATION, LatLng(selectedItem.geometry?.location?.lat!!, selectedItem.geometry?.location?.lng!!))
            }else{
                destAddressText = selectedItem.structuredFormatting?.mainText.toString()
                destSubAddressText = selectedItem.structuredFormatting?.secondaryText.toString()
                sharedPreference.putLatLng(Constants.DROP_LOCATION, LatLng(selectedItem.geometry?.location?.lat!!, selectedItem.geometry?.location?.lng!!))
            }

            if(binding.sourceEdittext.text.isNotEmpty() && binding.destinationEdittext.text.isNotEmpty()){
              sharedPreference.putString(Constants.PICKUP_ADDRESS, "$sourceAddressText || $sourceSubAddressText")
              sharedPreference.putString(Constants.DROP_ADDRESS, "$destAddressText || $destSubAddressText")
                startActivity(Intent(myContext, BookRideActivity::class.java))

//                val intent = Intent(myContext, BookRideActivity::class.java)
//                intent.putExtra("sourceText", binding.sourceEdittext.text.toString())
//                intent.putExtra("destText", binding.destinationEdittext.text.toString())

//                Log.d(TAG, "showPredictionsInAutoComplete: pickUp => ${sharedPreference.getLatLng(Constants.CURRENT_LATLNG)}")
//                Log.d(TAG, "showPredictionsInAutoComplete: dropUp => ${sharedPreference.getLatLng(Constants.DROP_LOCATION)}")

//                startActivity(Intent(myContext, BookRideActivity::class.java))
            }


        }



    }

//    private fun fetchPlaceDetails(placeId: String, locationType: AutoCompleteTextView) {
//        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
//        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
//
//        placesClient.fetchPlace(request)
//            .addOnSuccessListener { response ->
//                val place = response.place
//
//                // Use place details, such as name and LatLng
//                Log.i("Place Details", "Place found: ${place.name}, LatLng: ${place.latLng} , ${locationType.id} == ${ binding.sourceEdittext.id}")
//
//                if(locationType.id == binding.sourceEdittext.id){
//                    sharedPreference.putLatLng(Constants.PICKUP_LOCATION, place.latLng)
//                }else{
//                    sharedPreference.putLatLng(Constants.DROP_LOCATION, place.latLng)
//                }
//
//                if(binding.sourceEdittext.text.isNotEmpty() && binding.destinationEdittext.text.isNotEmpty()){
//                    val intent = Intent(myContext, BookRideActivity::class.java)
//                    intent.putExtra("sourceText", binding.sourceEdittext.text.toString())
//                    intent.putExtra("destText", binding.destinationEdittext.text.toString())
//                    startActivity(intent)
//                }
//
//
//            }
//            .addOnFailureListener { exception ->
//                Log.e("Place Details", "Place not found: ${exception.message}")
//            }
//    }
//***********************************************************************************

}