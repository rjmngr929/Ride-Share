package com.my.raido.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.R
import com.my.raido.databinding.CoinTrxnRowLayoutBinding
import com.my.raido.models.CoinTransactionModel

class CoinTransactionRecyclerView(private var context: Context, private var transactionList : List<CoinTransactionModel> ) : RecyclerView.Adapter<CoinTransactionRecyclerView.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : CoinTrxnRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  CoinTrxnRowLayoutBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder){
            with(transactionList[position]){
                binding.titleText.text = this.coinTitle
                binding.dateTimeText.text = this.transactionDate
                binding.coinValText.text = this.transactionAmount
                if(this.transactionStatus == "expired"){
                    binding.statusIcon.background = ContextCompat.getDrawable(context, R.drawable.voucherexpired)
                }else{
                    binding.statusIcon.background = ContextCompat.getDrawable(context, R.drawable.voucheravailable)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    // Add this function to update the dataset
    fun updateItems(newItems: ArrayList<CoinTransactionModel> ) {
        transactionList = newItems
        notifyDataSetChanged() // Notify adapter of data change
    }


}