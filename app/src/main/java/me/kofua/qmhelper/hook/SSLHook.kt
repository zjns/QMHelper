@file:Suppress("DEPRECATION")

package me.kofua.qmhelper.hook

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import me.kofua.qmhelper.utils.callMethod
import me.kofua.qmhelper.utils.callMethodAs
import me.kofua.qmhelper.utils.callStaticMethodAs
import me.kofua.qmhelper.utils.from
import me.kofua.qmhelper.utils.getObjectField
import me.kofua.qmhelper.utils.hookAfterConstructor
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.new
import me.kofua.qmhelper.utils.on
import me.kofua.qmhelper.utils.replaceMethod
import me.kofua.qmhelper.utils.runCatchingOrNull
import me.kofua.qmhelper.utils.setObjectField
import org.apache.http.conn.scheme.HostNameResolver
import org.apache.http.conn.ssl.SSLSocketFactory
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SSLHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    override fun startHook() {
        @SuppressLint("CustomX509TrustManager")
        val emptyTrustManagers = arrayOf(object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

            @Suppress("unused", "UNUSED_PARAMETER")
            fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String,
                host: String
            ): List<X509Certificate> = emptyList()
        })

        "javax.net.ssl.TrustManagerFactory".hookBeforeMethod(
            classLoader,
            "getTrustManagers"
        ) { it.result = emptyTrustManagers }

        "javax.net.ssl.SSLContext".hookBeforeMethod(
            classLoader,
            "init",
            "javax.net.ssl.KeyManager[]",
            "javax.net.ssl.TrustManager[]",
            SecureRandom::class.java
        ) { param ->
            param.args[0] = null
            param.args[1] = emptyTrustManagers
            param.args[2] = null
        }

        "javax.net.ssl.HttpsURLConnection".hookBeforeMethod(
            classLoader,
            "setSSLSocketFactory",
            javax.net.ssl.SSLSocketFactory::class.java
        ) { it.args[0] = "javax.net.ssl.SSLSocketFactory".on(classLoader).new() }

        "org.apache.http.conn.scheme.SchemeRegistry".from(classLoader)
            ?.hookBeforeMethod("register", "org.apache.http.conn.scheme.Scheme") { param ->
                if (param.args[0].callMethodAs<String>("getName") == "https") {
                    param.args[0] = param.args[0].javaClass.new(
                        "https",
                        SSLSocketFactory.getSocketFactory(),
                        443
                    )
                }
            }

        "org.apache.http.conn.ssl.HttpsURLConnection".from(classLoader)?.run {
            hookBeforeMethod("setDefaultHostnameVerifier", HostnameVerifier::class.java) { param ->
                param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            }

            hookBeforeMethod("setHostnameVerifier", HostnameVerifier::class.java) { param ->
                param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            }

        }
        "org.apache.http.conn.ssl.SSLSocketFactory".hookBeforeMethod(
            classLoader,
            "getSocketFactory"
        ) { it.result = SSLSocketFactory::class.java.new() }

        "org.apache.http.conn.ssl.SSLSocketFactory".from(classLoader)
            ?.hookAfterConstructor(
                String::class.java,
                KeyStore::class.java,
                String::class.java,
                KeyStore::class.java,
                SecureRandom::class.java,
                HostNameResolver::class.java
            ) { param ->
                val algorithm = param.args[0] as? String
                val keystore = param.args[1] as? KeyStore
                val keystorePassword = param.args[2] as? String
                val random = param.args[4] as? SecureRandom

                @Suppress("UNCHECKED_CAST")
                val trustManagers = emptyTrustManagers as Array<TrustManager>

                val keyManagers = keystore?.let {
                    SSLSocketFactory::class.java.callStaticMethodAs<Array<KeyManager>>(
                        "createKeyManagers",
                        keystore,
                        keystorePassword
                    )
                }

                param.thisObject.run {
                    setObjectField("sslcontext", SSLContext.getInstance(algorithm))
                    getObjectField("sslcontext")
                        ?.callMethod("init", keyManagers, trustManagers, random)
                    setObjectField(
                        "socketfactory",
                        getObjectField("sslcontext")?.callMethod("getSocketFactory")
                    )
                }
            }

        "org.apache.http.conn.ssl.SSLSocketFactory".hookAfterMethod(
            classLoader,
            "isSecure",
            Socket::class.java
        ) { it.result = true }

        "okhttp3.CertificatePinner".from(classLoader)?.run {
            (runCatchingOrNull { getDeclaredMethod("findMatchingPins", String::class.java) }
                ?: declaredMethods.firstOrNull { it.parameterTypes.size == 1 && it.parameterTypes[0] == String::class.java && it.returnType == List::class.java })?.hookBeforeMethod { param ->
                param.args[0] = ""
            }
        }

        "android.webkit.WebViewClient".from(classLoader)?.run {
            replaceMethod(
                "onReceivedSslError",
                WebView::class.java,
                SslErrorHandler::class.java,
                SslError::class.java
            ) { param ->
                (param.args[1] as SslErrorHandler).proceed()
                null
            }
            replaceMethod(
                "onReceivedError",
                WebView::class.java,
                Int::class.javaPrimitiveType,
                String::class.java,
                String::class.java
            ) {
                null
            }
        }
    }
}
