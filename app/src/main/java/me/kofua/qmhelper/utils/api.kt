package me.kofua.qmhelper.utils

import android.os.Build
import android.webkit.WebView
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.hookInfo
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import java.util.zip.DeflaterInputStream
import java.util.zip.GZIPInputStream

val webViewDefUA by lazy {
    runCatching {
        instance.x5WebViewClass?.new(currentContext)
            ?.callMethod("getSettings")
            ?.callMethodAs<String>("getUserAgentString")
            ?: WebView(currentContext).settings.userAgentString ?: ""
    }.onFailure { Log.e(it) }.getOrNull() ?: ""
}

val defaultUA =
    "Mozilla/5.0 (Linux; Android %s; %s Build/RQ3A.211001.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046141 Mobile Safari/537.36"
        .format(Build.VERSION.RELEASE, Build.MODEL)

fun cookies(url: String? = null): String {
    return instance.webRequestHeadersClass?.getStaticObjectField(
        hookInfo.webRequestHeaders.instance
    )?.callMethodAs<String>(hookInfo.webRequestHeaders.getCookies, url) ?: ""
}

fun webUA(): String {
    return instance.webRequestHeadersClass?.getStaticObjectField(
        hookInfo.webRequestHeaders.instance
    )?.callMethodAs<String>(hookInfo.webRequestHeaders.getUA, null, null)?.let {
        "$webViewDefUA QQJSSDK/1.3 /$it"
    } ?: ""
}

fun uin(): String {
    return instance.userManagerClass?.callStaticMethod(
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
    return instance.userManagerClass?.callStaticMethod(
        hookInfo.userManager.get
    )?.callMethodAs(hookInfo.userManager.isLogin) ?: false
}

fun nativeSign(body: String, query: String): String {
    return instance.mERJniClass?.callStaticMethodOrNullAs<String?>(
        "calc", body.toByteArray(), query.toByteArray()
    )?.split(' ')?.firstOrNull() ?: ""
}

private const val encNonce = "CJBPACrRuNy7"
private const val signPrefix = "zza"
private val chars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray()

fun webSign(params: String): String {
    val minLen = 10
    val maxLen = 16
    val ran = Random()
    val ranLen = ran.nextInt(maxLen - minLen) + minLen
    val uuid = buildString(ranLen) {
        for (i in 0 until ranLen)
            append(chars[ran.nextInt(chars.size)])
    }
    return signPrefix + uuid + (encNonce + params).md5
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
    val sign = webSign(reqBodyJson)
    val newUrl = if (url.contains("?")) "$url&_=$time&sign=$sign" else "$url?_=$time&sign=$sign"
    val connection = URL(newUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = timeout
    connection.readTimeout = timeout
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.setRequestProperty("Accept", "application/json")
    connection.setRequestProperty("Cookie", cookies(newUrl))
    //connection.setRequestProperty("User-Agent", webUA())
    connection.setRequestProperty("User-Agent", defaultUA)
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
