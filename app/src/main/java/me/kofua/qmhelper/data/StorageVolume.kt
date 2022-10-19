package me.kofua.qmhelper.data

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.new

data class StorageVolume(
    val storageId: Int,
    val path: String,
    val descriptionId: Int,
    val primary: Boolean,
    val removable: Boolean,
    val emulated: Boolean,
    val state: String
) {
    companion object {
        private val volumeRegex =
            Regex("""^StorageVolume\s\[mStorageId=(\d+),\smPath=(.*),\smDescriptionId=(\d+),\smPrimary=(true|false),\smRemovable=(true|false),\smEmulated=(true|false),\smState=(.*)]$""")

        fun Any.toMockVolume(): StorageVolume? {
            return volumeRegex.matchEntire(toString())?.run {
                groupValues.let {
                    StorageVolume(
                        it[1].toInt(),
                        it[2],
                        it[3].toInt(),
                        it[4].toBoolean(),
                        it[5].toBoolean(),
                        it[6].toBoolean(),
                        it[7]
                    )
                }
            }
        }

        fun StorageVolume.toRealVolume(): Any? {
            return instance.storageVolumeClass?.new(path, primary, removable, state)
        }
    }
}
