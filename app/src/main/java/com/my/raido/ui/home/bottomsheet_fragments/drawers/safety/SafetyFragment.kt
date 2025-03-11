package com.my.raido.ui.home.bottomsheet_fragments.drawers.safety

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.showPermanentlyDeniedDialog
import com.my.raido.Utils.showRationaleDialog
import com.my.raido.Utils.visible
import com.my.raido.adapters.ContactListRecyclerviewAdapter
import com.my.raido.databinding.FragmentSafetyBinding
import com.my.raido.models.contacts.ContactList
import com.my.raido.models.contacts.ContactRequestData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SafetyFragment : BottomSheetDialogFragment(), PermissionRequest.Listener {

    companion object{
        private const val TAG = "Safety Fragment"
    }

    private lateinit var binding: FragmentSafetyBinding

    private val contactsViewModel: ContactsViewModel by viewModels()

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private lateinit var contactListAdapter: ContactListRecyclerviewAdapter

    val contactList = ArrayList<ContactList>()
    val selectedContactList = ArrayList<ContactList>()

    private lateinit var loader: AlertDialog

    private val request by lazy {
        permissionsBuilder(
            Manifest.permission.READ_CONTACTS).build()
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK){
//            showLoader(myContext, loader)
            contactsViewModel.fetchContacts()
        }else{
            request.send()
        }

    }

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSafetyBinding.inflate(inflater, container, false)

        loader = getLoadingDialog(myContext)

        request.addListener(this)

        contactsViewModel.fetchContactList()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.addContactBtn.setOnClickListener {
            if(binding.addContactBtn.text.toString().contains("Add")) {
                request.send()
                binding.emptyContactCardLayout.gone()
                binding.addContactBtn.text = "Update Contact"
            }else{
               val aryData = ArrayList<ContactList>()
                aryData.addAll(contactList.filter { it.isSelected })
                contactList.clear()
                contactList.addAll(aryData)
                Log.d(TAG, "onViewCreated: array length => ${aryData.size}")
//                contactListAdapter.updateItems(aryData, false)

                val requestData = ContactRequestData(toolkit_number = aryData)

                contactsViewModel.updateContactList(contactList = requestData)
            }
        }

        // Observe the contacts LiveData
        contactsViewModel.contacts.observe(viewLifecycleOwner, Observer { contacts ->

            contactList.clear()
            contactList.addAll(contacts)

            if(selectedContactList.isNotEmpty()){
                val selectedList = selectedContactList.associateBy { it.number }

                contactList.forEach { contact ->
                    selectedList[contact.number]?.let { newContact ->
                        contact.isSelected = newContact.isSelected
                    }
                }
            }

            contactListAdapter.updateItems(contactList, true)
            Log.d(TAG, "onViewCreated: hide loader success")
            hideLoader(myContext, loader)

        })

        contactsViewModel.updateContactListResponseLiveData.observe(viewLifecycleOwner, Observer { it ->
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)

                    Log.d(TAG, "bindObservers: response received for contact update => ${it}")
                    contactListAdapter.updateItems(contactList, false)

                    selectedContactList.clear()
                    selectedContactList.addAll(contactList)

                    contactListAdapter.notifyDataSetChanged()

                    if(contactList.isEmpty()){
                        binding.emptyContactCardLayout.visible()
                        binding.addContactLabel.text = "Add trusted contact"
                        binding.addContactBtn.text = "Add here"
                    }else{
                        binding.emptyContactCardLayout.gone()
                        binding.addContactLabel.text = "Trusted contact"
                        binding.addContactBtn.text = "Add more"
                    }

                    contactsViewModel.clearUpdateContactRes()
                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)
                    alertDialogService.alertDialogAnim(
                        myContext,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(myContext, loader)
                }
            }
        })

//  *************************** Recent Rides *******************************************************************

        binding.selectContactRecyclerview.layoutManager = LinearLayoutManager(context)
        // Initialize the adapter with an empty list and set it to the RecyclerView
        contactListAdapter = ContactListRecyclerviewAdapter(contactList, myContext) { contactData ->
//          Log.d("TAG", "onBindViewHolder: contact list data => ${contactList} and ${this}")
            contactList.remove(contactData)

            if(contactList.isEmpty()) {
                val requestData = ContactRequestData(toolkit_number = emptyList())
                contactsViewModel.updateContactList(contactList = requestData)
            }else{
                val requestData = ContactRequestData(toolkit_number = contactList)
                contactsViewModel.updateContactList(contactList = requestData)
            }

        }
        binding.selectContactRecyclerview.adapter = contactListAdapter

//  *************************** Recent Rides *******************************************************************



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

    override fun onPermissionsResult(result: List<PermissionStatus>) {
        when {
            result.anyPermanentlyDenied() -> myContext.showPermanentlyDeniedDialog(result, "Please allowed Permission"){
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", myContext.packageName, null)
                }
                permissionLauncher.launch(intent)
            }
            result.anyShouldShowRationale() -> myContext.showRationaleDialog(result, request, "Please allowed Permission")
            result.allGranted() -> {
//                showToast("permission granted")
//                showLoader(myContext, loader)
                contactsViewModel.fetchContacts()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        bindObservers()

    }

    private fun bindObservers() {
        contactsViewModel.contactListResponseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is NetworkResult.Success -> {

                    hideLoader(myContext, loader)

                    contactList.clear()

                    contactList.addAll(it.data?.ContactList ?: ArrayList())

                    selectedContactList.clear()
                    selectedContactList.addAll(contactList)

                    if(contactList.isEmpty()){
                        binding.emptyContactCardLayout.visible()
                        binding.addContactLabel.text = "Add trusted contact"
                        binding.addContactBtn.text = "Add here"
                    }else{
                        binding.emptyContactCardLayout.gone()
                        binding.addContactLabel.text = "Trusted contact"
                        contactListAdapter.updateItems(contactList, false)
                        binding.addContactBtn.text = "Add more"
                    }


                    contactsViewModel.clearContactListRes()

                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)

                    alertDialogService.alertDialogAnim(
                        myContext,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(myContext, loader)
                }
            }
        })
    }

}