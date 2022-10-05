@file:Suppress("DEPRECATION")

package me.kofua.qmhelper

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.DexHelper
import me.kofua.qmhelper.utils.Log
import me.kofua.qmhelper.utils.Weak
import me.kofua.qmhelper.utils.findDexClassLoader
import me.kofua.qmhelper.utils.findFieldByExactType
import me.kofua.qmhelper.utils.from
import me.kofua.qmhelper.utils.getVersionCode
import me.kofua.qmhelper.utils.hostPackageName
import me.kofua.qmhelper.utils.isAbstract
import me.kofua.qmhelper.utils.isFinal
import me.kofua.qmhelper.utils.isNotStatic
import me.kofua.qmhelper.utils.isPublic
import me.kofua.qmhelper.utils.isStatic
import me.kofua.qmhelper.utils.print
import me.kofua.qmhelper.utils.runCatchingOrNull
import java.io.File
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

infix fun Configs.Class.from(cl: ClassLoader) = if (hasName()) name.from(cl) else null
val Configs.Method.orNull get() = if (hasName()) name else null
val Configs.Field.orNull get() = if (hasName()) name else null

class QMPackage(private val classLoader: ClassLoader, context: Context) {
    init {
        instance = this
    }

    @OptIn(ExperimentalTime::class)
    private val hookInfo: Configs.HookInfo = run {
        val (result, time) = measureTimedValue { readHookInfo(context) }
        Log.d("load hookinfo $time")
        Log.d(result.print())
        result
    }

    val baseFragmentClass by Weak { hookInfo.baseFragment.class_ from classLoader }
    val splashAdapterClass by Weak { hookInfo.splashAdapter from classLoader }
    val homePageFragmentClass by Weak { hookInfo.homePageFragment.class_ from classLoader }
    val mainDesktopHeaderClass by Weak { hookInfo.mainDesktopHeader.class_ from classLoader }
    val adManagerClass by Weak { hookInfo.adManager.class_ from classLoader }
    val personalEntryViewClass by Weak { hookInfo.personalEntryView.class_ from classLoader }
    val moreFragmentClass by Weak { hookInfo.moreFragment.class_ from classLoader }
    val bannerTipsClass by Weak { hookInfo.bannerTips.class_ from classLoader }
    val settingFragmentClass by Weak { hookInfo.settingFragment.class_ from classLoader }
    val userInfoHolderClass by Weak { hookInfo.userInfoHolder.class_ from classLoader }
    val baseAbTesterClass by Weak { hookInfo.abTester.class_ from classLoader }
    val topAreaDelegateClass by Weak { hookInfo.topAreaDelegate.class_ from classLoader }
    val strategyModuleClass by Weak { hookInfo.abTester.strategyModule from classLoader }
    val playViewModelClass by Weak { hookInfo.playViewModel.class_ from classLoader }
    val spManagerClass by Weak { hookInfo.spManager.class_ from classLoader }
    val appStarterActivityClass by Weak { hookInfo.appStarterActivity.class_ from classLoader }
    val modeFragmentClass by Weak { hookInfo.modeFragment from classLoader }
    val settingClass by Weak { hookInfo.setting.class_ from classLoader }
    val switchListenerClass by Weak { hookInfo.setting.switchListener.class_ from classLoader }
    val baseSettingFragmentClass by Weak { hookInfo.setting.baseSettingFragment.class_ from classLoader }
    val baseSettingPackClass by Weak { hookInfo.setting.baseSettingPack.class_ from classLoader }
    val baseSettingProviderClass by Weak { hookInfo.setting.baseSettingProvider.class_ from classLoader }
    val authAgentClass by Weak { hookInfo.authAgent.class_ from classLoader }
    val jsonRespParserClass by Weak { hookInfo.jsonRespParser.class_ from classLoader }
    val gsonClass by Weak { hookInfo.gson.class_ from classLoader }
    val jsonObjectClass by Weak { hookInfo.gson.jsonObject from classLoader }
    val uiModeManagerClass by Weak { hookInfo.uiModeManager.class_ from classLoader }
    val adBarClass by Weak { hookInfo.adBar.class_ from classLoader }
    val musicWorldTouchListenerClass by Weak { hookInfo.musicWorldTouchListener from classLoader }
    val eKeyManagerClass by Weak { hookInfo.eKeyManager.class_ from classLoader }
    val eKeyDecryptorClass by Weak { hookInfo.eKeyDecryptor.class_ from classLoader }
    val vipDownloadHelperClass by Weak { hookInfo.vipDownloadHelper.class_ from classLoader }
    val bottomTipControllerClass by Weak { hookInfo.bottomTipController.class_ from classLoader }
    val videoViewDelegateClass by Weak { hookInfo.videoViewDelegate.class_ from classLoader }
    val genreViewDelegateClass by Weak { hookInfo.genreViewDelegate.class_ from classLoader }
    val userGuideViewDelegateClass by Weak { hookInfo.userGuideViewDelegate.class_ from classLoader }
    val topSongViewDelegateClass by Weak { hookInfo.topSongViewDelegate.class_ from classLoader }
    val dataPluginClass by Weak { hookInfo.dataPlugin.class_ from classLoader }
    val expandableTextViewClass by Weak { "com.tencent.expandabletextview.ExpandableTextView" from classLoader }
    val albumIntroViewHolderClass by Weak { hookInfo.albumIntroViewHolder.class_ from classLoader }
    val albumTagViewHolderClass by Weak { hookInfo.albumTagViewHolder from classLoader }

