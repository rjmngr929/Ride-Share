package com.my.raido.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.DateUtils
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.visible
import com.my.raido.databinding.ActivityRiderHistoryDetailBinding
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RiderHistoryDetailActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "Rider History Detail Activity"
    }

    private lateinit var binding: ActivityRiderHistoryDetailBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val cabViewModel: CabViewModel by viewModels()

    private lateinit var loader: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRiderHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loader = getLoadingDialog(this)

        val rideId = intent.getStringExtra("rideId").toString()

        cabViewModel.fetchRideDetail(rideId)

        binding.alertdialogCloseBtn.setOnSingleClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()

        bindObservers()

    }

    override fun onDestroy() {
        loader.dismiss()

        super.onDestroy()

    }

    private fun bindObservers() {
        cabViewModel.recentRideDetailResponseLiveData.observe(this, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(this, loader)
//                    Log.d(TAG, "bindObservers: response received => ${it}")

                    val riderDetail = it.data?.recentRideDetail
                    val imgUrl = it.data?.baseUrl

//                    Log.d(TAG, "bindObservers: riderList data => ${it.data?.status}, ${it.data?.message} and ${riderDetail}")

                    if (riderDetail != null) {

                        val rideStatus = riderDetail.completedRide

                        if(rideStatus == "completed"){
                            binding.rideAmountStatus.text = String.format("₹%s | Completed", riderDetail.finalFareAfterDiscount)

                            binding.driverNameText.text = String.format("You rated %s", riderDetail.driverName)
                            binding.driverRating.rating = riderDetail.ratting.toFloat()

                            Glide.with(this)
                                .load(String.format("%1s/%2s", imgUrl, riderDetail.driverImg))
                                .apply(RequestOptions().transform(RoundedCorners(30)))
                                .into(binding.driverProfile)

                            binding.fareText.text = String.format("₹%s", riderDetail.finalFareAfterDiscount)
                            binding.paidViaText.text = String.format("Paid via %s", riderDetail.paymentMethod)
                            val rideCharge = riderDetail.baseFare.toDouble() + riderDetail.distanceFare.toDouble() + riderDetail.timeFare.toDouble() + riderDetail.nightFare.toDouble()
                            binding.rideChargeText.text = String.format("₹%s", rideCharge.toString())
                            binding.bookingFeesText.text = String.format("₹%s", riderDetail.bookingFees)
                            binding.discountText.text = String.format("- ₹%s", riderDetail.discount)

                            if(!riderDetail.waitingTime.isNullOrEmpty() && riderDetail.waitingTime != "0") {
                                binding.waitingTimeRow.visible()
                                binding.waitingTimeText.text = String.format(
                                    "Waiting Time (%s minute)",
                                    riderDetail.waitingTime
                                )
                                binding.waitingTimeChargeText.text =
                                    String.format("₹ %s", riderDetail.waitingTimeFare)
                            }else{
                                binding.waitingTimeRow.gone()
                            }

                            binding.driverDetailLayout.visible()
                            binding.invoiceSection.visible()

                        }else{
                            binding.rideAmountStatus.text = "₹0 | Cancelled"

                            binding.driverDetailLayout.gone()
                            binding.invoiceSection.gone()
                        }

                        val date = DateUtils.shared.formateDateProgrammatically(fromFormat = "yyyy-MM-dd HH:mm:ss", toFormat = "dd MMM yyyy | hh:mm a", dateToFormat = riderDetail.rideDate)

                        binding.rideDate.text = date

                        val pickAdd = riderDetail.pickupLocation.split("||")
                        if(pickAdd.size == 2) {
                            binding.sourceText.text = pickAdd[0]
                            binding.sourceTextDetail.text = pickAdd[1]
                        }else{
                            binding.sourceText.text = riderDetail.pickupLocation
                            binding.sourceTextDetail.text = riderDetail.pickupLocation
                        }

                        val dropAdd = riderDetail.dropLocation.split("||")
                        if(dropAdd.size == 2){
                            binding.destinationText.text = dropAdd[0]
                            binding.destinationTextDetail.text = dropAdd[1]
                        }else {
                            binding.destinationText.text = riderDetail.dropLocation
                            binding.destinationTextDetail.text = riderDetail.dropLocation
                        }

                        binding.durationText.text = riderDetail.duration
                        binding.distanceText.text = riderDetail.distance
//                        binding.rideIdText.text = String.format("Raido%s",riderDetail.riderId )
                        binding.rideIdText.text = riderDetail.rideId



                    }

                    cabViewModel.clearRecentRideDetailRes()

                }
                is NetworkResult.Error -> {
                    hideLoader(this, loader)

                    alertDialogService.alertDialogAnim(
                        this,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
                    showLoader(this, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(this, loader)
                }
            }
        })
    }


}