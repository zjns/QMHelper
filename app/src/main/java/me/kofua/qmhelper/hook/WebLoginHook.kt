package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.hookBeforeMethod

class WebLoginHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    override fun startHook() {
        instance.authAgentClass?.declaredMethods?.find {
            it.name == instance.startActionActivity()
                    && it.returnType == Boolean::class.javaPrimitiveType
                    && it.parameterTypes.size == 5
        }?.hookBeforeMethod { it.result = false }
    }
}
