package com.my.raido.ui.home.bottomsheet_fragments.drawers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.adapters.CoinTransactionRecyclerView
import com.my.raido.databinding.FragmentRaidoCoinsBinding
import com.my.raido.models.CoinTransactionModel


class RaidoCoinsFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Raido Coins Fragment"
    }

    private lateinit var binding: FragmentRaidoCoinsBinding

    private lateinit var coinTransactionAdapter: CoinTransactionRecyclerView

    private val coinTransactionArray = ArrayList<CoinTransactionModel>()

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentRaidoCoinsBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

//  *************************** CoinTransaction RecyclerView *******************************************************************
        coinTransactionArray.add(CoinTransactionModel("Coin Expired","03 Sept 2024 | 09:37am","-₹1","expired"))
        coinTransactionArray.add(CoinTransactionModel("Earn from scratch card","03 Sept 2024 | 09:37am","+₹1","available"))
        coinTransactionArray.add(CoinTransactionModel("Coin Expired","03 Sept 2024 | 09:37am","-₹1","expired"))
        coinTransactionArray.add(CoinTransactionModel("Earn from scratch card","03 Sept 2024 | 09:37am","+₹1","available"))
        coinTransactionArray.add(CoinTransactionModel("Earn from scratch card","03 Sept 2024 | 09:37am","+₹1","available"))
        coinTransactionArray.add(CoinTransactionModel("Coin Expired","03 Sept 2024 | 09:37am","-₹1","expired"))

        binding.cointrxnRecyclerView.layoutManager = LinearLayoutManager(context)
        coinTransactionAdapter = CoinTransactionRecyclerView(myContext, coinTransactionArray.take(3))
        binding.cointrxnRecyclerView.adapter = coinTransactionAdapter
//  *************************** CoinTransaction RecyclerView *******************************************************************


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

}