package com.my.raido.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.databinding.SearchAddressLayoutBinding
import com.ola.maps.sdk.model.places.response.Predictions


class AddressRecyclerViewAdapter(private var addressList : ArrayList<Predictions> = ArrayList(), private var selectedField : AutoCompleteTextView, private val onAddressItemClick: (Predictions, AutoCompleteTextView) -> Unit ) : RecyclerView.Adapter<AddressRecyclerViewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : SearchAddressLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  SearchAddressLayoutBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder){
            if(addressList.isNotEmpty()) {
                with(addressList[position]) {

                    binding.titleTextView.text = this.structuredFormatting?.mainText
                    binding.subtitleTextView.text = this.structuredFormatting?.secondaryText

                    binding.root.setOnSingleClickListener {
                        onAddressItemClick(this, selectedField)
                    }

                }
            }
        }


    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    // Add this function to update the dataset
    fun updateAddressListItems(newItems: ArrayList<Predictions>, fieldType : AutoCompleteTextView ) {
        addressList = newItems
        selectedField = fieldType
        notifyDataSetChanged() // Notify adapter of data change
    }



}