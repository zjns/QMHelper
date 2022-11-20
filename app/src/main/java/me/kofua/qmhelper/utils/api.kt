package me.kofua.qmhelper.utils

import android.os.Build
import android.webkit.WebView
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.qmPackage
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import java.util.zip.DeflaterInputStream
import java.util.zip.GZIPInputStream

val webViewDefUA by lazy {
    runCatching {
        qmPackage.x5WebViewClass?.new(currentContext)
            ?.callMethod("getSettings")
            ?.callMethodAs<String>("getUserAgentString")
            ?: WebView(currentContext).settings.userAgentString ?: ""
    }.onFailure { Log.e(it) }.getOrNull() ?: ""
}

val defaultUA =
    "Mozilla/5.0 (Linux; Android %s; %s Build/RQ3A.211001.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046141 Mobile Safari/537.36"
        .format(Build.VERSION.RELEASE, Build.MODEL)

fun cookies(url: String? = null): String {
    return qmPackage.webRequestHeadersClass?.getStaticObjectField(
        hookInfo.webRequestHeaders.instance
    )?.callMethodAs<String>(hookInfo.webRequestHeaders.getCookies, url) ?: ""
}

fun webUA(): String {
    return qmPackage.webRequestHeadersClass?.getStaticObjectField(
        hookInfo.webRequestHeaders.instance
    )?.callMethodAs<String>(hookInfo.webRequestHeaders.getUA, null, null)?.let {
        //"$webViewDefUA QQJSSDK/1.3 /$it"
        "$defaultUA QQJSSDK/1.3 /$it"
    } ?: ""
}

fun uin(): String {
    return qmPackage.userManagerClass?.callStaticMethod(
        hookInfo.userManager.get
    )?.callMethodAs(hookInfo.userManager.getMusicUin) ?: ""
}

fun guid(): String {
    return qmSp.getString("KEY_OPEN_UDID", null) ?: ""
}

fun uid(): String {
    return sessionCacheSp.getString("UID", null) ?: ""
}

fun isLogin(): Boolean {
    return qmPackage.userManagerClass?.callStaticMethod(
        hookInfo.userManager.get
    )?.callMethodAs(hookInfo.userManager.isLogin) ?: false
}

fun nativeSign(body: String, query: String): String {
    return qmPackage.mERJniClass?.callStaticMethodOrNullAs<String?>(
        "calc", body.toByteArray(), query.toByteArray()
    )?.split(' ')?.firstOrNull() ?: ""
}

fun webSign(data: String): String {
    val minLen = 10
    val maxLen = 16
    val ran = Random()
    val ranLen = ran.nextInt(maxLen - minLen) + minLen
    val encNonce = "CJBPACrRuNy7"
    val signPrefix = "zza"
    val chars = "0123456789abcdefghijklmnopqrstuvwxyz"
    val uuid = buildString(ranLen) {
        for (i in 0 until ranLen)
            append(chars[ran.nextInt(chars.length)])
    }
    return signPrefix + uuid + (encNonce + data).md5Hex
}

fun webSignB(data: String): String {
    val md5 = data.md5Hex
    val prefixIndex = intArrayOf(21, 4, 9, 26, 16, 20, 27, 30)
    val suffixIndex = intArrayOf(18, 11, 3, 2, 1, 7, 6, 25)
    val xorKey = intArrayOf(212, 45, 80, 68, 195, 163, 163, 203, 157, 220, 254, 91, 204, 79, 104, 6)
    val prefixSign = buildString { prefixIndex.forEach { append(md5[it]) } }
    val suffixSign = buildString { suffixIndex.forEach { append(md5[it]) } }
    val middleSign = ByteArray(xorKey.size).apply {
        xorKey.forEachIndexed { i, k ->
            val s = md5.substring(i * 2, i * 2 + 2)
            val xor = s.toInt(16) xor k
            this[i] = xor.toByte()
        }
    }.base64.replace("[+/=]".toRegex(), "").lowercase()
    return "zzb$prefixSign$middleSign$suffixSign"
}

fun webJsonRequestBody(module: String, method: String, param: Map<String, Any>) =
    JSONObject().apply {
        put("comm", JSONObject().apply {
            put("g_tk", 635035947)
            put("uin", uin())
            put("format", "json")
            put("inCharset", "utf-8")
            put("outCharset", "utf-8")
            put("notice", 0)
            put("platform", "h5")
            put("needNewCode", 1)
            put("ct", 23)
            put("cv", 0)
        })
        put("req_0", JSONObject().apply {
            put("module", module)
            put("method", method)
            put("param", JSONObject(param))
        })
    }

fun webJsonPost(url: String, module: String, method: String, param: Map<String, Any>): String? {
    val timeout = 10_000
    val time = System.currentTimeMillis()
    val reqBodyJson = webJsonRequestBody(module, method, param).toString()
    val sign = webSignB(reqBodyJson)
    val newUrl = if (url.contains("?")) "$url&_=$time&sign=$sign" else "$url?_=$time&sign=$sign"
    val connection = URL(newUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = timeout
    connection.readTimeout = timeout
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.setRequestProperty("Accept", "application/json")
    connection.setRequestProperty("Cookie", cookies(newUrl))
    connection.setRequestProperty("User-Agent", webUA())
    connection.setRequestProperty("Accept-Encoding", "gzip, deflate")
    connection.doOutput = true
    connection.outputStream.buffered().use {
        it.write(reqBodyJson.toByteArray())
    }
    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = when (connection.contentEncoding?.lowercase()) {
            "gzip" -> GZIPInputStream(connection.inputStream)
            "deflate" -> DeflaterInputStream(connection.inputStream)
            else -> connection.inputStream
        }
        return inputStream.bufferedReader().use { it.readText() }
    }
    return null
}

fun webHtmlGet(url: String): String? {
    val timeout = 10_000
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = timeout
    connection.readTimeout = timeout
    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
    connection.setRequestProperty("Cookie", cookies(url))
    connection.setRequestProperty("User-Agent", webUA())
    connection.setRequestProperty("Accept-Encoding", "gzip, deflate")
    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = when (connection.contentEncoding?.lowercase()) {
            "gzip" -> GZIPInputStream(connection.inputStream)
            "deflate" -> DeflaterInputStream(connection.inputStream)
            else -> connection.inputStream
        }
        return inputStream.bufferedReader().use { it.readText() }
    }
    return null
}
