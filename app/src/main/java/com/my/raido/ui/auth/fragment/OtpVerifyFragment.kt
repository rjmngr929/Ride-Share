package com.my.raido.ui.auth.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.messaging.FirebaseMessaging
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.Helper
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.TokenManager
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.invisible
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.visible
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.FragmentOtpVerifyBinding
import com.my.raido.models.Database.DataModel.User
import com.my.raido.models.login.OtpVerifyRequest
import com.my.raido.ui.auth.AuthViewModel
import com.my.raido.ui.home.HomeActivity
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OtpVerifyFragment : Fragment() {

    companion object{
        private const val TAG = "OTP Verify Fragment"
    }

    private lateinit var binding: FragmentOtpVerifyBinding

    private lateinit var myContext: Context

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val authViewModel: AuthViewModel by viewModels()
    private val userDataViewModel: UserDataViewModel by viewModels()
    private val masterDataViewModel: MasterViewModel by viewModels()

    private var resendTimer: CountDownTimer? = null

    private lateinit var loader: AlertDialog

    private lateinit var firstField : EditText
    private lateinit var secondField : EditText
    private lateinit var thirdField : EditText
    private lateinit var forthField : EditText

    private var mobileNumber: String = ""

    private  var otp_code : String = ""

    private  var fcmToken : String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpVerifyBinding.inflate(inflater, container, false)

        //GenericTextWatcher here works only for moving to next EditText when a number is entered
        //first parameter is the current EditText and second parameter is next EditText
        binding.otpOne.addTextChangedListener(GenericTextWatcher(binding.otpOne, binding.otpTwo))
        binding.otpTwo.addTextChangedListener(GenericTextWatcher(binding.otpTwo, binding.otpThree))
        binding.otpThree.addTextChangedListener(GenericTextWatcher(binding.otpThree, binding.otpFour))
        binding.otpFour.addTextChangedListener(GenericTextWatcher(binding.otpFour, null))

        //GenericKeyEvent here works for deleting the element and to switch back to previous EditText
        //first parameter is the current EditText and second parameter is previous EditText
        binding.otpOne.setOnKeyListener(GenericKeyEvent(binding.otpOne, null))
        binding.otpTwo.setOnKeyListener(GenericKeyEvent(binding.otpTwo, binding.otpOne))
        binding.otpThree.setOnKeyListener(GenericKeyEvent(binding.otpThree, binding.otpTwo))
        binding.otpFour.setOnKeyListener(GenericKeyEvent(binding.otpFour,binding.otpThree))

        resendTimer()

        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments.let{
            if (it != null) {
                mobileNumber = it.getString("mobile_no", "").toString()
            }
        }

        loader = getLoadingDialog(myContext)

        binding.mobileOtpText.text = mobileNumber

    //        *************************** FCM Token ****************************************************
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Failed to get FCM token: ${task.exception}")
                return@addOnCompleteListener
            }

            // Get the FCM token
            fcmToken = task.result

            sharedPrefManager.putString(Constants.FCM_TOKEN, fcmToken)

            println("FCM Token: $fcmToken")
        }
//        *************************** FCM Token ****************************************************

        firstField = binding.otpOne
        secondField = binding.otpTwo
        thirdField = binding.otpThree
        forthField = binding.otpFour

        firstField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                firstField.backgroundTintList = null
            } else {
                firstField.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.bg_light)
            }
        }

        secondField.setOnFocusChangeListener { _, hasFocus ->
            if(firstField.text.isNotEmpty()) {
                if (hasFocus) {
                    secondField.backgroundTintList = null
                } else {
                    secondField.backgroundTintList =
                        ContextCompat.getColorStateList(myContext, R.color.bg_light)
                }
            }else{
                firstField.requestFocus()
                secondField.backgroundTintList =
                    ContextCompat.getColorStateList(myContext, R.color.bg_light)
            }
        }

        thirdField.setOnFocusChangeListener { _, hasFocus ->
            if(secondField.text.isNotEmpty()) {
                if (hasFocus) {
                    thirdField.backgroundTintList = null
                } else {
                    thirdField.backgroundTintList =
                        ContextCompat.getColorStateList(myContext, R.color.bg_light)
                }
            }else{
                secondField.requestFocus()
                thirdField.backgroundTintList =
                    ContextCompat.getColorStateList(myContext, R.color.bg_light)
            }
        }

        forthField.setOnFocusChangeListener { _, hasFocus ->
            if(thirdField.text.isNotEmpty()) {
                if (hasFocus) {
                    forthField.backgroundTintList = null
                } else {
                    forthField.backgroundTintList =
                        ContextCompat.getColorStateList(myContext, R.color.bg_light)
                }
            }else{
                thirdField.requestFocus()
                forthField.backgroundTintList =
                    ContextCompat.getColorStateList(myContext, R.color.bg_light)
            }
        }

        binding.changeNumber.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.otpVerifyFragment, true)  // Pops SendOtpFragment from the back stack
                .build()
