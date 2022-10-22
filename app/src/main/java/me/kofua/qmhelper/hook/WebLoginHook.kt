package me.kofua.qmhelper.hook

import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.isFakeSigEnabledFor
import me.kofua.qmhelper.utils.isPackageInstalled

object WebLoginHook : BaseHook {
    private val loginClients = arrayOf("com.tencent.mobileqq", "com.tencent.tim")

    override fun hook() {
        hookInfo.authAgent.hookBeforeMethod({ startActionActivity }) { param ->
            loginClients.find { isPackageInstalled(it) }?.let {
                if (!isFakeSigEnabledFor(it))
                    param.result = false
            }
        }
    }
}
