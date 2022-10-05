package me.kofua.qmhelper.setting

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kofua.qmhelper.BuildConfig
import me.kofua.qmhelper.R
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.Decryptor
import me.kofua.qmhelper.utils.Weak
import me.kofua.qmhelper.utils.currentContext
import me.kofua.qmhelper.utils.edit
import me.kofua.qmhelper.utils.iterator
import me.kofua.qmhelper.utils.logFile
import me.kofua.qmhelper.utils.mainScope
import me.kofua.qmhelper.utils.oldLogFile
import me.kofua.qmhelper.utils.realDirPath
import me.kofua.qmhelper.utils.runCatchingOrNull
import me.kofua.qmhelper.utils.sCaches
import me.kofua.qmhelper.utils.sPrefs
import me.kofua.qmhelper.utils.shouldSaveLog
import me.kofua.qmhelper.utils.showMessageDialog
import me.kofua.qmhelper.utils.showMessageDialogX
import me.kofua.qmhelper.utils.string
import me.kofua.qmhelper.utils.stringArray
import org.json.JSONArray
import java.io.File
import java.net.URL
import kotlin.system.exitProcess

class SettingPack {
    var activity by Weak<Activity> { null }

    private var clickCounter = 0

    companion object {
        private const val CODE_EXPORT = 2333
        private const val CODE_IMPORT = 2334
        private const val CODE_STORAGE = 2335
        private const val CODE_CHOOSE_DECRYPT_DIR = 2336

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
        Setting.category(R.string.prefs_category_main)
            ?.let { add(it) }
        Setting.switch(
            R.string.prefs_copy_enhance_title,
            R.string.prefs_copy_enhance_summary,
            isSwitchOn = { sPrefs.getBoolean("copy_enhance", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("copy_enhance", enabled) }
            }
        )?.let { add(it) }

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
            R.string.prefs_hide_music_world_summary,
            isSwitchOn = { sPrefs.getBoolean("hide_music_world", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("hide_music_world", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_hide_vip_bubble_title,
            R.string.prefs_hide_vip_bubble_summary,
            isSwitchOn = { sPrefs.getBoolean("hide_vip_bubble", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("hide_vip_bubble", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_purify_live_guide_title,
            R.string.prefs_purify_live_guide_summary,
            isSwitchOn = { sPrefs.getBoolean("purify_live_guide", false) },
            onSwitchChanged = { enabled: Boolean ->
                sPrefs.edit { putBoolean("purify_live_guide", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_forbid_slide_title,
            R.string.prefs_forbid_slide_summary,
            isSwitchOn = { sPrefs.getBoolean("forbid_slide", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("forbid_slide", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_block_live_title,
            isSwitchOn = { sPrefs.getBoolean("block_live", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("block_live", enabled) }
            }
        )?.let { add(it) }
        Setting.button(
            R.string.prefs_purify_search_title,
        ) {
            onPurifySearchClicked()
        }?.let { add(it) }
        Setting.switch(
            R.string.prefs_hide_ad_bar_title,
            R.string.prefs_hide_ad_bar_summary,
            isSwitchOn = { sPrefs.getBoolean("hide_ad_bar", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("hide_ad_bar", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_forbid_music_world_title,
            R.string.prefs_forbid_music_world_summary,
            isSwitchOn = { sPrefs.getBoolean("forbid_music_world", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("forbid_music_world", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_block_bottom_tips_title,
            R.string.prefs_block_bottom_tips_summary,
            isSwitchOn = { sPrefs.getBoolean("block_bottom_tips", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("block_bottom_tips", enabled) }
            }
        )?.let { add(it) }
        Setting.button(
            R.string.prefs_block_cover_ads_title,
            R.string.prefs_block_cover_ads_summary
        ) {
            onBlockCoverAdsClicked()
        }?.let { add(it) }
        Setting.switch(
            R.string.prefs_block_user_guide_title,
            R.string.prefs_block_user_guide_summary,
            isSwitchOn = { sPrefs.getBoolean("block_user_guide", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("block_user_guide", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_block_comment_banners_title,
            R.string.prefs_block_comment_banners_summary,
            isSwitchOn = { sPrefs.getBoolean("block_comment_banners", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("block_comment_banners", enabled) }
            }
        )?.let { add(it) }
        Setting.switch(
            R.string.prefs_remove_comment_recommend_title,
            R.string.prefs_remove_comment_recommend_summary,
            isSwitchOn = { sPrefs.getBoolean("remove_comment_recommend", false) },
            onSwitchChanged = { enabled ->
                sPrefs.edit { putBoolean("remove_comment_recommend", enabled) }
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

        if (sPrefs.getBoolean("hidden", false)) {
            Setting.category(R.string.prefs_category_hidden)
                ?.let { add(it) }
            Setting.switch(
                R.string.prefs_hidden_title,
                R.string.prefs_hidden_summary,
                isSwitchOn = { sPrefs.getBoolean("hidden", false) },
                onSwitchChanged = { enabled ->
                    sPrefs.edit { putBoolean("hidden", enabled) }
                    if (enabled) {
                        BannerTips.success(R.string.hidden_enabled)
                    } else {
                        BannerTips.success(R.string.hidden_disabled)
                    }
                }
            )?.let { add(it) }
            Setting.button(
                R.string.prefs_decrypt_downloads_title
            ) {
                onDecryptButtonClicked()
            }?.let { add(it) }
        }

        Setting.category(R.string.prefs_category_settings)
            ?.let { add(it) }
        Setting.switch(
            string(R.string.prefs_save_log_title),
            string(R.string.prefs_save_log_summary, logFile.absolutePath),
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
        ) {
            activity?.let { restartApplication(it) }
        }?.let { add(it) }

        Setting.category(R.string.prefs_category_about)
            ?.let { add(it) }
        Setting.button(
            string(R.string.prefs_version_title),
            "%s (%s)".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            arrow = false
        ) {
            onVersionClicked()
        }?.let { add(it) }
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

            CODE_CHOOSE_DECRYPT_DIR -> {
                val uri = data?.data
                if (uri == null || resultCode != Activity.RESULT_OK) return
                val saveDir = uri.realDirPath()?.let { File(it) }
                    ?: run { BannerTips.failed(R.string.invalid_storage_path); return }
                decryptSongs(saveDir)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CODE_STORAGE) {
            val activity = activity ?: return
            if (grantResults.isNotEmpty() && grantResults.first() == PERMISSION_GRANTED) {
                chooseDecryptSaveDir()
            } else {
                val storagePerm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (activity.shouldShowRequestPermissionRationale(storagePerm)) {
                    activity.showMessageDialog(
                        R.string.tips_title,
                        R.string.need_to_request_storage_perm,
                        android.R.string.ok,
                        android.R.string.cancel
                    ) {
                        activity.requestPermissions(arrayOf(storagePerm), CODE_STORAGE)
                    }
                } else {
                    BannerTips.failed(R.string.storage_perm_grant_failed)
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
                activity?.showMessageDialog(
                    string(R.string.found_update_with_version, latestVer),
                    changelog,
                    string(R.string.update_now),
                    string(R.string.i_know)
                ) {
                    val uri = Uri.parse(string(R.string.update_url, latestVerTag))
                    activity?.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
        } else {
            sCaches.edit { remove("latest_version") }
            if (!dialog) return@launch
            BannerTips.success(R.string.no_update)
        }
    }

    private fun onVersionClicked() {
        if (sPrefs.getBoolean("hidden", false) || clickCounter == 7) return
        if (++clickCounter == 7) {
            sPrefs.edit { putBoolean("hidden", true) }
            BannerTips.success(R.string.hidden_enabled)
        } else if (clickCounter >= 4) {
            BannerTips.success(string(R.string.hidden_remain_click_count, 7 - clickCounter))
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

    private fun onPurifySearchClicked() {
        val checkedValues = sPrefs.getStringSet("purify_search", null) ?: setOf()
        val entries = stringArray(R.array.purify_search_entries)
        val values = stringArray(R.array.purify_search_values)
        showMultiChoiceDialog(entries, values, checkedValues) {
            sPrefs.edit { putStringSet("purify_search", it) }
        }
    }

    private fun onDecryptButtonClicked() {
        val activity = activity ?: return

        val storagePerm = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (activity.checkSelfPermission(storagePerm) == PERMISSION_GRANTED) {
            chooseDecryptSaveDir()
        } else {
            activity.requestPermissions(arrayOf(storagePerm), CODE_STORAGE)
        }
    }

    private fun onBlockCoverAdsClicked() {
        val entries = stringArray(R.array.block_cover_ads_entries)
        val values = stringArray(R.array.block_cover_ads_values)
        val checkedValues = sPrefs.getStringSet("block_cover_ads", null) ?: setOf()
        showMultiChoiceDialog(entries, values, checkedValues) {
            sPrefs.edit { putStringSet("block_cover_ads", it) }
        }
    }

    @SuppressLint("InlinedApi")
    private fun chooseDecryptSaveDir() {
        val initialUri = "content://com.android.externalstorage.documents/document/primary:Music"
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(initialUri))
        }
        try {
            activity?.startActivityForResult(intent, CODE_CHOOSE_DECRYPT_DIR)
            BannerTips.success(R.string.pls_choose_save_dir)
        } catch (_: ActivityNotFoundException) {
            BannerTips.error(R.string.open_file_manager_failed)
        }
    }

    private fun decryptSongs(saveDir: File) = mainScope.launch {
        val (total, success, successOrigSongs) = withContext(Dispatchers.IO) {
            Decryptor.batchDecrypt(saveDir) { srcFile, current, total, success ->
                val name = srcFile.name
                if (success) {
                    BannerTips.success(
                        string(R.string.single_decrypt_success, current, total, name)
                    )
                } else {
                    BannerTips.failed(
                        string(R.string.single_decrypt_failed, current, total, name)
                    )
                }
            }
        }
        if (total != 0) delay(3000)
        val failed = total - success
        if (total == 0)
            activity?.showMessageDialog(
                R.string.decrypt_completed_title,
                R.string.decrypt_completed_summary_none,
                android.R.string.ok
            )
        else if (failed == 0)
            activity?.showMessageDialogX(
                string(R.string.decrypt_completed_title),
                string(R.string.decrypt_completed_summary_all, total),
                string(R.string.yes),
                string(R.string.no)
            )?.let {
                if (it) withContext(Dispatchers.IO) {
                    Decryptor.deleteOrigSongs(successOrigSongs)
                }
            }
        else
            activity?.showMessageDialogX(
                string(R.string.decrypt_completed_title),
                string(R.string.decrypt_completed_summary, total, success, failed),
                string(R.string.yes),
                string(R.string.no)
            )?.let {
                if (it) withContext(Dispatchers.IO) {
                    Decryptor.deleteOrigSongs(successOrigSongs)
                }
            }
    }
}
