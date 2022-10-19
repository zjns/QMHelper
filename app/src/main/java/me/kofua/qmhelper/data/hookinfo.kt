@file:Suppress("UNCHECKED_CAST")

package me.kofua.qmhelper.data

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

fun <T> ObjectInput.readAny() = readObject() as T

fun clazz(action: Class.() -> Unit) = Class().apply(action)
fun method(action: Method.() -> Unit) = Method().apply(action)
fun field(action: Field.() -> Unit) = Field().apply(action)

sealed class Element : Externalizable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var name: String = ""

    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(name)
    }

    override fun readExternal(`in`: ObjectInput) {
        name = `in`.readAny()
    }
}

class Class : Element() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

class Field : Element() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

class Method : Element() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var paramTypes = arrayOf<String>()

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(paramTypes)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        paramTypes = `in`.readAny()
    }
}

open class ClassInfo : Externalizable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var clazz: Class = clazz { }

    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(clazz)
    }

    override fun readExternal(`in`: ObjectInput) {
        clazz = `in`.readAny()
    }
}

class BaseFragment : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var resume: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(resume)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        resume = `in`.readAny()
    }
}

class HomePageFragment : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var initTabFragment: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(initTabFragment)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        initTabFragment = `in`.readAny()
    }
}

class MainDesktopHeader : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var addTabById: Method = method { }
    var addTabByName: Method = method { }
    var showMusicWorld: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(addTabById)
        out.writeObject(addTabByName)
        out.writeObject(showMusicWorld)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        addTabById = `in`.readAny()
        addTabByName = `in`.readAny()
        showMusicWorld = `in`.readAny()
    }
}

class AdManager : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var get: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(get)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        get = `in`.readAny()
    }
}

class Setting : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var with: Method = method { }
    var type: Field = field { }
    var title: Field = field { }
    var rightDesc: Field = field { }
    var redDotListener: Field = field { }
    var builder: SettingBuilder = SettingBuilder()
    var switchListener: SwitchListener = SwitchListener()
    var baseSettingFragment: BaseSettingFragment = BaseSettingFragment()
    var baseSettingPack: BaseSettingPack = BaseSettingPack()
    var baseSettingProvider: BaseSettingProvider = BaseSettingProvider()
    var drawerSettingPack: DrawerSettingPackage = DrawerSettingPackage()

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(with)
        out.writeObject(type)
        out.writeObject(title)
        out.writeObject(rightDesc)
        out.writeObject(redDotListener)
        out.writeObject(builder)
        out.writeObject(switchListener)
        out.writeObject(baseSettingFragment)
        out.writeObject(baseSettingPack)
        out.writeObject(baseSettingProvider)
        out.writeObject(drawerSettingPack)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        with = `in`.readAny()
        type = `in`.readAny()
        title = `in`.readAny()
        rightDesc = `in`.readAny()
        redDotListener = `in`.readAny()
        builder = `in`.readAny()
        switchListener = `in`.readAny()
        baseSettingFragment = `in`.readAny()
        baseSettingPack = `in`.readAny()
        baseSettingProvider = `in`.readAny()
        drawerSettingPack = `in`.readAny()
    }
}

class SettingBuilder : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var build: Method = method { }
    var type: Field = field { }
    var title: Field = field { }
    var rightDesc: Field = field { }
    var dotRightDesc: Field = field { }
    var summary: Field = field { }
    var switchListener: Field = field { }
    var tag: Field = field { }
    var redDot: Field = field { }
    var enabled: Field = field { }
    var clickListener: Field = field { }
    var touchListener: Field = field { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(build)
        out.writeObject(type)
        out.writeObject(title)
        out.writeObject(rightDesc)
        out.writeObject(dotRightDesc)
        out.writeObject(summary)
        out.writeObject(switchListener)
        out.writeObject(tag)
        out.writeObject(redDot)
        out.writeObject(enabled)
        out.writeObject(clickListener)
        out.writeObject(touchListener)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        build = `in`.readAny()
        type = `in`.readAny()
        title = `in`.readAny()
        rightDesc = `in`.readAny()
        dotRightDesc = `in`.readAny()
        summary = `in`.readAny()
        switchListener = `in`.readAny()
        tag = `in`.readAny()
        redDot = `in`.readAny()
        enabled = `in`.readAny()
        clickListener = `in`.readAny()
        touchListener = `in`.readAny()
    }
}

