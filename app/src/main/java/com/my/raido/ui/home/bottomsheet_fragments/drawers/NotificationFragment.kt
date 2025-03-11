package com.my.raido.ui.home.bottomsheet_fragments.drawers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
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
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.visible
import com.my.raido.adapters.NotificationRecyclerViewAdapter
import com.my.raido.databinding.FragmentNotificationBinding
import com.my.raido.models.NotificationList
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Notification Fragment"
    }

    private lateinit var binding: FragmentNotificationBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val userViewModel: UserDataViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationRecyclerViewAdapter

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
        binding = FragmentNotificationBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        userViewModel.fetchNotification()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }


//  *************************** Notification RecyclerView *******************************************************************
        val notificationList = ArrayList<NotificationList>()

        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationRecyclerViewAdapter(notificationList)
        binding.notificationRecyclerView.adapter = notificationAdapter
//  *************************** Notification RecyclerView *******************************************************************


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

    override fun onResume() {
        super.onResume()

        bindObservers()

    }


    private fun bindObservers() {
        userViewModel.notificationResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)

                    Log.d(TAG, "bindObservers: response received => ${it}")

                    val notificationAry  = it.data?.NotificationList ?: ArrayList<NotificationList>()

                    if(notificationAry.isNullOrEmpty()){
                        binding.emptylist.emptyData.visible()
                    }else{
                        binding.emptylist.emptyData.gone()
                    }

                    Log.d(TAG, "bindObservers: notification data => ${it.data?.status}, ${it.data?.message} ")

                    // Update the adapter with new data
                    notificationAdapter.updateItems(notificationAry)

                    userViewModel.clearNotificationRes()

                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)

                    alertDialogService.alertDialogAnim(
                        myContext,
                        it.message.toString(),
                        R.raw.failed
                    )

//                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
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