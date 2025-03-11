package com.my.raido.Validations

import com.my.raido.Utils.isAlphabetWithSpace


class UserNameRule(
    override val errorMessage: String = "Please enter valid name"
): ValidationRule(predicate = {

    !it.isAlphabetWithSpace()

})