class SwitchListener : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var isSwitchOn: Method = method { }
    var onSwitchStatusChange: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(isSwitchOn)
        out.writeObject(onSwitchStatusChange)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        isSwitchOn = `in`.readAny()
        onSwitchStatusChange = `in`.readAny()
    }
}

class BaseSettingFragment : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var settingPackage: Method = method { }
    var title: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(settingPackage)
        out.writeObject(title)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        settingPackage = `in`.readAny()
        title = `in`.readAny()
    }
}

class BaseSettingPack : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var createSettingProvider: Method = method { }
    var host: Field = field { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(createSettingProvider)
        out.writeObject(host)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        createSettingProvider = `in`.readAny()
        host = `in`.readAny()
    }
}

class BaseSettingProvider : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var create: Method = method { }
    var getSetting: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(create)
        out.writeObject(getSetting)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        create = `in`.readAny()
        getSetting = `in`.readAny()
    }
}

class DrawerSettingPackage : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var initKolEnter: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(initKolEnter)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        initKolEnter = `in`.readAny()
    }
}

class PersonalEntryView : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var update: Method = method { }
    var rightDescView: Field = field { }
    var redDotView: Field = field { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(update)
        out.writeObject(rightDescView)
        out.writeObject(redDotView)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        update = `in`.readAny()
        rightDescView = `in`.readAny()
        redDotView = `in`.readAny()
    }
}

class BannerTips : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var showStyledToast: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(showStyledToast)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        showStyledToast = `in`.readAny()
    }
}

class SettingFragment : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var settingList: Field = field { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(settingList)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        settingList = `in`.readAny()
    }
}

class UserInfoHolder : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var showBubble: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(showBubble)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        showBubble = `in`.readAny()
    }
}

class BaseABTester : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var getProperty: Method = method { }
    var strategyModule: Class = clazz {}
    var getStrategyId: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(getProperty)
        out.writeObject(strategyModule)
        out.writeObject(getStrategyId)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        getProperty = `in`.readAny()
        strategyModule = `in`.readAny()
        getStrategyId = `in`.readAny()
    }
}

class TopAreaDelegate : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var initLiveGuide: Method = method { }
    var showCurListen: Method = method { }
    var showShareGuide: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(initLiveGuide)
        out.writeObject(showCurListen)
        out.writeObject(showShareGuide)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        initLiveGuide = `in`.readAny()
        showCurListen = `in`.readAny()
        showShareGuide = `in`.readAny()
    }
}

class PlayerViewModel : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var setCanSlide: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(setCanSlide)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        setCanSlide = `in`.readAny()
    }
}

class SpManager : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var get: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(get)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        get = `in`.readAny()
    }
}

class AppStarterActivity : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var doOnCreate: Method = method { }
    var addSecondFragment: Method = method { }
    var showMessageDialog: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(doOnCreate)
        out.writeObject(addSecondFragment)
        out.writeObject(showMessageDialog)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        doOnCreate = `in`.readAny()
        addSecondFragment = `in`.readAny()
        showMessageDialog = `in`.readAny()
    }
}

class AuthAgent : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var startActionActivity: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(startActionActivity)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        startActionActivity = `in`.readAny()
    }
}

class JsonRespParser : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var parseModuleItem: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(parseModuleItem)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        parseModuleItem = `in`.readAny()
    }
}

class Gson : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var fromJson: Method = method { }
    var toJson: Method = method { }
    var jsonObject: Class = clazz { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(fromJson)
        out.writeObject(toJson)
        out.writeObject(jsonObject)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        fromJson = `in`.readAny()
        toJson = `in`.readAny()
        jsonObject = `in`.readAny()
    }
}

class UiModeManager : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var isThemeForbid: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(isThemeForbid)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        isThemeForbid = `in`.readAny()
    }
}

class ApkDownloadAdBar : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var methods: List<Method> = listOf()

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(methods)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        methods = `in`.readAny()
    }
}

class AudioStreamEKeyManager : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var instance: Field = field { }
    var getFileEKey: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(instance)
        out.writeObject(getFileEKey)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        instance = `in`.readAny()
        getFileEKey = `in`.readAny()
    }
}

class EKeyDecryptor : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var instance: Field = field { }
    var decryptFile: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(instance)
        out.writeObject(decryptFile)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        instance = `in`.readAny()
        decryptFile = `in`.readAny()
    }
}

class VipDownloadHelper : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var decryptFile: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(decryptFile)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        decryptFile = `in`.readAny()
    }
}

