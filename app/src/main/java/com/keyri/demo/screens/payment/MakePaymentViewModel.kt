package com.keyri.demo.screens.payment

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.demo.data.KeyriProfiles
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.json.JSONObject

class MakePaymentViewModel(
    private val keyri: Keyri,
    private val dataStore: DataStore<KeyriProfiles>
) : ViewModel() {
    fun performMakePaymentEvent(recipient: String, amount: Float, result: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.data
                .mapNotNull { it.currentProfile }
                .collectLatest { email ->
                    val eventResult =
                        keyri.sendEvent(email, EventType.withdrawal(metadata = JSONObject().apply {
                            put("recipient", recipient)
                            put("amount", amount)
                        }), true)
                    result(eventResult.isSuccess)
                }
        }
    }
}
