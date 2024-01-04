package me.kofua.qmhelper

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import dalvik.system.BaseDexClassLoader
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kofua.qmhelper.XposedInit.Companion.modulePath
import me.kofua.qmhelper.data.*
import me.kofua.qmhelper.utils.*
import me.kofua.qmhelper.utils.BannerTips
import org.json.JSONObject
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.time.measureTimedValue

infix fun Class.from(classLoader: ClassLoader) =
    if (name.isNotEmpty()) name.from(classLoader) else null

val Member.paramTypes: Array<String>
    get() = when (this) {
        is Method -> parameterTypes.map { it.name }.toTypedArray()
        is Constructor<*> -> parameterTypes.map { it.name }.toTypedArray()
        else -> arrayOf()
    }

val qmPackage get() = QMPackage.instance
val hookInfo get() = QMPackage.instance.hookInfo

class QMPackage private constructor() {

    val bannerTipsClass by Weak { hookInfo.bannerTips.clazz from classLoader }
    val storageUtilsClass by Weak { hookInfo.storageUtils.clazz from classLoader }
    val eKeyManagerClass by Weak { hookInfo.eKeyManager.clazz from classLoader }
    val eKeyDecryptorClass by Weak { hookInfo.eKeyDecryptor.clazz from classLoader }
    val vipDownloadHelperClass by Weak { hookInfo.vipDownloadHelper.clazz from classLoader }
    val skinManagerClass by Weak { hookInfo.skinManager.clazz from classLoader }
    val baseSettingFragmentClass by Weak { hookInfo.setting.baseSettingFragment.clazz from classLoader }
    val baseSettingPackClass by Weak { hookInfo.setting.baseSettingPack.clazz from classLoader }
    val baseSettingProviderClass by Weak { hookInfo.setting.baseSettingProvider.clazz from classLoader }
    val settingClass by Weak { hookInfo.setting.clazz from classLoader }
    val switchListenerClass by Weak { hookInfo.setting.switchListener.clazz from classLoader }
    val jsonObjectClass by Weak { hookInfo.gson.jsonObject from classLoader }
    val appStarterActivityClass by Weak { hookInfo.appStarterActivity.clazz from classLoader }
    val storageVolumeClass by Weak { hookInfo.storageVolume from classLoader }
    val webRequestHeadersClass by Weak { hookInfo.webRequestHeaders.clazz from classLoader }
    val x5WebViewClass by Weak { "com.tencent.smtt.sdk.WebView" from classLoader }
    val userManagerClass by Weak { hookInfo.userManager.clazz from classLoader }
    val mERJniClass by Weak { "com.tencent.qqmusic.modular.framework.encrypt.logic.MERJni" from classLoader }
    val baseFragmentClass by Weak { hookInfo.baseFragment.clazz from classLoader }

    val hookInfo = run {
        val (result, time) = measureTimedValue { readHookInfo() }
        Log.d("Load hook info took $time")
        mainScope.launch(Dispatchers.IO) {
            result.toJson()?.let {
                JSONObject(it).toString(2)
            }.let { Log.d("Loaded hook info:\n$it") }
        }
        result
    }

    private fun readHookInfo(): HookInfo {
        try {
            val hookInfoFile = File(currentContext.cacheDir, HOOK_INFO_FILE_NAME)
            Log.d("Reading hook info: $hookInfoFile")
            val start = System.currentTimeMillis()
            if (hookInfoFile.isFile && hookInfoFile.canRead()) {
                val lastUpdateTime = getPackageLastUpdateTime(hostPackageName)
                val lastModuleUpdateTime = File(modulePath).lastModified()
                val info = hookInfoFile.inputStream().use {
                    runCatchingOrNull { ObjectInputStream(it).readAny<HookInfo>() } ?: HookInfo()
                }
                if (info.lastUpdateTime >= max(lastUpdateTime, lastModuleUpdateTime)
                    && getVersionCode(hostPackageName) == info.clientVersionCode
                    && BuildConfig.VERSION_CODE == info.moduleVersionCode
                    && BuildConfig.VERSION_NAME == info.moduleVersionName
                ) {
                    val end = System.currentTimeMillis()
                    Log.d("Read hook info from cache success, took ${end - start} ms")
                    return info
                }
            }
        } catch (e: Throwable) {
            Log.w(e)
        }
        return initHookInfo().also {
            try {
                val hookInfoFile = File(currentContext.cacheDir, HOOK_INFO_FILE_NAME)
                if (hookInfoFile.exists()) hookInfoFile.delete()
                hookInfoFile.outputStream().use { o -> ObjectOutputStream(o).writeObject(it) }
            } catch (e: Exception) {
                Log.e(e)
            }
        }
    }

