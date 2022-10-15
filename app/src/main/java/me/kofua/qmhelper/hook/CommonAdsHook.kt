package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.getObjectField
import me.kofua.qmhelper.utils.getObjectFieldAs
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.sPrefs

class CommonAdsHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    override fun startHook() {
        if (!sPrefs.getBoolean("block_common_ads", false)) return

        instance.adResponseData().forEach { (c, ml) ->
            ml.forEach { m ->
                c?.hookAfterMethod(m) { param ->
                    val ads = param.result as? MutableList<*>
                        ?: return@hookAfterMethod
                    for (i in ads.size - 1 downTo 0) {
                        ads[i]?.getObjectField("creative")
                            ?.getObjectField("option")
                            ?.getObjectFieldAs<Boolean>("isShowAdMark")
                            ?.takeIf { it }?.run {
                                ads.removeAt(i)
                            }
                    }
                }
            }
        }
    }
}
