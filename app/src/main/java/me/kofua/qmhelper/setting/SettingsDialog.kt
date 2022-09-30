@file:Suppress("DEPRECATION")

package me.kofua.qmhelper.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.ViewGroup
import android.widget.TextView
import me.kofua.qmhelper.R
import me.kofua.qmhelper.XposedInit.Companion.modulePath
import me.kofua.qmhelper.utils.BannerTips
import me.kofua.qmhelper.utils.callMethod
import me.kofua.qmhelper.utils.callMethodOrNull
import me.kofua.qmhelper.utils.currentContext
import me.kofua.qmhelper.utils.getObjectFieldOrNull
import me.kofua.qmhelper.utils.hookAfterMethod
import me.kofua.qmhelper.utils.logFile
import me.kofua.qmhelper.utils.oldLogFile
import me.kofua.qmhelper.utils.shouldSaveLog
import me.kofua.qmhelper.utils.string
import java.io.File
import kotlin.system.exitProcess

class SettingsDialog(context: Context) : AlertDialog.Builder(context) {

    class PrefsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

        private lateinit var prefs: SharedPreferences

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = "qmhelper"
            addPreferencesFromResource(R.xml.prefs_settings)
            prefs = preferenceManager.sharedPreferences
            findPreference("export").onPreferenceClickListener = this
            findPreference("import").onPreferenceClickListener = this
            findPreference("share_log")?.onPreferenceClickListener = this
        }

        @Deprecated("Deprecated in Java", ReplaceWith("true"))
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            return true
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference) = when (preference.key) {
            "export" -> onExportClick()
            "import" -> onImportClick()
            "share_log" -> onShareLogClick()
            else -> false
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                CODE_EXPORT, CODE_IMPORT -> {
                    val prefsFile = File(currentContext.filesDir, "../shared_prefs/qmhelper.xml")
                    val uri = data?.data
                    if (resultCode == Activity.RESULT_CANCELED || uri == null) return
                    when (requestCode) {
                        CODE_IMPORT -> {
                            try {
                                prefsFile.outputStream().use { o ->
                                    activity.contentResolver.openInputStream(uri)?.use {
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
                                    activity.contentResolver.openOutputStream(uri)?.use { o ->
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

        private fun onExportClick(): Boolean {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "text/xml"
                putExtra(Intent.EXTRA_TITLE, "qmhelper.xml")
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            try {
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        string(R.string.save_prefs_file)
                    ), CODE_EXPORT
                )
            } catch (_: ActivityNotFoundException) {
                BannerTips.error(R.string.open_file_manager_failed)
            }
            return true
        }

        private fun onImportClick(): Boolean {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/xml"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            try {
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        string(R.string.choose_prefs_file)
                    ), CODE_IMPORT
                )
            } catch (_: ActivityNotFoundException) {
                BannerTips.error(R.string.open_file_manager_failed)
            }
            return true
        }

        private fun onShareLogClick(): Boolean {
            if ((!logFile.exists() && !oldLogFile.exists()) || !shouldSaveLog) {
                BannerTips.failed(R.string.not_found_log_file)
                return true
            }
            AlertDialog.Builder(activity)
                .setTitle(R.string.prefs_share_log_title)
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
                            File(activity.cacheDir, "com_qq_e_download/log.txt"),
                            overwrite = true
                        )
                        val uri =
                            Uri.parse("content://${activity.packageName}.fileprovider/gdt_sdk_download_path2/log.txt")
                        activity.startActivity(Intent.createChooser(Intent().apply {
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
            return true
        }
    }

    init {
        val activity = context as Activity
        activity.assets.callMethod("addAssetPath", modulePath)
        val prefsFragment = PrefsFragment()
        activity.fragmentManager.run {
            beginTransaction().add(prefsFragment, "Setting").commit()
            executePendingTransactions()
        }

        prefsFragment.onActivityCreated(null)

        val unhook = Preference::class.java.hookAfterMethod(
            "onCreateView", ViewGroup::class.java
        ) { param ->
            if (PreferenceCategory::class.java.isInstance(param.thisObject)
                && TextView::class.java.isInstance(param.result)
            ) {
                val textView = param.result as TextView
                if (textView.textColors.defaultColor == -13816531)
                    textView.setTextColor(Color.GRAY)
            }
        }

        setView(prefsFragment.view)
        setTitle(R.string.settings_title)
        setNegativeButton(R.string.back) { _, _ ->
            unhook?.unhook()
        }
        setPositiveButton(R.string.reboot_host) { _, _ ->
            unhook?.unhook()
            prefsFragment.preferenceManager.forceSavePreference()
            restartApplication(activity)
        }
    }

    companion object {

        private const val CODE_EXPORT = 1145140
        private const val CODE_IMPORT = 1145141

        @JvmStatic
        fun restartApplication(activity: Activity) {
            val pm = activity.packageManager
            val intent = pm.getLaunchIntentForPackage(activity.packageName)
            activity.finishAffinity()
            activity.startActivity(intent)
            exitProcess(0)
        }

        @SuppressLint("CommitPrefEdits")
        @JvmStatic
        fun PreferenceManager.forceSavePreference() {
            sharedPreferences.let {
                val cm = (getObjectFieldOrNull("mEditor")
                    ?: it.edit()).callMethodOrNull("commitToMemory")
                val lock = it.getObjectFieldOrNull("mWritingToDiskLock") ?: return@let
                synchronized(lock) {
                    it.callMethodOrNull("writeToFile", cm, true)
                }
            }
        }
    }
}
