package fr.pchab.androidrtc.ui

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakkto.videosnoads.ext.Resource
import fr.pchab.androidrtc.data.network.NoConnectivityException
import fr.pchab.androidrtc.ext.setError
import fr.pchab.androidrtc.ext.setLoading
import fr.pchab.androidrtc.ext.setSuccess
import io.reactivex.subscribers.DisposableSubscriber
import retrofit2.HttpException

class Consumer<T>(
  private val liveData: MutableLiveData<Resource<T>>? = null,
  private val payload: Any? = null
) : DisposableSubscriber<T>() {
    override fun onStart() {
        super.onStart()
        liveData?.setLoading()
    }

    override fun onNext(t: T) { liveData?.setSuccess(t, payload) }

    override fun onError(throwable: Throwable?) {
        throwable?.printStackTrace()
        val error: JsonObject? = try {
            throwable?.let { it ->
                when(it) {
                    is NoConnectivityException -> {
                        val jsonObject = JsonObject()
                        jsonObject.addProperty(ErrorKeys.NoInternetConnection, it.message)
                        jsonObject
                    }
                    is HttpException -> {
                        val httpException = it as HttpException?
                        val errorMessage = httpException?.response()?.errorBody()?.string()
                        errorMessage?.let { message ->  JsonParser().parse(message).asJsonObject }
                    }
                    else -> null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        liveData?.setError(error, payload)
    }

    override fun onComplete() {}
}