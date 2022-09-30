package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.R
import me.kofua.qmhelper.utils.currentContext
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.runCatchingOrNull
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.stringArray

class HomeTopTabHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    private val purifyHomeTopTabIds by lazy {
        sPrefs.getStringSet("purify_home_top_tab", null)
            ?.sorted()?.map { it.toInt() } ?: listOf()
    }
    private val purifyHomeTobTabNames by lazy {
        stringArray(R.array.home_top_tab_values).map { it.toInt() }
            .zip(stringArray(R.array.home_top_tab_entries))
            .filter { purifyHomeTopTabIds.contains(it.first) }
            .map { it.second }
    }

    override fun startHook() {
        if (purifyHomeTopTabIds.isEmpty()) return

        instance.homePageFragmentClass?.hookBeforeMethod(
            instance.initTabFragment(),
            Int::class.javaPrimitiveType
        ) { param ->
            if (purifyHomeTopTabIds.contains(param.args[0] as Int))
                param.result = null
        }
        instance.mainDesktopHeaderClass?.hookBeforeMethod(
            instance.addTabByName(),
            String::class.java
        ) { param ->
            if (purifyHomeTobTabNames.contains(param.args[0] as String))
                param.result = null
        }
        instance.mainDesktopHeaderClass?.hookBeforeMethod(
            instance.addTabById(),
            Int::class.javaPrimitiveType,
            String::class.java
        ) { param ->
            val name = currentContext.runCatchingOrNull { getString(param.args[0] as Int) }
                ?: return@hookBeforeMethod
            if (purifyHomeTobTabNames.contains(name))
                param.result = null
        }
    }
}
