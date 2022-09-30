package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.Weak
import me.kofua.qmhelper.utils.from
import me.kofua.qmhelper.utils.getStaticObjectField
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.replaceMethod
import me.kofua.qmhelper.utils.sPrefs

class SplashHook(classLoader: ClassLoader) : BaseHook(classLoader) {

    private val splashShowTypeClass by Weak { "com.tencentmusic.ad.core.constant.SplashShowType" from classLoader }
    private val noAdSplashType by lazy { splashShowTypeClass?.getStaticObjectField("NO_AD") }

    override fun startHook() {
        if (!sPrefs.getBoolean("purify_splash", false)) return

        instance.splashAdapterClass?.declaredMethods
            ?.filter { it.returnType == splashShowTypeClass }
            ?.forEach { m ->
                m.hookBeforeMethod { param ->
                    noAdSplashType?.let { param.result = it }
                }
            }
        instance.adManagerClass?.replaceMethod(instance.get()) { null }
    }
}
