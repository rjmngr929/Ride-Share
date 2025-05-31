package com.my.raido.Utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.my.raido.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class AlertDialogUtility @Inject constructor(@ApplicationContext val context: Context) {

        private var mToast: Toast? = null
        private var mAlert: AlertDialog? = null
        private var mConfirmationAlert: AlertDialog? = null

    private var isDialogShowing = false

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        showToast(context, message, duration)
    }

    fun showToast(context: Context, resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        showToast(context, context.getString(resId), duration)
    }

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (mToast != null) {
            if (mToast!!.view?.isShown == true) mToast?.setText(message) else createToast(context, message, duration)
        } else
            createToast(context, message, duration)
    }

    private fun createToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        mToast = Toast.makeText(context, message, duration)
        mToast?.show()
    }



//        fun alertDialogMultiLogin(context: Context) {
//
//            val layoutBuilder = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null)
//            val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.MyDialogStyle).setView(layoutBuilder).setCancelable(false)
//            val alertDialog:AlertDialog = builder.show()
//
//            val title = layoutBuilder.findViewById<TextView>(R.id.alert_title)
//            val messaage = layoutBuilder.findViewById<TextView>(R.id.alert_message)
////            val icon = layoutBuilder.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.alert_icon)
//            val okBtn = layoutBuilder.findViewById<com.google.android.material.button.MaterialButton>(R.id.dismiss_alert)
//            val controller = AppController.mInstance!!
//
//            title.text = "Multiple login detected!"
//            messaage.text = "Please login again!.."
////            icon.setAnimation(R.raw.failed)
//
//            val activity = context as Activity
//
//            okBtn.setOnClickListener {
//                controller.clearAll()
//                startActivity(context, Intent(context, SplashScreenActivity::class.java), null)
//                activity.finish()
//                alertDialog.dismiss()
//            }
//
//
//        }

        @SuppressLint("MissingInflatedId")
        fun alertDialogStatusAnim(context: Context, msg: String, rawFile: Int, finishedActivity: Boolean = false){
//            var dialogBuilder = android.app.AlertDialog.Builder(context)
//            val layoutView: View = LayoutInflater.from(context).inflate(R.layout.alertdialog_anim_layout, null)
//
//            val icon : com.airbnb.lottie.LottieAnimationView = layoutView.findViewById(R.id.alertdialog_anim_icon)
//            val title : TextView = layoutView.findViewById(R.id.alertdialog_title)
//            val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_btn)
//
//            icon.setAnimation(rawFile)
//
//            dialogBuilder.setView(layoutView)
//            val alertDialog = dialogBuilder.create()
//            alertDialog.setCancelable(false)
//            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
//            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            alertDialog.show()
//
//            title.text = msg
//
//            val activity = context as Activity
//
//            confirmBtn.setOnClickListener {
//                if(finishedActivity){
//                    activity.finish()
//                }
//                alertDialog.dismiss()
//            }
        }

        fun showAlert(
            context: Context,
            title: String? = null,
            message: String
        ) {
            if (mAlert != null && mAlert!!.isShowing) {
                mAlert!!.dismiss()
            }
            mAlert = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.okay), null)
                .create()
            mAlert!!.show()
        }

        fun alertDialogAnim(context: Context, msg: String, rawFile: Int, finishedActivity: Boolean = false){

            // Agar dialog already dikh raha hai, to skip
            if (isDialogShowing) return

            isDialogShowing = true // Dialog ab dikh raha hai

            var dialogBuilder = android.app.AlertDialog.Builder(context)
            val layoutView: View = LayoutInflater.from(context).inflate(R.layout.alertdialog_anim_layout, null)

            val icon : com.airbnb.lottie.LottieAnimationView = layoutView.findViewById(R.id.alertdialog_anim_icon)
            val title : TextView = layoutView.findViewById(R.id.alertdialog_title)
            val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_btn)

            icon.setAnimation(rawFile)

            dialogBuilder.setView(layoutView)
            val alertDialog = dialogBuilder.create()
            alertDialog.setCancelable(false)
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()

            title.text = msg

            val activity = context as Activity

            confirmBtn.setOnClickListener {
                if (finishedActivity) {
                    activity.finish()
                }
                alertDialog.dismiss()
                isDialogShowing = false // Dialog dismiss ho gaya
            }
        }

    @SuppressLint("MissingInflatedId")
    fun customAlertDialogAnim(myContext: Context, titleMsg: String, msg: String, rawFile: Int, msgDisplay: Boolean = true, btnText : String= "", onButtonClick: () -> Unit = {}){
        var dialogBuilder = android.app.AlertDialog.Builder(myContext)
        val layoutView: View = LayoutInflater.from(myContext).inflate(R.layout.alertdialog_custom_anim_layout, null)

        val icon : com.airbnb.lottie.LottieAnimationView = layoutView.findViewById(R.id.alertdialog_anim_icon)
        val title : TextView = layoutView.findViewById(R.id.alertdialog_title)
        val message : TextView = layoutView.findViewById(R.id.alertdialog_desc)
        val confirmBtn : MaterialButton = layoutView.findViewById(R.id.alertdialog_confirm_btn)

        icon.setAnimation(rawFile)

        dialogBuilder.setView(layoutView)
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        title.text = titleMsg
        if(msgDisplay) {
            message.text = msg
        }else{
            message.gone()
        }

        if(btnText.isNotEmpty()){
            confirmBtn.text = btnText
        }

        confirmBtn.setOnClickListener {
            onButtonClick()
            alertDialog.dismiss()
        }

    }

}