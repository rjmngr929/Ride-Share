package com.my.raido.Utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.InputStream

class Helper {
    companion object {
        fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun showKeyboard(view: View, editText: EditText){
            try {
                // Optionally show the keyboard
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }catch (e: Exception){

            }
        }

        fun hideKeyboard(view: View){
            try {
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }catch (e: Exception){

            }
        }

        private fun copyText(text: String, myContext: Context) {
            if (!TextUtils.isEmpty(text)) {
                val clipboard = myContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Recognized Text", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(myContext, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
            }
        }

        // Convert the text data to RequestBody
        fun getMultiPartFormRequestBody(tag: String): RequestBody {
            return tag.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        }

        // Convert the image file to MultipartBody.Part
        fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, file.name, requestBody)
        }



        fun changeWordColor(myContext: Context, view: TextView, word: String, sentence: String, colorCode: Int){
            val textView =  view //binding.didNtGetOtpCode

// The sentence in which you want to change a single word's color
            val sentence = sentence // resources.getString(R.string.did_nt_get_otp_code)

// Create a SpannableString with the sentence
            val spannable = SpannableString(sentence)

// Set the color for a specific portion (e.g., the word "example")
            val wordToColor = word// "OTP code?"

            if(sentence.contains(wordToColor)) {
                val startIndex = sentence.indexOf(wordToColor)
                val endIndex = startIndex + wordToColor.length

                val color = ContextCompat.getColor(myContext, colorCode)

                if (startIndex != -1) {
                    // Apply color to the word "example"
                    spannable.setSpan(
                        ForegroundColorSpan(color), // You can replace RED with your desired color
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

// Set the spannable string to the TextView
                textView.text = spannable
            }
        }

        private fun getSystemDetail(): JSONObject {

            val deviceInfo = JSONObject()

            deviceInfo.put("Brand", Build.BRAND)
            deviceInfo.put("Model", Build.MODEL)
            deviceInfo.put("ID", Build.ID)
            deviceInfo.put("SDK", Build.VERSION.SDK_INT)
            deviceInfo.put("Manufacture", Build.MANUFACTURER)
            deviceInfo.put("Board", Build.BOARD)
            deviceInfo.put("version_code", Build.VERSION.RELEASE)

            if(Build.HOST != null)
                deviceInfo.put("Host", Build.HOST)
            else
                deviceInfo.put("Host", "")

//            //  Total Ram
//            val actManager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
//            val memInfo = ActivityManager.MemoryInfo()
//            actManager.getMemoryInfo(memInfo)
//            val availMemory = memInfo.availMem.toDouble()/(1024*1024*1024)
//            val totalMemory= memInfo.totalMem.toDouble()/(1024*1024*1024)
//
//
//            //  Total Storage
//            val iPath: File = Environment.getDataDirectory()
//            val iStat = StatFs(iPath.path)
//            val iBlockSize = iStat.blockSizeLong
//            val iAvailableBlocks = iStat.availableBlocksLong
//            val iTotalBlocks = iStat.blockCountLong
//            val iAvailableSpace = formatSize(iAvailableBlocks * iBlockSize)
//            val iTotalSpace = formatSize(iTotalBlocks * iBlockSize)

            return deviceInfo
        }

        fun getWordAfterKeyword(fullString: String, keyword: String): String? {
            // Escape the keyword to avoid regex special characters issues
            val escapedKeyword = Regex.escape(keyword)
            // Regex to find the keyword followed by a word
            val regex = Regex("$escapedKeyword\\s+(\\w+)")
            val matchResult = regex.find(fullString)
            return matchResult?.groups?.get(1)?.value
        }

        fun fetchPanCardNumber(fullString: String): String? {
            val panCardPattern = "[A-Z]{5}[0-9]{4}[A-Z]"
            val regex = Regex(panCardPattern)
            val matchResult = regex.find(fullString)
            return matchResult?.value
        }

        fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
            return try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun formatSize(size: Long): String? {
            var size = size
            var suffix: String? = null
            if (size >= 1024) {
                suffix = " KB"
                size /= 1024
                if (size >= 1024) {
                    suffix = " MB"
                    size /= 1024
                    if(size >= 1024){
                        suffix = " GB"
                        size /= 1024
                    }
                }
            }
            val resultBuffer = StringBuilder(java.lang.Long.toString(size))
            var commaOffset = resultBuffer.length - 3
            while (commaOffset > 0) {
                resultBuffer.insert(commaOffset, ',')
                commaOffset -= 3
            }
            if (suffix != null) resultBuffer.append(suffix)
            return resultBuffer.toString()
        }




    }

}