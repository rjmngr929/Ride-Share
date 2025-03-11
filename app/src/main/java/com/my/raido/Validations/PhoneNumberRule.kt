package com.my.raido.Validations

import android.content.Context
import com.my.raido.R
import java.util.regex.Pattern

class PhoneNumberRule(context: Context,
    override val errorMessage: String = context.getString(R.string.mobileValidate)
): ValidationRule(predicate = {

    val number = Regex (  "[^0-9]").replace(it, "")
    //number.length != PHONE_NUMBER_LENGTH &&
            !Pattern.compile("^[6-9]\\d{9}\$").matcher(number).matches()

})