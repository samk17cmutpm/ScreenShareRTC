package fr.pchab.androidrtc.base

import fr.pchab.androidrtc.data.executor.PostExecutionThread
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

class UIThread  internal constructor() : PostExecutionThread {
    override val scheduler: Scheduler get() = AndroidSchedulers.mainThread()
}