package com.keyri.demo.utils

import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController

fun CharSequence.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun CharSequence.isValidPhoneNumber() =
    !isNullOrEmpty() && startsWith("+1") && length == 10

fun Context.getActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

fun NavHostController.navigateWithPopUp(toRoute: String, fromRoute: String) {
    this.navigate(toRoute) {
        popUpTo(fromRoute) {
            inclusive = true
        }
    }
}
