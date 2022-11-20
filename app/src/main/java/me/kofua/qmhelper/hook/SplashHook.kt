package me.kofua.qmhelper.hook

import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object SplashHook : BaseHook {

    private val splashShowTypeClass by Weak { "com.tencentmusic.ad.core.constant.SplashShowType" from classLoader }
    private val noAdSplashType by lazy { splashShowTypeClass?.getStaticObjectField("NO_AD") }

    override fun hook() {
        if (!sPrefs.getBoolean("purify_splash", false)) return

        hookInfo.splashAdapter.from(classLoader)?.declaredMethods
            ?.filter { it.returnType == splashShowTypeClass }
            ?.forEach { m ->
                m.hookBefore { param ->
                    noAdSplashType?.let { param.result = it }
                }
            }
        hookInfo.adManager.replaceMethod({ get }) { null }
    }
}
