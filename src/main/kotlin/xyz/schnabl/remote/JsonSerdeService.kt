package xyz.schnabl

/**
 * TODO KDOC
 */
interface JsonSerdeService {

    fun <T> fromJson(json: String, clazz: Class<T>) : T

    fun <T> toJson(obj: T): String
}