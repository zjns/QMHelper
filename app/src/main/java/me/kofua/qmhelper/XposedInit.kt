package me.kofua.qmhelper

import android.app.Application
import android.app.Instrumentation
import android.content.res.Resources
import android.content.res.XModuleResources
import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import me.kofua.qmhelper.hook.*
import me.kofua.qmhelper.utils.*

val classLoader get() = XposedInit.classLoader

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        moduleRes = XModuleResources.createInstance(modulePath, null)
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.tencent.qqmusic") return
        classLoader = lpparam.classLoader
        disableTinker()

        Instrumentation::class.java.hookBeforeMethod(
            "callApplicationOnCreate",
            Application::class.java
        ) {
            when {
                !lpparam.processName.contains(":") -> {
                    if (shouldSaveLog) startLog()
                    currentContext.addModuleAssets()

                    Log.d("QQMusic process launched ...")
                    Log.d("QMHelper version: ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}) from $modulePath${if (isBuiltIn) " (BuiltIn)" else ""}")
                    Log.d("QQMusic version: ${getPackageVersion(lpparam.packageName)} (${if (is64) "64" else "32"}bit)")
                    Log.d("SDK: ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT}); Phone: ${Build.BRAND} ${Build.MODEL}")
                    Log.d("Config: ${sPrefs.all}")

                    preloadProxyClasses()
                    val debugHooks = if (BuildConfig.DEBUG) {
                        listOf(SSLHook, DebugHook, ABTesterHook)
                    } else listOf()
                    val buildInHooks = if (isBuiltIn) {
                        listOf(WebLoginHook)
                    } else listOf()
                    val normalHooks = listOf(
                        SettingsHook,
                        SplashHook,
                        HomeTopTabHook,
                        HomePageHook,
                        CgiHook,
                        CopyHook,
                        MiscHook,
                        CommonAdsHook,
                        WebViewHook,
                        JceHook
                    )
                    val allHooks = buildList {
                        addAll(debugHooks)
                        addAll(buildInHooks)
                        addAll(normalHooks)
                    }
                    startHook(allHooks)
                }
            }
        }
    }

    private fun startHook(hooks: List<BaseHook>) {
        hooks.forEach {
            try {
                it.hook()
            } catch (t: Throwable) {
                Log.e(t)
                val errorMessage = t.message ?: ""
                BannerTips.error(string(R.string.hook_error, errorMessage))
            }
        }
    }

    private fun startLog() = try {
        if (logFile.exists()) {
            if (oldLogFile.exists())
                oldLogFile.delete()
            logFile.renameTo(oldLogFile)
        }
        logFile.delete()
        logFile.createNewFile()
        Runtime.getRuntime().exec(arrayOf("logcat", "-T", "100", "-f", logFile.absolutePath))
    } catch (e: Throwable) {
        Log.e(e)
        null
    }

    private fun disableTinker() {
        "com.tencent.tinker.loader.app.TinkerApplication".from(classLoader)
            ?.hookBeforeAllConstructors { it.args[0] = 0 }
    }

    companion object {
        lateinit var modulePath: String
        lateinit var moduleRes: Resources
        lateinit var classLoader: ClassLoader
    }
}
