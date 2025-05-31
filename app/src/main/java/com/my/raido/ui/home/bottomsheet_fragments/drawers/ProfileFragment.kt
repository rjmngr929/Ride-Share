package com.my.raido.ui.home.bottomsheet_fragments.drawers

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.DatePickerHelper
import com.my.raido.Utils.Helper
import com.my.raido.Utils.ImageUtils
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.showPermanentlyDeniedDialog
import com.my.raido.Utils.showRationaleDialog
import com.my.raido.Utils.showToast
import com.my.raido.Validations.EmailRule
import com.my.raido.Validations.EmptyTextRule
import com.my.raido.Validations.UserNameRule
import com.my.raido.Validations.validateRule
import com.my.raido.constants.Constants
import com.my.raido.databinding.FragmentProfileBinding
import com.my.raido.models.Database.DataModel.User
import com.my.raido.models.profile.ProfileDetailRequest
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BottomSheetDialogFragment(), PermissionRequest.Listener {

    companion object{
        private const val TAG = "Profile Fragment"
    }

    private lateinit var binding: FragmentProfileBinding

    private lateinit var genderSelectAdapter : ArrayAdapter<String>

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val userViewModel: UserDataViewModel by viewModels()

    private lateinit var loader: AlertDialog

    lateinit var datePicker: DatePickerHelper

    private var selectedGender : String = ""
    private var selectedDob : String = ""

    private var photoURI: Uri? = null;
    private lateinit var bitmapdata : Bitmap
    private lateinit var resized : Bitmap
    private lateinit var currentPhotoPath: String

    private val request by lazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionsBuilder(
                arrayListOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ).build()
        }else{
            permissionsBuilder(
                arrayListOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                )
            ).build()
        }
    }

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Camera data is $result: ")
//            binding.profileError.visibility = View.GONE
//            binding.profilePic.setImageURI(photoURI)

            Glide.with(this@ProfileFragment)
                .load(photoURI)
                .circleCrop() // Apply circular crop to the image
                .placeholder(R.drawable.dummyuser) // Placeholder image while loading
                .error(R.drawable.dummyuser)
                .transition(DrawableTransitionOptions.withCrossFade()) // Fade transition
                .into(binding.profilePic)

            //                            CropImage.activity(uri)
//                                .setCropShape(CropImageView.CropShape.RECTANGLE)
//                                .setFixAspectRatio(true)
//                                .start(this)

            lifecycleScope.launch(Dispatchers.IO) {
//                bitmapdata = MediaStore.Images.Media.getBitmap(this@ProfileActivity.contentResolver, Uri.parse(
//                    photoURI.toString()
//                ))
//                resized = Bitmap.createScaledBitmap(bitmapdata, 400, 400, true)

                bitmapdata = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(myContext.contentResolver, Uri.parse(
                    photoURI.toString()
                )), 400, 400, true)

//                photoURI = getImageUriFromBitmap(this@ProfileActivity, bitmapdata)

                withContext(Dispatchers.Main) {
//                    confirmAlert(R.layout.animate_confirmation_layout)
                    updateProfilePic()
                    myContext.showToast("Please confirm profile")
                }

            }

        }
    }

    private var gallerylauncher = registerForActivityResult(ActivityResultContracts.GetContent()){

        if(it != null){


            lifecycleScope.launch(Dispatchers.IO) {
                bitmapdata = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(myContext.contentResolver, Uri.parse(
                    it.toString()
                )), 400, 400, true)

                withContext(Dispatchers.Main) {
//                    confirmAlert(R.layout.animate_confirmation_layout)

                    Glide.with(myContext)
                        .load(bitmapdata)
                        .circleCrop() // Makes the image circular
                        .placeholder(R.drawable.dummyuser) // Optional: placeholder while loading
                        .error(R.drawable.dummyuser) // Optional: error image if loading fails
                        .into(binding.profilePic)

                    updateProfilePic()
                    myContext.showToast("Please confirm profile")
                }

            }

        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        datePicker = DatePickerHelper(myContext)

        loader = getLoadingDialog(myContext)

        request.addListener(this)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dismiss()
        }