    val rightDescViewField get() = hookInfo.personalEntryView.rightDescView.orNull
    val redDotViewField get() = hookInfo.personalEntryView.redDotView.orNull
    val moreListField get() = hookInfo.moreFragment.moreList.orNull
    val settingListField get() = hookInfo.settingFragment.settingList.orNull
    val titleField get() = hookInfo.setting.title.orNull
    val rightDescField get() = hookInfo.setting.rightDesc.orNull
    val redDotListenerField get() = hookInfo.setting.redDotListener.orNull
    val builderTypeField get() = hookInfo.setting.builder.type.orNull
    val builderTitleField get() = hookInfo.setting.builder.title.orNull
    val builderRightDescField get() = hookInfo.setting.builder.rightDesc.orNull
    val builderSummaryField get() = hookInfo.setting.builder.summary.orNull
    val builderSwitchListenerField get() = hookInfo.setting.builder.switchListener.orNull
    val builderClickListenerField get() = hookInfo.setting.builder.clickListener.orNull
    val eKeyDecryptorInstanceField get() = hookInfo.eKeyDecryptor.instance.orNull
    val eKeyManagerInstanceField get() = hookInfo.eKeyManager.instance.orNull
    val runtimeField get() = hookInfo.dataPlugin.runtime.orNull
    val tvAlbumDetailField get() = hookInfo.albumIntroViewHolder.tvAlbumDetail.orNull
    val lastTextContentField get() = hookInfo.albumIntroViewHolder.lastTextContent.orNull

    val adBarMethods get() = hookInfo.adBar.methodsList.map { it.name }

    fun initTabFragment() = hookInfo.homePageFragment.initTabFragment.orNull
    fun addTabById() = hookInfo.mainDesktopHeader.addTabById.orNull
    fun addTabByName() = hookInfo.mainDesktopHeader.addTabByName.orNull
    fun get() = hookInfo.adManager.get.orNull
    fun update() = hookInfo.personalEntryView.update.orNull
    fun resume() = hookInfo.baseFragment.resume.orNull
    fun showStyledToast() = hookInfo.bannerTips.showStyledToast.orNull
    fun showMusicWorld() = hookInfo.mainDesktopHeader.showMusicWorld.orNull
    fun showBubble() = hookInfo.userInfoHolder.showBubble.orNull
    fun getProperty() = hookInfo.abTester.getProperty.orNull
    fun showCurListen() = hookInfo.topAreaDelegate.showCurListen.orNull
    fun initLiveGuide() = hookInfo.topAreaDelegate.initLiveGuide.orNull
    fun getStrategyId() = hookInfo.abTester.getStrategyId.orNull
    fun setCanSlide() = hookInfo.playViewModel.setCanSlide.orNull
    fun getSp() = hookInfo.spManager.get.orNull
    fun doOnCreate() = hookInfo.appStarterActivity.doOnCreate.orNull
    fun addSecondFragment() = hookInfo.appStarterActivity.addSecondFragment.orNull
    fun showMessageDialog() = hookInfo.appStarterActivity.showMessageDialog.orNull
    fun with() = hookInfo.setting.with.orNull
    fun build() = hookInfo.setting.builder.build.orNull
    fun isSwitchOn() = hookInfo.setting.switchListener.isSwitchOn.orNull
    fun onSwitchStatusChange() = hookInfo.setting.switchListener.onSwitchStatusChange.orNull
    fun settingPackage() = hookInfo.setting.baseSettingFragment.settingPackage.orNull
    fun title() = hookInfo.setting.baseSettingFragment.title.orNull
    fun createSettingProvider() = hookInfo.setting.baseSettingPack.createSettingProvider.orNull
    fun create() = hookInfo.setting.baseSettingProvider.create.orNull
    fun startActionActivity() = hookInfo.authAgent.startActionActivity.orNull
    fun parseModuleItem() = hookInfo.jsonRespParser.parseModuleItem.orNull
    fun fromJson() = hookInfo.gson.fromJson.orNull
    fun toJson() = hookInfo.gson.toJson.orNull
    fun isThemeForbid() = hookInfo.uiModeManager.isThemeForbid.orNull
    fun getFileEKey() = hookInfo.eKeyManager.getFileEKey.orNull
    fun decryptFile() = hookInfo.eKeyDecryptor.decryptFile.orNull
    fun staticDecryptFile() = hookInfo.vipDownloadHelper.decryptFile.orNull
    fun updateBottomTips() = hookInfo.bottomTipController.updateBottomTips.orNull
    fun onResult() = hookInfo.videoViewDelegate.onResult.orNull
    fun onBind() = hookInfo.genreViewDelegate.onBind.orNull
    fun showUserGuide() = hookInfo.userGuideViewDelegate.showUserGuide.orNull
    fun topSongOnBind() = hookInfo.topSongViewDelegate.onBind.orNull
    fun handleJsRequest() = hookInfo.dataPlugin.handleJsRequest.orNull
    fun activity() = hookInfo.dataPlugin.activity.orNull
    fun onHolderCreated() = hookInfo.albumIntroViewHolder.onHolderCreated.orNull

