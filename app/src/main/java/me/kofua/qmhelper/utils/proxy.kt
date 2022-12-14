package me.kofua.qmhelper.utils

import android.content.Context
import com.android.dx.stock.ProxyBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.qmPackage
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
                this@proxy.findMethod(deep = true) { it.name == m }
            }.toTypedArray())
    }.buildProxyClass()

fun Any.invocationHandler(handler: InvocationHandler) =
    ProxyBuilder.setInvocationHandler(this, handler)

fun Any.callSuper(m: Method, vararg args: Any?): Any? = ProxyBuilder.callSuper(this, m, *args)

fun preloadProxyClasses() = mainScope.launch(Dispatchers.IO) {
    val baseSettingFragmentClass = qmPackage.baseSettingFragmentClass ?: return@launch
    val settingPackage =
        hookInfo.setting.baseSettingFragment.settingPackage.name.ifEmpty { return@launch }
    val title = hookInfo.setting.baseSettingFragment.title.name.ifEmpty { return@launch }
    val baseSettingPackClass = qmPackage.baseSettingPackClass ?: return@launch
    val createSettingProvider =
        hookInfo.setting.baseSettingPack.createSettingProvider.name.ifEmpty { return@launch }
    val baseSettingProviderClass = qmPackage.baseSettingProviderClass ?: return@launch
    val create = hookInfo.setting.baseSettingProvider.create.name.ifEmpty { return@launch }

    baseSettingFragmentClass.proxy(settingPackage, title)
    baseSettingPackClass.proxy(createSettingProvider)
    baseSettingProviderClass.proxy(create)
}
