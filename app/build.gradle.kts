@file:Suppress("UnstableApiUsage")

import org.gradle.configurationcache.extensions.capitalized
import java.io.ByteArrayOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.lsplugin.cmaker)
    alias(libs.plugins.lsplugin.resopt)
}

val releaseStoreFile: String? by rootProject
val releaseStorePassword: String? by rootProject
val releaseKeyAlias: String? by rootProject
val releaseKeyPassword: String? by rootProject

val appVerName: String by rootProject
val buildWithGitSuffix: String by rootProject

val gitCommitCount = "git rev-list HEAD --count".execute().toInt()
val gitCommitHash = "git rev-parse --verify --short HEAD".execute()

fun String.execute(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        workingDir = currentWorkingDir
        commandLine = split(' ')
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

cmaker {
    default {
        targets("qmhelper")
        abiFilters("armeabi-v7a", "arm64-v8a", "x86")
        arguments += "-DANDROID_STL=none"
        cppFlags += "-Wno-c++2b-extensions"
    }

    buildTypes {
        arguments += "-DDEBUG_SYMBOLS_PATH=${project.buildDir.absolutePath}/symbols/${it.name}"
    }
}

android {
    namespace = "me.kofua.qmhelper"
    compileSdk = 33
    ndkVersion = "25.1.8937393"

    defaultConfig {
        applicationId = "me.kofua.qmhelper"
        minSdk = 23
        targetSdk = 33
        versionCode = gitCommitCount
        versionName = appVerName

        if (buildWithGitSuffix.toBoolean())
            versionNameSuffix = ".r$gitCommitCount.$gitCommitHash"
    }

    signingConfigs {
        releaseStoreFile?.let {
            create("release") {
                storeFile = rootProject.file(it)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        } ?: run {
            val keystore = rootProject.file("signing.properties")
                .takeIf { it.isFile } ?: return@run
            create("release") {
                val prop = Properties().apply {
                    keystore.inputStream().use(this::load)
                }
                storeFile = rootProject.file(prop.getProperty("keystore.path"))
                storePassword = prop.getProperty("keystore.password")
                keyAlias = prop.getProperty("key.alias")
                keyPassword = prop.getProperty("key.password")
            }
        }
    }

    buildFeatures {
        prefab = true
        buildConfig = true
    }

    buildTypes {
        all {
            signingConfig = signingConfigs.run {
                find { it.name == "release" } ?: find { it.name == "debug" }
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-language-version=2.0",
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )
    }

    packaging {
        resources {
            excludes += "**"
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    dependenciesInfo {
        includeInApk = false
    }

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x33")
    }

    externalNativeBuild {
        cmake {
            path("src/main/jni/CMakeLists.txt")
            version = "3.22.1+"
        }
    }
}

afterEvaluate {
    tasks.getByPath("installDebug").finalizedBy(restartHost)
    android.applicationVariants.forEach { variant ->
        if (variant.name != "release") return@forEach
        val variantCapped = variant.name.capitalized()
        val packageTask = tasks["package$variantCapped"]

        task<Sync>("sync${variantCapped}Apk") {
            into(variant.name)
            from(packageTask.outputs) {
                include("*.apk")
                rename(".*\\.apk", "QMHelper-v${variant.versionName}.apk")
            }
        }.let { packageTask.finalizedBy(it) }
    }
}

configurations.all {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
}

dependencies {
    compileOnly(libs.xposed)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.coroutines.jdk)
    implementation(libs.androidx.annotation)
    implementation(libs.cxx)
    implementation(libs.dexmaker)
}

val restartHost: Task = task("restartHost").doLast {
    val adb: String = androidComponents.sdkComponents.adb.get().asFile.absolutePath
    exec {
        commandLine(adb, "shell", "am", "force-stop", "com.tencent.qqmusic")
    }
    exec {
        Thread.sleep(2000)
        commandLine(
            adb, "shell", "am", "start",
            "$(pm resolve-activity --components com.tencent.qqmusic)"
        )
    }
}
