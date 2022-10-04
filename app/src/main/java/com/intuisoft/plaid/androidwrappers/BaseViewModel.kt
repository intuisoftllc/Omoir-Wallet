package com.intuisoft.plaid.androidwrappers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _loadng = MutableLiveData<Boolean>()
    val loadng: LiveData<Boolean> = _loadng

    fun <T> execute(call: suspend () -> T,  onFinish: suspend (Result<T>) -> Unit) {
        _loadng.postValue(true)
        viewModelScope.launch {
            var result : Result<T>

            withContext(Dispatchers.IO) {
                try {
                    result = Result.success(call())
                } catch (e: Throwable) {
                    result = Result.failure(e)
                }

                _loadng.postValue(false)

                onFinish(result)
            }
        }
    }
}