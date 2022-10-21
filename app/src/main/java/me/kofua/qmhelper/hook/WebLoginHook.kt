package me.kofua.qmhelper.hook

import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.isFakeSigEnabledFor
import me.kofua.qmhelper.utils.isPackageInstalled

object WebLoginHook : BaseHook {
    private const val PACKAGE_QQ = "com.tencent.mobileqq"
    private const val PACKAGE_TIM = "com.tencent.tim"

    override fun hook() {
        hookInfo.authAgent.hookBeforeMethod({ startActionActivity }) { param ->
            if (isPackageInstalled(PACKAGE_QQ)) {
                if (!isFakeSigEnabledFor(PACKAGE_QQ))
                    param.result = false
            } else if (isPackageInstalled(PACKAGE_TIM)) {
                if (!isFakeSigEnabledFor(PACKAGE_TIM))
                    param.result = false
            }
        }
    }
}
