package me.kofua.qmhelper.hook

import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.Log
import me.kofua.qmhelper.utils.hookBeforeMethod

object ABTesterHook : BaseHook {
    private interface ITester {
        val testId: String
        val strategy: IStrategy

        interface IStrategy {
            val id: String
        }
    }

    private object SongWithoutRelatedVideoABTester : ITester {
        override val testId = "2784"
        override val strategy = Strategy.DISABLED

        enum class Strategy(override val id: String) : ITester.IStrategy {
            ENABLED("2784001"), DISABLED("2784002")
        }
    }

    private object SongWithRelatedVideoABTester : ITester {
        override val testId = "2782"
        override val strategy = Strategy.BASE

        enum class Strategy(override val id: String) : ITester.IStrategy {
            BASE("2782007"), A("2782001"), B("2782002"), C("2782003"),
            D("2782004"), F("2782005"), G("2782006")
        }
    }

    override fun hook() {
        hookInfo.abTester.clazz.from(classLoader)?.hookBeforeMethod(
            hookInfo.abTester.getProperty.name,
            String::class.java
        ) { param ->
            val property = param.args[0] as String
            Log.d("kofua, ab tester property: $property")
            //if (property == KEY_LISTEN_ENTRANCE) param.result = null
        }
        hookInfo.abTester.strategyModule.from(classLoader)?.hookBeforeMethod(
            hookInfo.abTester.getStrategyId.name,
            *hookInfo.abTester.getStrategyId.paramTypes
        ) { param ->
            val testId = param.args[0] as? String
            Log.d("kofua, get strategy, testId: $testId")
            if (testId == SongWithoutRelatedVideoABTester.testId)
                param.result = SongWithoutRelatedVideoABTester.strategy.id
            if (testId == SongWithRelatedVideoABTester.testId)
                param.result = SongWithRelatedVideoABTester.strategy.id
        }
    }
}
