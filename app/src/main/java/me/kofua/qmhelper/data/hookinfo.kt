@file:Suppress("UNCHECKED_CAST")

package me.kofua.qmhelper.data

import java.io.ObjectInput
import java.io.Serializable

fun <T> ObjectInput.readAny() = readObject() as T

fun clazz(action: Class.() -> Unit) = Class().apply(action)
fun method(action: Method.() -> Unit) = Method().apply(action)
fun field(action: Field.() -> Unit) = Field().apply(action)

val defClazz = clazz { }
val defMethod = method { }
val defField = field { }

sealed class Element(var name: String = "") : Serializable
class Class : Element()
class Field : Element()
class Method(var paramTypes: Array<String> = arrayOf()) : Element()

sealed class ClassInfo(var clazz: Class = defClazz) : Serializable

class BaseFragment(var resume: Method = defMethod) : ClassInfo()

class HomePageFragment(var initTabFragment: Method = defMethod) : ClassInfo()

class MainDesktopHeader : ClassInfo() {
    var addTabById: Method = defMethod
    var addTabByName: Method = defMethod
    var showMusicWorld: Method = defMethod
}

class AdManager(var get: Method = defMethod) : ClassInfo()

class Setting : ClassInfo() {
    var with: Method = defMethod
    var type: Field = defField
    var title: Field = defField
    var rightDesc: Field = defField
    var redDotListener: Field = defField
    var builder: SettingBuilder = SettingBuilder()
    var switchListener: SwitchListener = SwitchListener()
    var baseSettingFragment: BaseSettingFragment = BaseSettingFragment()
    var baseSettingPack: BaseSettingPack = BaseSettingPack()
    var baseSettingProvider: BaseSettingProvider = BaseSettingProvider()
    var drawerSettingPack: DrawerSettingPack = DrawerSettingPack()
}

class SettingBuilder : ClassInfo() {
    var build: Method = defMethod
    var type: Field = defField
    var title: Field = defField
    var rightDesc: Field = defField
    var summary: Field = defField
    var switchListener: Field = defField
    var clickListener: Field = defField
}

class SwitchListener : ClassInfo() {
    var isSwitchOn: Method = defMethod
    var onSwitchStatusChange: Method = defMethod
}

class BaseSettingFragment : ClassInfo() {
    var settingPackage: Method = defMethod
    var title: Method = defMethod
}

class BaseSettingPack : ClassInfo() {
    var createSettingProvider: Method = defMethod
    var host: Field = defField
}

class BaseSettingProvider : ClassInfo() {
    var create: Method = defMethod
    var getSetting: Method = defMethod
}

class DrawerSettingPack : ClassInfo() {
    var createSettingProvider: Method = defMethod
    var initKolEnter: Method = defMethod
}

class PersonalEntryView : ClassInfo() {
    var update: Method = defMethod
    var rightDescView: Field = defField
    var redDotView: Field = defField
}

class BannerTips(var showStyledToast: Method = defMethod) : ClassInfo()

class SettingFragment : ClassInfo() {
    var resume: Method = defMethod
    var settingList: Field = defField
}

class UserInfoHolder(var showBubble: Method = defMethod) : ClassInfo()

class StrategyModule(var getStrategyId: Method = defMethod) : ClassInfo()

class TopAreaDelegate : ClassInfo() {
    var initLiveGuide: Method = defMethod
    var showCurListen: Method = defMethod
    var showShareGuide: Method = defMethod
}

class PlayerViewModel : ClassInfo() {
    var postCanSlide: Method = defMethod
    var setCanSlide: Method = defMethod
}

class SpManager(var get: Method = defMethod) : ClassInfo()

class AppStarterActivity : ClassInfo() {
    var doOnCreate: Method = defMethod
    var addSecondFragment: Method = defMethod
    var showMessageDialog: Method = defMethod
}

class AuthAgent(var startActionActivity: Method = defMethod) : ClassInfo()

class JsonRespParser(var parseModuleItem: Method = defMethod) : ClassInfo()

class Gson : ClassInfo() {
    var fromJson: Method = defMethod
    var toJson: Method = defMethod
    var jsonObject: Class = defClazz
}

class UiModeManager(var isThemeForbid: Method = defMethod) : ClassInfo()

class ApkDownloadAdBar(var methods: List<Method> = listOf()) : ClassInfo()

class AudioStreamEKeyManager : ClassInfo() {
    var instance: Field = defField
    var getFileEKey: Method = defMethod
}

