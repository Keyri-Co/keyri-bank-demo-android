package com.keyri.demo.screens.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerifyViewModel(private val keyri: Keyri) : ViewModel() {

    fun sendEvent(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (keyri.getAssociationKey(email).getOrThrow() == null) {
                keyri.generateAssociationKey(email)
            }

            keyri.sendEvent(email, EventType.signup(), true)
        }
    }
}