class BottomTipController : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var updateBottomTips: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(updateBottomTips)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        updateBottomTips = `in`.readAny()
    }
}

class VideoViewDelegate : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var onResult: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(onResult)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        onResult = `in`.readAny()
    }
}

class GenreViewDelegate : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var onBind: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(onBind)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        onBind = `in`.readAny()
    }
}

class UserGuideViewDelegate : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var showUserGuide: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(showUserGuide)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        showUserGuide = `in`.readAny()
    }
}

class TopSongViewDelegate : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var onBind: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(onBind)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        onBind = `in`.readAny()
    }
}

class DataPlugin : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var handleJsRequest: Method = method { }
    var runtime: Field = field { }
    var activity: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(handleJsRequest)
        out.writeObject(runtime)
        out.writeObject(activity)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        handleJsRequest = `in`.readAny()
        runtime = `in`.readAny()
        activity = `in`.readAny()
    }
}

class AlbumIntroViewHolder : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var onHolderCreated: Method = method { }
    var tvAlbumDetail: Field = field { }
    var lastTextContent: Field = field { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(onHolderCreated)
        out.writeObject(tvAlbumDetail)
        out.writeObject(lastTextContent)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        onHolderCreated = `in`.readAny()
        tvAlbumDetail = `in`.readAny()
        lastTextContent = `in`.readAny()
    }
}

class SettingView : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var setSetting: Method = method { }
    var setLastClickTime: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(setSetting)
        out.writeObject(setLastClickTime)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        setSetting = `in`.readAny()
        setLastClickTime = `in`.readAny()
    }
}

class FileUtils : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var toValidFilename: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(toValidFilename)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        toValidFilename = `in`.readAny()
    }
}

class StorageUtils : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var getVolumes: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(getVolumes)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        getVolumes = `in`.readAny()
    }
}

class SkinManager : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var getSkinId: Method = method { }

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(getSkinId)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        getSkinId = `in`.readAny()
    }
}

class AdResponseDataItem : ClassInfo() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var getAds: List<Method> = listOf()

    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)
        out.writeObject(getAds)
    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)
        getAds = `in`.readAny()
    }
}

class AdResponseData : Externalizable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var item: List<AdResponseDataItem> = listOf()

    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(item)
    }

    override fun readExternal(`in`: ObjectInput) {
        item = `in`.readAny()
    }
}

