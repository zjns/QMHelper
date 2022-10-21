package me.kofua.qmhelper.hook

import android.content.pm.PackageManager
import android.util.Base64
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.Log
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.systemContext
import org.json.JSONObject

object WebLoginHook : BaseHook {
    override fun hook() {
        hookInfo.authAgent.hookBeforeMethod({ startActionActivity }) { param ->
            if (!isFakeSigEnabledForQQ())
                param.result = false
        }
    }

    private fun isFakeSigEnabledForQQ(): Boolean {
        try {
            val metaData = systemContext.packageManager
                .getApplicationInfo("com.tencent.mobileqq", PackageManager.GET_META_DATA)
                .metaData
            val encoded = metaData.getString("lspatch")
            if (encoded != null) {
                val json = Base64.decode(encoded, Base64.DEFAULT).toString(Charsets.UTF_8)
                val patchConfig = JSONObject(json)
                val sigBypassLevel = patchConfig.optInt("sigBypassLevel", -1)
                val lspVerCode = patchConfig.optJSONObject("lspConfig")
                    ?.optInt("VERSION_CODE", -1) ?: -1
                if (sigBypassLevel > 0 && lspVerCode >= 339)
                    return true
            }
        } catch (t: Throwable) {
            Log.e(t)
        }
        return false
    }
}
