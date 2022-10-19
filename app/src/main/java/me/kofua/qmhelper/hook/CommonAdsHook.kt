package me.kofua.qmhelper.hook

import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object CommonAdsHook : BaseHook {
    override fun hook() {
        if (!sPrefs.getBoolean("block_common_ads", false)) return

        hookInfo.adResponseData.item.forEach { item ->
            item.getAds.forEach { m ->
                item.clazz.from(classLoader)?.hookAfterMethod(m.name) { param ->
                    val ads = param.result as? MutableList<*>
                        ?: return@hookAfterMethod
                    for (i in ads.size - 1 downTo 0) {
                        ads[i]?.getObjectField("creative")
                            ?.getObjectField("option")
                            ?.getObjectFieldAs<Boolean>("isShowAdMark")
                            ?.yes { ads.removeAt(i) }
                    }
                }
            }
        }
    }
}
