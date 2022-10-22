package me.kofua.qmhelper.hook

import me.kofua.qmhelper.QMPackage.Companion.instance
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.utils.*
import org.json.JSONArray
import org.json.JSONObject

object CgiHook : BaseHook {
    override fun hook() {
        val blockLive = sPrefs.getBoolean("block_live", false)
        val purifySearch = sPrefs.getStringSet("purify_search", null) ?: setOf()
        val blockCommentBanners = sPrefs.getBoolean("block_comment_banners", false)
        val removeCommentRecommend = sPrefs.getBoolean("remove_comment_recommend", false)
        val removeMineKol = sPrefs.getBoolean("remove_mine_kol", false)
        val moveDownRecently = sPrefs.getBoolean("move_down_recently", false)

        hookInfo.jsonRespParser.hookBeforeMethod({ parseModuleItem }) { param ->
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
            } else if (path == "notice" && blockCommentBanners) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val data = jo.optJSONObject(path)
                    ?.optJSONObject("data") ?: return@hookBeforeMethod
                if (data.optJSONArray("Banners").isNotEmpty()) {
                    data.remove("Banners")
                    param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                        ?: return@hookBeforeMethod
                }
            } else if (path == "recommend" && removeCommentRecommend) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val data = jo.optJSONObject(path)
                    ?.optJSONObject("data") ?: return@hookBeforeMethod
                if (data.optJSONArray("RecItems").isNotEmpty()) {
                    data.remove("RecItems")
                    param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                        ?: return@hookBeforeMethod
                }
            } else if (path == "music.sociality.KolEntrance.GetKolEntrance" && removeMineKol) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val data = jo.optJSONObject(path)
                    ?.optJSONObject("data") ?: return@hookBeforeMethod
                if (data.optBoolean("ShowEntrance")) {
                    data.put("ShowEntrance", false)
                    param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                        ?: return@hookBeforeMethod
                }
            } else if (path == "music.individuation.Recommend.GetRecommend" && moveDownRecently) {
                val json = param.args[2]?.toString() ?: return@hookBeforeMethod
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@hookBeforeMethod
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@hookBeforeMethod
                val shelfList = data.optJSONArray("v_shelf") ?: return@hookBeforeMethod
                var recentJson: JSONObject? = null
                val newShelf = JSONArray()
                for (item in shelfList.asSequence<JSONObject>()) {
                    val title = item.optString("title_content")
                    if (title != "最近播放" && title != "收藏歌单") {
                        newShelf.put(item)
                    } else if (title == "最近播放") {
                        recentJson = item
                    } else if (title == "收藏歌单") {
                        newShelf.put(item)
                        recentJson?.let {
                            newShelf.put(it)
                        }
                    }
                }
                data.put("v_shelf", newShelf)
                param.args[2] = jo.toString().fromJson(instance.jsonObjectClass)
                    ?: return@hookBeforeMethod
            }
        }
    }
}
