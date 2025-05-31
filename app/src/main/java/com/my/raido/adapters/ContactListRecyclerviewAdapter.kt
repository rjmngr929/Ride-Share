package com.my.raido.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.my.raido.Utils.gone
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showToast
import com.my.raido.Utils.visible
import com.my.raido.databinding.ContactRowLayoutBinding
import com.my.raido.models.contacts.ContactList

class ContactListRecyclerviewAdapter(private var contactList : MutableList<ContactList>, private var myContext: Context, private var selectType : Boolean = false, private val onItemClick: (ContactList) -> Unit) : RecyclerView.Adapter<ContactListRecyclerviewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding : ContactRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =  ContactRowLayoutBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {

        with(holder){

            if(selectType){
                binding.contactSelected.visible()
                binding.contactDeleteBtn.gone()
            }else{
                binding.contactSelected.gone()
                binding.contactDeleteBtn.visible()
            }


            with(contactList[position]){
                binding.contactNameText.text = this.name
                binding.contactNumberText.text = this.number
                binding.contactSelected.isChecked = this.isSelected

                binding.contactSelected.setOnCheckedChangeListener { compoundButton, b ->
                    val aryData = contactList.filter { it.isSelected }
                    if(aryData.size < 5) {
                        binding.contactSelected.isChecked = b
                        this.isSelected = b
                    }else{
                        this.isSelected = false
                        binding.contactSelected.isChecked = this.isSelected
                        myContext.showToast("Max limit exceed to select contact")
                    }
                }

                binding.contactDeleteBtn.setOnSingleClickListener {
                    onItemClick(this)
                }

            }
        }

    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    // Add this function to update the dataset
    fun updateItems(newItems: MutableList<ContactList>, listtype: Boolean ) {
        contactList = newItems
        selectType = listtype
        notifyDataSetChanged() // Notify adapter of data change
    }


}