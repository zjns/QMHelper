package me.kofua.qmhelper.hook

import me.kofua.qmhelper.BuildConfig
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object ABTesterHook : BaseHook {
    override fun hook() {
        if (sPrefs.getBoolean("hide_song_list_guide", false)) {
            hookInfo.abTester.hookBeforeMethod({ getProperty }) { param ->
                val property = param.args[0] as String
                if (property == "page") {
                    param.result = instance.jsonPrimitiveClass
                        ?.new("Newpage2") ?: return@hookBeforeMethod
                }
            }
        }
        if (BuildConfig.DEBUG) {
            hookInfo.abTester.hookAfterMethod({ getProperty }) { param ->
                val property = param.args[0] as String
                Log.d("kofua, abTester, get property, property: $property, result: ${param.result}")
            }
            hookInfo.strategyModule.hookAfterMethod({ getStrategyId }) { param ->
                val testId = param.args[0] as String
                Log.d("kofua, abTester, get strategy, testId: $testId, strategy: ${param.result}")
            }
        }
    }
}