//            findNavController().navigate(R.id.action_otpVerifyFragment_to_sendOtpFragment, null , navOptions)

            val bundle = Bundle()
            bundle.putString("mobile_no", mobileNumber)
            NavHostFragment.findNavController(this@OtpVerifyFragment)
                .navigate(
                    R.id.action_otpVerifyFragment_to_sendOtpFragment,
                    bundle, navOptions = navOptions
                );
        }

        binding.verifyOtpBtn.setOnClickListener {
            Log.d(TAG, "onViewCreated: otp btn clicked => ${otp_code}")
            if(otp_code.length == 4) {
                binding.otpErr.invisible()
                val otpRequestData =  OtpVerifyRequest(
                    mobileNumber,
                    otp_code,
                    fcmToken
                )
                authViewModel.submitOtp(otpRequestData)

//                startActivity(Intent(myContext,HomeActivity::class.java))
//                activity?.finish()

            }else{
                binding.otpErr.visible()
            }
        }

        binding.resendOtpBtn.setOnClickListener {
            authViewModel.sendOtp(mobileNumber.toString())
        }

    }

    override fun onResume() {
        super.onResume()
        resendOTPListener()
        verifyOTPListener()

        Helper.changeWordColor(myContext, binding.didNtGetOtpCode, "OTP code?", resources.getString(R.string.did_nt_get_otp_code), R.color.primary)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resendTimer?.cancel()
        resendTimer = null
    }

    fun clearOtpFields(){

        firstField.requestFocus()

        firstField.text.clear()
        secondField.text.clear()
        thirdField.text.clear()
        forthField.text.clear()
    }

    private fun resendOTPListener() {
        authViewModel.otpSendResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "resendOTPListener: response received from otp send => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)
                    Log.d(TAG, "resendOTPListener: response received from otp send => ${it}")

                    authViewModel.clearOtpSendRes()
                    resendTimer()

                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)

                    alertDialogService.alertDialogAnim(
                        myContext,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "resendOTPListener: response received from otp send => Error = ${it.message}")
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

    private fun resendTimer(){
        resendTimer = object : CountDownTimer(59000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
//                TimerText.setText("00 : " + millisUntilFinished / 1000)
                binding.resendOtpLayout.visibility = View.GONE
                binding.otpTimerLayout.visibility = View.VISIBLE
                binding.otpTimer.text = if(millisUntilFinished / 1000 < 10)  "0${millisUntilFinished / 1000}" else "${millisUntilFinished / 1000} " //Auto fetching OTP 10sec
            }

            override fun onFinish() {
                binding.resendOtpLayout.visibility = View.VISIBLE
                binding.otpTimerLayout.visibility = View.GONE
            }
        }.start()
    }

    private fun verifyOTPListener() {
        authViewModel.otpVerifyResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "verifyOTPListener: verify otp data => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)
                    binding.otpErr.invisible()

                    tokenManager.saveToken(it.data?.authToken.toString())

                    val userData = it.data?.userData!!

                    sharedPrefManager.putInt(Constants.USER_ID, userData.userId)

                    sharedPrefManager.putBoolean(Constants.WALLET_ACTIVE_STATUS, userData.isWalletActive != "false")

                    masterDataViewModel.setWalletBalanceData(userData.walletBalance)

                    userDataViewModel.insertUser(
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
                    startActivity(Intent(myContext,HomeActivity::class.java))
                    activity?.finish()

                }
                is NetworkResult.Error -> {
                    binding.otpErr.visible()
                    hideLoader(myContext, loader)
                    alertDialogService.alertDialogAnim(myContext, it.message.toString(), R.raw.failed)
                    otp_code = ""
                    clearOtpFields()
                }
                is NetworkResult.Loading ->{
                    binding.otpErr.invisible()
                    showLoader(myContext, loader)
                }
                is NetworkResult.Empty -> {
                    binding.otpErr.invisible()
                    hideLoader(myContext, loader)
                }
            }
        })
    }

    class GenericKeyEvent internal constructor(private val currentView: EditText, private val previousView: EditText?) : View.OnKeyListener{
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if(event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.otp_one && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }

    inner class GenericTextWatcher internal constructor(private val currentView: View, private val nextView: View?) :
        TextWatcher {
        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.otp_one -> if (text.length == 1) {
                    nextView!!.requestFocus()
                    otp_code = firstField.text.toString()

//                    currentView.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.primary)
                }
                R.id.otp_two -> if (text.length == 1) {
                    nextView!!.requestFocus()
                    otp_code += secondField.text.toString()

//                    currentView.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.primary)
                }
                R.id.otp_three -> if (text.length == 1) {
                    nextView!!.requestFocus()
                    otp_code += thirdField.text.toString()

//                    currentView.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.primary)
                }
                R.id.otp_four -> if (text.length == 1) {

//                    currentView.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.primary)

                    otp_code = binding.otpOne.text.toString() + binding.otpTwo.text.toString() + binding.otpThree.text.toString() + binding.otpFour.text.toString()

                    binding.otpErr.invisible()

                    Helper.hideKeyboard(binding.otpFour)
                    // Remove focus from EditText
                    binding.otpFour.clearFocus()

//                    if(otp_code.length == 6){
//                        if(fromRegister){
//                            registerVerfyUser(otp_code)
//                        }else{
//                            loginVerfyUser()
//                        }
//                    }else{
//                        Toast.makeText(context, "please enter valid otp", Toast.LENGTH_SHORT).show()
//                    }

                }else{
                    currentView.backgroundTintList = ContextCompat.getColorStateList(myContext, R.color.bg_light)
                }
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) {

        }

        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) {
//            when (currentView.id) {
//                R.id.otp_one -> if (arg0.isEmpty()) {
//                    currentView.backgroundTintList =
//                        ContextCompat.getColorStateList(myContext, R.color.gray_light)
//                }
//
//                R.id.otp_two -> if (arg0.isEmpty()) {
//                    currentView.backgroundTintList =
//                        ContextCompat.getColorStateList(myContext, R.color.gray_light)
//                }
//
//                R.id.otp_three -> if (arg0.isEmpty()) {
//                    currentView.backgroundTintList =
//                        ContextCompat.getColorStateList(myContext, R.color.gray_light)
//                }
//
//                R.id.otp_four -> if (arg0.isEmpty()) {
//                    currentView.backgroundTintList =
//                        ContextCompat.getColorStateList(myContext, R.color.gray_light)
//                }
//            }
        }

    }

}