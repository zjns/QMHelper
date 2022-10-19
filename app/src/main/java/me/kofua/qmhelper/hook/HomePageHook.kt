package me.kofua.qmhelper.hook

import android.view.MotionEvent
import android.view.View
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object HomePageHook : BaseHook {

    override fun hook() {
        if (sPrefs.getBoolean("hide_music_world", false)) {
            hookInfo.mainDesktopHeader.clazz.from(classLoader)?.replaceMethod(
                hookInfo.mainDesktopHeader.showMusicWorld.name,
                *hookInfo.mainDesktopHeader.showMusicWorld.paramTypes
            ) { null }
        }
        if (sPrefs.getBoolean("hide_vip_bubble", false)) {
            hookInfo.userInfoHolder.clazz.from(classLoader)?.replaceMethod(
                hookInfo.userInfoHolder.showBubble.name,
                *hookInfo.userInfoHolder.showBubble.paramTypes
            ) { null }
            hookInfo.vipAdBarData.from(classLoader)?.declaredConstructors
                ?.find { m -> m.parameterTypes.let { it.size == 10 && it[8] == Boolean::class.javaPrimitiveType } }
                ?.hookBeforeMethod { it.args[8] = true }
        }
        if (sPrefs.getBoolean("purify_live_guide", false)) {
            hookInfo.topAreaDelegate.clazz.from(classLoader)?.run {
                replaceMethod(hookInfo.topAreaDelegate.initLiveGuide.name) { null }
                replaceMethod(
                    hookInfo.topAreaDelegate.showCurListen.name,
                    *hookInfo.topAreaDelegate.showCurListen.paramTypes
                ) { null }
            }
        }
        if (sPrefs.getBoolean("purify_share_guide", false)) {
            hookInfo.topAreaDelegate.clazz.from(classLoader)?.replaceMethod(
                hookInfo.topAreaDelegate.showShareGuide.name,
                Int::class.javaPrimitiveType
            ) { null }
        }
        if (sPrefs.getBoolean("forbid_slide", false)) {
            hookInfo.playViewModel.clazz.from(classLoader)?.hookBeforeMethod(
                hookInfo.playViewModel.setCanSlide.name,
                Boolean::class.javaPrimitiveType
            ) { it.args[0] = false }
        }
        if (sPrefs.getBoolean("hide_ad_bar", false)) {
            hookInfo.adBar.clazz.from(classLoader)?.run {
                hookAfterAllConstructors { param ->
                    val view = param.thisObject as View
                    view.visibility = View.GONE
                }
                val methods = hookInfo.adBar
                    .methods.takeIf { it.size == 2 } ?: return@run
                val methodA = methods[0].name
                val methodB = methods[1].name
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
            hookInfo.musicWorldTouchListener.from(classLoader)
                ?.replaceMethod("onTouch", View::class.java, MotionEvent::class.java) { false }
        }
        if (sPrefs.getBoolean("block_bottom_tips", false)) {
            hookInfo.bottomTipController.clazz.from(classLoader)
                ?.replaceMethod(hookInfo.bottomTipController.updateBottomTips.name) { null }
        }
        val blockCoverAds = sPrefs.getStringSet("block_cover_ads", null) ?: setOf()
        if (blockCoverAds.contains("video")) {
            hookInfo.videoViewDelegate.clazz.from(classLoader)
                ?.replaceMethod(hookInfo.videoViewDelegate.onResult.name) { null }
        }
        if (blockCoverAds.contains("genre")) {
            hookInfo.genreViewDelegate.clazz.from(classLoader)
                ?.replaceMethod(
                    hookInfo.genreViewDelegate.onBind.name,
                    *hookInfo.genreViewDelegate.onBind.paramTypes
                ) { null }
            hookInfo.topSongViewDelegate.clazz.from(classLoader)
                ?.replaceMethod(
                    hookInfo.topSongViewDelegate.onBind.name,
                    *hookInfo.topSongViewDelegate.onBind.paramTypes
                ) { null }
        }
        if (sPrefs.getBoolean("block_user_guide", false)) {
            hookInfo.userGuideViewDelegate.clazz.from(classLoader)?.declaredMethods?.find {
                it.name == hookInfo.userGuideViewDelegate.showUserGuide.name
                        && it.returnType == Void::class.javaPrimitiveType
            }?.replaceMethod { null }
        }
    }
}