//  ****************************** Profile update **************************************************
        binding.editProfile.setOnClickListener {
            request.send()
        }
//  ****************************** Profile update **************************************************

//  ************************************* Gender Section *******************************************
        val genderAry = arrayListOf("male", "female", "other")
        genderSelectAdapter = ArrayAdapter(
            myContext, android.R.layout.simple_spinner_dropdown_item, genderAry
        )
        binding.genderFilter.setAdapter(genderSelectAdapter)


        if(genderAry.size > 5)
            binding.genderFilter.dropDownHeight = 500

        binding.genderFilter.setDropDownBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_border,
                null
            )
        )

        if(genderAry.size > 5)
            binding.genderFilter.dropDownHeight = 1000

        binding.genderFilter.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, id->
                Log.d(TAG, "onCreate: selected gender => ${genderAry[position]}")
                selectedGender = genderAry[position]
                (binding.dobText.parent.parent as TextInputLayout).error = null
            }
//  ************************************* Gender Section *******************************************

//  ********************************* Select Date of Birth ****************************************
        binding.dobText.setOnClickListener {

                val cal = Calendar.getInstance()
                val d = cal.get(Calendar.DAY_OF_MONTH)
                val m = cal.get(Calendar.MONTH)
                val y = cal.get(Calendar.YEAR) - 10

                val maxDate = Calendar.getInstance()
                maxDate.add(Calendar.YEAR, -10)
                maxDate.add(Calendar.MONTH, 0)
                maxDate.add(Calendar.DATE, 0)
                datePicker.setMaxDate(maxDate.timeInMillis)

                datePicker.showDialog(d, m, y, object : DatePickerHelper.Callback {
                    override fun onDateSelected(dayofMonth: Int, month: Int, year: Int) {
                        val dayStr = if (dayofMonth < 10) "0${dayofMonth}" else "${dayofMonth}"
                        val mon = month + 1
                        val monthStr = if (mon < 10) "0${mon}" else "${mon}"
                        binding.dobText.setText("${dayStr}-${monthStr}-${year}")
                        selectedDob = "${dayStr}-${monthStr}-${year}"
                        (binding.dobText.parent.parent as TextInputLayout).error = null
                    }
                })
        }
//  ********************************* Select Date of Birth ****************************************

        val isValidName = binding.fullNameText.validateRule(
            rules = listOf(
                EmptyTextRule(myContext),
                UserNameRule()
            )
        )

        val isValidEmail = binding.emailText.validateRule(
            rules = listOf(
                EmptyTextRule(myContext),
                EmailRule()
            )
        )

        binding.updateBtn.setOnClickListener {

            val isValidate = (isValidName && isValidEmail)

            if(isValidate) {
                if(!selectedGender.isNullOrEmpty()) {
                    if(!selectedDob.isNullOrEmpty()) {
                        userViewModel.updateProfile(
                            ProfileDetailRequest(
                                userName = Helper.getMultiPartFormRequestBody(binding.fullNameText.text.toString()),
                                userEmail = Helper.getMultiPartFormRequestBody(binding.emailText.text.toString()),
                                userDob = Helper.getMultiPartFormRequestBody(binding.dobText.text.toString()),
                                userGender = Helper.getMultiPartFormRequestBody(selectedGender)
                            )
                        )
                    }else{
                        binding.dobLayout.error = "Please select date of birth"
                        myContext.showToast("Please select date of birth")
                    }
                }else{
//                    (binding.selectGender.parent.parent as TextInputLayout).error = "Please select gender"
                    binding.selectGender.error = "Please select gender"
                    myContext.showToast("Please select gender")
                }
            }
        }

