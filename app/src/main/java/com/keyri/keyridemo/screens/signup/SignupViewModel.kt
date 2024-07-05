package com.keyri.keyridemo.screens.signup

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.keyri.keyridemo.data.SignupInputState
import com.keyri.keyridemo.utils.isValidEmail
import com.keyri.keyridemo.utils.isValidPhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignupViewModel : ViewModel() {

    private val _signupState = MutableStateFlow(SignupInputState())
    val signupState = _signupState.asStateFlow()

    fun validateInputs(): Boolean {
        return _signupState.value.name.length > 2 && _signupState.value.email.isValidEmail() && _signupState.value.mobile.text.isEmpty() or _signupState.value.mobile.text.isValidPhoneNumber()
    }

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

