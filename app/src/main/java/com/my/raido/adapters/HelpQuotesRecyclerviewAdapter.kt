package com.my.raido.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.databinding.HelpQuotesRowLayoutBinding
import com.my.raido.models.response.help.HelpTopics

//, private var helpQuotesDetailList : ArrayList<HelpDetail> = ArrayList()
class HelpQuotesRecyclerviewAdapter(private var helpQuotesList : ArrayList<HelpTopics> = ArrayList() , private val onHelpQuoteItemClick: (HelpTopics) -> Unit ) : RecyclerView.Adapter<HelpQuotesRecyclerviewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : HelpQuotesRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  HelpQuotesRowLayoutBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder){
            if(helpQuotesList.isNotEmpty()) {
                with(helpQuotesList[position]) {
                    binding.title.text = this.topicName

                    binding.root.setOnSingleClickListener {
                        onHelpQuoteItemClick(this)
                    }

                }
            }
        }


    }

    override fun getItemCount(): Int {
        return helpQuotesList.size
    }

    // Add this function to update the dataset
    fun updateHelpQuotesItems(newItems: ArrayList<HelpTopics> ) {
        helpQuotesList = newItems
        notifyDataSetChanged() // Notify adapter of data change
    }


}