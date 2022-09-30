package me.kofua.qmhelper.utils

private const val KEY_UI_MODE = "KEY_SETTING_UI_MODE"

enum class UiMode(val key: Int) {
    NORMAL(1), CLEAN(2), ELDER(3);

    companion object {
        fun of(key: Int) = values().find { it.key == key } ?: NORMAL
    }
}

val uiMode get() = UiMode.of(qmSp.getInt(KEY_UI_MODE, UiMode.NORMAL.key))
