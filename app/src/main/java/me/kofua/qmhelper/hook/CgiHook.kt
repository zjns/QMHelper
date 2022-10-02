package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.utils.asSequence
import me.kofua.qmhelper.utils.fromJson
import me.kofua.qmhelper.utils.hookBeforeMethod
import me.kofua.qmhelper.utils.runCatchingOrNull
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.toJSONObject
import org.json.JSONObject

class CgiHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    override fun startHook() {
        val blockLive = sPrefs.getBoolean("block_live", false)
        val purifySearch = sPrefs.getStringSet("purify_search", null) ?: setOf()

        if (!blockLive && purifySearch.isEmpty()) return
        instance.jsonRespParserClass?.declaredMethods?.find {
            it.name == instance.parseModuleItem() && it.parameterTypes.size == 4
        }?.hookBeforeMethod { param ->
            val path = param.args[1] as? String
            if (path == "music.recommend.RecommendFeed.get_recommend_feed" && blockLive) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val shelfList = jo.optJSONObject(path)?.optJSONObject("data")
                    ?.optJSONArray("v_shelf") ?: return@hookBeforeMethod
                var position = -1
                for ((idx, item) in shelfList.asSequence<JSONObject>().withIndex()) {
                    val module = item.optJSONObject("extra_info")
                        ?.optString("moduleID") ?: ""
                    if (module.startsWith("mlive")) {
                        position = idx
                        break
                    }
                }
                if (position != -1) {
                    shelfList.remove(position)
                    param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                        ?: return@hookBeforeMethod
                }
            } else if (path == "music.musicsearch.HotkeyService.GetHotkeyAllBusinessForQQMusicMobile"
                && purifySearch.any { it != "scroll" }
            ) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val data = jo.optJSONObject(path)
                    ?.optJSONObject("data") ?: return@hookBeforeMethod
                if (purifySearch.contains("ads"))
                    data.remove("ads")
                if (purifySearch.contains("rcmd"))
                    data.remove("vec_reckey")
                if (purifySearch.contains("rcmd-list")) {
                    data.remove("business_en_cn")
                    data.remove("map_business_hotkey")
                }
                param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                    ?: return@hookBeforeMethod
            } else if (path == "music.defaultKey.DefaultKeyService.GetDefaultKeyForQQMusicMobile"
                && purifySearch.contains("scroll")
            ) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val data = jo.optJSONObject(path)
                    ?.optJSONObject("data") ?: return@hookBeforeMethod
                data.remove("keys")
                data.remove("map_business_keys")
                param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                    ?: return@hookBeforeMethod
            }
        }
    }
}