//  ********************** Fetch Local Database Data ***********************************************
        userViewModel.allUsers.observe(viewLifecycleOwner) { userList ->
            Log.d(TAG, "onViewCreated: user data => ${userList.size}")
            if (userList.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: user data => ${userList[0].userDob == null}")

                binding.fullNameText.setText(userList[0].userName.toString())
                binding.mobileText.setText(userList[0].userMobile.toString())

                if(!userList[0].userEmail.isNullOrEmpty())
                    binding.emailText.setText(userList[0].userEmail.toString())

                binding.memberSinceText.setText( extractDate("2024-10-21T08:34:14.000000Z") ) //userList[0].memberSince.toString()

                if(!userList[0].userDob.isNullOrEmpty()){
                    binding.dobText.setText(userList[0].userDob.toString())
                    selectedDob = userList[0].userDob.toString()
                }


                if(!userList[0].gender.isNullOrEmpty()){
                    selectedGender = userList[0].gender.toString()
                    binding.genderFilter.setText(userList[0].gender, false)
                }

                if (!userList[0].userProfileImg.isNullOrEmpty()){
                    Glide.with(myContext)
                        .load("${Constants.IMAGE_BASE_URL}/${userList[0].userProfileImg}")
                        .circleCrop() // Makes the image circular
                        .placeholder(R.drawable.dummyuser) // Optional: placeholder while loading
                        .error(R.drawable.dummyuser) // Optional: error image if loading fails
                        .into(binding.profilePic)
                }

            }
        }
//  ********************** Fetch Local Database Data ***********************************************

