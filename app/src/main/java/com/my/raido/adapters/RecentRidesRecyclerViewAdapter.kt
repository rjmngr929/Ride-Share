package com.my.raido.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.Utils.DateUtils
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.databinding.RecentRidesRowLayoutBinding
import com.my.raido.models.RidesData

class RecentRidesRecyclerViewAdapter(private var rideList : List<RidesData>, private val onItemClick: (RidesData) -> Unit ) : RecyclerView.Adapter<RecentRidesRecyclerViewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : RecentRidesRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  RecentRidesRowLayoutBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with(holder){
            with(rideList[position]){

                try {

                    val dropAddress = this.dropLocation.split("||")
                    if(dropAddress.size == 2) {
                        binding.locationText.text  = dropAddress[0]
                    }else{
                        binding.locationText.text  = this.dropLocation
                    }

                    binding.priceStatusText.text = String.format("₹%1s * %2s", if(this.rideStatus == "completed") this.price else "0", if(this.rideStatus == "completed") "Completed" else "Cancelled")

                    binding.dateTimeText.text = DateUtils.shared.formateDateProgrammatically(fromFormat = "yyyy-MM-dd HH:mm:ss", toFormat = "dd MMM yyyy | hh:mma", dateToFormat = this.rideDate)

                    binding.root.setOnSingleClickListener {
                        onItemClick(this)
                    }

                }catch (err: Exception){
                    Log.d("TAG", "onBindViewHolder: exception when date format invalid")
                }


//                Glide.with(itemView).load(this.image).into(binding.productItemIv)
            }
        }
    }

    override fun getItemCount(): Int {
        return rideList.size
    }

    // Add this function to update the dataset
    fun updateItems(newItems: List<RidesData> ) {
        rideList = newItems
        notifyDataSetChanged() // Notify adapter of data change
    }

}