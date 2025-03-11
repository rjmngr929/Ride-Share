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
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.adapters.HelpQuotesRecyclerviewAdapter
import com.my.raido.databinding.FragmentHelpBinding
import com.my.raido.models.response.help.HelpTopics
import com.my.raido.ui.home.bottomsheet_fragments.Help.HelpTopicsFragment
import com.my.raido.ui.viewmodels.helpViewModel.HelpViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Help Fragment"
    }

    private lateinit var binding: FragmentHelpBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private lateinit var helpQuotesAdapter: HelpQuotesRecyclerviewAdapter

    private val helpViewModel: HelpViewModel by viewModels()

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
        binding = FragmentHelpBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        helpViewModel.fetchHelpQuotes()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }


//  *************************** Help Quotes *******************************************************************

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        // Initialize the adapter with an empty list and set it to the RecyclerView
        helpQuotesAdapter = HelpQuotesRecyclerviewAdapter(ArrayList()){ helpTopic->
            Log.d(TAG, "onViewCreated: clicked That topic => ${helpTopic.topicName}")

            val bottomSheetFragment = HelpTopicsFragment()
            val bundle = Bundle()
            bundle.putString("helpTitles", helpTopic.topicName) // Example of another type
            bundle.putInt("questionId", helpTopic.id) // Example of another type
            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

        }
        binding.recyclerView.adapter = helpQuotesAdapter


//  *************************** Help Quotes *******************************************************************



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
        helpViewModel.helpQuotesResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)
                    Log.d(TAG, "bindObservers: response received => ${it.data?.topics}")

                    val helpQuoteList = it.data?.topics ?: ArrayList<HelpTopics>()

                    // Update the adapter with new data
                    helpQuotesAdapter.updateHelpQuotesItems(helpQuoteList)

                    helpViewModel.clearHelpQuotesRes()

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