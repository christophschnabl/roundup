package xyz.schnabl.remote

/**
 * Declares an interface to serialize and deserialize to and from json to not depend on one json framework
 */
interface JsonSerdeService {

    fun <T> fromJson(json: String, clazz: Class<T>) : T

    fun <T> toJson(obj: T): String
}