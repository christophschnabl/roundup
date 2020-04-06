package xyz.schnabl

import com.google.gson.Gson
import com.google.inject.Inject

/**
 * TODO kdoc
 */
class GsonSerdeService @Inject constructor(private val gson: Gson) : JsonSerdeService {
    override fun <T> fromJson(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    override fun <T> toJson(obj: T): String {
        return gson.toJson(obj)
    }
}