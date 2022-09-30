package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.Log
import me.kofua.qmhelper.utils.Weak
import me.kofua.qmhelper.utils.from
import me.kofua.qmhelper.utils.hookBeforeMethod

class ABTesterHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    private val abTestTypeClass by Weak { "com.tencent.qqmusic.abtest.update.ABTestUpdateType" from classLoader }

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

    override fun startHook() {
        instance.baseAbTesterClass?.hookBeforeMethod(
            instance.getProperty(),
            String::class.java
        ) { param ->
            val property = param.args[0] as String
            Log.d("kofua, ab tester property: $property")
            //if (property == KEY_LISTEN_ENTRANCE) param.result = null
        }
        instance.strategyModuleClass?.hookBeforeMethod(
            instance.getStrategyId(),
            String::class.java,
            abTestTypeClass
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
