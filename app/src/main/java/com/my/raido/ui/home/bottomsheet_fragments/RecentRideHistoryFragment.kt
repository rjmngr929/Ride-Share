package com.my.raido.ui.home.bottomsheet_fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.visible
import com.my.raido.adapters.RecentRidesRecyclerViewAdapter
import com.my.raido.databinding.FragmentRecentRideHistoryBinding
import com.my.raido.models.RidesData
import com.my.raido.ui.RiderHistoryDetailActivity
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecentRideHistoryFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Recent Ride History Fragment"
    }

    private lateinit var binding: FragmentRecentRideHistoryBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val cabViewModel: CabViewModel by viewModels()

    private lateinit var recentRidesAdapter: RecentRidesRecyclerViewAdapter

    private lateinit var loader: AlertDialog

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentRideHistoryBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        cabViewModel.fetchRideList()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnSingleClickListener {
            dismiss()
        }

        //  *************************** Recent Rides *******************************************************************
        val recentRides = ArrayList<RidesData>()

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        // Initialize the adapter with an empty list and set it to the RecyclerView
        recentRidesAdapter = RecentRidesRecyclerViewAdapter(ArrayList()){ clickedRide ->
            // Handle item click here
            // For example, navigate to a detail screen or show a Toast
            val intent = Intent(myContext, RiderHistoryDetailActivity::class.java)
            intent.putExtra("rideId", clickedRide.rideId)
            startActivity(intent)
        }

        binding.recyclerView.adapter = recentRidesAdapter

//  *************************** Recent Rides *******************************************************************

    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 1.0).toInt() // 75% of screen height
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
                behavior.isDraggable = true

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                // Set the height to match parent (full-screen)
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        return dialog
    }

    override fun onResume() {
        super.onResume()

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

                    val aryData = riderList.asReversed()

                    // Update the adapter with new data
                    recentRidesAdapter.updateItems(aryData)

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

}