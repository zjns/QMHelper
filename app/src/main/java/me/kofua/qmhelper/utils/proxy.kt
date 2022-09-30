package me.kofua.qmhelper.utils

import android.content.Context
import com.android.dx.stock.ProxyBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kofua.qmhelper.QMPackage.Companion.instance
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

fun Class<*>.proxy(
    vararg onlyMethods: String,
): Class<*> = ProxyBuilder.forClass(this)
    .parentClassLoader(classLoader)
    .dexCache(currentContext.getDir("qmhelper_dx", Context.MODE_PRIVATE))
    .apply {
        if (onlyMethods.isNotEmpty())
            onlyMethods(onlyMethods.map { m ->
                this@proxy.methods.first { it.name == m }
            }.toTypedArray())
    }.buildProxyClass()

fun Any.invocationHandler(handler: InvocationHandler) =
    ProxyBuilder.setInvocationHandler(this, handler)

fun Any.callSuper(m: Method, vararg args: Any?): Any? = ProxyBuilder.callSuper(this, m, *args)

fun preloadProxyClasses() = mainScope.launch(Dispatchers.IO) {
    val baseSettingFragmentClass = instance.baseSettingFragmentClass ?: return@launch
    val baseSettingPackClass = instance.baseSettingPackClass ?: return@launch
    val settingPackage = instance.settingPackage() ?: return@launch
    val title = instance.title() ?: return@launch
    val createSettingProvider = instance.createSettingProvider() ?: return@launch
    val baseSettingProviderClass = instance.baseSettingProviderClass ?: return@launch
    val create = instance.create() ?: return@launch

    baseSettingFragmentClass.proxy(settingPackage, title)
    baseSettingPackClass.proxy(createSettingProvider)
    baseSettingProviderClass.proxy(create)
}
