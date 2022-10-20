package me.kofua.qmhelper.utils

import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.R
import me.kofua.qmhelper.hookInfo

object BannerTips {

    @JvmStatic
    private fun showStyledToast(type: Int, message: String) {
        val newMessage = string(R.string.app_name) + "：" + message
        val action = Runnable {
            instance.bannerTipsClass?.also {
                it.callStaticMethod(
                    hookInfo.bannerTips.showStyledToast,
                    currentContext, type, newMessage, 0, 0, true, 0
                )
            } ?: Toast.makeText(currentContext, newMessage, Toast.LENGTH_SHORT).show()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runCatchingOrNull { action.run() }
        } else {
            runCatchingOrNull { handler.post(action) }
        }
    }

    @JvmStatic
    fun success(message: String) {
        showStyledToast(0, message)
    }

    @JvmStatic
    fun success(@StringRes resId: Int) {
        showStyledToast(0, string(resId))
    }

    @JvmStatic
    fun error(message: String) {
        showStyledToast(1, message)
    }

    @JvmStatic
    fun error(@StringRes resId: Int) {
        showStyledToast(1, string(resId))
    }

    @JvmStatic
    fun failed(message: String) {
        showStyledToast(2, message)
    }

    @JvmStatic
    fun failed(@StringRes resId: Int) {
        showStyledToast(2, string(resId))
    }
}
