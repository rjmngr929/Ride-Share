package com.my.raido.Validations

import com.my.raido.Utils.isValidAadhaar

class AadharCardRule(
    override val errorMessage: String = "Invalid Aadhaar number. It should be a 12-digit number."
): ValidationRule(predicate = {

    !it.isValidAadhaar()

})