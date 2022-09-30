package me.kofua.qmhelper.hook

abstract class BaseHook(val classLoader: ClassLoader) {
    abstract fun startHook()
    open fun lateInitHook() {}
}
