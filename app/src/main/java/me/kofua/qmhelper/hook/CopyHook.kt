package me.kofua.qmhelper.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import me.kofua.qmhelper.R
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

typealias OnCopyAllListener = (text: CharSequence) -> Unit

object CopyHook : BaseHook {
    private var forceInterceptTouchField = ""

    private val expandableTextViewClass by Weak { "com.tencent.expandabletextview.ExpandableTextView" from classLoader }

    override fun hook() {
        if (!sPrefs.getBoolean("copy_enhance", false)) return

        @Suppress("UNCHECKED_CAST")
        hookInfo.dataPlugin.hookBeforeMethod({ handleJsRequest }) { param ->
            val dataPlugin = param.thisObject
            val method = param.args[2] as String
            val params = param.args[3] as Array<String>
            if (method == "setClipboard") {
                val text = params.firstOrNull()?.runCatchingOrNull { toJSONObject() }
                    ?.optString("text")?.replace("\\n", "\n")
                    ?: return@hookBeforeMethod
                val activity = dataPlugin.getObjectField(
                    hookInfo.dataPlugin.runtime
                )?.callMethodAs<Activity?>(
                    hookInfo.dataPlugin.activity
                ) ?: return@hookBeforeMethod
                showCopyDialog(activity, text, param)
                param.result = null
            }
        }
        expandableTextViewClass?.hookAfterConstructor(
            Context::class.java, AttributeSet::class.java, Int::class.javaPrimitiveType
        ) { param ->
            forceInterceptTouchField.ifEmpty {
                expandableTextViewClass?.declaredFields?.find {
                    it.type == Boolean::class.javaPrimitiveType && it.modifiers == 0
                            && param.thisObject.getBooleanField(it.name)
                }?.name?.also { forceInterceptTouchField = it }
            }.ifNotEmpty {
                param.thisObject.setBooleanField(it, false)
            }
        }
        hookInfo.albumIntroViewHolder.hookAfterMethod({ onHolderCreated }) { param ->
            val holder = param.thisObject
            holder.getObjectFieldAs<View?>(
                hookInfo.albumIntroViewHolder.tvAlbumDetail
            )?.longClick { v ->
                val text = holder.getObjectFieldAs(
                    hookInfo.albumIntroViewHolder.lastTextContent
                ) ?: ""
                showCopyDialog(v.context, text, null) {
                    it.copyToClipboard()
                    BannerTips.success(R.string.copy_success)
                }
                true
            }
        }
        hookInfo.albumTagViewHolder.hookAfterMethod({ onHolderCreated }) { param ->
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
