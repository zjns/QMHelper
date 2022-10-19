package me.kofua.qmhelper.utils

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.*
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import kotlinx.coroutines.MainScope
import me.kofua.qmhelper.XposedInit.Companion.modulePath
import me.kofua.qmhelper.XposedInit.Companion.moduleRes
import me.kofua.qmhelper.classLoader
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
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
    hookInfo.spManager.clazz.from(classLoader)
        ?.callStaticMethodAs<SharedPreferences>(hookInfo.spManager.get.name) ?: sPrefs
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

fun Uri.realDirPath() = when (scheme) {
    null, ContentResolver.SCHEME_FILE -> path

    ContentResolver.SCHEME_CONTENT -> {
        if (authority == "com.android.externalstorage.documents") {
            val treeDocId = runCatchingOrNull {
                DocumentsContract.getTreeDocumentId(this)
            } ?: ""
            if (!treeDocId.contains(":")) {
                null
            } else {
                val type = treeDocId.substringBefore(':')
                val dirPath = treeDocId.substringAfter(':')
                val externalStorage = if (type == "primary") {
                    Environment.getExternalStorageDirectory().absolutePath
                } else "/storage/$type"
                File(externalStorage, dirPath).absolutePath
            }
        } else null
    }

    else -> null
}

fun CharSequence.copyToClipboard(label: CharSequence = "") {
    ClipData.newPlainText(label, this)?.let {
        currentContext.getSystemService(ClipboardManager::class.java)
            .setPrimaryClip(it)
    }
}

inline fun <C : CharSequence, R> C?.ifNotEmpty(action: (text: C) -> R) =
    if (!isNullOrEmpty()) action(this) else null

fun String.toUri(): Uri = Uri.parse(this)

fun Context.addModuleAssets() = assets.callMethod("addAssetPath", modulePath)
