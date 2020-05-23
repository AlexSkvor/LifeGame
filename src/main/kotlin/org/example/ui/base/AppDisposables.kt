package org.example.ui.base

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class AppDisposables : DisposablesProvider {
    override val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    override fun clear() {
        compositeDisposable.clear()
    }

    override fun Disposable.bind() {
        compositeDisposable.add(this)
    }
}