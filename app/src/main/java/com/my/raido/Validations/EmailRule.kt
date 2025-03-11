package com.my.raido.Validations

import com.my.raido.Utils.isValidEmail

class EmailRule(
    override val errorMessage: String = "Please enter valid email"
): ValidationRule(predicate = {

    !it.isValidEmail()

})