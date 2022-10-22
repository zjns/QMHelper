@file:Suppress("UNUSED", "UNCHECKED_CAST")

package me.kofua.qmhelper.utils

import de.robv.android.xposed.XposedHelpers.*
import me.kofua.qmhelper.classLoader
import me.kofua.qmhelper.data.ClassInfo
import me.kofua.qmhelper.data.Field
import me.kofua.qmhelper.data.Method
import me.kofua.qmhelper.from

inline fun <T : ClassInfo> T.hookBeforeMethod(
    method: T.() -> Method,
    vararg exArgs: Any?,
    crossinline hooker: Hooker
) {
    val m = this.method()
    clazz.from(classLoader)?.hookBeforeMethod(m.name, *m.paramTypes, *exArgs, hooker = hooker)
}

inline fun <T : ClassInfo> T.hookAfterMethod(
    method: T.() -> Method,
    vararg exArgs: Any?,
    crossinline hooker: Hooker
) {
    val m = this.method()
    clazz.from(classLoader)?.hookAfterMethod(m.name, *m.paramTypes, *exArgs, hooker = hooker)
}

inline fun <T : ClassInfo> T.replaceMethod(
    method: T.() -> Method,
    vararg exArgs: Any?,
    crossinline replacer: Replacer
) {
    val m = this.method()
    clazz.from(classLoader)?.replaceMethod(m.name, *m.paramTypes, *exArgs, replacer = replacer)
}

fun Any.getObjectField(field: Field): Any? = getObjectField(this, field.name)

fun Any.getObjectFieldOrNull(field: Field): Any? = runCatchingOrNull {
    getObjectField(this, field.name)
}

fun <T> Any.getObjectFieldAs(field: Field) = getObjectField(this, field.name) as T

fun <T> Any.getObjectFieldOrNullAs(field: Field) = runCatchingOrNull {
    getObjectField(this, field.name) as T
}

fun Any.getIntField(field: Field) = getIntField(this, field.name)

fun Any.getIntFieldOrNull(field: Field) = runCatchingOrNull {
    getIntField(this, field.name)
}

fun Any.getLongField(field: Field) = getLongField(this, field.name)

fun Any.getLongFieldOrNull(field: Field) = runCatchingOrNull {
    getLongField(this, field.name)
}

fun Any.getBooleanField(field: Field) = getBooleanField(this, field.name)

fun Any.getBooleanFieldOrNull(field: Field) = runCatchingOrNull {
    getBooleanField(this, field.name)
}

fun Any.callMethod(method: Method, vararg args: Any?): Any? =
    callMethod(this, method.name, *args)

fun Any.callMethodOrNull(method: Method, vararg args: Any?): Any? = runCatchingOrNull {
    callMethod(this, method.name, *args)
}

fun <T> Any.callMethodAs(method: Method, vararg args: Any?) =
    callMethod(this, method.name, *args) as T

fun <T> Any.callMethodOrNullAs(method: Method, vararg args: Any?) = runCatchingOrNull {
    callMethod(this, method.name, *args) as T
}

fun Class<*>.callStaticMethod(method: Method, vararg args: Any?): Any? =
    callStaticMethod(this, method.name, *args)

fun Class<*>.callStaticMethodOrNull(method: Method, vararg args: Any?): Any? =
    runCatchingOrNull {
        callStaticMethod(this, method.name, *args)
    }

fun <T> Class<*>.callStaticMethodAs(method: Method, vararg args: Any?) =
    callStaticMethod(this, method.name, *args) as T

fun <T> Class<*>.callStaticMethodOrNullAs(method: Method, vararg args: Any?) =
    runCatchingOrNull {
        callStaticMethod(this, method.name, *args) as T
    }

fun <T> Class<*>.getStaticObjectFieldAs(field: Field) =
    getStaticObjectField(this, field.name) as T

fun <T> Class<*>.getStaticObjectFieldOrNullAs(field: Field) = runCatchingOrNull {
    getStaticObjectField(this, field.name) as T
}

fun Class<*>.getStaticObjectField(field: Field): Any? =
    getStaticObjectField(this, field.name)

fun Class<*>.getStaticObjectFieldOrNull(field: Field): Any? = runCatchingOrNull {
    getStaticObjectField(this, field.name)
}

fun Class<*>.setStaticObjectField(field: Field, obj: Any?) = apply {
    setStaticObjectField(this, field.name, obj)
}

fun Class<*>.setStaticObjectFieldIfExist(field: Field, obj: Any?) = apply {
    try {
        setStaticObjectField(this, field.name, obj)
    } catch (ignored: Throwable) {
    }
}

fun <T> T.setIntField(field: Field, value: Int) = apply {
    setIntField(this, field.name, value)
}

fun <T> T.setLongField(field: Field, value: Long) = apply {
    setLongField(this, field.name, value)
}

fun <T> T.setObjectField(field: Field, value: Any?) = apply {
    setObjectField(this, field.name, value)
}

fun <T> T.setBooleanField(field: Field, value: Boolean) = apply {
    setBooleanField(this, field.name, value)
}
