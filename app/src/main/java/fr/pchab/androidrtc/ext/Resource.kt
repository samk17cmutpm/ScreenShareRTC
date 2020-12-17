package com.sakkto.videosnoads.ext

import com.google.gson.JsonObject

data class Resource<out T> constructor(
    val state: ResourceState,
    val data: T? = null,
    val error: JsonObject? = null,
    val payload: Any? = null
)
