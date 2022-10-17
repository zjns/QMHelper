package me.kofua.qmhelper.utils

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun Boolean.yes(action: () -> Unit): Boolean {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    if (this) action()
    return this
}

inline fun Boolean.no(action: () -> Unit): Boolean {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    if (!this) action()
    return this
}
