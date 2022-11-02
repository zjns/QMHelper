package me.kofua.qmhelper.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val HEX_DIGITS =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

@Suppress("SameParameterValue")
private fun hashTemplate(data: ByteArray, algorithm: String): ByteArray? {
    return if (data.isEmpty()) null else try {
        val md: MessageDigest = MessageDigest.getInstance(algorithm)
        md.update(data)
        md.digest()
    } catch (e: NoSuchAlgorithmException) {
        null
    }
}

fun ByteArray.toHexString(): String {
    val hexDigits = HEX_DIGITS
    val len = size
    if (len <= 0) return ""
    val ret = CharArray(len shl 1)
    var i = 0
    var j = 0
    while (i < len) {
        ret[j++] = hexDigits[this[i].toInt() shr 4 and 0x0f]
        ret[j++] = hexDigits[this[i].toInt() and 0x0f]
        i++
    }
    return String(ret)
}

val String.md5Hex: String
    get() = hashTemplate(toByteArray(), "MD5")?.toHexString() ?: ""

val ByteArray.base64: String
    get() = Base64.encodeToString(this, Base64.NO_WRAP)
