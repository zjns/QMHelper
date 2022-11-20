package me.kofua.qmhelper.hook

import me.kofua.qmhelper.BuildConfig
import me.kofua.qmhelper.hookInfo
import me.kofua.qmhelper.qmPackage
import me.kofua.qmhelper.utils.*
import org.json.JSONArray
import org.json.JSONObject

object CgiHook : BaseHook {
    override fun hook() {
        val hidden = sPrefs.getBoolean("hidden", false)
        val blockLive = sPrefs.getBoolean("block_live", false)
        val purifySearch = sPrefs.getStringSet("purify_search", null) ?: setOf()
        val blockCommentBanners = sPrefs.getBoolean("block_comment_banners", false)
        val removeCommentRecommend = sPrefs.getBoolean("remove_comment_recommend", false)
        val removeMineKol = sPrefs.getBoolean("remove_mine_kol", false)
        val moveDownRecently = sPrefs.getBoolean("move_down_recently", false)
        val unlockTheme = sPrefs.getBoolean("unlock_theme", false)
        val unlockFont = sPrefs.getBoolean("unlock_font", false)
        val unlockLyricKinetic = sPrefs.getBoolean("unlock_lyric_kinetic", false)
        val blockCommonAds = sPrefs.getBoolean("block_common_ads", false)

        hookInfo.jsonRespParser.hookBeforeMethod({ parseModuleItem }) out@{ param ->
            val path = param.args[1] as? String
            if (BuildConfig.DEBUG && path != "traceid") {
                val json = param.args[2]?.toString() ?: return@out
                Log.d("net.cgi, path: $path, json: $json")
            }
            if (path == "music.recommend.RecommendFeed.get_recommend_feed" && blockLive) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val shelfList = jo.optJSONObject(path)?.optJSONObject("data")
                    ?.optJSONArray("v_shelf") ?: return@out
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
                    param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
                }
            } else if (path == "music.musicsearch.HotkeyService.GetHotkeyAllBusinessForQQMusicMobile"
                && purifySearch.any { it != "scroll" }
            ) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                if (purifySearch.contains("ads"))
                    data.remove("ads")
                if (purifySearch.contains("rcmd"))
                    data.remove("vec_reckey")
                if (purifySearch.contains("rcmd-list")) {
                    data.remove("business_en_cn")
                    data.remove("map_business_hotkey")
                }
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            } else if (path == "music.defaultKey.DefaultKeyService.GetDefaultKeyForQQMusicMobile"
                && purifySearch.contains("scroll")
            ) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                data.remove("keys")
                data.remove("map_business_keys")
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            } else if (path == "notice" && blockCommentBanners) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                if (data.optJSONArray("Banners").isNotEmpty()) {
                    data.remove("Banners")
                    param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
                }
            } else if (path == "recommend" && removeCommentRecommend) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                if (data.optJSONArray("RecItems").isNotEmpty()) {
                    data.remove("RecItems")
                    param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
                }
            } else if (path == "music.sociality.KolEntrance.GetKolEntrance" && removeMineKol) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                if (data.optBoolean("ShowEntrance")) {
                    data.put("ShowEntrance", false)
                    param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
                }
            } else if (path == "music.individuation.Recommend.GetRecommend" && moveDownRecently) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                val shelfList = data.optJSONArray("v_shelf") ?: return@out
                var recentJson: JSONObject? = null
                val newShelf = JSONArray()
                val songListTitle = shelfList.asSequence<JSONObject>()
                    .map { it.optString("title_content") }.toList()
                    .let { tl -> tl.find { it == "收藏歌单" } ?: tl.find { it == "自建歌单" } }
                    ?: return@out
                for (item in shelfList) {
                    val title = item.optString("title_content")
                    if (title != "最近播放" && title != songListTitle) {
                        newShelf.put(item)
                    } else if (title == "最近播放") {
                        recentJson = item
                    } else if (title == songListTitle) {
                        newShelf.put(item)
                        recentJson?.also {
                            newShelf.put(it)
                        } ?: return@out
                    }
                }
                data.put("v_shelf", newShelf)
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            } else if (path == "Personal.PersonalCenterV2.get_subject_info" && hidden && unlockTheme) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                val themeList = data.optJSONArray("vlist") ?: return@out
                data.optJSONObject("alert")?.put("revertTheme", 0)
                for (item in themeList)
                    item.put("enable", 1)
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            } else if (path == "music.lyricsPoster.PicturePoster.getFont" && hidden && unlockFont) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                for (font in data.optJSONArray("fontList").orEmpty())
                    font.put("enable", 1)
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            } else if (path == "musictv.openapi.LyricSvr.GetKineticLyricCategory" && hidden && unlockLyricKinetic) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                for (tab in data.optJSONArray("tabs").orEmpty())
                    for (template in tab.optJSONArray("templates").orEmpty())
                        template.put("vip_needed", "0")
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            } else if (path == "Advert.SdkAdvertServer.ProcessRequest" && blockCommonAds) {
                val json = param.args[2]?.toString() ?: return@out
                val jo = json.runCatchingOrNull { toJSONObject() } ?: return@out
                val data = jo.optJSONObject(path)?.optJSONObject("data") ?: return@out
                // see com.tencent.qqmusic.business.ad.naming.SdkAdId
                // set to 0 for LockScreenLiveController, 10602: AD_ID_PLAYER_LIVE_INFO
                if (data.optInt("musicadtype") == 10602)
                    data.put("musicadtype", 0)
                data.optJSONObject("data")?.run {
                    put("ad_list", JSONArray())
                    put("maxreqtimes", 0)
                    put("maxshowtimes", 0)
                }
                param.args[2] = jo.toString().fromJson(qmPackage.jsonObjectClass) ?: return@out
            }
        }
    }
}
