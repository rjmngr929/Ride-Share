package com.my.raido.ui.home.bottomsheet_fragments.Help

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
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
import com.my.raido.adapters.HelpQuotesRecyclerviewAdapter
import com.my.raido.databinding.FragmentHelpTopicsBinding
import com.my.raido.models.response.help.HelpDetail
import com.my.raido.models.response.help.HelpTopics
import com.my.raido.ui.viewmodels.helpViewModel.HelpViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpTopicsFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Help Topics Fragment"
    }

    private lateinit var binding: FragmentHelpTopicsBinding

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private lateinit var helpQuotesAdapter: HelpQuotesRecyclerviewAdapter

    private val helpViewModel: HelpViewModel by viewModels()
    
    private var questionId = 0
    private var questionTitle = ""

    private val helpQuoteList = ArrayList<HelpTopics>()
    private val helpQuoteDetailList = ArrayList<HelpDetail>()

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
        binding = FragmentHelpTopicsBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        questionId = arguments?.getInt("questionId")!!
        questionTitle = arguments?.getString("helpTitles")!!

        binding.helpTopicText.text = questionTitle

        Log.d(TAG, "onCreateView: question id => ${questionId}")
        
        helpViewModel.fetchHelpQuotesDetails(questionId.toString())

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            if(binding.tabLayout.isVisible){
                binding.tabLayout.gone()
                binding.recyclerView.visible()
            }else {
                dismiss()
            }
        }

//  *************************** Help Quotes *******************************************************************

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        // Initialize the adapter with an empty list and set it to the RecyclerView
        helpQuotesAdapter = HelpQuotesRecyclerviewAdapter(ArrayList()){ helpTopic ->

            val data = helpQuoteDetailList.filter { it.question == helpTopic.topicName }
            Log.d(TAG, "onViewCreated: clicked That topic => ${data.size}")
            if(data.isNotEmpty()) {
                binding.recyclerView.gone()
                binding.tabLayout.visible()
                binding.questionText.text = data[0].question
                binding.answerText.text = data[0].answer
            }

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

        // Handle the back press
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                // Handle back press logic here
                onBackPressed()
                true // Return true to consume the event
            } else {
                false // Pass the event to the default implementation
            }
        }

        return dialog
    }

    // Handle your custom back press logic
    private fun onBackPressed() {
        if(binding.tabLayout.isVisible){
            binding.tabLayout.gone()
            binding.recyclerView.visible()
        }else {
            dismiss()
        }
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
        helpViewModel.helpQuotesDetailResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)
                    Log.d(TAG, "bindObservers: response received => ${it.data?.helpData}")
                    helpQuoteList.clear()
                    helpQuoteDetailList.clear()
                    val helpQuoteList = it.data?.helpData ?: ArrayList<HelpDetail>()
                    helpQuoteDetailList.addAll(helpQuoteList)

                    val adapterData = ArrayList<HelpTopics>()

                    for ( i in 0 until helpQuoteList.size){
                        adapterData.add(HelpTopics(i,helpQuoteList[i].question))
                    }

                    // Update the adapter with new data
                    helpQuotesAdapter.updateHelpQuotesItems( adapterData)
//                    helpQuotesAdapter.updateHelpQuotesDetailsItems( helpQuoteList)

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