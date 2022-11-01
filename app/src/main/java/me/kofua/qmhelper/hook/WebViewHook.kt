package me.kofua.qmhelper.hook

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import me.kofua.qmhelper.BuildConfig
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.*

object WebViewHook : BaseHook {
    private val hidden by lazy { sPrefs.getBoolean("hidden", false) }
    private val unlockTheme by lazy { sPrefs.getBoolean("unlock_theme", false) }

    private val hookedClient = HashSet<Class<*>>()
    private val hooker: Hooker = { param ->
        try {
            val url = param.args[1] as String
            if (url.startsWith("https://i.y.qq.com/n2/m/theme/index.html")
                || url.startsWith("https://y.qq.com/m/basic/client/themev2/detail/index.html")
                || url.startsWith("https://y.qq.com/m/client/player_detail/index.html")
            ) {
                param.args[0].callMethod(
                    "evaluateJavascript",
                    """(function(){$js})()""".trimMargin(),
                    null
                )
            }
        } catch (e: Throwable) {
            Log.e(e)
        }
    }

    private val jsHooker = object : Any() {
        @Suppress("UNUSED")
        @JavascriptInterface
        fun hook(url: String, requestBody: String?, text: String): String {
            return this@WebViewHook.hook(url, requestBody, text)
        }
    }

    private val js by lazy {
        try {
            WebViewHook::class.java.classLoader?.getResourceAsStream("assets/xhook.js")
                ?.use { `is` -> return@lazy `is`.reader().readText() }
        } catch (_: Exception) {
        }
        ""
    }

    override fun hook() {
        if (BuildConfig.DEBUG)
            WebView.setWebContentsDebuggingEnabled(true)
        WebView::class.java.hookBeforeMethod(
            "setWebViewClient",
            WebViewClient::class.java
        ) { param ->
            val clazz = param.args[0]?.javaClass ?: return@hookBeforeMethod
            (param.thisObject as WebView).run {
                addJavascriptInterface(jsHooker, "hooker")
            }
            if (hookedClient.contains(clazz)) return@hookBeforeMethod
            try {
                clazz.getDeclaredMethod(
                    "onPageStarted",
                    WebView::class.java,
                    String::class.java,
                    Bitmap::class.java
                ).hookBeforeMethod(hooker)
                hookedClient.add(clazz)
                Log.d("hook webview $clazz")
            } catch (_: NoSuchMethodException) {
            }
        }
        if (BuildConfig.DEBUG)
            instance.x5WebViewClass?.callStaticMethod("setWebContentsDebuggingEnabled", true)
        instance.x5WebViewClass?.hookBeforeMethod(
            "setWebViewClient",
            "com.tencent.smtt.sdk.WebViewClient"
        ) { param ->
            val clazz = param.args[0]?.javaClass ?: return@hookBeforeMethod
            param.thisObject.callMethod("addJavascriptInterface", jsHooker, "hooker")
            if (hookedClient.contains(clazz)) return@hookBeforeMethod
            try {
                clazz.getDeclaredMethod(
                    "onPageStarted",
                    instance.x5WebViewClass,
                    String::class.java,
                    Bitmap::class.java
                ).hookBeforeMethod(hooker)
                hookedClient.add(clazz)
                Log.d("hook webview $clazz")
            } catch (_: NoSuchMethodException) {
            }
        }
    }

    fun hook(url: String, requestBody: String?, text: String): String {
        if (BuildConfig.DEBUG)
            Log.d("net.webview, url: $url, requestBody: $requestBody, text: $text")
        if (hidden && unlockTheme) {
            if (url.contains("/cgi-bin/musics.fcg?_webcgikey=GetSubject")
                || url.contains("/cgi-bin/musics.fcg?_webcgikey=GetIcon")
            ) {
                val json = text.runCatchingOrNull { toJSONObject() } ?: return text
                val data = json.optJSONObject("req_0")?.optJSONObject("data") ?: return text
                data.optJSONObject("auth")?.run {
                    put("enable", 1)
                    put("authType", 0)
                }
                return json.toString()
            } else if (url.contains("/cgi-bin/musics.fcg?_webcgikey=get_subject_info")) {
                val json = text.runCatchingOrNull { toJSONObject() } ?: return text
                val data = json.optJSONObject("req_0")?.optJSONObject("data") ?: return text
                val themeList = data.optJSONArray("vlist") ?: return text
                data.optJSONObject("alert")?.put("revertTheme", 0)
                for (item in themeList) {
                    item.put("enable", 1)
                }
                return json.toString()
            } else if (url.contains("/cgi-bin/musics.fcg?_webcgikey=GetPlayerStyleDetail")) {
                val json = text.runCatchingOrNull { toJSONObject() } ?: return text
                val data = json.optJSONObject("req_0")?.optJSONObject("data") ?: return text
                val styleConf = data.optJSONObject("styleConf") ?: return text
                data.optJSONObject("alert")?.put("revertTheme", 0)
                styleConf.put("status", 0)
                return json.toString()
            }
        }
        return text
    }
}
