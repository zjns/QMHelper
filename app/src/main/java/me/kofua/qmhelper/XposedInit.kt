package me.kofua.qmhelper

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.res.Resources
import android.content.res.XModuleResources
import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import me.kofua.qmhelper.hook.ABTesterHook
import me.kofua.qmhelper.hook.BaseHook
import me.kofua.qmhelper.hook.CgiHook
import me.kofua.qmhelper.hook.CopyHook
import me.kofua.qmhelper.hook.DebugHook
import me.kofua.qmhelper.hook.HomePageHook
import me.kofua.qmhelper.hook.HomeTopTabHook
import me.kofua.qmhelper.hook.MiscHook
import me.kofua.qmhelper.hook.SSLHook
import me.kofua.qmhelper.hook.SettingsHook
import me.kofua.qmhelper.hook.SplashHook
import me.kofua.qmhelper.hook.WebLoginHook
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.Log
import me.kofua.qmhelper.utils.callMethod
import me.kofua.qmhelper.utils.currentContext
import me.kofua.qmhelper.utils.from
import me.kofua.qmhelper.utils.getPackageVersion
import me.kofua.qmhelper.utils.hookBeforeAllConstructors
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.is64
import me.kofua.qmhelper.utils.isBuiltIn
import me.kofua.qmhelper.utils.logFile
import me.kofua.qmhelper.utils.oldLogFile
import me.kofua.qmhelper.utils.preloadProxyClasses
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.shouldSaveLog
import me.kofua.qmhelper.utils.string

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        moduleRes = XModuleResources.createInstance(modulePath, null)
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != Constant.QM_PACKAGE_NAME) return
        // TODO: allow tinker, use tinker classloader and update hook info after tinker patched
        disableTinker(lpparam)

        Instrumentation::class.java.hookBeforeMethod(
            "callApplicationOnCreate",
            Application::class.java
        ) { param ->
            when {
                !lpparam.processName.contains(":") -> {
                    if (shouldSaveLog) startLog()
                    currentContext.assets.callMethod("addAssetPath", modulePath)

                    Log.d("QQMusic process launched ...")
                    Log.d("QMHelper version: ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}) from $modulePath${if (isBuiltIn) " (BuiltIn)" else ""}")
                    Log.d("QQMusic version: ${getPackageVersion(lpparam.packageName)} (${if (is64) "64" else "32"}bit)")
                    Log.d("SDK: ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT}); Phone: ${Build.BRAND} ${Build.MODEL}")
                    Log.d("Config: ${sPrefs.all}")

                    QMPackage(lpparam.classLoader, param.args[0] as Context)
                    preloadProxyClasses()
                    if (BuildConfig.DEBUG) {
                        startHook(SSLHook(lpparam.classLoader))
                        startHook(DebugHook(lpparam.classLoader))
                        startHook(ABTesterHook(lpparam.classLoader))
                    }
                    if (isBuiltIn) {
                        startHook(WebLoginHook(lpparam.classLoader))
                    }
                    startHook(SettingsHook(lpparam.classLoader))
                    startHook(SplashHook(lpparam.classLoader))
                    startHook(HomeTopTabHook(lpparam.classLoader))
                    startHook(HomePageHook(lpparam.classLoader))
                    startHook(CgiHook(lpparam.classLoader))
                    startHook(CopyHook(lpparam.classLoader))
                    startHook(MiscHook(lpparam.classLoader))
                }
            }
        }
        lateInitHook = Activity::class.java.hookBeforeMethod("onResume") {
            startLateHook()
            lateInitHook?.unhook()
        }
    }

    private fun startHook(hooker: BaseHook) {
        try {
            hookers.add(hooker)
            hooker.startHook()
        } catch (t: Throwable) {
            Log.e(t)
            val errorMessage = t.message ?: ""
            val stackTrace = t.stackTrace.joinToString("\n")
            BannerTips.error(string(R.string.hook_error, errorMessage, stackTrace))
        }
    }

    private fun startLateHook() {
        hookers.forEach {
            try {
                it.lateInitHook()
            } catch (t: Throwable) {
                Log.e(t)
                val errorMessage = t.message ?: ""
                val stackTrace = t.stackTrace.joinToString("\n")
                BannerTips.error(string(R.string.hook_error, errorMessage, stackTrace))
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

    private fun disableTinker(lpparam: LoadPackageParam) {
        "com.tencent.tinker.loader.app.TinkerApplication".from(lpparam.classLoader)
            ?.hookBeforeAllConstructors { it.args[0] = 0 }
    }

    companion object {
        lateinit var modulePath: String
        lateinit var moduleRes: Resources

        private val hookers = ArrayList<BaseHook>()
        private var lateInitHook: XC_MethodHook.Unhook? = null
    }
}
