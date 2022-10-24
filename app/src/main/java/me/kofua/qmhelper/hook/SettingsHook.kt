package me.kofua.qmhelper.hook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.R
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.setting.Setting
import me.kofua.qmhelper.setting.SettingPack
import me.kofua.qmhelper.utils.*
import java.lang.reflect.InvocationHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

object SettingsHook : BaseHook {
    private val purifyRedDots by lazy { sPrefs.getBoolean("purify_red_dots", false) }
    private val purifyMoreItems by lazy {
        sPrefs.getStringSet("purify_more_items", null) ?: setOf()
    }
    private val settingPack = SettingPack()
    private val settingViewTextFields by lazy {
        hookInfo.settingView.clazz.from(classLoader)
            ?.declaredFields?.filter { it.type == TextView::class.java }
            ?.map { it.name } ?: listOf()
    }
    private val todayFormat: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            .format(Date())

    override fun hook() {
        hookInfo.appStarterActivity.hookAfterMethod({ doOnCreate }) { param ->
            val activity = param.thisObject as Activity
            activity.addModuleAssets()
            settingPack.activity = activity
            settingPack.checkUpdate(dialog = false)
            if (sPrefs.getBoolean("daily_sign_in", false))
                handler.postDelayed({ dailySignIn() }, 8000L)
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
                        hookInfo.modeFragment.from(classLoader)?.let {
                            activity.callMethod(
                                hookInfo.appStarterActivity.addSecondFragment,
                                it, null
                            )
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
        hookInfo.uiModeManager.replaceMethod({ isThemeForbid }) { false }
        hookInfo.settingView.hookAfterMethod({ setSetting }) { param ->
            val viewGroup = param.thisObject as ViewGroup
            val setting = param.args[0] ?: return@hookAfterMethod
            settingViewTextFields.forEach {
                viewGroup.getObjectFieldAs<TextView?>(it)?.isSingleLine = false
            }
            val typeField = hookInfo.setting.type
            if (setting.getIntField(typeField) != Setting.Type.DIVIDER.key) {
                (viewGroup.layoutParams as? RelativeLayout.LayoutParams)?.apply {
                    height = RelativeLayout.LayoutParams.WRAP_CONTENT
                }?.let { viewGroup.layoutParams = it }
                if (viewGroup.minimumHeight == 50.dp)
                    viewGroup.setPadding(0, 6.dp, 0, 6.dp)
            }
        }
        hookInfo.settingView.hookBeforeMethod({ setLastClickTime }) { it.args[1] = 0L }
        @Suppress("UNCHECKED_CAST")
        hookInfo.setting.drawerSettingPack.hookAfterMethod({ createSettingProvider }) { param ->
            val settingProviders = param.result as CopyOnWriteArrayList<Any?>
            val hostField = hookInfo.setting.baseSettingPack.host
            val fragment = param.thisObject.getObjectField(hostField)
                ?: return@hookAfterMethod
            val getSetting = hookInfo.setting.baseSettingProvider.getSetting
            val rightDescField = hookInfo.setting.rightDesc
            val redDotListenerField = hookInfo.setting.redDotListener
            settingProviders.map { it?.callMethod(getSetting) }.forEach { s ->
                if (purifyRedDots) {
                    s?.getObjectField(rightDescField)?.takeIf {
                        it != "未开启"
                    }?.run { s.setObjectField(rightDescField, null) }
                    s.setObjectField(redDotListenerField, null)
                }
            }
            val titleField = hookInfo.setting.title
            for (i in settingProviders.size - 1 downTo 0) {
                settingProviders[i]?.callMethod(getSetting)
                    ?.getObjectFieldAs<String>(titleField)?.takeIf {
                        purifyMoreItems.contains(it)
                    }?.run { settingProviders.removeAt(i) }
            }
            if (settingProviders.last()?.callMethod(getSetting)
                    ?.getObjectField(titleField) == Setting.TITLE_DIVIDER
            ) settingProviders.removeAt(settingProviders.lastIndex)

            val moduleSetting = Setting.button(R.string.app_name) {
                onQMHelperSettingClicked(it.context)
            }
            settingProviders.add(1, settingProvider(fragment, moduleSetting))
        }
        if (purifyMoreItems.contains("创作者中心"))
            hookInfo.setting.drawerSettingPack.replaceMethod({ initKolEnter }) { null }

        if (!purifyRedDots) return
        hookInfo.personalEntryView.hookAfterMethod({ update }) { param ->
            param.thisObject.getObjectFieldAs<TextView>(hookInfo.personalEntryView.rightDescView)
                .run { text = "" }
            param.thisObject.getObjectFieldAs<View>(hookInfo.personalEntryView.redDotView)
                .run { visibility = View.GONE }
        }
        hookInfo.settingFragment.hookBeforeMethod({ resume }) { param ->
            param.thisObject.getObjectFieldAs<List<*>>(hookInfo.settingFragment.settingList)
                .forEach {
                    it?.setObjectField(hookInfo.setting.redDotListener, null)
                }
        }
    }

    private fun settingProvider(fragment: Any, setting: Any?): Any? {
        val baseSettingProviderClass = instance.baseSettingProviderClass ?: return null
        val create = hookInfo.setting.baseSettingProvider.create.name
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
        val settingPackage = hookInfo.setting.baseSettingFragment.settingPackage.name
        val title = hookInfo.setting.baseSettingFragment.title.name
        val createSettingProvider = hookInfo.setting.baseSettingPack.createSettingProvider.name
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
        context.callMethod(
            hookInfo.appStarterActivity.addSecondFragment,
            moduleSettingFragmentClass, null
        )
        unhook?.unhook()
    }

    private fun dailySignIn() {
        val recordKey = "${uin()}_daily_sign_in_record"
        if (!isLogin() || sCaches.getString(recordKey, null) == todayFormat)
            return
        mainScope.launch(Dispatchers.IO) {
            runCatching {
                val url = "https://u.y.qq.com/cgi-bin/musics.fcg?_webcgikey=doSignIn"
                webJsonPost(
                    url,
                    "music.actCenter.DaysignactSvr",
                    "doSignIn",
                    mapOf("date" to "")
                )
            }.onFailure {
                Log.e(it)
            }.onSuccess {
                val response = it?.runCatchingOrNull { toJSONObject() } ?: return@launch
                val code = response.optJSONObject("req_0")
                    ?.optJSONObject("data")?.optInt("code") ?: return@launch
                if (code == 0) {
                    sCaches.edit { putString(recordKey, todayFormat) }
                    BannerTips.success(R.string.daily_sign_in_success)
                } else if (code == 2) {
                    sCaches.edit { putString(recordKey, todayFormat) }
                }
            }
        }
    }
}
