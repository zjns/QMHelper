package me.kofua.qmhelper.hook

import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.getObjectField
import me.kofua.qmhelper.utils.getObjectFieldAs
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.sPrefs

object CommonAdsHook : BaseHook {
    override fun hook() {
        if (!sPrefs.getBoolean("block_common_ads", false)) return

        hookInfo.adResponseData.item.forEach { item ->
            item.getAds.forEach { m ->
                item.clazz.from(classLoader)?.hookAfterMethod(m.name) { param ->
                    val ads = param.result as? MutableList<*>
                        ?: return@hookAfterMethod
                    for (i in ads.size - 1 downTo 0) {
                        val showAdMark = ads[i]?.getObjectField("creative")
                            ?.getObjectField("option")
                            ?.getObjectFieldAs<Boolean>("isShowAdMark") ?: false
                        val adTag = ads[i]?.getObjectField("madAdInfo")
                            ?.getObjectFieldAs<String?>("adTag") == "广告"
                        if (showAdMark || adTag)
                            ads.removeAt(i)
                    }
                }
            }
        }
    }
}
