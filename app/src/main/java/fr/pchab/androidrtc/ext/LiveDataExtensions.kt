package fr.pchab.androidrtc.ext

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.sakkto.videosnoads.ext.Resource
import com.sakkto.videosnoads.ext.ResourceState

fun <T> MutableLiveData<Resource<T>>.setSuccess(bean: T, payload: Any? = null) =
    postValue(Resource(ResourceState.SUCCESS, bean, payload = payload))

fun <T> MutableLiveData<Resource<T>>.setLoading() =
    postValue(Resource(ResourceState.LOADING, value?.data))

fun <T> MutableLiveData<Resource<T>>.setError(jsonObject: JsonObject? = null, payload: Any? = null) =
    postValue(Resource(ResourceState.ERROR, value?.data, jsonObject, payload = payload))
