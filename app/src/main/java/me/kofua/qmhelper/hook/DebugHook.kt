package me.kofua.qmhelper.hook

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.kofua.qmhelper.qmPackage
import me.kofua.qmhelper.utils.*
import org.json.JSONObject

object DebugHook : BaseHook {

    override fun hook() {
        /*"com.tencent.qqmusiccommon.appconfig.ChannelConfig".hookBeforeMethod(
            classLoader, "a"
        ) { param ->
            Thread.currentThread().stackTrace
                .find { it.methodName == "createSettingProvider" }
                ?.run {
                    param.result = "80000"
                }
        }
        "com.tencent.qqmusiccommon.appconfig.w".hookBeforeMethod(
            classLoader, "j", String::class.java
        ) { it.result = true }*/

        View::class.java.hookBeforeMethod(
            "setOnLongClickListener",
            View.OnLongClickListener::class.java
        ) { param ->
            val listener = param.args[0] as? View.OnLongClickListener ?: return@hookBeforeMethod
            param.args[0] = View.OnLongClickListener {
                Log.d("kofua, onLongClicked, view: $it, listener: ${listener.javaClass.name}")
                listener.onLongClick(it)
            }
        }
        View::class.java.hookBeforeMethod(
            "setOnClickListener",
            View.OnClickListener::class.java
        ) { param ->
            val listener = param.args[0] as? View.OnClickListener ?: return@hookBeforeMethod
            param.args[0] = View.OnClickListener {
                Log.d("kofua, onClicked, view: $it, listener: ${listener.javaClass.name}")
                listener.onClick(it)
            }
        }
        Activity::class.java.hookBeforeMethod("onCreate", Bundle::class.java) { param ->
            Log.d("kofua, creating activity: ${param.thisObject}")
        }
        qmPackage.baseFragmentClass?.hookAfterMethod(
            "onCreateView",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Bundle::class.java
        ) { param ->
            Log.d("kofua, creating fragment: ${param.thisObject}, view: ${param.result}")
        }
        "com.tencent.qqmusiccommon.util.MLog".from(classLoader)?.declaredMethods?.filter { m ->
            m.isPublic && m.isStatic && m.returnType == Void.TYPE && m.parameterTypes.let {
                it.size >= 2 && it[0] == String::class.java && it[1] == String::class.java
            }
        }?.forEach { m ->
            m.hookBefore { param ->
                val methodName = param.method.name
                val tag = param.args[0] as? String ?: ""
                val message = param.args[1] as? String ?: ""
                val other = param.args.getOrNull(2)
                when (methodName) {
                    "d" -> when (other) {
                        null -> android.util.Log.d(tag, message)
                        is Throwable -> android.util.Log.d(tag, message, other)
                        is Array<*> -> android.util.Log.d(tag, message.format(*other))
                    }

                    "e" -> when (other) {
                        null -> android.util.Log.e(tag, message)
                        is Throwable -> android.util.Log.e(tag, message, other)
                        is Array<*> -> android.util.Log.e(tag, message.format(*other))
                    }

                    "i" -> when (other) {
                        null -> android.util.Log.i(tag, message)
                        is Throwable -> android.util.Log.i(tag, message, other)
                        is Array<*> -> android.util.Log.i(tag, message.format(*other))
                    }

                    "v" -> when (other) {
                        null -> android.util.Log.v(tag, message)
                        is Throwable -> android.util.Log.v(tag, message, other)
                        is Array<*> -> android.util.Log.v(tag, message.format(*other))
                    }

                    "w" -> when (other) {
                        null -> android.util.Log.w(tag, message)
                        is Throwable -> android.util.Log.w(tag, message, other)
                        is Array<*> -> android.util.Log.w(tag, message.format(*other))
                    }
                }
                param.result = null
            }
        }
        "com.tencent.qqmusiccommon.hippy.bridge.WebApiHippyBridge".from(classLoader)
            ?.declaredMethods?.find { it.name == "invoke" }?.hookAfter { param ->
                Log.d("kofua, invoke, hippyMapJson: ${JSONObject(param.args[0].getObjectField("mDatas") as Map<*, *>)}")
            }
    }
}