class EKeyDecryptor : ClassInfo() {
    var instance: Field = defField
    var decryptFile: Method = defMethod
}

class VipDownloadHelper(var decryptFile: Method = defMethod) : ClassInfo()

class BottomTipController(var updateBottomTips: Method = defMethod) : ClassInfo()

class VideoViewDelegate(var onResult: Method = defMethod) : ClassInfo()

class GenreViewDelegate(var onBind: Method = defMethod) : ClassInfo()

class UserGuideViewDelegate(var showUserGuide: Method = defMethod) : ClassInfo()

class TopSongViewDelegate(var onBind: Method = defMethod) : ClassInfo()

class DataPlugin : ClassInfo() {
    var handleJsRequest: Method = defMethod
    var runtime: Field = defField
    var activity: Method = defMethod
}

class AlbumIntroViewHolder : ClassInfo() {
    var onHolderCreated: Method = defMethod
    var tvAlbumDetail: Field = defField
    var lastTextContent: Field = defField
}

class AlbumTagViewHolder(var onHolderCreated: Method = defMethod) : ClassInfo()

class SettingView : ClassInfo() {
    var setSetting: Method = defMethod
    var setLastClickTime: Method = defMethod
}

class FileUtils(var toValidFilename: Method = defMethod) : ClassInfo()

class StorageUtils(var getVolumes: Method = defMethod) : ClassInfo()

class SkinManager(var getSkinId: Method = defMethod) : ClassInfo()

class AdResponseDataItem(var getAds: List<Method> = listOf()) : ClassInfo()

class AdResponseData(var item: List<AdResponseDataItem> = listOf()) : Serializable

class JceResponseConverter(var parse: Method = defMethod) : ClassInfo()

class WebRequestHeaders : ClassInfo() {
    var instance: Field = defField
    var getCookies: Method = defMethod
    var getUA: Method = defMethod
}

class UserManager : ClassInfo() {
    var get: Method = defMethod
    var getMusicUin: Method = defMethod
    var isLogin: Method = defMethod
}

class BannerManager(var requestAd: Method = defMethod) : ClassInfo()

class MusicWorldPullEntrance(var showButton: Method = defMethod) : ClassInfo()

class HookInfo : Serializable {
    var lastUpdateTime: Long = 0L
    var clientVersionCode: Int = 0
    var moduleVersionCode: Int = 0
    var moduleVersionName: String = ""
    var baseFragment: BaseFragment = BaseFragment()
    var splashAdapter: Class = defClazz
    var homePageFragment: HomePageFragment = HomePageFragment()
    var mainDesktopHeader: MainDesktopHeader = MainDesktopHeader()
    var adManager: AdManager = AdManager()
    var setting: Setting = Setting()
    var personalEntryView: PersonalEntryView = PersonalEntryView()
    var bannerTips: BannerTips = BannerTips()
    var settingFragment: SettingFragment = SettingFragment()
    var userInfoHolder: UserInfoHolder = UserInfoHolder()
    var strategyModule: StrategyModule = StrategyModule()
    var topAreaDelegate: TopAreaDelegate = TopAreaDelegate()
    var playViewModel: PlayerViewModel = PlayerViewModel()
    var spManager: SpManager = SpManager()
    var appStarterActivity: AppStarterActivity = AppStarterActivity()
    var modeFragment: Class = defClazz
    var authAgent: AuthAgent = AuthAgent()
    var jsonRespParser: JsonRespParser = JsonRespParser()
    var gson: Gson = Gson()
    var uiModeManager: UiModeManager = UiModeManager()
    var adBar: ApkDownloadAdBar = ApkDownloadAdBar()
    var musicWorldTouchListener: Class = defClazz
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
    var albumTagViewHolder: AlbumTagViewHolder = AlbumTagViewHolder()
    var settingView: SettingView = SettingView()
    var fileUtils: FileUtils = FileUtils()
    var storageVolume: Class = defClazz
    var storageUtils: StorageUtils = StorageUtils()
    var vipAdBarData: Class = defClazz
    var skinManager: SkinManager = SkinManager()
    var adResponseData: AdResponseData = AdResponseData()
    var jceRespConverter: JceResponseConverter = JceResponseConverter()
    var webRequestHeaders: WebRequestHeaders = WebRequestHeaders()
    var userManager: UserManager = UserManager()
    var bannerManager: BannerManager = BannerManager()
    var musicWorldPullEntrance: MusicWorldPullEntrance = MusicWorldPullEntrance()
}
