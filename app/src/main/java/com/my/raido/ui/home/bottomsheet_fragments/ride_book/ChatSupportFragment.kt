package com.my.raido.ui.home.bottomsheet_fragments.ride_book

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ListView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.R
import com.my.raido.Utils.showToast
import com.my.raido.adapters.MessageAdapter
import com.my.raido.databinding.FragmentChatSupportBinding
import com.my.raido.models.SupportChatModel
import com.my.raido.socket.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatSupportFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Chat Support Fragment"
    }

    private lateinit var binding: FragmentChatSupportBinding

    private lateinit var myContext: Context

    private lateinit var messageListView: ListView
    private var messageAdapter: MessageAdapter? = null

    @Inject
    lateinit var socketManager: SocketManager

    private var messageData = ArrayList<SupportChatModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatSupportBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mobileNumber = arguments?.getString("mobile_number")

        messageListView = binding.messageListView

        messageData.add(
            SupportChatModel(
                chatId = "gvgx",
                msg = "Hello, How can i Help?",
                type = "robot",
                attachment = "",
                date = "02:00 pm"
            )
        )

        messageData.add(
            SupportChatModel(
                chatId = "gvgx",
                msg = "Nothing",
                type = "customer",
                attachment = "",
                date = "02:06 pm"
            )
        )

        messageAdapter = MessageAdapter(myContext, R.layout.my_msg_layout, messageData)

        messageListView.adapter = messageAdapter
        onTypeButtonEnable()

        binding.chatBackBtn.setOnClickListener {
            dismiss()
        }

        binding.callCustomerBtn.setOnClickListener {
            if(!mobileNumber.isNullOrEmpty()){
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$mobileNumber")
                }
                startActivity(intent)
            }else{
                myContext.showToast("Something went wrong.")
            }

        }

        binding.sendChatBtn.setOnClickListener {
            if(binding.msgEdittext.text.isNotEmpty()){
//                sendMsgApi()

                messageData.add(
                    SupportChatModel(
                        chatId = "sbjbsj",
                        msg =  binding.msgEdittext.text.toString(),
                        type =  "user",
                        attachment =  "",
                        date =  "5:00 pm"
                    )
                )
                messageAdapter!!.notifyDataSetChanged()
//                Helper.hideKeyboard(binding.msgEdittext)
                binding.msgEdittext.text.clear()
            }
        }

//  ********************* Socket Operations ********************************************************
        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
            override fun onConnected() {
                socketManager.listenToEvent("receive_message_by_user") { data ->
                    Log.d(TAG, "onViewCreated: driver message receive to user => $data")
                }
            }
        })
//  ********************* Socket Operations ********************************************************

    }

    private fun onTypeButtonEnable() {
        binding.msgEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                binding.msgEdittext.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
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
}