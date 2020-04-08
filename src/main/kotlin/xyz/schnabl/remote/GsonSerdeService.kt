package xyz.schnabl.remote

import com.google.gson.Gson
import com.google.inject.Inject

/**
 * Implements the Json Service using the configured gson provided
 * @property gson : Gson the gson object configured in the module
 */
class GsonSerdeService @Inject constructor(private val gson: Gson) :
    JsonSerdeService {
    override fun <T> fromJson(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    override fun <T> toJson(obj: T): String {
        return gson.toJson(obj)
    }
}