package com.intuisoft.plaid.androidwrappers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

fun <T> Observable<T>.observeOnMain(
    onNext: (T) -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}

fun <T> Observable<T>.observeOnMain(
    onNext: (T) -> Unit,
    onError: (Throwable) -> Unit,
    onComplete: () -> Unit
): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError, onComplete)
}

fun <T> Maybe<T>.observeOnMain(
    onNext: (T) -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}

fun <T> Single<T>.observeOnMain(
    onNext: (T) -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}

fun Completable.observeOnMain(
    onComplete: () -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribe(onComplete, onError)
}

fun <T> Observable<T>.toLiveData() : LiveData<T> =
    MutableLiveData<T>().apply {
        this@toLiveData.subscribe { value = it }
    }

