package me.kofua.qmhelper.hook

import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.replaceMethod

object WebLoginHook : BaseHook {
    override fun hook() {
        hookInfo.authAgent.replaceMethod({ startActionActivity }) { false }
    }
}