    private fun readHookInfo(context: Context): Configs.HookInfo {
        try {
            val hookInfoFile = File(context.cacheDir, Constant.HOOK_INFO_FILE_NAME)
            Log.d("Reading hook info: $hookInfoFile")
            val t = measureTimeMillis {
                if (hookInfoFile.isFile && hookInfoFile.canRead()) {
                    val lastUpdateTime = context.packageManager.getPackageInfo(
                        hostPackageName,
                        0
                    ).lastUpdateTime
                    val lastModuleUpdateTime = try {
                        context.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
                    } catch (e: Throwable) {
                        null
                    }?.lastUpdateTime ?: 0
                    val info = hookInfoFile.inputStream().use {
                        runCatchingOrNull { Configs.HookInfo.parseFrom(it) }
                            ?: Configs.HookInfo.newBuilder().build()
                    }
                    if (info.lastUpdateTime >= lastUpdateTime && info.lastUpdateTime >= lastModuleUpdateTime
                        && getVersionCode(hostPackageName) == info.clientVersionCode
                        && BuildConfig.VERSION_CODE == info.moduleVersionCode
                        && BuildConfig.VERSION_NAME == info.moduleVersionName
                    ) return info
                }
            }
            Log.d("Read hook info completed: take $t ms")
        } catch (e: Throwable) {
            Log.w(e)
        }
        return initHookInfo(context).also {
            try {
                val hookInfoFile = File(context.cacheDir, Constant.HOOK_INFO_FILE_NAME)
                if (hookInfoFile.exists()) hookInfoFile.delete()
                hookInfoFile.outputStream().use { o -> it.writeTo(o) }
            } catch (e: Exception) {
                Log.e(e)
            }
        }
    }

