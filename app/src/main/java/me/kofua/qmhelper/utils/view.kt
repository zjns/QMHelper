package me.kofua.qmhelper.utils

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import kotlin.math.roundToInt

operator fun ViewGroup.iterator(): MutableIterator<View> = object : MutableIterator<View> {
    private var index = 0
    override fun hasNext() = index < childCount
    override fun next() = getChildAt(index++) ?: throw IndexOutOfBoundsException()
    override fun remove() = removeViewAt(--index)
}

val ViewGroup.children: Sequence<View>
    get() = object : Sequence<View> {
        override fun iterator() = this@children.iterator()
    }

@Suppress("UNCHECKED_CAST")
inline fun <T : View> T.click(crossinline action: (v: T) -> Unit) = apply {
    setOnClickListener { action(it as T) }
}

@Suppress("UNCHECKED_CAST")
inline fun <T : View> T.longClick(crossinline action: (v: T) -> Boolean) = apply {
    setOnLongClickListener { action(it as T) }
}

fun View.addBackgroundRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

val Int.dp: Int
    inline get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        currentContext.resources.displayMetrics
    ).roundToInt()
