package me.kofua.qmhelper.hook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.R
import me.kofua.qmhelper.XposedInit.Companion.modulePath
import me.kofua.qmhelper.setting.Setting
import me.kofua.qmhelper.setting.SettingPack
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.UiMode
import me.kofua.qmhelper.utils.callMethod
import me.kofua.qmhelper.utils.callSuper
import me.kofua.qmhelper.utils.currentContext
import me.kofua.qmhelper.utils.edit
import me.kofua.qmhelper.utils.getObjectField
import me.kofua.qmhelper.utils.getObjectFieldAs
import me.kofua.qmhelper.utils.handler
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.hookBeforeConstructor
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.invocationHandler
import me.kofua.qmhelper.utils.new
import me.kofua.qmhelper.utils.proxy
import me.kofua.qmhelper.utils.replaceMethod
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.setObjectField
import me.kofua.qmhelper.utils.showMessageDialog
import me.kofua.qmhelper.utils.string
import me.kofua.qmhelper.utils.uiMode
import java.lang.reflect.InvocationHandler
import java.util.concurrent.CopyOnWriteArrayList

class SettingsHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    private val purifyRedDots by lazy { sPrefs.getBoolean("purify_red_dots", false) }
    private val purifyMoreItems by lazy {
        sPrefs.getStringSet("purify_more_items", null) ?: setOf()
    }
    private val settingPack = SettingPack()

    override fun startHook() {
        instance.appStarterActivityClass?.hookAfterMethod(
            instance.doOnCreate(), Bundle::class.java
        ) { param ->
            val activity = param.thisObject as Activity
            settingPack.activity = activity
            activity.assets.callMethod("addAssetPath", modulePath)
            settingPack.checkUpdate(dialog = false)
            if (uiMode == UiMode.NORMAL && !sPrefs.getBoolean("ui_mode_hint", false)) {
                handler.postDelayed({
                    activity.showMessageDialog(
                        string(R.string.tips_title),
                        string(R.string.tips_open_clean_mode),
                        string(R.string.go_to_mode_setting),
                        string(R.string.i_know),
                        { sPrefs.edit { putBoolean("ui_mode_hint", true) } },
                    ) {
                        sPrefs.edit { putBoolean("ui_mode_hint", true) }
                        instance.modeFragmentClass?.let {
                            activity.callMethod(instance.addSecondFragment(), it, null)
                        } ?: BannerTips.error(R.string.jump_failed)
                    }
                }, 2000L)
            }
        }
        instance.appStarterActivityClass?.hookAfterMethod(
            "onActivityResult",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Intent::class.java
        ) { param ->
            val requestCode = param.args[0] as Int
            val resultCode = param.args[1] as Int
            val data = param.args[2] as? Intent
            settingPack.onActivityResult(requestCode, resultCode, data)
        }
        @Suppress("UNCHECKED_CAST")
        instance.appStarterActivityClass?.hookAfterMethod(
            "onRequestPermissionsResult",
            Int::class.javaPrimitiveType,
            Array<String>::class.java,
            IntArray::class.java,
        ) { param ->
            val requestCode = param.args[0] as Int
            val permissions = param.args[1] as Array<String>
            val grantResults = param.args[2] as IntArray
            settingPack.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        instance.uiModeManagerClass?.replaceMethod(
            instance.isThemeForbid(),
            String::class.java
        ) { false }
        instance.moreFragmentClass?.hookAfterMethod(instance.resume()) { param ->
            val settings = param.thisObject
                .getObjectFieldAs<MutableList<Any?>>(instance.moreListField)
            settings.forEach { s ->
                if (purifyRedDots) {
                    s?.getObjectField(instance.rightDescField)?.takeIf {
                        it != "未开启"
                    }?.run { s.setObjectField(instance.rightDescField, null) }
                    s.setObjectField(instance.redDotListenerField, null)
                }
            }
            for (i in settings.size - 1 downTo 0) {
                settings[i]?.getObjectFieldAs<String>(instance.titleField)?.takeIf {
                    purifyMoreItems.contains(it)
                }?.run { settings.removeAt(i) }
            }
            if (settings.last()?.getObjectField(instance.titleField) == Setting.TITLE_DIVIDER) {
                settings.removeAt(settings.lastIndex)
            }
            settings.add(1, Setting.button(R.string.app_name) {
                onQMHelperSettingClicked(it.context)
            })
        }
        if (!purifyRedDots) return
        instance.personalEntryViewClass?.declaredMethods?.find { it.name == instance.update() }
            ?.hookAfterMethod { param ->
                param.thisObject.getObjectFieldAs<TextView>(instance.rightDescViewField)
                    .run { text = "" }
                param.thisObject.getObjectFieldAs<View>(instance.redDotViewField)
                    .run { visibility = View.GONE }
            }
        instance.settingFragmentClass?.hookBeforeMethod(instance.resume()) { param ->
            param.thisObject.getObjectFieldAs<List<*>>(instance.settingListField).forEach {
                it?.setObjectField(instance.redDotListenerField, null)
            }
        }
    }

    private fun settingProvider(fragment: Any, setting: Any?): Any? {
        val baseSettingProviderClass = instance.baseSettingProviderClass ?: return null
        val create = instance.create() ?: return null
        val handler = InvocationHandler { _, _, _ -> setting }
        val settingProviderClass = baseSettingProviderClass.proxy(create)
        val unhook = settingProviderClass.constructors.first().hookBeforeMethod {
            it.thisObject.invocationHandler(handler)
        }
        return settingProviderClass.new(currentContext, fragment).also { unhook?.unhook() }
    }

    private fun onQMHelperSettingClicked(context: Context) {
        val baseSettingFragmentClass = instance.baseSettingFragmentClass ?: return
        val baseSettingPackClass = instance.baseSettingPackClass ?: return
        val settingPackage = instance.settingPackage() ?: return
        val title = instance.title() ?: return
        val createSettingProvider = instance.createSettingProvider() ?: return
        val handler = InvocationHandler { fp, fm, fArgs ->
            if (fm.name == settingPackage) {
                val packSettingHandler = InvocationHandler { _, _, _ ->
                    val settingProviders = settingPack.getSettings()
                        .map { settingProvider(fp, it) }
                    CopyOnWriteArrayList<Any?>(settingProviders)
                }
                val moduleSettingPackClass = baseSettingPackClass.proxy(createSettingProvider)
                val unhook = moduleSettingPackClass.constructors.first().hookBeforeMethod {
                    it.thisObject.invocationHandler(packSettingHandler)
                }
                moduleSettingPackClass.new(currentContext, fp, Bundle()).also { unhook?.unhook() }
            } else if (fm.name == title) {
                string(R.string.app_name)
            } else if (fArgs.isNullOrEmpty()) {
                fp.callSuper(fm)
            } else {
                fp.callSuper(fm, fArgs)
            }
        }
        val moduleSettingFragmentClass = baseSettingFragmentClass.proxy(settingPackage, title)
        val unhook = moduleSettingFragmentClass.hookBeforeConstructor {
            it.thisObject.invocationHandler(handler)
        }
        context.callMethod(instance.addSecondFragment(), moduleSettingFragmentClass, null)
        unhook?.unhook()
    }
}
