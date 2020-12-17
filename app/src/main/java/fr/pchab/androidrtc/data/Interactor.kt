package fr.pchab.androidrtc.data

import fr.pchab.androidrtc.data.executor.PostExecutionThread
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

abstract class Interactor<in Q : Interactor.RequestValues, P>
    (private val postExecutionThread: PostExecutionThread)
{
    private val disposable: CompositeDisposable = CompositeDisposable()

    interface RequestValues

    abstract fun buildUseCase(requestValues: Q?) : Flowable<P>

    fun run(consumer: DisposableSubscriber<P>, requestValues: Q? = null) {
        val observable: Flowable<P> = this.buildUseCase(requestValues)
        disposable.add(observable.subscribeOn(Schedulers.io())
                .observeOn(postExecutionThread.scheduler)
                .subscribeWith(consumer))
    }

    open fun dispose() {
        disposable.clear()
    }
}