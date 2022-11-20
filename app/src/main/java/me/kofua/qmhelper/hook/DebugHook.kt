package me.kofua.qmhelper.hook

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.kofua.qmhelper.qmPackage
import me.kofua.qmhelper.utils.*
import org.json.JSONObject
import java.lang.reflect.Proxy

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
            param.args[0] = Proxy.newProxyInstance(
                listener.javaClass.classLoader,
                arrayOf(View.OnLongClickListener::class.java)
            ) { _, m, args ->
                val v = args[0] as View
                Log.d("kofua, onLongClicked, v: $v, listener: ${listener.javaClass.name}")
                m(listener, *args)
            }
        }
        val btnId = getResId("info_layout", "id")
        View::class.java.hookBeforeMethod(
            "setOnClickListener",
            View.OnClickListener::class.java
        ) { param ->
            val listener = param.args[0] as? View.OnClickListener ?: return@hookBeforeMethod
            param.args[0] = Proxy.newProxyInstance(
                listener.javaClass.classLoader,
                arrayOf(View.OnClickListener::class.java)
            ) { _, m, args ->
                args ?: return@newProxyInstance m(listener)
                val v = args[0] as View
                val name = v.javaClass.name
                if (name == "com.tencent.qqmusic.fragment.morefeatures.settings.view.SettingView") {
                    val parent = v.parent as? ViewGroup
                    Log.d("kofua, parent: $parent, pos: ${parent?.indexOfChild(v)}")
                }
                Log.d("kofua, onClicked, v: $v, listener: ${listener.javaClass.name}")
                if (v.id == btnId) {
                    val tag = v.tag
                    val parent = v.parent
                    Log.d("kofua, onButtonClicked, tag: $tag, tag class: ${tag?.javaClass?.name}, parent: $parent")
                }
                m(listener, *args)
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
        "com.tencent.qqmusiccommon.util.MLog".from(classLoader)?.declaredMethods
            ?.filter { m ->
                m.isPublic && m.isStatic && m.returnType == Void::class.javaPrimitiveType
                        && m.parameterTypes.let { it.size >= 2 && it[0] == String::class.java && it[1] == String::class.java }
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
