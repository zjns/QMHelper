package me.kofua.qmhelper.hook

import android.content.Context
import me.kofua.qmhelper.data.StorageVolume.Companion.toMockVolume
import me.kofua.qmhelper.data.StorageVolume.Companion.toRealVolume
import me.kofua.qmhelper.from
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*

object MiscHook : BaseHook {
    override fun hook() {
        if (sPrefs.getBoolean("fix_song_filename", false)) {
            hookInfo.fileUtils.clazz.from(classLoader)?.hookBeforeMethod(
                hookInfo.fileUtils.toValidFilename.name,
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
            hookInfo.storageUtils.clazz.from(classLoader)?.hookAfterMethod(
                hookInfo.storageUtils.getVolumes.name, Context::class.java
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
