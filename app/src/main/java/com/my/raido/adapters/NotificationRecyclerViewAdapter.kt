package com.my.raido.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.databinding.NotificationRowLayoutBinding
import com.my.raido.models.NotificationList

class NotificationRecyclerViewAdapter(private var notificationList : List<NotificationList> ) : RecyclerView.Adapter<NotificationRecyclerViewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : NotificationRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  NotificationRowLayoutBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {


        with(holder){
            with(notificationList[position]){
                binding.notifyTitleText.text = this.type
                binding.notifyDescText.text = this.notificationMessage
            }
        }


    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    // Add this function to update the dataset
    fun updateItems(newItems: ArrayList<NotificationList> ) {
        notificationList = newItems
        notifyDataSetChanged() // Notify adapter of data change
    }


}