    companion object {
        val instance by lazy { QMPackage() }
        private const val HOOK_INFO_FILE_NAME = "hookinfo.dat"

        @JvmStatic
        fun initHookInfo() = HookInfo().apply out@{
            val classLoader = currentContext.classLoader
            val dexClassLoaderDelegate = { delegator: BaseDexClassLoader ->
                if (delegator.javaClass != PathClassLoader::class.java) {
                    delegator.getFirstFieldByExactTypeOrNull<BaseDexClassLoader>() ?: delegator
                } else delegator
            }
            //val classesList = classLoader.allClassesList(dexClassLoaderDelegate).asSequence()

            try {
                System.loadLibrary("qmhelper")
            } catch (e: Throwable) {
                Log.e(e)
                BannerTips.error(R.string.not_supported)
                return@out
            }
            val dexClassLoader =
                classLoader.findDexClassLoader(dexClassLoaderDelegate) ?: return@out
            val dexHelper = DexHelper(dexClassLoader)
            lastUpdateTime = max(
                getPackageLastUpdateTime(hostPackageName),
                File(modulePath).lastModified()
            )
            clientVersionCode = getVersionCode(hostPackageName)
            moduleVersionCode = BuildConfig.VERSION_CODE
            moduleVersionName = BuildConfig.VERSION_NAME
            baseFragment = BaseFragment().apply {
                val resumeMethod = dexHelper.findMethodUsingString(
                    "[onResume]this[%s]", true
                ).firstOrNull()?.run {
                    dexHelper.findMethodInvoking(
                        this,
                        parameterShorty = "V",
                        findFirst = false
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)?.takeIf { m -> m.isAbstract }
                    }
                } ?: return@apply
                clazz = clazz { name = resumeMethod.declaringClass.name }
                resume = method {
                    name = resumeMethod.name
                    paramTypes = resumeMethod.paramTypes
                }
            }
            splashAdapter = clazz {
                val splashAdapterClass =
                    "com.tencentmusic.ad.adapter.madams.splash.OperExpertSplashAdapter"
                        .from(classLoader) ?: dexHelper.findMethodUsingString(
                        "postSplashResult", true
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@clazz
                name = splashAdapterClass.name
            }
            homePageFragment = HomePageFragment().apply {
                val initTabFragmentMethod = dexHelper.findMethodUsingString(
                    "not support native tab, do nothing",
                ).firstOrNull()?.run {
                    dexHelper.findMethodInvoking(
                        this,
                        parameterShorty = "VI",
                        findFirst = false
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)?.takeIf { m ->
                            m is Method && m.isNotStatic && m.isFinal
                        }
                    }
                } ?: return@apply
                clazz = clazz { name = initTabFragmentMethod.declaringClass.name }
                initTabFragment = method {
                    name = initTabFragmentMethod.name
                    paramTypes = initTabFragmentMethod.paramTypes
                }
            }
            mainDesktopHeader = MainDesktopHeader().apply {
                val clazz = "com.tencent.qqmusic.ui.MainDesktopHeader".from(classLoader)
                    ?: dexHelper.findMethodUsingString("MainDesktopHeader").firstOrNull()?.let {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@apply
                val clazzIndex = dexHelper.encodeClassIndex(clazz)
                val addTabByIdMethod = dexHelper.findMethodUsingString(
                    "iconUrl",
                    declaringClass = clazzIndex,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@apply
                val addTabByNameMethod = dexHelper.findMethodUsingString(
                    "tabName",
                    declaringClass = clazzIndex,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@apply
                val showMusicWorldMethod = dexHelper.findMethodUsingString(
                    "showMusicWorldEntranceBtn",
                    declaringClass = clazzIndex,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                }
                this.clazz = clazz { name = clazz.name }
                addTabById = method {
                    name = addTabByIdMethod.name
                    paramTypes = addTabByIdMethod.paramTypes
                }
                addTabByName = method {
                    name = addTabByNameMethod.name
                    paramTypes = addTabByNameMethod.paramTypes
                }
                showMusicWorldMethod?.let {
                    showMusicWorld = method {
                        name = it.name
                        paramTypes = it.paramTypes
                    }
                }
            }
            adManager = AdManager().apply {
                val onStartIndex = dexHelper.encodeMethodIndex(
                    "com.tencent.qqmusic.activity.AppStarterActivity".from(classLoader)
                        ?.getDeclaredMethod("onStart") ?: return@apply
                )
                val getMethod = dexHelper.findMethodInvoking(
                    onStartIndex,
                    parameterShorty = "L",
                    findFirst = false
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.takeIf { m ->
                        m is Method && m.isNotStatic
                    }
                } ?: return@apply
                clazz = clazz { name = getMethod.declaringClass.name }
                get = method { name = getMethod.name }
            }
            val settingBuilderClass = dexHelper.findMethodUsingString(
                "onSwitchLister can't be null while type is TYPE_SWITCH"
            ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
            val settingClass = settingBuilderClass?.declaringClass
            setting = Setting().apply {
                settingBuilderClass ?: return@apply
                settingClass ?: return@apply
                val withMethod = settingClass.declaredMethods.find {
                    it.isStatic && it.returnType == settingBuilderClass
                } ?: return@apply
                val buildMethod = settingBuilderClass.declaredMethods.find {
                    it.returnType == settingClass
                } ?: return@apply
                val fields = settingClass.declaredFields
                val builderFields = settingBuilderClass.declaredFields
                val switchListenerField = builderFields.find { f ->
                    f.type.let { it.isInterface && it.methods.size == 2 }
                } ?: return@apply
                val clickListenerField = builderFields.find { f ->
                    f.type == View.OnClickListener::class.java
                } ?: return@apply
                val switchListenerClass = switchListenerField.type
                val isSwitchOnMethod = switchListenerClass.methods.find {
                    it.returnType == Boolean::class.javaPrimitiveType
                } ?: return@apply
                val onSwitchStatusChangeMethod = switchListenerClass.methods.find { m ->
                    m.parameterTypes.contentEquals(arrayOf(Boolean::class.javaPrimitiveType))
                } ?: return@apply
                val redDotListenerField = fields.find { f ->
                    f.type.let { t ->
                        t.isInterface && t.methods.size == 1 && t.methods[0].let {
                            it.parameterTypes.isEmpty() && it.returnType == Boolean::class.javaPrimitiveType
                        }
                    }
                } ?: return@apply
                val startPos = fields.indexOfFirst { it.type == Int::class.javaPrimitiveType }
                    .takeIf { it > -1 } ?: return@apply
                clazz = clazz { name = settingClass.name }
                with = method { name = withMethod.name }
                type = field { name = fields[startPos].name }
                title = field { name = fields[startPos + 1].name }
                if (clientVersionCode >= 4958/*13.0.0.8*/) {
                    rightDesc = field { name = fields[startPos + 2].name }
                } else {
                    rightDesc = field { name = fields[startPos + 3].name }
                }
                redDotListener = field { name = redDotListenerField.name }
                builder = SettingBuilder().apply {
                    clazz = clazz { name = settingBuilderClass.name }
                    build = method { name = buildMethod.name }
                    type = field { name = builderFields[0].name }
                    title = field { name = builderFields[1].name }
                    rightDesc = field { name = builderFields[2].name }
                    if (clientVersionCode >= 4958/*13.0.0.8*/) {
                        summary = field { name = builderFields[4].name }
                    } else {
                        summary = field { name = builderFields[5].name }
                    }
                    switchListener = field { name = switchListenerField.name }
                    clickListener = field { name = clickListenerField.name }
                }
                switchListener = SwitchListener().apply {
                    clazz = clazz { name = switchListenerClass.name }
                    isSwitchOn = method { name = isSwitchOnMethod.name }
                    onSwitchStatusChange = method { name = onSwitchStatusChangeMethod.name }
                }

                val debugSettingFragmentClass =
                    "com.tencent.qqmusic.fragment.debug.DebugSettingFragment"
                        .from(classLoader) ?: arrayOf(
                        "DebugMode",
                        "\u4f53\u9a8c\u529f\u80fd\u5f00\u5173"
                    ).firstNotNullOfOrNull { str ->
                        dexHelper.findMethodUsingString(str).firstOrNull()
                            ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                    } ?: return@apply
                val baseSettingFragmentClass =
                    debugSettingFragmentClass.superclass ?: return@apply
                val settingPackMethod = debugSettingFragmentClass.declaredMethods.find { m ->
                    m.returnType.let { it != String::class.java && !it.isPrimitive } && m.parameterTypes.isEmpty()
                } ?: return@apply
                val titleMethod = debugSettingFragmentClass.declaredMethods.find {
                    it.returnType == String::class.java && it.parameterTypes.isEmpty()
                } ?: return@apply
                val baseSettingPackClass = settingPackMethod.returnType
                val createSettingProviderMethod = baseSettingPackClass.methods.find {
                    it.returnType == CopyOnWriteArrayList::class.java && it.isAbstract
                } ?: return@apply
                val hostField = baseSettingPackClass.declaredFields.find {
                    it.type.isInterface && baseSettingPackClass.interfaces.contains(it.type)
                } ?: return@apply
                val baseSettingProviderClass =
                    dexHelper.findMethodUsingString("setting is null").firstOrNull()
                        ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@apply
                val createMethod = baseSettingProviderClass.methods.find {
                    it.returnType == settingClass && it.isAbstract
                } ?: return@apply
                val getSettingMethod = baseSettingProviderClass.declaredMethods.find {
                    it.returnType == settingClass
                } ?: return@apply
                val initKolEnterMethod = dexHelper.findMethodUsingString(
                    "initMusicAccoutEnter: start"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                baseSettingFragment = BaseSettingFragment().apply {
                    clazz = clazz { name = baseSettingFragmentClass.name }
                    settingPackage = method { name = settingPackMethod.name }
                    title = method { name = titleMethod.name }
                }
                baseSettingPack = BaseSettingPack().apply {
                    clazz = clazz { name = baseSettingPackClass.name }
                    createSettingProvider = method {
                        name = createSettingProviderMethod.name
                        paramTypes = createSettingProviderMethod.paramTypes
                    }
                    host = field { name = hostField.name }
                }
                baseSettingProvider = BaseSettingProvider().apply {
                    clazz = clazz { name = baseSettingProviderClass.name }
                    create = method { name = createMethod.name }
                    getSetting = method { name = getSettingMethod.name }
                }
                drawerSettingPack = DrawerSettingPack().apply {
                    clazz = clazz { name = initKolEnterMethod.declaringClass.name }
                    createSettingProvider = baseSettingPack.createSettingProvider
                    initKolEnter = method {
                        name = initKolEnterMethod.name
                        paramTypes = initKolEnterMethod.paramTypes
                    }
                }
            }
            personalEntryView = PersonalEntryView().apply {
                val entryViewClass =
                    "com.tencent.qqmusic.fragment.morefeatures.settings.view.PersonalCenterEntryView"
                        .from(classLoader) ?: dexHelper.findMethodUsingString(
                        "PersonalCenterEntryView",
                    ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                        ?.declaringClass ?: return@apply
                val updateMethod = entryViewClass.declaredMethods.find {
                    it.isSynthetic && it.parameterTypes.size == 1
                } ?: return@apply
                val rightDescViewField = entryViewClass.declaredFields.find {
                    TextView::class.java.isAssignableFrom(it.type)
                } ?: return@apply
                val redDotViewField = entryViewClass.declaredFields.find {
                    ImageView::class.java.isAssignableFrom(it.type)
                } ?: return@apply
                clazz = clazz { name = entryViewClass.name }
                update = method {
                    name = updateMethod.name
                    paramTypes = updateMethod.paramTypes
                }
                rightDescView = field { name = rightDescViewField.name }
                redDotView = field { name = redDotViewField.name }
            }
            bannerTips = me.kofua.qmhelper.data.BannerTips().apply {
                val showStyledToastMethod = dexHelper.findMethodUsingString(
                    "showToast() >>> IS USING QPLAY_AUTO, FORBID BANNER_TIPS",
                    parameterCount = 7,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@apply
                clazz = clazz { name = showStyledToastMethod.declaringClass.name }
                showStyledToast = method { name = showStyledToastMethod.name }
            }
            settingFragment = SettingFragment().apply {
                val settingFragmentClass =
                    "com.tencent.qqmusic.fragment.morefeatures.SettingFeaturesFragment"
                        .from(classLoader) ?: dexHelper.findMethodUsingString(
                        "KEY_SETTING_JUMP_ITEM_ENUM",
                        parameterCount = 1,
                    ).asSequence().firstNotNullOfOrNull {
                        dexHelper.decodeMethodIndex(it)
                    }?.declaringClass ?: return@apply
                val settingListField = settingFragmentClass
                    .findFieldByExactType(List::class.java) ?: return@apply
                clazz = clazz { name = settingFragmentClass.name }
                resume = baseFragment.resume
                settingList = field { name = settingListField.name }
            }
            userInfoHolder = UserInfoHolder().apply {
                val showBubbleMethod =
                    "com.tencent.qqmusic.modular.module.musichall.views.viewholders.cell.MyMusicUserInfoViewHolder"
                        .from(classLoader)?.declaredMethods?.find {
                            it.name == "showTaskAndVipBubble"
                        } ?: run {
                        val clazz = dexHelper.findMethodUsingString("refreshVipIcons")
                            .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                            ?.declaringClass ?: return@apply
                        val clazzIndex = dexHelper.encodeClassIndex(clazz)
                        dexHelper.findMethodUsingString(
                            "3",
                            parameterShorty = "VL",
                            declaringClass = clazzIndex,
                            findFirst = false
                        ).asSequence().firstNotNullOfOrNull {
                            dexHelper.decodeMethodIndex(it)?.takeIf { m -> m.isFinal }
                        }
                    } ?: return@apply
                clazz = clazz { name = showBubbleMethod.declaringClass.name }
                showBubble = method {
                    name = showBubbleMethod.name
                    paramTypes = showBubbleMethod.paramTypes
                }
            }
            strategyModule = StrategyModule().apply {
                val getStrategyIdMethod = dexHelper.findMethodUsingString(
                    "[getClientStrategyStrategyId]: this:"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = getStrategyIdMethod.declaringClass.name }
                getStrategyId = method {
                    name = getStrategyIdMethod.name
                    paramTypes = getStrategyIdMethod.paramTypes
                }
            }
            topAreaDelegate = TopAreaDelegate().apply {
                val initLiveGuideMethod = dexHelper.findMethodUsingString(
                    "[initLiveGuide]"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                val showCurListenMethod = dexHelper.findMethodUsingString(
                    "[showCurrentListen]--block by long audio ad recall entrance show"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                val clazz = initLiveGuideMethod.declaringClass
                val classIndex = dexHelper.encodeClassIndex(clazz)
                val showShareGuideMethod = dexHelper.findMethodUsingString(
                    "ACTION_QQFRIEND",
                    declaringClass = classIndex,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                initLiveGuide = method {
                    name = initLiveGuideMethod.name
                    paramTypes = initLiveGuideMethod.paramTypes
                }
                showCurListenMethod?.let {
                    showCurListen = method {
                        name = it.name
                        paramTypes = it.paramTypes
                    }
                }
                showShareGuide = method {
                    name = showShareGuideMethod.name
                    paramTypes = showShareGuideMethod.paramTypes
                }
            }
            playViewModel = PlayerViewModel().apply {
                val clazz = "com.tencent.qqmusic.business.playernew.viewmodel.PlayerViewModel"
                    .from(classLoader) ?: dexHelper.findMethodUsingString("PlayerViewModel")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                ?: return@apply
                val setCanSlideMethod = clazz.declaredMethods.find { m ->
                    !m.isSynthetic && m.returnType == Void.TYPE
                            && m.parameterTypes.let { it.size == 1 && it[0] == clazz }
                } ?: return@apply
                val postCanSlideMethod = dexHelper.encodeMethodIndex(setCanSlideMethod).run {
                    dexHelper.findMethodInvoking(this, parameterShorty = "VZ")
                        .asSequence().firstNotNullOfOrNull { dexHelper.decodeMethodIndex(it) }
                } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                postCanSlide = method { name = postCanSlideMethod.name }
                setCanSlide = method {
                    name = setCanSlideMethod.name
                    paramTypes = setCanSlideMethod.paramTypes
                }
            }
            spManager = SpManager().apply {
                val clazz = dexHelper.findMethodUsingString("SPManager").firstOrNull()
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@apply
                val getMethod = clazz.declaredMethods.find {
                    it.isStatic && it.returnType == clazz && it.parameterTypes.isEmpty()
                } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                get = method { name = getMethod.name }
            }
            appStarterActivity = AppStarterActivity().apply {
                val doOnCreateMethod = dexHelper
                    .findMethodUsingString("Appstarter activity oncreate")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?: return@apply
                val addSecondFragmentMethod = dexHelper
                    .findMethodUsingString("[addSecondFragment] gotoFirstFragment").firstOrNull()
                    ?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                val appStarterActivityClass = doOnCreateMethod.declaringClass
                val showMessageDialogMethod = appStarterActivityClass.methods.find { m ->
                    Dialog::class.java.isAssignableFrom(m.returnType) && m.parameterTypes.let {
                        it.size == 8 && it[0] == String::class.java && it[7] == Boolean::class.javaPrimitiveType
                    }
                } ?: return@apply
                clazz = clazz { name = appStarterActivityClass.name }
                doOnCreate = method {
                    name = doOnCreateMethod.name
                    paramTypes = doOnCreateMethod.paramTypes
                }
                addSecondFragment = method { name = addSecondFragmentMethod.name }
                showMessageDialog = method { name = showMessageDialogMethod.name }
            }
            modeFragment = clazz {
                name = "com.tencent.qqmusic.fragment.morefeatures.ModeSettingFragment"
                    .from(classLoader)?.name ?: dexHelper.findMethodUsingString(
                    "runningModeSetting"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?.declaringClass?.name ?: return@clazz
            }
            authAgent = AuthAgent().apply {
                val startActionActivityMethod = dexHelper.findMethodUsingString(
                    "LOGIN_CHECK_SDK"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = startActionActivityMethod.declaringClass.name }
                startActionActivity = method {
                    name = startActionActivityMethod.name
                    paramTypes = startActionActivityMethod.paramTypes
                }
            }
            val parseModuleItemMethod = dexHelper.findMethodUsingString(
                "[parseModuleItem] "
            ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) as? Method }
            jsonRespParser = JsonRespParser().apply {
                parseModuleItemMethod ?: return@apply
                clazz = clazz { name = parseModuleItemMethod.declaringClass.name }
                parseModuleItem = method {
                    name = parseModuleItemMethod.name
                    paramTypes = parseModuleItemMethod.paramTypes
                }
            }
            gson = Gson().apply {
                val gsonClass = "com.google.gson.Gson".from(classLoader)
                    ?: dexHelper.findMethodUsingString("GSON cannot serialize ").firstOrNull()
                        ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass ?: return@apply
                val fromJsonMethod = gsonClass.declaredMethods.find { m ->
                    m.parameterTypes.let { it.size == 2 && it[0] == String::class.java && it[1] == java.lang.Class::class.java }
                } ?: return@apply
                val toJsonMethod = gsonClass.declaredMethods.find { m ->
                    m.parameterTypes.let { it.size == 1 && it[0] == Any::class.java } && m.returnType == String::class.java
                } ?: return@apply
                val jsonObjectClass = parseModuleItemMethod?.parameterTypes
                    ?.getOrNull(2) ?: return@apply
                clazz = clazz { name = gsonClass.name }
                fromJson = method { name = fromJsonMethod.name }
                toJson = method { name = toJsonMethod.name }
                jsonObject = clazz { name = jsonObjectClass.name }
            }
            uiModeManager = UiModeManager().apply {
                val clazz = dexHelper.findMethodUsingString("UIModeManager")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                    ?: return@apply
                val method = clazz.declaredMethods.find { m ->
                    m.returnType == Boolean::class.javaPrimitiveType && m.parameterTypes.let { it.size == 1 && it[0] == String::class.java }
                } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                isThemeForbid = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            adBar = ApkDownloadAdBar().apply {
                val clazz = "com.tencent.qqmusic.business.ad.topbarad.apkdownload.ApkDownloadAdBar"
                    .from(classLoader) ?: dexHelper.findMethodUsingString("ApkDownloadAdBar")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                ?: return@apply
                val methods = clazz.interfaces.firstOrNull()?.methods?.filter {
                    it.returnType == Void.TYPE && it.parameterTypes.isEmpty()
                } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                this.methods = methods.map { method { name = it.name } }
            }
            musicWorldTouchListener = clazz {
                name = dexHelper.findMethodUsingString(
                    "MusicWorldEntrance",
                    findFirst = false,
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)?.declaringClass?.takeIf { c ->
                        View.OnTouchListener::class.java.isAssignableFrom(c)
                    }
                }?.name ?: return@clazz
            }
            eKeyManager = AudioStreamEKeyManager().apply {
                val getFileEKeyMethod = dexHelper.findMethodUsingString(
                    "getFileEKey filePath is empty!"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?: return@apply
                val eKeyManagerClass = getFileEKeyMethod.declaringClass
                val instanceField = eKeyManagerClass.declaredFields.find {
                    it.type == eKeyManagerClass
                } ?: return@apply
                clazz = clazz { name = eKeyManagerClass.name }
                instance = field { name = instanceField.name }
                getFileEKey = method { name = getFileEKeyMethod.name }
            }
            eKeyDecryptor = EKeyDecryptor().apply {
                val decryptFileMethod = dexHelper.findMethodUsingString(
                    "decryptFile srcFilePath = "
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                val companionClass = decryptFileMethod.declaringClass
                val decryptorClass = companionClass.declaringClass ?: return@apply
                val instanceField = decryptorClass.declaredFields.find {
                    it.type == companionClass
                } ?: return@apply
                clazz = clazz { name = decryptorClass.name }
                instance = field { name = instanceField.name }
                decryptFile = method { name = decryptFileMethod.name }
            }
            vipDownloadHelper = VipDownloadHelper().apply {
                val decryptFileMethod = dexHelper.findMethodUsingString(
                    "decryptFile create dest = "
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = decryptFileMethod.declaringClass.name }
                decryptFile = method { name = decryptFileMethod.name }
            }
            bottomTipController = BottomTipController().apply {
                val method = dexHelper.findMethodUsingString("updateBottomTips ")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?: return@apply
                clazz = clazz { name = method.declaringClass.name }
                updateBottomTips = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            videoViewDelegate = VideoViewDelegate().apply {
                val method = dexHelper.findMethodUsingString("[onResult] show mv icon.")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?: return@apply
                clazz = clazz { name = method.declaringClass.name }
                onResult = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            genreViewDelegate = GenreViewDelegate().apply {
                val method = dexHelper.findMethodUsingString(
                    "[onBind] hide the song genre tags info."
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = method.declaringClass.name }
                onBind = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            userGuideViewDelegate = UserGuideViewDelegate().apply {
                val method = dexHelper.findMethodUsingString(
                    "showNewUserGuide hasGuideShowing ="
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?: return@apply
                clazz = clazz { name = method.declaringClass.name }
                showUserGuide = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            topSongViewDelegate = TopSongViewDelegate().apply {
                val method = dexHelper.findMethodUsingString(
                    "[onBind] show top song info: peakCount="
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?: return@apply
                clazz = clazz { name = method.declaringClass.name }
                onBind = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            dataPlugin = DataPlugin().apply {
                val handleJsRequestMethod = dexHelper.findMethodUsingString(
                    "[handleJsRequest] writeGlobalData"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                val dataPluginClass = handleJsRequestMethod.declaringClass
                val basePluginClass = dataPluginClass.superclass ?: return@apply
                val runtimeField = basePluginClass.declaredFields
                    .find { it.isPublic } ?: return@apply
                val activityMethod = runtimeField.type.declaredMethods.find {
                    it.parameterTypes.isEmpty() && it.returnType == Activity::class.java
                } ?: return@apply
                clazz = clazz { name = dataPluginClass.name }
                handleJsRequest = method {
                    name = handleJsRequestMethod.name
                    paramTypes = handleJsRequestMethod.paramTypes
                }
                runtime = field { name = runtimeField.name }
                activity = method { name = activityMethod.name }
            }
            albumIntroViewHolder = AlbumIntroViewHolder().apply {
                val c = "com.tencent.qqmusic.albumdetail.ui.viewholders.AlbumIntroduceViewHolder"
                    .from(classLoader) ?: dexHelper.findMethodUsingString("tvAlbumDetail")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass
                ?: return@apply
                val m = c.declaredMethods.find { m ->
                    m.isPublic && m.returnType == Void.TYPE && m.parameterTypes
                        .let { it.size == 1 && it[0] == View::class.java }
                } ?: return@apply
                val tvAlbumDetailField = c.declaredFields.find {
                    it.isNotStatic && TextView::class.java.isAssignableFrom(it.type)
                } ?: return@apply
                val lastTextContentField = c.declaredFields.find {
                    it.isNotStatic && it.type == String::class.java
                } ?: return@apply
                clazz = clazz { name = c.name }
                onHolderCreated = method {
                    name = m.name
                    paramTypes = m.paramTypes
                }
                tvAlbumDetail = field { name = tvAlbumDetailField.name }
                lastTextContent = field { name = lastTextContentField.name }
            }
            albumTagViewHolder = AlbumTagViewHolder().apply {
                val clazz = "com.tencent.qqmusic.albumdetail.ui.viewholders.AlbumTagViewHolder"
                    .from(classLoader)?.name ?: dexHelper.findMethodUsingString("tvAlbumInfo")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?.declaringClass?.name ?: return@apply
                this.clazz = clazz { name = clazz }
                onHolderCreated = albumIntroViewHolder.onHolderCreated
            }
            settingView = SettingView().apply {
                val clazz = "com.tencent.qqmusic.fragment.morefeatures.settings.view.SettingView"
                    .from(classLoader) ?: dexHelper.findMethodUsingString(
                    "initView: titleText "
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    ?.declaringClass ?: return@apply
                val method = clazz.declaredMethods.find { m ->
                    m.parameterTypes.let { it.size == 1 && it[0] == settingClass }
                } ?: return@apply
                val setLastClickTimeMethod = clazz.declaredMethods.find { m ->
                    m.isSynthetic && m.parameterTypes.let { it.size == 2 && it[1] == Long::class.javaPrimitiveType }
                } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                setSetting = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
                setLastClickTime = method {
                    name = setLastClickTimeMethod.name
                    paramTypes = setLastClickTimeMethod.paramTypes
                }
            }
            fileUtils = FileUtils().apply {
                val getSongNameIndex = dexHelper.findMethodUsingString(
                    "[getDownloadSongName] songInfo is null"
                ).firstOrNull() ?: return@apply
                val stringIndex = dexHelper.encodeClassIndex(String::class.java)
                val toValidFilenameMethod = dexHelper.findMethodInvoking(
                    getSongNameIndex,
                    returnType = stringIndex,
                    parameterTypes = longArrayOf(stringIndex, stringIndex, stringIndex),
                ).asSequence().firstNotNullOfOrNull {
                    dexHelper.decodeMethodIndex(it)
                } ?: return@apply
                clazz = clazz { name = toValidFilenameMethod.declaringClass.name }
                toValidFilename = method {
                    name = toValidFilenameMethod.name
                    paramTypes = toValidFilenameMethod.paramTypes
                }
            }
            storageVolume = clazz {
                name = "com.tencent.qqmusiccommon.storage.StorageVolume".from(classLoader)?.name
                    ?: dexHelper.findMethodUsingString("StorageVolume [mStorageId=")
                        .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                        ?.declaringClass?.name ?: return@clazz
            }
            storageUtils = StorageUtils().apply {
                val getVolumesMethod = dexHelper.findMethodUsingString(
                    "StorageVolume From Android API SDK_INT : "
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = getVolumesMethod.declaringClass.name }
                getVolumes = method {
                    name = getVolumesMethod.name
                    paramTypes = getVolumesMethod.paramTypes
                }
            }
            vipAdBarData = clazz {
                name = dexHelper.findMethodUsingString("VipAdBarData(posId=").firstOrNull()
                    ?.let { dexHelper.decodeMethodIndex(it) }?.declaringClass?.name ?: return@clazz
            }
            skinManager = SkinManager().apply {
                val getSkinIdMethod = dexHelper.findMethodUsingString(
                    "[getSyncSkinIdInUse][event:has not update skinIdInUse,use SkinIdToSwitch = %s]"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = getSkinIdMethod.declaringClass.name }
                getSkinId = method { name = getSkinIdMethod.name }
            }
            adResponseData = AdResponseData().apply {
                val classes = dexHelper.findMethodUsingString(
                    "AdResponseData(retCode=",
                    findFirst = false,
                ).map { dexHelper.decodeMethodIndex(it)?.declaringClass }
                    .filterNotNull().ifEmpty { return@apply }
                val getAdsMethods = classes.map { c ->
                    c.declaredMethods.filter { it.returnType == List::class.java && it.parameterTypes.isEmpty() }
                }
                classes.zip(getAdsMethods) { c, ml ->
                    AdResponseDataItem().apply {
                        clazz = clazz { name = c.name }
                        getAds = ml.map { method { name = it.name } }
                    }
                }.let { item = it }
            }
            jceRespConverter = JceResponseConverter().apply {
                val parseMethod = dexHelper.findMethodUsingString(
                    "JceResponseItemConverter"
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = parseMethod.declaringClass.name }
                parse = method {
                    name = parseMethod.name
                    paramTypes = parseMethod.paramTypes
                }
            }
            webRequestHeaders = WebRequestHeaders().apply {
                val getUAMethod = dexHelper.findMethodUsingString(
                    " ReleasedForAndroid["
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                val clazz = getUAMethod.declaringClass
                val getCookiesMethod = clazz.declaredMethods.find { m ->
                    m.returnType == String::class.java && m.parameterTypes.let { it.size == 1 && it[0] == String::class.java }
                } ?: return@apply
                val instanceField = clazz.declaredFields.find { it.type == clazz } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                instance = field { name = instanceField.name }
                getCookies = method { name = getCookiesMethod.name }
                getUA = method { name = getUAMethod.name }
            }
            userManager = UserManager().apply {
                val getMusicUinMethod = dexHelper.findMethodUsingString("getMusicUin")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                val clazz = getMusicUinMethod.declaringClass
                val getMethod = clazz.declaredMethods.find {
                    it.isStatic && it.returnType == clazz && it.parameterTypes.isEmpty()
                } ?: return@apply
                val clazzIndex = dexHelper.encodeClassIndex(clazz)
                val isLoginMethod = dexHelper.findMethodUsingString(
                    "isLogin",
                    declaringClass = clazzIndex
                ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                this.clazz = clazz { name = clazz.name }
                get = method { name = getMethod.name }
                getMusicUin = method { name = getMusicUinMethod.name }
                isLogin = method { name = isLoginMethod.name }
            }
            bannerManager = BannerManager().apply {
                val method = dexHelper.findMethodUsingString("[requestAdvertisement] start")
                    .firstOrNull()?.let { dexHelper.decodeMethodIndex(it) } ?: return@apply
                clazz = clazz { name = method.declaringClass.name }
                requestAd = method {
                    name = method.name
                    paramTypes = method.paramTypes
                }
            }
            musicWorldPullEntrance = MusicWorldPullEntrance().apply {
                var clazz =
                    "com.tencent.qqmusic.fragment.mymusic.my.musicworld.MusicWorldPullEntrance"
                        .from(classLoader)
                val onClickListenerClass = View.OnClickListener::class.java
                var showButtonMethod: Member? = clazz?.declaredMethods?.find { m ->
                    m.parameterTypes.let { it.size == 1 && it[0] == onClickListenerClass }
                            && m.returnType == Void.TYPE
                }
                if (showButtonMethod == null) {
                    showButtonMethod = dexHelper.findMethodUsingString(
                        "[showMusicWorldEntranceBtn]",
                        parameterTypes = longArrayOf(dexHelper.encodeClassIndex(onClickListenerClass))
                    ).firstOrNull()?.let { dexHelper.decodeMethodIndex(it) }
                    clazz = showButtonMethod?.declaringClass ?: return@apply
                }
                clazz?.let { this.clazz = clazz { name = it.name } }
                showButton = method {
                    name = showButtonMethod.name
                    paramTypes = showButtonMethod.paramTypes
                }
            }

            dexHelper.close()
        }
    }
}
