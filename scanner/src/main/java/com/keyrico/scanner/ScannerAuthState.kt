package com.keyrico.scanner

internal sealed class ScannerAuthState {
    object Empty : ScannerAuthState()

    object Loading : ScannerAuthState()

    object Authenticated : ScannerAuthState()

    class Error(
        val message: String,
    ) : ScannerAuthState()
}
