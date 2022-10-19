package me.kofua.qmhelper.hook

import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.replaceMethod

object WebLoginHook : BaseHook {
    override fun hook() {
        hookInfo.authAgent.clazz.from(classLoader)?.replaceMethod(
            hookInfo.authAgent.startActionActivity.name,
            *hookInfo.authAgent.startActionActivity.paramTypes
        ) { false }
    }
}
