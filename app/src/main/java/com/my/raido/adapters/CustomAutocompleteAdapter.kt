package com.my.raido.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.my.raido.R
import com.ola.maps.sdk.model.places.response.Predictions

class CustomAutocompleteAdapter(
    context: Context,
    private val items: List<Predictions>
) : ArrayAdapter<Predictions>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = items[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_autocomplete, parent, false)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val subtitleTextView = view.findViewById<TextView>(R.id.subtitleTextView)

        titleTextView.text = item.structuredFormatting?.mainText
        subtitleTextView.text = item.structuredFormatting?.secondaryText

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredItems = if (constraint.isNullOrEmpty()) {
                    items
                } else {
                    items.filter {
                        it.structuredFormatting?.mainText?.contains(constraint, true) ?: false ||
                        it.structuredFormatting?.secondaryText?.contains(constraint, true) ?: false
                    }
                }

                return FilterResults().apply {
                    values = filteredItems
                    count = filteredItems.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                try {
                    if(results != null){
                        clear()
                        addAll(results.values as List<Predictions>)
                        notifyDataSetChanged()
                    }
                }catch (err: Exception){
                    Log.d("TAG", "publishResults: error occur on catch part for publish result => $err")
                }

            }
        }
    }
}