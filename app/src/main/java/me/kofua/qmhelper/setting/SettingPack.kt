package me.kofua.qmhelper.setting

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kofua.qmhelper.BuildConfig
import me.kofua.qmhelper.R
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.Weak
import me.kofua.qmhelper.utils.currentContext
import me.kofua.qmhelper.utils.edit
import me.kofua.qmhelper.utils.iterator
import me.kofua.qmhelper.utils.logFile
import me.kofua.qmhelper.utils.mainScope
import me.kofua.qmhelper.utils.oldLogFile
import me.kofua.qmhelper.utils.runCatchingOrNull
import me.kofua.qmhelper.utils.sCaches
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.shouldSaveLog
import me.kofua.qmhelper.utils.string
import me.kofua.qmhelper.utils.stringArray
import org.json.JSONArray
import java.io.File
import java.net.URL
import kotlin.system.exitProcess

class SettingPack {
    var activity by Weak<Activity> { null }

    companion object {
        private const val CODE_EXPORT = 2333
        private const val CODE_IMPORT = 2334

        @JvmStatic
        fun restartApplication(activity: Activity) {
            val pm = activity.packageManager
            val intent = pm.getLaunchIntentForPackage(activity.packageName)
            activity.finishAffinity()
            activity.startActivity(intent)
            exitProcess(0)
        }
    }