class HookInfo : Externalizable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    var lastUpdateTime: Long = 0L
    var clientVersionCode: Int = 0
    var moduleVersionCode: Int = 0
    var moduleVersionName: String = ""
    var baseFragment: BaseFragment = BaseFragment()
    var splashAdapter: Class = clazz { }
    var homePageFragment: HomePageFragment = HomePageFragment()
    var mainDesktopHeader: MainDesktopHeader = MainDesktopHeader()
    var adManager: AdManager = AdManager()
    var setting: Setting = Setting()
    var personalEntryView: PersonalEntryView = PersonalEntryView()
    var bannerTips: BannerTips = BannerTips()
    var settingFragment: SettingFragment = SettingFragment()
    var userInfoHolder: UserInfoHolder = UserInfoHolder()
    var abTester: BaseABTester = BaseABTester()
    var topAreaDelegate: TopAreaDelegate = TopAreaDelegate()
    var playViewModel: PlayerViewModel = PlayerViewModel()
    var spManager: SpManager = SpManager()
    var appStarterActivity: AppStarterActivity = AppStarterActivity()
    var modeFragment: Class = clazz { }
    var authAgent: AuthAgent = AuthAgent()
    var jsonRespParser: JsonRespParser = JsonRespParser()
    var gson: Gson = Gson()
    var uiModeManager: UiModeManager = UiModeManager()
    var adBar: ApkDownloadAdBar = ApkDownloadAdBar()
    var musicWorldTouchListener: Class = clazz { }
    var eKeyManager: AudioStreamEKeyManager = AudioStreamEKeyManager()
    var eKeyDecryptor: EKeyDecryptor = EKeyDecryptor()
    var vipDownloadHelper: VipDownloadHelper = VipDownloadHelper()
    var bottomTipController: BottomTipController = BottomTipController()
    var videoViewDelegate: VideoViewDelegate = VideoViewDelegate()
    var genreViewDelegate: GenreViewDelegate = GenreViewDelegate()
    var userGuideViewDelegate: UserGuideViewDelegate = UserGuideViewDelegate()
    var topSongViewDelegate: TopSongViewDelegate = TopSongViewDelegate()
    var dataPlugin: DataPlugin = DataPlugin()
    var albumIntroViewHolder: AlbumIntroViewHolder = AlbumIntroViewHolder()
    var albumTagViewHolder: Class = clazz { }
    var settingView: SettingView = SettingView()
    var fileUtils: FileUtils = FileUtils()
    var storageVolume: Class = clazz { }
    var storageUtils: StorageUtils = StorageUtils()
    var vipAdBarData: Class = clazz { }
    var skinManager: SkinManager = SkinManager()
    var adResponseData: AdResponseData = AdResponseData()

    override fun writeExternal(out: ObjectOutput) {
        out.writeLong(lastUpdateTime)
        out.writeInt(clientVersionCode)
        out.writeInt(moduleVersionCode)
        out.writeObject(moduleVersionName)
        out.writeObject(baseFragment)
        out.writeObject(splashAdapter)
        out.writeObject(homePageFragment)
        out.writeObject(mainDesktopHeader)
        out.writeObject(adManager)
        out.writeObject(setting)
        out.writeObject(personalEntryView)
        out.writeObject(bannerTips)
        out.writeObject(settingFragment)
        out.writeObject(userInfoHolder)
        out.writeObject(abTester)
        out.writeObject(topAreaDelegate)
        out.writeObject(playViewModel)
        out.writeObject(spManager)
        out.writeObject(appStarterActivity)
        out.writeObject(modeFragment)
        out.writeObject(authAgent)
        out.writeObject(jsonRespParser)
        out.writeObject(gson)
        out.writeObject(uiModeManager)
        out.writeObject(adBar)
        out.writeObject(musicWorldTouchListener)
        out.writeObject(eKeyManager)
        out.writeObject(eKeyDecryptor)
        out.writeObject(vipDownloadHelper)
        out.writeObject(bottomTipController)
        out.writeObject(videoViewDelegate)
        out.writeObject(genreViewDelegate)
        out.writeObject(userGuideViewDelegate)
        out.writeObject(topSongViewDelegate)
        out.writeObject(dataPlugin)
        out.writeObject(albumIntroViewHolder)
        out.writeObject(albumTagViewHolder)
        out.writeObject(settingView)
        out.writeObject(fileUtils)
        out.writeObject(storageVolume)
        out.writeObject(storageUtils)
        out.writeObject(vipAdBarData)
        out.writeObject(skinManager)
        out.writeObject(adResponseData)
    }

    override fun readExternal(`in`: ObjectInput) {
        lastUpdateTime = `in`.readLong()
        clientVersionCode = `in`.readInt()
        moduleVersionCode = `in`.readInt()
        moduleVersionName = `in`.readAny()
        baseFragment = `in`.readAny()
        splashAdapter = `in`.readAny()
        homePageFragment = `in`.readAny()
        mainDesktopHeader = `in`.readAny()
        adManager = `in`.readAny()
        setting = `in`.readAny()
        personalEntryView = `in`.readAny()
        bannerTips = `in`.readAny()
        settingFragment = `in`.readAny()
        userInfoHolder = `in`.readAny()
        abTester = `in`.readAny()
        topAreaDelegate = `in`.readAny()
        playViewModel = `in`.readAny()
        spManager = `in`.readAny()
        appStarterActivity = `in`.readAny()
        modeFragment = `in`.readAny()
        authAgent = `in`.readAny()
        jsonRespParser = `in`.readAny()
        gson = `in`.readAny()
        uiModeManager = `in`.readAny()
        adBar = `in`.readAny()
        musicWorldTouchListener = `in`.readAny()
        eKeyManager = `in`.readAny()
        eKeyDecryptor = `in`.readAny()
        vipDownloadHelper = `in`.readAny()
        bottomTipController = `in`.readAny()
        videoViewDelegate = `in`.readAny()
        genreViewDelegate = `in`.readAny()
        userGuideViewDelegate = `in`.readAny()
        topSongViewDelegate = `in`.readAny()
        dataPlugin = `in`.readAny()
        albumIntroViewHolder = `in`.readAny()
        albumTagViewHolder = `in`.readAny()
        settingView = `in`.readAny()
        fileUtils = `in`.readAny()
        storageVolume = `in`.readAny()
        storageUtils = `in`.readAny()
        vipAdBarData = `in`.readAny()
        skinManager = `in`.readAny()
        adResponseData = `in`.readAny()
    }
}
