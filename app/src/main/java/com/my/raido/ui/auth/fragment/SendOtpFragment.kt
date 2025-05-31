package com.my.raido.ui.auth.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.Helper
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showLoader
import com.my.raido.Validations.EmptyTextRule
import com.my.raido.Validations.PhoneNumberRule
import com.my.raido.Validations.validateRule
import com.my.raido.databinding.FragmentSendOtpBinding
import com.my.raido.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SendOtpFragment : Fragment() {

    companion object{
        private const val TAG = "Send OTP Fragment"
    }

    private lateinit var binding: FragmentSendOtpBinding

    private lateinit var myContext: Context

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var loader: AlertDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSendOtpBinding.inflate(inflater, container, false)


//        val myView = binding.termConditionPolicy
        excludeViewFromGestures(binding.termConditionPolicy)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loader = getLoadingDialog(myContext)

        arguments.let{
            if (it != null) {
                val number = it.getString("mobile_no", null)
                if(number != null){
                    binding.mobileEdittext.setText(number)
                }
            }
        }

        binding.mobileEdittext.validateRule(
            rules = listOf(
                EmptyTextRule(myContext),
                PhoneNumberRule(myContext)
            ),
            binding.mobileNumberErr
        )

        binding.mobileLayout.setOnClickListener {
            binding.mobileEdittext.requestFocus()
            Helper.showKeyboard(binding.mobileLayout, binding.mobileEdittext)
        }

        binding.getOtpBtn.setOnClickListener {
            if(binding.mobileEdittext.text.isNotEmpty()){
                if(binding.mobileNumberErr.text.toString() == "true"){

                    authViewModel.sendOtp(binding.mobileEdittext.text.toString())

//                    val navOptions = NavOptions.Builder()
//                        .setPopUpTo(R.id.sendOtpFragment, true)  // Pops SendOtpFragment from the back stack
//                        .build()
//
//                    val bundle = Bundle()
//                    bundle.putString("mobile_no", binding.mobileEdittext.text.toString())
//                    NavHostFragment.findNavController(this@SendOtpFragment)
//                        .navigate(
//                            R.id.action_sendOtpFragment_to_otpVerifyFragment,
//                            bundle, navOptions = navOptions
//                        );

                }
            }else{
                binding.mobileNumberErr.text = "Please enter valid number"
            }

//                val navOptions = NavOptions.Builder()
//                    .setPopUpTo(R.id.sendOtpFragment, true)  // Pops SendOtpFragment from the back stack
//                    .build()
//                findNavController().navigate(R.id.action_sendOtpFragment_to_otpVerifyFragment, null, navOptions)

        }

    }

    override fun onResume() {
        super.onResume()
        bindObservers()

        Helper.changeWordColor(myContext, binding.welcomeRaidoText, "Raido", resources.getString(R.string.hi_welcome_to_raido), R.color.primary)
    }


    private fun bindObservers() {
        authViewModel.otpSendResponseLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "bindObservers: response received from otp send => ${it}")
            when (it) {
                is NetworkResult.Success -> {
                    hideLoader(myContext, loader)
                    Log.d(TAG, "bindObservers: response received from otp send => ${it}")

                    authViewModel.clearOtpSendRes()

                    try {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.sendOtpFragment, true)  // Pops SendOtpFragment from the back stack
                            .build()

                        val bundle = Bundle()
                        bundle.putString("mobile_no", binding.mobileEdittext.text.toString())
                        NavHostFragment.findNavController(this@SendOtpFragment)
                            .navigate(
                                R.id.action_sendOtpFragment_to_otpVerifyFragment,
                                bundle, navOptions = navOptions
                            )
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationError", "Navigation action not found: ${e.message}")
                    }


                }
                is NetworkResult.Error -> {
                    hideLoader(myContext, loader)

                    alertDialogService.alertDialogAnim(
                        myContext,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received from otp send => Error = ${it.message}")
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

    fun excludeViewFromGestures(textView: TextView) {
        // Get the full localized text from strings.xml
        val fullText = getString(R.string.by_continuing_you_agree_to_the_t_amp_c_and_privacy_policy)

        // Get the localized placeholders
        val termsAndConditions = getString(R.string.terms_and_conditions)
        val privacyPolicy = getString(R.string.privacy_policy)

        // Format the full string with placeholders
        val formattedText = String.format(fullText, termsAndConditions, privacyPolicy)

        // Create a SpannableString from the formatted text
        val spannableString = SpannableString(formattedText)

        // Create a clickable span for "T&C"
        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://techoniqfusion.com/"))
                startActivity(intent)
            }
        }

        // Create a clickable span for "Privacy Policy"
        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://techoniqfusion.com/"))
                startActivity(intent)
            }
        }

        // Find the start and end indices of the placeholders
        val termsStart = formattedText.indexOf(termsAndConditions)
        val termsEnd = termsStart + termsAndConditions.length

        val privacyStart = formattedText.indexOf(privacyPolicy)
        val privacyEnd = privacyStart + privacyPolicy.length

        // Set clickable spans for T&C and Privacy Policy
        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString to the TextView
        textView.text = spannableString

        // Enable links to be clickable
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

}