    companion object {
        @Volatile
        lateinit var instance: QMPackage

        @JvmStatic
        fun initHookInfo(context: Context) = hookInfo {
            val classLoader = context.classLoader
            //val classesList = classLoader.allClassesList().asSequence()

            try {
                System.loadLibrary("qmhelper")
            } catch (e: Throwable) {
                Log.e(e)
                BannerTips.error(R.string.not_supported)
                return@hookInfo
            }
            val dexHelper = DexHelper(classLoader.findDexClassLoader() ?: return@hookInfo)
            lastUpdateTime = max(
                context.packageManager.getPackageInfo(hostPackageName, 0).lastUpdateTime,
                runCatchingOrNull {
                    context.packageManager.getPackageInfo(
                        BuildConfig.APPLICATION_ID,
                        0
                    )
                }?.lastUpdateTime ?: 0
            )
            clientVersionCode = getVersionCode(hostPackageName)
            moduleVersionCode = BuildConfig.VERSION_CODE
            moduleVersionName = BuildConfig.VERSION_NAME
            baseFragment = baseFragment {
                val resumeMethod = dexHelper.findMethodUsingString(
                    "[onResume]this[%s]",
                    true,
                    -1,
                    -1,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    true
                ).firstOrNull()?.run {
                    dexHelper.findMethodInvoking(
                        this,
                        -1,
                        -1,
                        "V",
                        -1,
                        null,
                        null,
                        null,
                        false
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)?.takeIf { m -> m.isAbstract }
                    }
                } ?: return@baseFragment
                class_ = class_ { name = resumeMethod.declaringClass.name }
                resume = method { name = resumeMethod.name }
            }
            splashAdapter = class_ {
                val splashAdapterClass =
                    "com.tencentmusic.ad.adapter.madams.splash.OperExpertSplashAdapter"
                        .from(classLoader) ?: dexHelper.findMethodUsingString(
                        "postSplashResult",
                        true,
                        -1,
                        -1,
                        null,
                        -1,
                        null,
                        null,
                        null,
                        true
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@class_
                name = splashAdapterClass.name
            }
            homePageFragment = homePageFragment {
                val initTabFragmentMethod = dexHelper.findMethodUsingStringExtract(
                    "not support native tab, do nothing",
                )?.run {
                    dexHelper.findMethodInvoking(
                        this,
                        -1,
                        -1,
                        "VI",
                        -1,
                        null,
                        null,
                        null,
                        false
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)?.takeIf { m ->
                            m is Method && m.isNotStatic && m.isFinal
                        }
                    }
                } ?: return@homePageFragment
                class_ = class_ { name = initTabFragmentMethod.declaringClass.name }
                initTabFragment = method { name = initTabFragmentMethod.name }
            }
            mainDesktopHeader = mainDesktopHeader {
                val clazz = "com.tencent.qqmusic.ui.MainDesktopHeader".from(classLoader)
                    ?: dexHelper.findMethodUsingStringExtract(
                        "MainDesktopHeader",
                    )?.let {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@mainDesktopHeader
                val clazzIndex = dexHelper.encodeClassIndex(clazz)
                val addTabByIdMethod = dexHelper.findMethodUsingString(
                    "iconUrl",
                    false,
                    -1,
                    -1,
                    null,
                    clazzIndex,
                    null,
                    null,
                    null,
                    true
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@mainDesktopHeader
                val addTabByNameMethod = dexHelper.findMethodUsingString(
                    "tabName",
                    false,
                    -1,
                    -1,
                    null,
                    clazzIndex,
                    null,
                    null,
                    null,
                    true
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@mainDesktopHeader
                val showMusicWorldMethod = dexHelper.findMethodUsingString(
                    "showMusicWorldEntranceBtn",
                    false,
                    -1,
                    -1,
                    null,
                    clazzIndex,
                    null,
                    null,
                    null,
                    true
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@mainDesktopHeader
                class_ = class_ { name = clazz.name }
                addTabById = method { name = addTabByIdMethod.name }
                addTabByName = method { name = addTabByNameMethod.name }
                showMusicWorld = method { name = showMusicWorldMethod.name }
            }
            adManager = adManager {
                val onStartIndex = dexHelper.encodeMethodIndex(
                    "com.tencent.qqmusic.activity.AppStarterActivity".from(classLoader)
                        ?.getDeclaredMethod("onStart") ?: return@adManager
                )
                val getMethod = dexHelper.findMethodInvoking(
                    onStartIndex,
                    -1,
                    -1,
                    "L",
                    -1,
                    null,
                    null,
                    null,
                    false
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.takeIf { m ->
                        m is Method && m.isNotStatic
                    }
                } ?: return@adManager
                class_ = class_ { name = getMethod.declaringClass.name }
                get = method { name = getMethod.name }
            }
            setting = setting {
                val settingBuilderClass = dexHelper.findMethodUsingStringExtract(
                    "onSwitchLister can't be null while type is TYPE_SWITCH"
                )?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@setting
                val settingClass = settingBuilderClass.declaringClass ?: return@setting
                val withMethod = settingClass.declaredMethods.find {
                    it.isStatic && it.returnType == settingBuilderClass
                } ?: return@setting
                val buildMethod = settingBuilderClass.declaredMethods.find {
                    it.returnType == settingClass
                } ?: return@setting
                val fields = settingClass.declaredFields.takeIf { it.size > 15 } ?: return@setting
                val builderFields = settingBuilderClass.declaredFields
                    .takeIf { it.size >= 17 } ?: return@setting
                val switchListenerField = builderFields[6]
                val switchListenerClass = switchListenerField.type.takeIf {
                    it.isInterface && it.methods.size == 2
                } ?: return@setting
                val isSwitchOnMethod = switchListenerClass.methods.find {
                    it.returnType == Boolean::class.javaPrimitiveType
                } ?: return@setting
                val onSwitchStatusChangeMethod = switchListenerClass.methods.find { m ->
                    m.parameterTypes.let { it.size == 1 && it[0] == Boolean::class.javaPrimitiveType }
                } ?: return@setting
                class_ = class_ { name = settingClass.name }
                with = method { name = withMethod.name }
                title = field { name = fields[1].name }
                rightDesc = field { name = fields[3].name }
                redDotListener = field { name = fields[10].name }
                builder = settingBuilder {
                    class_ = class_ { name = settingBuilderClass.name }
                    build = method { name = buildMethod.name }
                    type = field { name = builderFields[0].name }
                    title = field { name = builderFields[1].name }
                    rightDesc = field { name = builderFields[2].name }
                    dotRightDesc = field { name = builderFields[3].name }
                    summary = field { name = builderFields[5].name }
                    switchListener = field { name = switchListenerField.name }
                    settingProvider = field { name = builderFields[8].name }
                    redDot = field { name = builderFields[11].name }
                    enabled = field { name = builderFields[12].name }
                    clickListener = field { name = builderFields[13].name }
                    touchListener = field { name = builderFields[14].name }
                }
                switchListener = switchListener {
                    class_ = class_ { name = switchListenerClass.name }
                    isSwitchOn = method { name = isSwitchOnMethod.name }
                    onSwitchStatusChange = method { name = onSwitchStatusChangeMethod.name }
                }

                val debugSettingFragmentClass = dexHelper.findMethodUsingStringExtract("DebugMode")
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@setting
                val baseSettingFragmentClass =
                    debugSettingFragmentClass.superclass ?: return@setting
                val settingPackMethod = debugSettingFragmentClass.declaredMethods.find {
                    it.returnType != String::class.java
                } ?: return@setting
                val titleMethod = debugSettingFragmentClass.declaredMethods.find {
                    it.returnType == String::class.java
                } ?: return@setting
                val baseSettingPackClass = settingPackMethod.returnType
                val createSettingProviderMethod = baseSettingPackClass.methods.find {
                    it.returnType == CopyOnWriteArrayList::class.java && it.isAbstract
                } ?: return@setting
                val baseSettingProviderClass =
                    dexHelper.findMethodUsingStringExtract("setting is null")
                        ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@setting
                val createMethod = baseSettingProviderClass.methods.find {
                    it.returnType == settingClass && it.isAbstract
                } ?: return@setting
                baseSettingFragment = baseSettingFragment {
                    class_ = class_ { name = baseSettingFragmentClass.name }
                    settingPackage = method { name = settingPackMethod.name }
                    title = method { name = titleMethod.name }
                }
                baseSettingPack = baseSettingPack {
                    class_ = class_ { name = baseSettingPackClass.name }
                    createSettingProvider = method { name = createSettingProviderMethod.name }
                }
                baseSettingProvider = baseSettingProvider {
                    class_ = class_ { name = baseSettingProviderClass.name }
                    create = method { name = createMethod.name }
                }
            }
            personalEntryView = personalEntryView {
                val entryViewClass =
                    "com.tencent.qqmusic.fragment.morefeatures.settings.view.PersonalCenterEntryView"
                        .from(classLoader) ?: dexHelper.findMethodUsingStringExtract(
                        "PersonalCenterEntryView",
                    )?.let {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@personalEntryView
                val updateMethod = entryViewClass.declaredMethods.find {
                    it.isSynthetic && it.parameterTypes.size == 1
                } ?: return@personalEntryView
                val rightDescViewField = entryViewClass.declaredFields.find {
                    TextView::class.java.isAssignableFrom(it.type)
                } ?: return@personalEntryView
                val redDotViewField = entryViewClass.declaredFields.find {
                    ImageView::class.java.isAssignableFrom(it.type)
                } ?: return@personalEntryView
                class_ = class_ { name = entryViewClass.name }
                update = method { name = updateMethod.name }
                rightDescView = field { name = rightDescViewField.name }
                redDotView = field { name = redDotViewField.name }
            }
            moreFragment = moreFragment {
                val moreFragmentClass =
                    "com.tencent.qqmusic.fragment.morefeatures.MoreFeaturesFragment"
                        .from(classLoader) ?: dexHelper.findMethodUsingStringExtract(
                        "AutoClose#MoreFeaturesFragment",
                    )?.let {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@moreFragment
                val moreListField = moreFragmentClass
                    .findFieldByExactType(List::class.java) ?: return@moreFragment
                class_ = class_ { name = moreFragmentClass.name }
                moreList = field { name = moreListField.name }
            }
            bannerTips = bannerTips {
                val showStyledToastMethod = dexHelper.findMethodUsingString(
                    "showToast() >>> IS USING QPLAY_AUTO, FORBID BANNER_TIPS",
                    false,
                    -1,
                    7,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    true
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@bannerTips
                class_ = class_ { name = showStyledToastMethod.declaringClass.name }
                showStyledToast = method { name = showStyledToastMethod.name }
            }
            settingFragment = settingFragment {
                val settingFragmentClass =
                    "com.tencent.qqmusic.fragment.morefeatures.SettingFeaturesFragment"
                        .from(classLoader) ?: dexHelper.findMethodUsingString(
                        "KEY_SETTING_JUMP_ITEM_ENUM",
                        false,
                        -1,
                        1,
                        null,
                        -1,
                        null,
                        null,
                        null,
                        true
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@settingFragment
                val settingListField = settingFragmentClass
                    .findFieldByExactType(List::class.java) ?: return@settingFragment
                class_ = class_ { name = settingFragmentClass.name }
                settingList = field { name = settingListField.name }
            }
            userInfoHolder = userInfoHolder {
                val showBubbleMethod =
                    "com.tencent.qqmusic.modular.module.musichall.views.viewholders.cell.MyMusicUserInfoViewHolder"
                        .from(classLoader)?.declaredMethods?.find {
                            it.name == "showTaskAndVipBubble"
                        } ?: run {
                        val clazz = dexHelper.findMethodUsingStringExtract(
                            "refreshVipIcons",
                        )?.let {
                            dexHelper.decodeMethodIndex(it)
                        }?.declaringClass ?: return@userInfoHolder
                        val clazzIndex = dexHelper.encodeClassIndex(clazz)
                        dexHelper.findMethodUsingString(
                            "3",
                            false,
                            -1,
                            -1,
                            "VL",
                            clazzIndex,
                            null,
                            null,
                            null,
                            false
                        ).asSequence().firstNotNullOfOrNull {
                            dexHelper.decodeMethodIndex(it)?.takeIf { m -> m.isFinal }
                        }
                    } ?: return@userInfoHolder
                class_ = class_ { name = showBubbleMethod.declaringClass.name }
                showBubble = method { name = showBubbleMethod.name }
            }
            abTester = baseABTester {
                val clazz = dexHelper.findMethodUsingStringExtract(
                    "[BaseABTester init]: need ABTestAnnotation"
                )?.let {
                    dexHelper.decodeMethodIndex(it)
                }?.declaringClass ?: return@baseABTester
                val getStrategyIdMethod = dexHelper.findMethodUsingStringExtract(
                    "[getClientStrategyStrategyId]: this:"
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@baseABTester
                val getPropertyMethod = clazz.declaredMethods.find {
                    it.parameterTypes.size == 1 && it.parameterTypes[0] == String::class.java
                            && it.returnType != Void::class.javaPrimitiveType && it.isPublic
                } ?: return@baseABTester
                class_ = class_ { name = clazz.name }
                getProperty = method { name = getPropertyMethod.name }
                strategyModule = class_ { name = getStrategyIdMethod.declaringClass.name }
                getStrategyId = method { name = getStrategyIdMethod.name }
            }
            topAreaDelegate = topAreaDelegate {
                val initLiveGuideMethod = dexHelper.findMethodUsingStringExtract(
                    "[initLiveGuide]"
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@topAreaDelegate
                val showCurListenMethod = dexHelper.findMethodUsingStringExtract(
                    "[showCurrentListen]--block by long audio ad recall entrance show"
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@topAreaDelegate
                class_ = class_ { name = initLiveGuideMethod.declaringClass.name }
                initLiveGuide = method { name = initLiveGuideMethod.name }
                showCurListen = method { name = showCurListenMethod.name }
            }
            playViewModel = playerViewModel {
                val clazz = "com.tencent.qqmusic.business.playernew.viewmodel.PlayerViewModel"
                    .from(classLoader) ?: dexHelper.findMethodUsingStringExtract("PlayerViewModel")
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                ?: return@playerViewModel
                val setCanSlideMethod =
                    dexHelper.encodeMethodIndex(clazz.declaredMethods.find { m ->
                        !m.isSynthetic && m.returnType == Void::class.javaPrimitiveType
                                && m.parameterTypes.let { it.size == 1 && it[0] == clazz }
                    } ?: return@playerViewModel).run {
                        dexHelper.findMethodInvoking(
                            this,
                            -1,
                            -1,
                            "VZ",
                            -1,
                            null,
                            null,
                            null,
                            true
                        ).asSequence().firstNotNullOfOrNull {
                            dexHelper.decodeMethodIndex(it)
                        }
                    } ?: return@playerViewModel
                class_ = class_ { name = clazz.name }
                setCanSlide = method { name = setCanSlideMethod.name }
            }
            spManager = spManager {
                val clazz = dexHelper.findMethodUsingStringExtract("SPManager")
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@spManager
                val getMethod = clazz.declaredMethods.find {
                    it.isStatic && it.returnType == clazz && it.parameterTypes.isEmpty()
                } ?: return@spManager
                class_ = class_ { name = clazz.name }
                get = method { name = getMethod.name }
            }
            appStarterActivity = appStarterActivity {
                val doOnCreateMethod = dexHelper
                    .findMethodUsingStringExtract("Appstarter activity oncreate")
                    ?.let { dexHelper.decodeMethodIndex(it) } ?: return@appStarterActivity
                val addSecondFragmentMethod = dexHelper
                    .findMethodUsingStringExtract("[addSecondFragment] gotoFirstFragment")
                    ?.let { dexHelper.decodeMethodIndex(it) } ?: return@appStarterActivity
                val appStarterActivityClass = doOnCreateMethod.declaringClass
                val showMessageDialogMethod = appStarterActivityClass.methods.find { m ->
                    Dialog::class.java.isAssignableFrom(m.returnType)
                            && m.parameterTypes.let { it.size == 8 && it[0] == String::class.java && it[7] == Boolean::class.javaPrimitiveType }
                } ?: return@appStarterActivity
                class_ = class_ { name = appStarterActivityClass.name }
                doOnCreate = method { name = doOnCreateMethod.name }
                addSecondFragment = method { name = addSecondFragmentMethod.name }
                showMessageDialog = method { name = showMessageDialogMethod.name }
            }
            modeFragment = class_ {
                name = "com.tencent.qqmusic.fragment.morefeatures.ModeSettingFragment"
                    .from(classLoader)?.name ?: dexHelper.findMethodUsingStringExtract(
                    "runningModeSetting"
                )?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass?.name ?: return@class_
            }
            authAgent = authAgent {
                val startActionActivityMethod = dexHelper.findMethodUsingStringExtract(
                    "LOGIN_CHECK_SDK"
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@authAgent
                class_ = class_ { name = startActionActivityMethod.declaringClass.name }
                startActionActivity = method { name = startActionActivityMethod.name }
            }
            val parseModuleItemMethod = dexHelper.findMethodUsingStringExtract(
                "[parseModuleItem] "
            )?.let { dexHelper.decodeMethodIndex(it) as? Method }
            jsonRespParser = jsonRespParser {
                parseModuleItemMethod ?: return@jsonRespParser
                class_ = class_ { name = parseModuleItemMethod.declaringClass.name }
                parseModuleItem = method { name = parseModuleItemMethod.name }
            }
            gson = gson {
                val gsonClass = "com.google.gson.Gson".from(classLoader)
                    ?: dexHelper.findMethodUsingStringExtract("GSON cannot serialize ")
                        ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@gson
                val fromJsonMethod = gsonClass.declaredMethods.find { m ->
                    m.parameterTypes.let { it.size == 2 && it[0] == String::class.java && it[1] == Class::class.java }
                } ?: return@gson
                val toJsonMethod = gsonClass.declaredMethods.find { m ->
                    m.parameterTypes.let { it.size == 1 && it[0] == Any::class.java } && m.returnType == String::class.java
                } ?: return@gson
                val jsonObjectClass = parseModuleItemMethod?.parameterTypes
                    ?.getOrNull(2) ?: return@gson
                class_ = class_ { name = gsonClass.name }
                fromJson = method { name = fromJsonMethod.name }
                toJson = method { name = toJsonMethod.name }
                jsonObject = class_ { name = jsonObjectClass.name }
            }
            uiModeManager = uiModeManager {
                val clazz = dexHelper.findMethodUsingStringExtract("UIModeManager")
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                    ?: return@uiModeManager
                val method = clazz.declaredMethods.find { m ->
                    m.returnType == Boolean::class.javaPrimitiveType && m.parameterTypes.let { it.size == 1 && it[0] == String::class.java }
                } ?: return@uiModeManager
                class_ = class_ { name = clazz.name }
                isThemeForbid = method { name = method.name }
            }
            adBar = apkDownloadAdBar {
                val clazz = "com.tencent.qqmusic.business.ad.topbarad.apkdownload.ApkDownloadAdBar"
                    .from(classLoader) ?: dexHelper.findMethodUsingStringExtract("ApkDownloadAdBar")
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                ?: return@apkDownloadAdBar
                val methods = clazz.interfaces.getOrNull(0)?.methods?.filter {
                    it.returnType == Void::class.javaPrimitiveType && it.parameterTypes.isEmpty()
                } ?: return@apkDownloadAdBar
                class_ = class_ { name = clazz.name }
                this.methods.addAll(methods.map { method { name = it.name } })
            }
            musicWorldTouchListener = class_ {
                name = dexHelper.findMethodUsingString(
                    "MusicWorldEntrance",
                    false,
                    -1,
                    -1,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    false
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.declaringClass?.takeIf { c ->
                        View.OnTouchListener::class.java.isAssignableFrom(c)
                    }
                }?.name ?: return@class_
            }
            eKeyManager = audioStreamEKeyManager {
                val getFileEKeyMethod = dexHelper.findMethodUsingStringExtract(
                    "getFileEKey filePath is empty!"
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@audioStreamEKeyManager
                val eKeyManagerClass = getFileEKeyMethod.declaringClass
                val instanceField = eKeyManagerClass.declaredFields.find {
                    it.type == eKeyManagerClass
                } ?: return@audioStreamEKeyManager
                class_ = class_ { name = eKeyManagerClass.name }
                instance = field { name = instanceField.name }
                getFileEKey = method { name = getFileEKeyMethod.name }
            }
            eKeyDecryptor = eKeyDecryptor {
                val decryptFileMethod = dexHelper.findMethodUsingStringExtract(
                    "decryptFile srcFilePath = "
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@eKeyDecryptor
                val companionClass = decryptFileMethod.declaringClass
                val decryptorClass = companionClass.declaringClass ?: return@eKeyDecryptor
                val instanceField = decryptorClass.declaredFields.find {
                    it.type == companionClass
                } ?: return@eKeyDecryptor
                class_ = class_ { name = decryptorClass.name }
                instance = field { name = instanceField.name }
                decryptFile = method { name = decryptFileMethod.name }
            }
            vipDownloadHelper = vipDownloadHelper {
                val decryptFileMethod = dexHelper.findMethodUsingStringExtract(
                    "decryptFile create dest = "
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@vipDownloadHelper
                class_ = class_ { name = decryptFileMethod.declaringClass.name }
                decryptFile = method { name = decryptFileMethod.name }
            }
            bottomTipController = bottomTipController {
                val method = dexHelper.findMethodUsingStringExtract("updateBottomTips ")
                    ?.let { dexHelper.decodeMethodIndex(it) } ?: return@bottomTipController
                class_ = class_ { name = method.declaringClass.name }
                updateBottomTips = method { name = method.name }
            }
            videoViewDelegate = videoViewDelegate {
                val method = dexHelper.findMethodUsingStringExtract("[onResult] show mv icon.")
                    ?.let { dexHelper.decodeMethodIndex(it) } ?: return@videoViewDelegate
                class_ = class_ { name = method.declaringClass.name }
                onResult = method { name = method.name }
            }
            genreViewDelegate = genreViewDelegate {
                val method = dexHelper.findMethodUsingStringExtract(
                    "[onBind] hide the song genre tags info."
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@genreViewDelegate
                class_ = class_ { name = method.declaringClass.name }
                onBind = method { name = method.name }
            }
            userGuideViewDelegate = userGuideViewDelegate {
                val method = dexHelper.findMethodUsingStringExtract(
                    "showNewUserGuide hasGuideShowing ="
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@userGuideViewDelegate
                class_ = class_ { name = method.declaringClass.name }
                showUserGuide = method { name = method.name }
            }
            topSongViewDelegate = topSongViewDelegate {
                val method = dexHelper.findMethodUsingStringExtract(
                    "[onBind] show top song info: peakCount="
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@topSongViewDelegate
                class_ = class_ { name = method.declaringClass.name }
                onBind = method { name = method.name }
            }
            dataPlugin = dataPlugin {
                val handleJsRequestMethod = dexHelper.findMethodUsingStringExtract(
                    "[handleJsRequest] writeGlobalData"
                )?.let { dexHelper.decodeMethodIndex(it) } ?: return@dataPlugin
                val dataPluginClass = handleJsRequestMethod.declaringClass
                val basePluginClass = dataPluginClass.superclass ?: return@dataPlugin
                val runtimeField = basePluginClass.declaredFields
                    .find { it.isPublic } ?: return@dataPlugin
                val activityMethod = runtimeField.type.declaredMethods.find {
                    it.parameterTypes.isEmpty() && it.returnType == Activity::class.java
                } ?: return@dataPlugin
                class_ = class_ { name = dataPluginClass.name }
                handleJsRequest = method { name = handleJsRequestMethod.name }
                runtime = field { name = runtimeField.name }
                activity = method { name = activityMethod.name }
            }
            albumIntroViewHolder = albumIntroViewHolder {
                val c = "com.tencent.qqmusic.albumdetail.ui.viewholders.AlbumIntroduceViewHolder"
                    .from(classLoader) ?: dexHelper.findMethodUsingStringExtract("tvAlbumDetail")
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                ?: return@albumIntroViewHolder
                val m = c.declaredMethods.find { m ->
                    m.isPublic && m.returnType == Void::class.javaPrimitiveType && m.parameterTypes
                        .let { it.size == 1 && it[0] == View::class.java }
                } ?: return@albumIntroViewHolder
                val tvAlbumDetailField = c.declaredFields.find {
                    it.isNotStatic && TextView::class.java.isAssignableFrom(it.type)
                } ?: return@albumIntroViewHolder
                val lastTextContentField = c.declaredFields.find {
                    it.isNotStatic && it.type == String::class.java
                } ?: return@albumIntroViewHolder
                class_ = class_ { name = c.name }
                onHolderCreated = method { name = m.name }
                tvAlbumDetail = field { name = tvAlbumDetailField.name }
                lastTextContent = field { name = lastTextContentField.name }
            }
            albumTagViewHolder = class_ {
                name = "com.tencent.qqmusic.albumdetail.ui.viewholders.AlbumTagViewHolder"
                    .from(classLoader)?.name ?: dexHelper.findMethodUsingStringExtract(
                    "tvAlbumInfo"
                )?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass?.name ?: return@class_
            }

            dexHelper.close()
        }
    }
}
