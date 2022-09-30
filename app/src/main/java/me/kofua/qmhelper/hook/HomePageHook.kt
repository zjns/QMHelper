package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
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
    }
}
