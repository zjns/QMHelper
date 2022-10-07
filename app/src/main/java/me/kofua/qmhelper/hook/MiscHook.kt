package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.toValidFatFilename

class MiscHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    override fun startHook() {
        if (sPrefs.getBoolean("fix_song_filename", false)) {
            instance.fileUtilsClass?.hookBeforeMethod(
                instance.toValidFilename(),
                String::class.java, String::class.java, String::class.java
            ) { param ->
                val name = param.args[0] as? String
                var extra = param.args[1] as? String
                val ext = param.args[2] as? String
                extra = if (extra.orEmpty().startsWith(" [mqms")) "" else extra
                param.result = (name + extra + ext).toValidFatFilename(250)
            }
        }
    }
}
