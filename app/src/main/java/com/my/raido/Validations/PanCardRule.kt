package com.my.raido.Validations

import com.my.raido.Utils.isValidPan

class PanCardRule(
    override val errorMessage: String = "Please enter valid pan number"
): ValidationRule(predicate = {

    !it.isValidPan()

})