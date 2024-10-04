package com.keyrico.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import com.keyrico.keyrisdk.exception.DenialException
import com.keyrico.keyrisdk.exception.RiskException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ScannerAuthVM : ViewModel() {
    private val _uiState = MutableStateFlow<ScannerAuthState>(ScannerAuthState.Empty)

    val uiState: StateFlow<ScannerAuthState> = _uiState.asStateFlow()

    fun easyKeyriAuth(
        activity: ScannerAuthActivity,
        url: Uri,
        appKey: String,
        publicApiKey: String?,
        serviceEncryptionKey: String?,
        payload: String,
        publicUserId: String?,
        detectionsConfig: KeyriDetectionsConfig,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ScannerAuthState.Loading

            Keyri(activity, appKey, publicApiKey, serviceEncryptionKey, detectionsConfig)
                .processLink(
                    fragmentManager = activity.supportFragmentManager,
                    url = url,
                    payload = payload,
                    publicUserId = publicUserId,
                ).onSuccess {
                    _uiState.value = ScannerAuthState.Authenticated
                }.onFailure {
                    if (it !is RiskException && it !is DenialException) {
                        processError(it)
                    } else {
                        setEmptyState(immediate = true)
                    }
                }
        }
    }

    private suspend fun processError(e: Throwable) {
        _uiState.value = ScannerAuthState.Error(e.message ?: "Unable to authorize")

        setEmptyState(immediate = false)
    }

    private suspend fun setEmptyState(immediate: Boolean) {
        if (!immediate) {
            delay(3_000L)
        }

        _uiState.value = ScannerAuthState.Empty
    }
}
