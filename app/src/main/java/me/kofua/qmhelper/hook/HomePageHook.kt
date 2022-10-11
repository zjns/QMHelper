package me.kofua.qmhelper.hook

import android.view.MotionEvent
import android.view.View
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.callMethod
import me.kofua.qmhelper.utils.hookAfterAllConstructors
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.replaceMethod
import me.kofua.qmhelper.utils.sPrefs

class HomePageHook(classLoader: ClassLoader) : BaseHook(classLoader) {

    override fun startHook() {
        if (sPrefs.getBoolean("hide_music_world", false)) {
            instance.mainDesktopHeaderClass?.declaredMethods?.find {
                it.name == instance.showMusicWorld()
            }?.replaceMethod { null }
        }
        if (sPrefs.getBoolean("hide_vip_bubble", false)) {
            instance.userInfoHolderClass?.declaredMethods?.find {
                it.name == instance.showBubble()
            }?.replaceMethod { null }
            instance.vipAdBarDataClass?.declaredConstructors
                ?.find { m -> m.parameterTypes.let { it.size == 10 && it[8] == Boolean::class.javaPrimitiveType } }
                ?.hookBeforeMethod { it.args[8] = true }
        }
        if (sPrefs.getBoolean("purify_live_guide", false)) {
            instance.topAreaDelegateClass?.run {
                declaredMethods.find { it.name == instance.initLiveGuide() && it.parameterTypes.isEmpty() }
                    ?.replaceMethod { null }
                declaredMethods.find { it.name == instance.showCurListen() && it.parameterTypes.size == 1 }
                    ?.replaceMethod { null }
            }
        }
        if (sPrefs.getBoolean("forbid_slide", false)) {
            instance.playViewModelClass?.hookBeforeMethod(
                instance.setCanSlide(),
                Boolean::class.javaPrimitiveType
            ) { it.args[0] = false }
        }
        if (sPrefs.getBoolean("hide_ad_bar", false)) {
            instance.adBarClass?.run {
                hookAfterAllConstructors { param ->
                    val view = param.thisObject as View
                    view.visibility = View.GONE
                }
                instance.adBarMethods.takeIf { it.size == 2 } ?: return@run
                val methodA = instance.adBarMethods[0]
                val methodB = instance.adBarMethods[1]
                hookAfterMethod(methodA) { param ->
                    val view = param.thisObject as View
                    if (view.visibility == View.VISIBLE)
                        view.callMethod(methodB)
                }
                hookAfterMethod(methodB) { param ->
                    val view = param.thisObject as View
                    if (view.visibility == View.VISIBLE)
                        view.callMethod(methodA)
                }
            }
        }
        if (sPrefs.getBoolean("forbid_music_world", false)) {
            instance.musicWorldTouchListenerClass
                ?.replaceMethod("onTouch", View::class.java, MotionEvent::class.java) { false }
        }
        if (sPrefs.getBoolean("block_bottom_tips", false)) {
            instance.bottomTipControllerClass?.replaceMethod(instance.updateBottomTips()) { null }
        }
        val blockCoverAds = sPrefs.getStringSet("block_cover_ads", null) ?: setOf()
        if (blockCoverAds.contains("video")) {
            instance.videoViewDelegateClass?.replaceMethod(instance.onResult()) { null }
        }
        if (blockCoverAds.contains("genre")) {
            instance.genreViewDelegateClass?.declaredMethods?.find { m ->
                m.name == instance.genreOnBind() && m.parameterTypes.let { it.size > 1 && it[0] == instance.genreViewDelegateClass }
            }?.replaceMethod { null }
            instance.topSongViewDelegateClass?.declaredMethods?.find { m ->
                m.name == instance.topSongOnBind() && m.parameterTypes.let { it.size > 1 && it[0] == instance.topSongViewDelegateClass }
            }?.replaceMethod { null }
        }
        if (sPrefs.getBoolean("block_user_guide", false)) {
            instance.userGuideViewDelegateClass?.declaredMethods?.find {
                it.name == instance.showUserGuide() && it.returnType == Void::class.javaPrimitiveType
            }?.replaceMethod { null }
        }
    }
}
