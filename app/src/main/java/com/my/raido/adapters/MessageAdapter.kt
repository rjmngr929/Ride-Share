package com.my.raido.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.my.raido.R
import com.my.raido.models.SupportChatModel


class MessageAdapter(context: Context?, resource: Int, objects: List<SupportChatModel?>?) :
    ArrayAdapter<SupportChatModel?>(context!!, resource, objects!!) {

    companion object{
        private const val TAG = "Message Adapter"
    }

    val messageFormatList =  ArrayList<SupportChatModel>()

//    private var messageAdapter: MessageAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView: View? = convertView
        Log.i("TAG", "getView: ")

        val data = getItem(position)

        if (data != null) {

//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//            val timeFormatter = DateTimeFormatter.ofPattern("d MMM, hh:mm a")
//            val localDateTime = LocalDateTime.parse(data.date, formatter)

            if(data.type == "robot"){
                convertView =
                    (context as Activity).layoutInflater.inflate(R.layout.server_rply_layout, parent, false)
                val messageText: TextView = convertView.findViewById(R.id.clientMessage)
                val dateText: TextView = convertView.findViewById(R.id.clientMessage_time)
                messageText.text = data.msg
//                dateText.text = localDateTime.format(timeFormatter)
                dateText.text = data.date
            }else{
                Log.d(TAG, "getView: atachment data => ${data.attachment == "null"}")
//                if(data.attachment != "null"){
                    convertView =
                        (context as Activity).layoutInflater.inflate(R.layout.my_msg_layout, parent, false)
                    val messageText: TextView = convertView.findViewById(R.id.myMessage)
                    val dateText: TextView = convertView.findViewById(R.id.myMessage_time)
//                    dateText.text = localDateTime.format(timeFormatter)
                    dateText.text = data.date
                    messageText.text = data.msg
//                }
            }


        }
        return convertView!!
    }

}