//  ************************* Profile Response *****************************************************
        userViewModel.profileResponseLiveData.observe(viewLifecycleOwner, Observer { it ->
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)

                    Log.d(TAG, "bindObservers: response received for profile data => ${it.data?.userData}")

                    val userData = it.data?.userData!!

                    userViewModel.updateUser(
                        User(
                            userId = userData.userId,
                            userName = userData.userName,
                            userEmail = userData.userEmail,
                            gender = userData.gender,
                            userDob = userData.dob,
                            userProfileImg = userData.profilePic,
                            userMobile = userData.mobileNumber,
                            memberSince = userData.memberSince,
                            currency = userData.currency,
                            totalRatingSum = userData.totalRatingSum,
                            totalRatings = userData.totalRatingsCount,
                            walletStatus = userData.isWalletActive != "false",
                            totalCompleteRides = userData.totalCompleteRides,
                            walletBalance = userData.walletBalance
                        )
                    )

                    Glide.with(this@ProfileFragment)
                        .load("${Constants.IMAGE_BASE_URL}/${userData.profilePic}")
                        .circleCrop() // Apply circular crop to the image
                        .placeholder(R.drawable.dummyuser) // Placeholder image while loading
                        .error(R.drawable.dummyuser)
                        .transition(DrawableTransitionOptions.withCrossFade()) // Fade transition
                        .into(binding.profilePic)

                    alertDialogService.customAlertDialogAnim(
                        myContext,
                        "Success",
                        "Profile Update Successfully",
                        R.raw.success,
                        btnText = "OK"
                    )

                    userViewModel.clearProfileRes()
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
//  ************************* Profile Response *****************************************************

    }

    fun extractDate(input: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Set UTC timezone

        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(input)
        return outputFormat.format(date!!)
    }

    private fun updateProfilePic(){
        if (::bitmapdata.isInitialized) {
            val compressedFile = ImageUtils.instance.bitmapToFile(myContext, bitmapdata)
            val photoPath = compressedFile?.absolutePath

           val profileImg = Helper.prepareFilePart(
                "profile_picture",
                File(photoPath)
            )

            userViewModel.updateProfilePhoto(
                profileImg
            )
        }else{
            myContext.showToast("Please update profile picture")
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

    override fun onDestroy() {
        super.onDestroy()
        request.removeListener(this)
    }

    private fun createImageFile(): File? {
        val directory = File(myContext.filesDir, "Raido")

        if (!directory.exists()) {
            val isDirectoryCreated = directory.mkdirs()

            if (isDirectoryCreated) {
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val storageDir: File? = myContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

                return File.createTempFile(
                    "JPEG_${timeStamp}_", /* prefix */
                    ".jpg", /* suffix */
                    directory /* directory */
                ).apply {
                    // Save a file: path for use with ACTION_VIEW intents
                    currentPhotoPath = absolutePath
                }
            } else{
                myContext.showToast("something went wrong")
                return null
            }
        } else {
            Log.d(TAG, "createImageFile: enter that function for create file")
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? = myContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            Log.d(TAG, "createImageFile: storage dir => $storageDir ")
            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                directory /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoPath = absolutePath
            }
        }

    }

    fun chooseProfile(){
        val bottomSheetDialog = BottomSheetDialog(myContext)
        bottomSheetDialog.setContentView(R.layout.edit_profile_dialog)

        val cameraBtn =
            bottomSheetDialog.findViewById<ImageButton>(R.id.camera_circle);
        val galleryBtn =
            bottomSheetDialog.findViewById<ImageButton>(R.id.gallery_circle);

        bottomSheetDialog.show()

        cameraBtn?.setOnClickListener {
            bottomSheetDialog.dismiss()
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .also { takePictureIntent ->

//                                Log.d(TAG, "onCreate: enter that section => ${takePictureIntent.resolveActivity(packageManager)}")
                    if (takePictureIntent.resolveActivity(myContext.packageManager) != null) {
                        // Ensure that there's a camera activity to handle the intent
                        takePictureIntent.resolveActivity(myContext.packageManager)?.also {
//                                        Log.d(
//                                            TAG,
//                                            "onCreate: enter that section => ${takePictureIntent.extras}"
//                                        )

                            // Create the File where the photo should go
                            val photoFile: File? = try {
                                createImageFile()
                            } catch (ex: IOException) {
                                // Error occurred while creating the File

                                null
                            }
                            // Continue only if the File was successfully created
                            photoFile?.also {
                                photoURI = FileProvider.getUriForFile(
                                    myContext,
                                    "${myContext.packageName}.provider",
                                    it
                                )
                                takePictureIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    photoURI
                                )
//                            startActivityForResult(takePictureIntent, 1)
//                                            Log.d(TAG, "onCreate: camera launcher launch")
                                cameraLauncher.launch(takePictureIntent)
                            }
                        }
                    } else {
//                                    Log.d(
//                                        TAG,
//                                        "onCreate: take picture intent => ${takePictureIntent}"
//                                    )
                        try {
                            val photoFile: File? = try {
                                createImageFile()
                            } catch (ex: IOException) {
                                null
                            }

                            photoFile?.also {
                                photoURI = FileProvider.getUriForFile(
                                    myContext,
                                    "${myContext.packageName}.provider",
                                    it
                                )
                                takePictureIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    photoURI
                                )
//                                            Log.d(TAG, "onCreate: take pic at that point")
                                cameraLauncher.launch(takePictureIntent)
                            }
                        } catch (e: ActivityNotFoundException) {
                            alertDialogService.alertDialogAnim(
                                myContext,
                                "something went wrong with profile",
                                R.raw.failed
                            )
//                            Toast.makeText(myContext, "something went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

        }

        galleryBtn?.setOnClickListener {
            bottomSheetDialog.dismiss()
            gallerylauncher.launch("image/*")
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK){
            chooseProfile()
        }else{
            request.send()
        }

    }

    override fun onPermissionsResult(result: List<PermissionStatus>) {

        when {
            result.anyPermanentlyDenied() -> myContext.showPermanentlyDeniedDialog(result, "Please allowed camera and media Permission"){
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", myContext.packageName, null)
                }
                permissionLauncher.launch(intent)
            }
            result.anyShouldShowRationale() -> myContext.showRationaleDialog(result, request, "Please allowed camera and media Permission")
            result.allGranted() -> {
                chooseProfile()
            }
        }




    }

}