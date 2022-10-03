package me.kofua.qmhelper.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import androidx.annotation.StringRes
import me.kofua.qmhelper.QMPackage.Companion.instance

typealias ButtonClickListener = (v: View) -> Unit

fun Activity.showMessageDialog(
    title: String,
    message: String,
    leftButton: String,
    rightButton: String,
    leftClick: ButtonClickListener? = null,
    rightClick: ButtonClickListener? = null,
): Dialog? {
    if (isDestroyed || isFinishing) return null
    return instance.showMessageDialog()?.let { showMethod ->
        callMethodOrNullAs<Dialog?>(
            showMethod,
            title,
            message,
            rightButton,
            leftButton,
            rightClick?.let { View.OnClickListener(it) },
            leftClick?.let { View.OnClickListener(it) },
            false,
            false
        )
    } ?: AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(leftButton, null)
        .setPositiveButton(rightButton, null)
        .create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_NEGATIVE)?.let { button ->
                    button.setOnClickListener {
                        dismiss()
                        leftClick?.invoke(it)
                    }
                }
                getButton(AlertDialog.BUTTON_POSITIVE)?.let { button ->
                    button.setOnClickListener {
                        dismiss()
                        rightClick?.invoke(it)
                    }
                }
            }
        }.apply { show() }
}

fun Activity.showMessageDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes leftButton: Int,
    @StringRes rightButton: Int,
    leftClick: ButtonClickListener? = null,
    rightClick: ButtonClickListener? = null,
) = showMessageDialog(
    string(title),
    string(message),
    string(leftButton),
    string(rightButton),
    leftClick,
    rightClick,
)
