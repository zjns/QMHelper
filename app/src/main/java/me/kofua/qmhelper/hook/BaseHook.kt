package me.kofua.qmhelper.hook

import me.kofua.qmhelper.XposedInit

interface BaseHook {
    val classLoader: ClassLoader
        get() = XposedInit.classLoader

    fun hook()
}
