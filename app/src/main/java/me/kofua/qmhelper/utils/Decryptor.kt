package me.kofua.qmhelper.utils

import android.os.Environment
import me.kofua.qmhelper.data.StorageVolume.Companion.toMockVolume
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.qmPackage
import java.io.File

typealias SingleDecryptListener = (
    srcFile: File, current: Int, total: Int, success: Boolean
) -> Unit

object Decryptor {
    private val wideEncRegex = Regex("""^(.+)\.(qmc\w+|mgg\w?|mflac\w?|mdolby|mmp4)(\.flac)?$""")
    private val pureRegex = Regex("""\s\[mqms(\d)*]""")

    data class Ext(val ext: String, val ver: Int)

    private val decExtMap = mapOf(
        "qmcflac" to Ext("flac", 1),
        "qmcogg" to Ext("ogg", 1),
        "qmc0" to Ext("mp3", 1),
        "qmc2" to Ext("m4a", 1),
        "qmc3" to Ext("mp3", 1),
        "qmc4" to Ext("m4a", 1),
        "qmc6" to Ext("m4a", 1),
        "qmc8" to Ext("m4a", 1),
        "qmcra" to Ext("m4a", 1),
        "mgg" to Ext("ogg", 2),
        "mgg0" to Ext("ogg", 2),
        "mggl" to Ext("ogg", 2),
        "mgg1" to Ext("ogg", 2),
        "mgg2" to Ext("ogg", 2),
        "mflac" to Ext("flac", 2),
        "mflac0" to Ext("flac", 2),
        "mflac1" to Ext("flac", 2),
        "mflac2" to Ext("flac", 2),
        "mdolby" to Ext("m4a", 2),
        "mmp4" to Ext("mp4", 2)
    )
    private val File.isEncrypted: Boolean
        get() = name.matches(wideEncRegex)

    fun batchDecrypt(
        saveDir: File,
        listener: SingleDecryptListener? = null
    ): Triple<Int, Int, List<File>> {
        val encSongs = getEncSongs().ifEmpty {
            return Triple(0, 0, listOf())
        }
        val total = encSongs.size
        var current = 1
        val successOrigSongs = mutableListOf<File>()
        val success = encSongs.count { f ->
            decrypt(f, saveDir).also {
                listener?.invoke(f, current++, total, it)
                it.yes { successOrigSongs.add(f) }
            }
        }
        return Triple(total, success, successOrigSongs)
    }

    private fun getEncSongs(): List<File> {
        return runCatching {
            qmPackage.storageUtilsClass?.callStaticMethodAs<Set<*>>(
                hookInfo.storageUtils.getVolumes, currentContext
            )?.mapNotNull { it?.toMockVolume()?.path }
                ?.flatMap { p ->
                    File(p, "qqmusic/song")
                        .takeIf { it.isDirectory }
                        ?.listFiles()?.toList()
                        ?: listOf()
                }?.filter { it.isEncrypted }
        }.onFailure { Log.e(it) }.getOrNull() ?: run {
            val externalDir = Environment.getExternalStorageDirectory()
            val songDir = File(externalDir, "qqmusic/song")
            songDir.listFiles()?.filter { it.isEncrypted }
        } ?: listOf()
    }

    fun deleteOrigSongs(songs: List<File>) = songs.forEach { it.delete() }

    private fun decrypt(srcFile: File, saveDir: File? = null): Boolean {
        srcFile.takeIf { it.isFile } ?: return false
        saveDir?.mkdirs()
        val srcFilePath = srcFile.absolutePath
        if (!srcFile.isEncrypted) return false
        val matchResult = wideEncRegex.matchEntire(srcFilePath)!!
        val newEnc = matchResult.groups[3] != null
        val fileNoExt = if (newEnc) {
            matchResult.groups[1]!!.value
        } else srcFilePath.substringBeforeLast(".")
        val fileExt = if (newEnc) {
            matchResult.groups[2]!!.value
        } else srcFilePath.substringAfterLast(".", "")
        val decExt = decExtMap[fileExt]?.ext
            ?: if (fileExt.isEmpty()) "dec" else "$fileExt.dec"
        val destFilePath = (if (saveDir == null) {
            "$fileNoExt.$decExt"
        } else if (newEnc) {
            File(saveDir, "${srcFile.name.replace(wideEncRegex, "$1")}.$decExt").absolutePath
        } else {
            File(saveDir, "${srcFile.nameWithoutExtension}.$decExt").absolutePath
        }).replace(pureRegex, "")
        File(destFilePath).delete()
        val eKey = getFileEKey(srcFilePath)
            .ifEmpty { return staticDecrypt(srcFilePath, destFilePath) }
        return decrypt(srcFilePath, destFilePath, eKey)
    }

    private fun getFileEKey(srcFilePath: String) = runCatching {
        qmPackage.eKeyManagerClass?.getStaticObjectField(hookInfo.eKeyManager.instance)
            ?.callMethodAs<String?>(hookInfo.eKeyManager.getFileEKey, srcFilePath, null)
    }.onFailure { Log.e(it) }.getOrNull() ?: ""

    private fun decrypt(srcFilePath: String, destFilePath: String, eKey: String) = runCatching {
        qmPackage.eKeyDecryptorClass?.getStaticObjectField(hookInfo.eKeyDecryptor.instance)
            ?.callMethod(hookInfo.eKeyDecryptor.decryptFile, srcFilePath, destFilePath, eKey)
        true
    }.onFailure { Log.e(it) }.getOrNull() ?: false

    private fun staticDecrypt(srcFilePath: String, destFilePath: String) = runCatching {
        qmPackage.vipDownloadHelperClass?.callStaticMethod(
            hookInfo.vipDownloadHelper.decryptFile, srcFilePath, destFilePath
        )
        true
    }.onFailure { Log.e(it) }.getOrNull() ?: false
}
