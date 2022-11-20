package me.kofua.qmhelper.hook

import android.view.MotionEvent
import android.view.View
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object HomePageHook : BaseHook {

    override fun hook() {
        if (sPrefs.getBoolean("hide_music_world", false)) {
            hookInfo.mainDesktopHeader.replaceMethod({ showMusicWorld }) { null }
        }
        if (sPrefs.getBoolean("hide_vip_bubble", false)) {
            hookInfo.userInfoHolder.replaceMethod({ showBubble }) { null }
            hookInfo.vipAdBarData.from(classLoader)?.declaredConstructors
                ?.find { m -> m.parameterTypes.let { it.size == 10 && it[8] == Boolean::class.javaPrimitiveType } }
                ?.hookBefore { it.args[8] = true }
        }
        if (sPrefs.getBoolean("purify_live_guide", false)) {
            hookInfo.topAreaDelegate.replaceMethod({ initLiveGuide }) { null }
            hookInfo.topAreaDelegate.showCurListen.name.ifNotEmpty {
                hookInfo.topAreaDelegate.replaceMethod({ showCurListen }) { null }
            }
        }
        if (sPrefs.getBoolean("purify_share_guide", false)) {
            hookInfo.topAreaDelegate.replaceMethod({ showShareGuide }) { null }
        }
        if (sPrefs.getBoolean("forbid_slide", false)) {
            hookInfo.playViewModel.hookBeforeMethod({ setCanSlide }) {
                it.args[0].callMethod(hookInfo.playViewModel.postCanSlide, false)
                it.result = null
            }
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
            hookInfo.bottomTipController.replaceMethod({ updateBottomTips }) { null }
        }
        val blockCoverAds = sPrefs.getStringSet("block_cover_ads", null) ?: setOf()
        if (blockCoverAds.contains("video")) {
            hookInfo.videoViewDelegate.replaceMethod({ onResult }) { null }
        }
        if (blockCoverAds.contains("genre")) {
            hookInfo.genreViewDelegate.replaceMethod({ onBind }) { null }
            hookInfo.topSongViewDelegate.replaceMethod({ onBind }) { null }
        }
        if (sPrefs.getBoolean("block_user_guide", false)) {
            hookInfo.userGuideViewDelegate.replaceMethod({ showUserGuide }) { null }
        }
    }
}