    fun getSettings() = buildList {
        Setting.category(R.string.prefs_category_purify)
            ?.let { add(it) }
        Setting.switch(
            R.string.prefs_purify_splash_title,
            R.string.prefs_purify_splash_summary,
            isSwitchOn = { sPrefs.getBoolean("purify_splash", false) },
            onSwitchChanged = { enabled: Boolean ->
                sPrefs.edit { putBoolean("purify_splash", enabled) }
            }
        )?.let { add(it) }
        Setting.button(
            R.string.prefs_purify_home_top_tab_title,
            R.string.prefs_purify_home_top_tab_summary
        ) {
            onPurifyHomeTopTabClicked()
        }?.let { add(it) }
        Setting.switch(
            R.string.prefs_purify_red_dots_title,
            R.string.prefs_purify_red_dots_summary,
            isSwitchOn = { sPrefs.getBoolean("purify_red_dots", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("purify_red_dots", enabled) }
            }
        )?.let { add(it) }
        Setting.button(
            R.string.prefs_purify_more_items_title,
            R.string.prefs_purify_more_items_summary,
        ) {
            onPurifyMorePageClicked()
        }?.let { add(it) }
        Setting.switch(
            R.string.prefs_hide_music_world_title,
            isSwitchOn = { sPrefs.getBoolean("hide_music_world", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("hide_music_world", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_hide_vip_bubble_title,
            isSwitchOn = { sPrefs.getBoolean("hide_vip_bubble", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("hide_vip_bubble", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_purify_live_guide_title,
            isSwitchOn = { sPrefs.getBoolean("purify_live_guide", false) },
            onSwitchChanged = { enabled: Boolean ->
                sPrefs.edit { putBoolean("purify_live_guide", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_forbid_slide_title,
            isSwitchOn = { sPrefs.getBoolean("forbid_slide", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("forbid_slide", enabled) }
            }
        )?.let { add(it) }

        Setting.category(R.string.prefs_category_backup)
            ?.let { add(it) }
        Setting.button(R.string.prefs_export_title) {
            onExportClicked()
        }?.let { add(it) }
        Setting.button(R.string.prefs_import_title) {
            onImportClicked()
        }?.let { add(it) }

        Setting.category(R.string.prefs_category_settings)
            ?.let { add(it) }
        Setting.switch(
            R.string.prefs_save_log_title,
            isSwitchOn = { sPrefs.getBoolean("save_log", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("save_log", enabled) }
            }
        )?.let { add(it) }
        Setting.button(R.string.prefs_share_log_title) {
            onShareLogClicked()
        }?.let { add(it) }
        Setting.button(
            R.string.reboot_host,
            R.string.reboot_host_summary,
            arrow = false
        ) {
            activity?.let { restartApplication(it) }
        }?.let { add(it) }

        Setting.category(R.string.prefs_category_about)
            ?.let { add(it) }
        Setting.button(
            string(R.string.prefs_version_title),
            "%s (%s)".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            arrow = false
        )?.let { add(it) }
        val latestVer = sCaches.getString("latest_version", "")
        Setting.button(
            R.string.prefs_check_update_title,
            rightDesc = if (!latestVer.isNullOrEmpty()) R.string.found_update else null
        ) {
            checkUpdate(dialog = true, force = true)
        }?.let { add(it) }
        Setting.button(
            R.string.prefs_author_title,
            R.string.prefs_author_summary
        ) {
            onAuthorClicked()
        }?.let { add(it) }
        Setting.button(
            R.string.prefs_tg_channel_title,
            R.string.prefs_tg_channel_summary
        ) {
            onTGChannelClicked()
        }?.let { add(it) }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CODE_EXPORT, CODE_IMPORT -> {
                val prefsFile = File(currentContext.filesDir, "../shared_prefs/qmhelper.xml")
                val uri = data?.data
                if (resultCode == Activity.RESULT_CANCELED || uri == null) return
                when (requestCode) {
                    CODE_IMPORT -> {
                        try {
                            prefsFile.outputStream().use { o ->
                                currentContext.contentResolver.openInputStream(uri)?.use {
                                    it.copyTo(o)
                                }
                            }
                        } catch (_: Exception) {
                        }
                        BannerTips.success(R.string.pls_reboot_host)
                    }

                    CODE_EXPORT -> {
                        try {
                            prefsFile.inputStream().use {
                                currentContext.contentResolver.openOutputStream(uri)?.use { o ->
                                    it.copyTo(o)
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    private fun onPurifyHomeTopTabClicked() {
        val checkedTabIds = sPrefs.getStringSet("purify_home_top_tab", null) ?: setOf()
        val tabs = stringArray(R.array.home_top_tab_entries)
        val tabIds = stringArray(R.array.home_top_tab_values)
        showMultiChoiceDialog(tabs, tabIds, checkedTabIds) {
            sPrefs.edit { putStringSet("purify_home_top_tab", it) }
        }
    }

    private fun onPurifyMorePageClicked() {
        val checkedItemIds = sPrefs.getStringSet("purify_more_items", null) ?: setOf()
        val items = stringArray(R.array.more_items_entries)
        val itemIds = stringArray(R.array.more_items_values)
        showMultiChoiceDialog(items, itemIds, checkedItemIds) {
            sPrefs.edit { putStringSet("purify_more_items", it) }
        }
    }

    private fun showMultiChoiceDialog(
        entries: Array<String>,
        values: Array<String>,
        checkedValues: Set<String>,
        onConfirm: (newCheckedValues: Set<String>) -> Unit
    ) {
        val checkedStates = values.map { checkedValues.contains(it) }
            .toBooleanArray()
        val newCheckedValues = mutableSetOf<String>()
            .apply { addAll(checkedValues) }
        AlertDialog.Builder(activity)
            .setMultiChoiceItems(entries, checkedStates) { _, which, isChecked ->
                if (isChecked) newCheckedValues.add(values[which])
                else newCheckedValues.remove(values[which])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm(newCheckedValues)
            }.show()
    }

    private fun onExportClicked() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "text/xml"
            putExtra(Intent.EXTRA_TITLE, "qmhelper.xml")
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            activity?.startActivityForResult(
                Intent.createChooser(
                    intent,
                    string(R.string.save_prefs_file)
                ), CODE_EXPORT
            )
        } catch (_: ActivityNotFoundException) {
            BannerTips.error(R.string.open_file_manager_failed)
        }
    }

    private fun onImportClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/xml"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            activity?.startActivityForResult(
                Intent.createChooser(
                    intent,
                    string(R.string.choose_prefs_file)
                ), CODE_IMPORT
            )
        } catch (_: ActivityNotFoundException) {
            BannerTips.error(R.string.open_file_manager_failed)
        }
    }

    private fun onShareLogClicked() {
        if ((!logFile.exists() && !oldLogFile.exists()) || !shouldSaveLog) {
            BannerTips.failed(R.string.not_found_log_file)
            return
        }
        AlertDialog.Builder(activity)
            .setTitle(string(R.string.prefs_share_log_title))
            .setItems(
                arrayOf(
                    "log.txt",
                    string(R.string.old_log_item, "old_log.txt")
                )
            ) { _, which ->
                val toShareLog = when (which) {
                    0 -> logFile
                    else -> oldLogFile
                }
                if (toShareLog.exists()) {
                    toShareLog.copyTo(
                        File(activity?.cacheDir, "com_qq_e_download/log.txt"),
                        overwrite = true
                    )
                    val uri =
                        Uri.parse("content://${activity?.packageName}.fileprovider/gdt_sdk_download_path2/log.txt")
                    activity?.startActivity(Intent.createChooser(Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(uri, "text/log")
                    }, string(R.string.prefs_share_log_title)))
                } else {
                    BannerTips.failed(R.string.log_file_not_exist)
                }
            }
            .show()
    }

    fun checkUpdate(dialog: Boolean, force: Boolean = false) = mainScope.launch(Dispatchers.IO) {
        val lastCheckTime = sCaches.getLong("last_check_update_time", 0L)
        if (!force && System.currentTimeMillis() - lastCheckTime < 10 * 60 * 1000)
            return@launch
        val json = URL(string(R.string.releases_url))
            .runCatchingOrNull { readText() } ?: run {
            if (dialog) BannerTips.error(R.string.check_update_failed)
            return@launch
        }
        val jsonArray = json.runCatchingOrNull { JSONArray(this) } ?: run {
            if (dialog) BannerTips.error(R.string.check_update_failed)
            return@launch
        }
        sCaches.edit { putLong("last_check_update_time", System.currentTimeMillis()) }
        var latestVer = ""
        var latestVerTag = ""
        var changelog = ""
        for (result in jsonArray) {
            val tagName = result.optString("tag_name").takeIf {
                it.startsWith("v")
            } ?: continue
            val name = result.optString("name")
            if (name.isNotEmpty() && BuildConfig.VERSION_NAME != name) {
                latestVer = name
                latestVerTag = tagName
                changelog = result.optString("body")
                    .substringAfterLast("更新日志").trim()
            }
            break
        }
        if (latestVer.isNotEmpty()) {
            sCaches.edit { putString("latest_version", latestVer) }
            if (!dialog) return@launch
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(activity)
                    .setTitle(string(R.string.found_update_with_version, latestVer))
                    .setMessage(changelog)
                    .setPositiveButton(string(R.string.update_now)) { _, _ ->
                        val uri = Uri.parse(string(R.string.update_url, latestVerTag))
                        activity?.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                    .setNegativeButton(string(R.string.i_know), null)
                    .show()
            }
        } else {
            sCaches.edit { remove("latest_version") }
            if (!dialog) return@launch
            BannerTips.success(R.string.no_update)
        }
    }

    private fun onAuthorClicked() {
        val uri = Uri.parse(string(R.string.repo_url))
        activity?.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun onTGChannelClicked() {
        val uri = Uri.parse(string(R.string.tg_url))
        activity?.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
