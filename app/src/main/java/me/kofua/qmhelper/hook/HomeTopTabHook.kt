package me.kofua.qmhelper.hook

import me.kofua.qmhelper.R
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object HomeTopTabHook : BaseHook {
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

    override fun hook() {
        if (purifyHomeTopTabIds.isEmpty()) return

        hookInfo.homePageFragment.clazz.from(classLoader)?.hookBeforeMethod(
            hookInfo.homePageFragment.initTabFragment.name,
            Int::class.javaPrimitiveType
        ) { param ->
            if (purifyHomeTopTabIds.contains(param.args[0] as Int))
                param.result = null
        }
        hookInfo.mainDesktopHeader.clazz.from(classLoader)?.hookBeforeMethod(
            hookInfo.mainDesktopHeader.addTabByName.name,
            String::class.java
        ) { param ->
            if (purifyHomeTobTabNames.contains(param.args[0] as String))
                param.result = null
        }
        hookInfo.mainDesktopHeader.clazz.from(classLoader)?.hookBeforeMethod(
            hookInfo.mainDesktopHeader.addTabById.name,
            Int::class.javaPrimitiveType,
            String::class.java
        ) { param ->
            val name = runCatchingOrNull { string(param.args[0] as Int) }
                ?: return@hookBeforeMethod
            if (purifyHomeTobTabNames.contains(name))
                param.result = null
        }
    }
}
