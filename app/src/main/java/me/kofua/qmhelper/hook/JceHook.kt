package me.kofua.qmhelper.hook

import me.kofua.qmhelper.BuildConfig
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object JceHook : BaseHook {
    override fun hook() {
        val hidden = sPrefs.getBoolean("hidden", false)
        val unlockTheme = sPrefs.getBoolean("unlock_theme", false)
        val unlockFont = sPrefs.getBoolean("unlock_font", false)

        hookInfo.jceRespConverter.hookAfterMethod({ parse }) { param ->
            val type = (param.args[1] as Class<*>).name
            if (BuildConfig.DEBUG)
                Log.d("net.jce, type: $type, json: ${param.result?.toJson()}")
            if (type == "com.tencent.jce.playerStyle.PlayerStyleRsp" && hidden && unlockTheme) {
                param.result?.run {
                    getObjectField("alert")?.setIntField("revertTheme", 0)
                    getObjectField("styleConf")?.setIntField("status", 0)
                }
            } else if ((type == "com.tencent.jce.personal.ApplyIconRsp"
                        || type == "com.tencent.jce.personal.GetIconRsp") && hidden && unlockTheme
            ) {
                param.result?.run {
                    getObjectField("auth")?.run {
                        setIntField("enable", 1)
                        setIntField("authType", 0)
                    }
                }
            } else if (type == "com.tencent.qqmusic.business.playernew.view.playerlyric.model.QueryLyricFontRsp" && hidden && unlockFont) {
                param.result?.run {
                    getObjectFieldAs<Array<*>?>("fontList")?.forEach {
                        it?.setIntField("auth", 0)
                    }
                }
            }
        }
    }
}
