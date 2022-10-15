package me.kofua.qmhelper.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.R
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.MethodHookParam
import me.kofua.qmhelper.utils.callMethodAs
import me.kofua.qmhelper.utils.copyToClipboard
import me.kofua.qmhelper.utils.getBooleanField
import me.kofua.qmhelper.utils.getObjectField
import me.kofua.qmhelper.utils.getObjectFieldAs
import me.kofua.qmhelper.utils.hookAfterConstructor
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.ifNotEmpty
import me.kofua.qmhelper.utils.invokeOriginalMethod
import me.kofua.qmhelper.utils.longClick
import me.kofua.qmhelper.utils.runCatchingOrNull
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.setBooleanField
import me.kofua.qmhelper.utils.string
import me.kofua.qmhelper.utils.themeIdForDialog
import me.kofua.qmhelper.utils.toJSONObject

typealias OnCopyAllListener = (text: CharSequence) -> Unit

class CopyHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    private var forceInterceptTouchField = ""

    override fun startHook() {
        if (!sPrefs.getBoolean("copy_enhance", false)) return

        @Suppress("UNCHECKED_CAST")
        instance.dataPluginClass?.hookBeforeMethod(
            instance.handleJsRequest(),
            String::class.java,
            String::class.java,
            String::class.java,
            Array<String>::class.java
        ) { param ->
            val dataPlugin = param.thisObject
            val method = param.args[2] as String
            val params = param.args[3] as Array<String>
            if (method == "setClipboard") {
                val text = params.getOrNull(0)?.runCatchingOrNull { toJSONObject() }
                    ?.optString("text")?.replace("\\n", "\n")
                    ?: return@hookBeforeMethod
                val activity = dataPlugin.getObjectField(instance.runtimeField)
                    ?.callMethodAs<Activity?>(instance.activity()) ?: return@hookBeforeMethod
                showCopyDialog(activity, text, param)
                param.result = null
            }
        }
        instance.expandableTextViewClass?.hookAfterConstructor(
            Context::class.java, AttributeSet::class.java, Int::class.javaPrimitiveType
        ) { param ->
            forceInterceptTouchField.ifEmpty {
                instance.expandableTextViewClass?.declaredFields?.find {
                    it.type == Boolean::class.javaPrimitiveType && it.modifiers == 0
                            && param.thisObject.getBooleanField(it.name)
                }?.name?.also { forceInterceptTouchField = it }
            }.ifNotEmpty {
                param.thisObject.setBooleanField(it, false)
            }
        }
        instance.albumIntroViewHolderClass?.hookAfterMethod(
            instance.onHolderCreated(),
            View::class.java
        ) { param ->
            val holder = param.thisObject
            holder.getObjectFieldAs<View?>(instance.tvAlbumDetailField)?.longClick { v ->
                val text = holder.getObjectFieldAs(instance.lastTextContentField) ?: ""
                showCopyDialog(v.context, text, null) {
                    it.copyToClipboard()
                    BannerTips.success(R.string.copy_success)
                }
                true
            }
        }
        instance.albumTagViewHolderClass?.hookAfterMethod(
            instance.onHolderCreated(),
            View::class.java
        ) { param ->
            val holder = param.thisObject
            holder.javaClass.declaredFields.filter {
                TextView::class.java.isAssignableFrom(it.type)
            }.forEach { f ->
                holder.getObjectFieldAs<TextView?>(f.name)?.longClick { v ->
                    showCopyDialog(v.context, v.text, null) {
                        it.copyToClipboard()
                        BannerTips.success(R.string.copy_success)
                    }
                    true
                }
            }
        }
    }

    private fun showCopyDialog(
        context: Context,
        text: CharSequence,
        param: MethodHookParam?,
        onCopyAll: OnCopyAllListener? = null
    ) {
        AlertDialog.Builder(context, themeIdForDialog).run {
            setTitle(string(R.string.copy_enhance))
            setMessage(text)
            setPositiveButton(string(R.string.share)) { _, _ ->
                context.startActivity(
                    Intent.createChooser(
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }, string(R.string.share_copy_content)
                    )
                )
            }
            setNeutralButton(string(R.string.copy_all)) { _, _ ->
                param?.invokeOriginalMethod()
                onCopyAll?.invoke(text)
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }.apply {
            findViewById<TextView>(android.R.id.message).setTextIsSelectable(true)
        }
    }
}
