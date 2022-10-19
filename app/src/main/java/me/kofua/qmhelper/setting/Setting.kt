package me.kofua.qmhelper.setting

import android.content.SharedPreferences
import android.view.View
import androidx.annotation.StringRes
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*
import java.lang.reflect.Proxy

typealias IsSwitchOn = () -> Boolean
typealias OnSwitchChanged = (enabled: Boolean) -> Unit

class Setting {
    enum class Type(val key: Int) {
        SWITCH(0), BUTTON_WITHOUT_ARROW(1), BUTTON_WITH_ARROW(2), CATEGORY(3), DIVIDER(6)
    }

    companion object {
        const val TITLE_DIVIDER = "blank space"
        private val emptyClickListener = View.OnClickListener { }

        fun get(build: Setting.() -> Unit) = Setting().apply(build).build()

        fun switch(
            title: String,
            summary: String? = null,
            isSwitchOn: IsSwitchOn? = null,
            onSwitchChanged: OnSwitchChanged? = null
        ) = get {
            this.type = Type.SWITCH
            this.title = title
            this.summary = summary
            this.isSwitchOn = isSwitchOn
            this.onSwitchChanged = onSwitchChanged
        }

        fun switch(
            @StringRes title: Int,
            @StringRes summary: Int? = null,
            isSwitchOn: IsSwitchOn? = null,
            onSwitchChanged: OnSwitchChanged? = null
        ) = switch(string(title), summary?.let { string(it) }, isSwitchOn, onSwitchChanged)

        fun switch(
            key: String,
            title: String,
            summary: String? = null,
            defValue: Boolean = false,
            prefs: SharedPreferences = sPrefs,
        ) = switch(
            title,
            summary,
            { prefs.getBoolean(key, defValue) },
        ) { prefs.edit { putBoolean(key, it) } }

        fun switch(
            key: String,
            @StringRes title: Int,
            @StringRes summary: Int? = null,
            defValue: Boolean = false,
            prefs: SharedPreferences = sPrefs,
        ) = switch(key, string(title), summary?.let { string(it) }, defValue, prefs)

        fun button(
            title: String,
            summary: String? = null,
            rightDesc: String? = null,
            arrow: Boolean = true,
            clickListener: View.OnClickListener? = null
        ) = get {
            this.type = if (arrow) Type.BUTTON_WITH_ARROW else Type.BUTTON_WITHOUT_ARROW
            this.title = title
            this.summary = summary
            this.rightDesc = rightDesc
            this.clickListener = clickListener
        }

        fun button(
            @StringRes title: Int,
            @StringRes summary: Int? = null,
            @StringRes rightDesc: Int? = null,
            arrow: Boolean = true,
            clickListener: View.OnClickListener? = null
        ) = button(
            string(title),
            summary?.let { string(it) },
            rightDesc?.let { string(it) },
            arrow,
            clickListener
        )

        fun category(title: String) = get {
            this.type = Type.CATEGORY
            this.title = title
        }

        fun category(@StringRes title: Int) = category(string(title))

        fun divider() = get {
            this.type = Type.DIVIDER
            this.title = TITLE_DIVIDER
        }
    }

    var type: Type = Type.SWITCH
    var title: String? = null
    var rightDesc: String? = null
    var dotRightDesc: String? = null
    var summary: String? = null
    var isSwitchOn: IsSwitchOn? = null
    var onSwitchChanged: OnSwitchChanged? = null
    var tag: Any? = null
    var redDot: Boolean = false
    var enabled: Boolean = true
    var clickListener: View.OnClickListener? = null
    var touchListener: View.OnTouchListener? = null

    private fun build() = instance.settingClass
        ?.callStaticMethod(hookInfo.setting.with.name, currentContext)
        ?.apply {
            setIntField(hookInfo.setting.builder.type.name, type.key)
            setObjectField(hookInfo.setting.builder.title.name, title)
            setObjectField(hookInfo.setting.builder.rightDesc.name, rightDesc)
            setObjectField(hookInfo.setting.builder.summary.name, summary)
            setObjectField(hookInfo.setting.builder.switchListener.name, Proxy.newProxyInstance(
                currentContext.classLoader,
                arrayOf(instance.switchListenerClass)
            ) { _, m, args ->
                when (m.name) {
                    hookInfo.setting.switchListener.isSwitchOn.name -> isSwitchOn?.invoke() ?: false

                    hookInfo.setting.switchListener.onSwitchStatusChange.name -> {
                        onSwitchChanged?.invoke(args[0] as Boolean)
                    }

                    else -> null
                }
            })
            setObjectField(
                hookInfo.setting.builder.clickListener.name,
                clickListener ?: emptyClickListener
            )
        }?.callMethod(hookInfo.setting.builder.build.name)
}
