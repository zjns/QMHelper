package me.kofua.qmhelper.utils

import me.kofua.qmhelper.classLoader
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import org.json.JSONArray
import org.json.JSONObject

fun String?.toJSONObject() = JSONObject(orEmpty())

@Suppress("UNCHECKED_CAST")
fun <T> JSONArray.asSequence() = (0 until length()).asSequence().map { get(it) as T }

operator fun JSONArray.iterator(): Iterator<JSONObject> =
    (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

fun JSONArray?.orEmpty() = this ?: JSONArray()
fun JSONArray?.isEmpty() = this == null || this.length() == 0
fun JSONArray?.isNotEmpty() = !isEmpty()

val gson by lazy { hookInfo.gson.clazz.from(classLoader)?.new() }

inline fun <reified T> String.fromJson(): T? {
    return gson?.callMethodAs(hookInfo.gson.fromJson.name, this, T::class.java)
}

fun String.fromJson(type: Class<*>?): Any? {
    return gson?.callMethod(hookInfo.gson.fromJson.name, this, type)
}

fun Any.toJson(): String? {
    return gson?.callMethodAs(hookInfo.gson.toJson.name, this)
}
