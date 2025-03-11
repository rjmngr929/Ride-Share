package com.my.raido.Utils

import android.app.AlertDialog
import android.content.Context
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.request.PermissionRequest
import com.my.raido.R

internal fun Context.showToast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

//componentId.visible()
fun View.gone() {
    visibility = View.GONE
}

//componentId.invisible()
fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setOnSingleClickListener(interval: Long = 1000, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            onClick(it)
        }
    }
}

fun View.showSnack(message: String, action: String = "", actionListener: () -> Unit = {}): Snackbar {
    var snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    if (action != "") {
        snackbar.duration = Snackbar.LENGTH_INDEFINITE
        snackbar.setAction(action) {
            actionListener()
            snackbar.dismiss()
        }
    }
    snackbar.show()
    return snackbar
}

fun String.capitalizeFirstLetter(): String {
    return if (isNotEmpty()) {
        this[0].uppercaseChar() + substring(1)
    } else {
        this
    }
}

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidMobile(): Boolean {
    return Patterns.PHONE.matcher(this).matches()
}

fun String.isAlphabetWithSpace(): Boolean {
//    return this.matches(Regex("^[a-zA-Z\\s]+$"))
    return this.matches(Regex("^[a-zA-Z\\s]{3,}$"))
}

fun String.isValidPan(): Boolean {
    return this.matches(Regex("^[A-Za-z]{5}[0-9]{4}[A-Za-z]$"))
}

fun String.isValidAadhaar(): Boolean {
    return this.matches(Regex("^[2-9]{1}[0-9]{11}$"))
}



//Animations
fun View.toptobottomAnimation(myContext: Context) {
    return startAnimation(AnimationUtils.loadAnimation(myContext , R.anim.ttb))
}

fun View.zoomOutAnimation(myContext: Context) {
    return startAnimation(AnimationUtils.loadAnimation(myContext , R.anim.stb))
}

fun View.bottomtotop_type_oneAnimation(myContext: Context) {
    return startAnimation(AnimationUtils.loadAnimation(myContext , R.anim.btt))
}

fun View.bottomtotop_type_twoAnimation(myContext: Context) {
    return startAnimation(AnimationUtils.loadAnimation(myContext , R.anim.btt2))
}

internal fun Context.showGrantedToast(permissions: List<PermissionStatus>) {
    val msg = getString(R.string.granted_permissions, permissions.toMessage<PermissionStatus.Granted>())
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

internal fun Context.showRationaleDialog(permissions: List<PermissionStatus>, request: PermissionRequest, msg: String) {
//    val msg = getString(R.string.rationale_permissions, permissions.toMessage<PermissionStatus.Denied.ShouldShowRationale>())

    AlertDialog.Builder(this)
        .setTitle(R.string.permissions_required)
        .setMessage(msg)
        .setPositiveButton(R.string.request_again) { _, _ ->
            // Send the request again.
            request.send()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

internal fun Context.showPermanentlyDeniedDialog(permissions: List<PermissionStatus>, msg: String, onButtonClick: () -> Unit = {}) {
//    val msg = getString(R.string.permanently_denied_permissions, permissions.toMessage<PermissionStatus.Denied.Permanently>())


    AlertDialog.Builder(this)
        .setTitle(R.string.permissions_required)
        .setMessage(msg)
        .setPositiveButton(R.string.action_settings) { _, _ ->


            onButtonClick()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

//private fun Context.createAppSettingsIntent() = Intent().apply {
//    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//    data = Uri.fromParts("package", packageName, null)
//}

//**************************** Loader ************************************************************
//Loader
fun getLoadingDialog(myContext: Context): androidx.appcompat.app.AlertDialog {
    val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(myContext, R.style.MyDialogStyle_transparent)
    alertDialogBuilder.setView(R.layout.layout_loading_alert_dialog)
    alertDialogBuilder.setCancelable(false)

    return alertDialogBuilder.create()
}

//show Loader
fun showLoader(myContext: Context, loader: androidx.appcompat.app.AlertDialog){
    loader.show()
}

//hide loader
fun hideLoader(myContext: Context, loader: androidx.appcompat.app.AlertDialog){
    if (loader.isShowing)
        loader.dismiss()
}
//**************************** Loader ************************************************************

private inline fun <reified T : PermissionStatus> List<PermissionStatus>.toMessage(): String = filterIsInstance<T>()
    .joinToString { it.permission }