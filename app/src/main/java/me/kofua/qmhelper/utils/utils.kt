package me.kofua.qmhelper.utils

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.google.protobuf.GeneratedMessageLite
import kotlinx.coroutines.MainScope
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.XposedInit.Companion.modulePath
import me.kofua.qmhelper.XposedInit.Companion.moduleRes
import java.io.File
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class Weak<T>(val initializer: () -> T?) {
    private var weakReference: WeakReference<T?>? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = weakReference?.get() ?: let {
        weakReference = WeakReference(initializer())
        weakReference
    }?.get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }
}

val systemContext: Context
    get() {
        val activityThread = "android.app.ActivityThread".from(null)
            ?.callStaticMethod("currentActivityThread")!!
        return activityThread.callMethodAs("getSystemContext")
    }

fun getPackageVersion(packageName: String) = try {
    @Suppress("DEPRECATION")
    systemContext.packageManager.getPackageInfo(packageName, 0).run {
        String.format("${packageName}@%s(%s)", versionName, getVersionCode(packageName))
    }
} catch (e: Throwable) {
    Log.e(e)
    "(unknown)"
}

fun getVersionCode(packageName: String) = try {
    @Suppress("DEPRECATION")
    systemContext.packageManager.getPackageInfo(packageName, 0).versionCode
} catch (e: Throwable) {
    Log.e(e)
    -1
}

val currentContext by lazy { AndroidAppHelper.currentApplication() as Context }

val hostPackageName: String by lazy { currentContext.packageName }

val isBuiltIn get() = modulePath.endsWith("so") || modulePath.contains("lspatch")

val is64 get() = currentContext.applicationInfo.nativeLibraryDir.contains("64")

val logFile by lazy { File(currentContext.externalCacheDir, "log.txt") }

val oldLogFile by lazy { File(currentContext.externalCacheDir, "old_log.txt") }

@Suppress("DEPRECATION")
val sPrefs
    get() = currentContext.getSharedPreferences("qmhelper", Context.MODE_MULTI_PROCESS)!!

@Suppress("DEPRECATION")
val sCaches
    get() = currentContext.getSharedPreferences("qmhelper_cache", Context.MODE_MULTI_PROCESS)!!

@SuppressLint("DiscouragedApi")
fun getResId(name: String, type: String) =
    currentContext.resources.getIdentifier(name, type, currentContext.packageName)

val shouldSaveLog get() = sPrefs.getBoolean("save_log", true)

fun GeneratedMessageLite<*, *>.print(indent: Int = 0): String {
    val sb = StringBuilder()
    for (f in javaClass.declaredFields) {
        if (f.name.startsWith("bitField")) continue
        if (f.isStatic) continue
        f.isAccessible = true
        val v = f.get(this)
        val name = buildString {
            for (i in 0 until indent) append('\t')
            append(f.name.substringBeforeLast("_"), ": ")
        }
        when (v) {
            is GeneratedMessageLite<*, *> -> {
                sb.appendLine(name).append(v.print(indent + 1))
            }

            is List<*> -> {
                for (vv in v) {
                    sb.append(name)
                    when (vv) {
                        is GeneratedMessageLite<*, *> -> {
                            sb.appendLine().append(vv.print(indent + 1))
                        }

                        else -> {
                            sb.appendLine(vv?.toString() ?: "null")
                        }
                    }
                }
            }

            else -> {
                sb.append(name).appendLine(v?.toString() ?: "null")
            }
        }
    }
    return sb.toString()
}

operator fun ViewGroup.iterator(): MutableIterator<View> = object : MutableIterator<View> {
    private var index = 0
    override fun hasNext() = index < childCount
    override fun next() = getChildAt(index++) ?: throw IndexOutOfBoundsException()
    override fun remove() = removeViewAt(--index)
}

val ViewGroup.children: Sequence<View>
    get() = object : Sequence<View> {
        override fun iterator() = this@children.iterator()
    }

fun View.addBackgroundRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun Any?.reflexToString() = this?.javaClass?.declaredFields?.joinToString {
    "${it.name}: ${
        it.run { isAccessible = true;get(this@reflexToString) }
    }"
}

fun string(@StringRes resId: Int) = currentContext.runCatchingOrNull {
    getString(resId)
} ?: moduleRes.getString(resId)

fun string(@StringRes resId: Int, vararg args: Any) = currentContext.runCatchingOrNull {
    getString(resId, *args)
} ?: moduleRes.getString(resId, *args)

fun stringArray(@ArrayRes resId: Int): Array<String> = currentContext.resources.runCatchingOrNull {
    getStringArray(resId)
} ?: moduleRes.getStringArray(resId)

val qmSp by lazy {
    instance.spManagerClass?.callStaticMethodAs<SharedPreferences>(instance.getSp()) ?: sPrefs
}

val handler = Handler(Looper.getMainLooper())
val mainScope = MainScope()

@SuppressLint("ApplySharedPref")
fun SharedPreferences.edit(commit: Boolean = false, action: SharedPreferences.Editor.() -> Unit) =
    edit().apply(action).run { if (commit) commit() else apply() }

inline fun <T, R> T.runCatchingOrNull(func: T.() -> R?) = try {
    func()
} catch (e: Throwable) {
    null
}
