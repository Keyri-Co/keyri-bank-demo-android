package com.keyri.androidFullExample.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyri.androidFullExample.repositories.KeyriDemoRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class LoginViewModel(
    private val repository: KeyriDemoRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val loading = _loading.asStateFlow()
    val errorMessage = _errorMessage.asStateFlow()

    private val throwableScope =
        CoroutineExceptionHandler { _, throwable ->
            _errorMessage.value = throwable.message

            timer(initialDelay = 1_000L, period = 1_000L) {
                _errorMessage.value = null
            }
        }

    fun emailLogin(
        email: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + throwableScope) {
            _loading.value = true

            repository.authWithFirebase(email)
            repository.emailLogin(email)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun stopLoading() {
        _loading.value = false
    }
}
