@file:Suppress("UNCHECKED_CAST")

package me.kofua.qmhelper.data

import java.io.ObjectInput
import java.io.Serializable

fun <T> ObjectInput.readAny() = readObject() as T

fun clazz(action: Class.() -> Unit) = Class().apply(action)
fun method(action: Method.() -> Unit) = Method().apply(action)
fun field(action: Field.() -> Unit) = Field().apply(action)

sealed class Element(var name: String = "") : Serializable
class Class : Element()
class Field : Element()
class Method(var paramTypes: Array<String> = arrayOf()) : Element()

open class ClassInfo(var clazz: Class = clazz { }) : Serializable

class BaseFragment(var resume: Method = method { }) : ClassInfo()

class HomePageFragment(var initTabFragment: Method = method { }) : ClassInfo()

class MainDesktopHeader : ClassInfo() {
    var addTabById: Method = method { }
    var addTabByName: Method = method { }
    var showMusicWorld: Method = method { }
}

class AdManager(var get: Method = method { }) : ClassInfo()

class Setting : ClassInfo() {
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
}

class SettingBuilder : ClassInfo() {
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
}

class SwitchListener : ClassInfo() {
    var isSwitchOn: Method = method { }
    var onSwitchStatusChange: Method = method { }
}

class BaseSettingFragment : ClassInfo() {
    var settingPackage: Method = method { }
    var title: Method = method { }
}

class BaseSettingPack : ClassInfo() {
    var createSettingProvider: Method = method { }
    var host: Field = field { }
}

class BaseSettingProvider : ClassInfo() {
    var create: Method = method { }
    var getSetting: Method = method { }
}

class DrawerSettingPackage(var initKolEnter: Method = method { }) : ClassInfo()

class PersonalEntryView : ClassInfo() {
    var update: Method = method { }
    var rightDescView: Field = field { }
    var redDotView: Field = field { }
}

class BannerTips(var showStyledToast: Method = method { }) : ClassInfo()

class SettingFragment(var settingList: Field = field { }) : ClassInfo()

class UserInfoHolder(var showBubble: Method = method { }) : ClassInfo()

class BaseABTester : ClassInfo() {
    var getProperty: Method = method { }
    var strategyModule: Class = clazz {}
    var getStrategyId: Method = method { }
}

class TopAreaDelegate : ClassInfo() {
    var initLiveGuide: Method = method { }
    var showCurListen: Method = method { }
    var showShareGuide: Method = method { }
}

class PlayerViewModel(var setCanSlide: Method = method { }) : ClassInfo()

class SpManager(var get: Method = method { }) : ClassInfo()

class AppStarterActivity : ClassInfo() {
    var doOnCreate: Method = method { }
    var addSecondFragment: Method = method { }
    var showMessageDialog: Method = method { }
}

class AuthAgent(var startActionActivity: Method = method { }) : ClassInfo()

class JsonRespParser(var parseModuleItem: Method = method { }) : ClassInfo()

class Gson : ClassInfo() {
    var fromJson: Method = method { }
    var toJson: Method = method { }
    var jsonObject: Class = clazz { }
}

class UiModeManager(var isThemeForbid: Method = method { }) : ClassInfo()

class ApkDownloadAdBar(var methods: List<Method> = listOf()) : ClassInfo()

class AudioStreamEKeyManager : ClassInfo() {
    var instance: Field = field { }
    var getFileEKey: Method = method { }
}

class EKeyDecryptor : ClassInfo() {
    var instance: Field = field { }
    var decryptFile: Method = method { }
}

class VipDownloadHelper(var decryptFile: Method = method { }) : ClassInfo()

class BottomTipController(var updateBottomTips: Method = method { }) : ClassInfo()

class VideoViewDelegate(var onResult: Method = method { }) : ClassInfo()

class GenreViewDelegate(var onBind: Method = method { }) : ClassInfo()

class UserGuideViewDelegate(var showUserGuide: Method = method { }) : ClassInfo()

class TopSongViewDelegate(var onBind: Method = method { }) : ClassInfo()

class DataPlugin : ClassInfo() {
    var handleJsRequest: Method = method { }
    var runtime: Field = field { }
    var activity: Method = method { }
}

class AlbumIntroViewHolder : ClassInfo() {
    var onHolderCreated: Method = method { }
    var tvAlbumDetail: Field = field { }
    var lastTextContent: Field = field { }
}

class SettingView : ClassInfo() {
    var setSetting: Method = method { }
    var setLastClickTime: Method = method { }
}

class FileUtils(var toValidFilename: Method = method { }) : ClassInfo()

class StorageUtils(var getVolumes: Method = method { }) : ClassInfo()

class SkinManager(var getSkinId: Method = method { }) : ClassInfo()

class AdResponseDataItem(var getAds: List<Method> = listOf()) : ClassInfo()

class AdResponseData(var item: List<AdResponseDataItem> = listOf()) : Serializable

class HookInfo : Serializable {
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
}
