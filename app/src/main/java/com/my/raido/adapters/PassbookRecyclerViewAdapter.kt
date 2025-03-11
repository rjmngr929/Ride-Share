package com.my.raido.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.Utils.DateUtils
import com.my.raido.databinding.TransactionHistoryItemBinding
import com.my.raido.models.response.TransactionModel

class PassbookRecyclerViewAdapter(private var trxnList : ArrayList<TransactionModel> ) : RecyclerView.Adapter<PassbookRecyclerViewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : TransactionHistoryItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  TransactionHistoryItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with(holder){
            with(trxnList[position]){

                try {

                    binding.debitCreditStatus.text  = this.entryType
                    binding.trxnDate.text = DateUtils.shared.formateDateProgrammatically(fromFormat = "yyyy-MM-dd HH:mm:ss", toFormat = "dd MMM yyyy | hh:mma", dateToFormat = this.entryDate)
                    binding.paymentSource.text = this.paymentMode


                    when (this.entryType) {
                        "credit" -> {
                            binding.trxnAmount.text = "+ ₹${this.amount}"
                        }
                        "debit" -> {
                            binding.trxnAmount.text = "- ₹${this.amount}"
                        }
                        else -> {
                            binding.trxnAmount.text = "+ ₹${this.amount}"
                        }
                    }

                }catch (err: Exception){
                    Log.d("TAG", "onBindViewHolder: exception when date format invalid")
                }


//                Glide.with(itemView).load(this.image).into(binding.productItemIv)
            }
        }
    }

    override fun getItemCount(): Int {
        return trxnList.size
    }

    // Add this function to update the dataset
    fun updateItems(newItems: ArrayList<TransactionModel> ) {
        trxnList = newItems
        notifyDataSetChanged() // Notify adapter of data change
    }

}