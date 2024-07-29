package com.keyri.androidFullExample

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.keyri.androidFullExample.data.KeyriProfiles

class MainActivityViewModel(val dataStore: DataStore<KeyriProfiles>) : ViewModel() {


}
