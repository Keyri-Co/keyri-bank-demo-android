package com.keyri.androidFullExample.screens.signup

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.keyri.androidFullExample.data.SignupInputState
import com.keyri.androidFullExample.utils.isValidEmail
import com.keyri.androidFullExample.utils.isValidPhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignupViewModel : ViewModel() {
    private val _signupState = MutableStateFlow(SignupInputState())
    val signupState = _signupState.asStateFlow()

    fun validateInputs(): Boolean =
        _signupState.value.name.length > 2 &&
            _signupState.value.email.isValidEmail() &&
            _signupState.value.mobile.text
                .isEmpty() or
            _signupState.value.mobile.text
                .isValidPhoneNumber()

    fun updateName(newName: String) {
        _signupState.value = _signupState.value.copy(name = newName)
    }

    fun updateEmail(newEmail: String) {
        _signupState.value = _signupState.value.copy(email = newEmail)
    }

    fun updateMobile(newMobile: TextFieldValue) {
        _signupState.value = _signupState.value.copy(mobile = newMobile)
    }
}
