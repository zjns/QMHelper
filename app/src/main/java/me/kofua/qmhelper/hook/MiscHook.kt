package me.kofua.qmhelper.hook

import android.content.Context
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.StorageVolume.Companion.toMockVolume
import me.kofua.qmhelper.utils.StorageVolume.Companion.toRealVolume
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.runCatchingOrNull
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
        if (sPrefs.getBoolean("allow_save_to_sdcard_extern", false)) {
            instance.storageUtilsClass?.hookAfterMethod(
                instance.getVolumes(), Context::class.java
            ) { param ->
                val volumes = param.result as Set<*>
                val newVolumes = hashSetOf<Any>()
                var changed = false
                runCatchingOrNull {
                    for (v in volumes) {
                        val mockVolume = v?.toMockVolume() ?: break
                        val removable = mockVolume.removable
                        if (!removable) {
                            newVolumes.add(v)
                        } else {
                            mockVolume.copy(removable = false)
                                .toRealVolume()?.let {
                                    changed = true
                                    newVolumes.add(it)
                                }
                        }
                    }
                }
                if (changed) param.result = newVolumes
            }
        }
    }
}
