@file:Suppress("UnstableApiUsage")

import org.gradle.internal.os.OperatingSystem
import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
}

fun findInPath(executable: String): String? {
    val pathEnv = System.getenv("PATH")
    return pathEnv.split(File.pathSeparator).map { folder ->
        Paths.get("${folder}${File.separator}${executable}${if (OperatingSystem.current().isWindows) ".exe" else ""}")
            .toFile()
    }.firstOrNull { path ->
        path.exists()
    }?.absolutePath
}

val releaseStoreFile: String? by rootProject
val releaseStorePassword: String? by rootProject
val releaseKeyAlias: String? by rootProject
val releaseKeyPassword: String? by rootProject

val appVerName: String by rootProject
val buildWithGitSuffix: String by rootProject

val kotlinVersion: String by rootProject

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

        externalNativeBuild {
            cmake {
                targets("qmhelper")
                abiFilters("armeabi-v7a", "arm64-v8a", "x86")
                arguments("-DANDROID_STL=none")
                val flags = arrayOf(
                    "-Wall",
                    "-Werror",
                    "-Qunused-arguments",
                    "-Wno-gnu-string-literal-operator-template",
                    "-fno-rtti",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-fno-exceptions",
                    "-fno-stack-protector",
                    "-fomit-frame-pointer",
                    "-Wno-builtin-macro-redefined",
                    "-Wno-unused-value",
                    "-Wno-c++2b-extensions",
                    "-D__FILE__=__FILE_NAME__",
                )
                cppFlags("-std=c++20", *flags)
                cFlags("-std=c18", *flags)
                findInPath("ccache")?.let {
                    println("Using ccache $it")
                    arguments += "-DANDROID_CCACHE=$it"
                }
            }
        }
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
    }

    androidResources {
        noCompress("libqmhelper.so")
    }

    buildTypes {
        all {
            signingConfig = signingConfigs.run {
                find { it.name == "release" } ?: find { it.name == "debug" }
            }
        }
        debug {
            externalNativeBuild {
                cmake {
                    arguments.addAll(
                        arrayOf(
                            "-DCMAKE_CXX_FLAGS_DEBUG=-Og",
                            "-DCMAKE_C_FLAGS_DEBUG=-Og",
                        )
                    )
                }
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
            externalNativeBuild {
                cmake {
                    val flags = arrayOf(
                        "-flto",
                        "-ffunction-sections",
                        "-fdata-sections",
                        "-Wl,--gc-sections",
                        "-fno-unwind-tables",
                        "-fno-asynchronous-unwind-tables",
                        "-Wl,--exclude-libs,ALL",
                    )
                    cppFlags.addAll(flags)
                    cFlags.addAll(flags)
                    val configFlags = arrayOf(
                        "-Oz",
                        "-DNDEBUG"
                    ).joinToString(" ")
                    arguments(
                        "-DCMAKE_BUILD_TYPE=Release",
                        "-DCMAKE_CXX_FLAGS_RELEASE=$configFlags",
                        "-DCMAKE_C_FLAGS_RELEASE=$configFlags",
                        "-DDEBUG_SYMBOLS_PATH=${project.buildDir.absolutePath}/symbols/$name",
                    )
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xuse-k2",
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )
    }

    packagingOptions {
        resources {
            excludes += arrayOf("**")
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
    android.applicationVariants.forEach { variant ->
        if (variant.name != "release") return@forEach
        val variantCapped = variant.name.capitalize()
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
    compileOnly("de.robv.android.xposed:api:82")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("dev.rikka.ndk.thirdparty:cxx:1.2.0")
    implementation("com.linkedin.dexmaker:dexmaker:2.28.3")
}

val restartHost = task("restartHost").doLast {
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

val optimizeReleaseRes = task("optimizeReleaseRes").doLast {
    val aapt2 = Paths.get(
        project.android.sdkDirectory.path,
        "build-tools", project.android.buildToolsVersion, "aapt2"
    )
    val zip = Paths.get(
        project.buildDir.path, "intermediates",
        "optimized_processed_res", "release", "resources-release-optimize.ap_"
    )
    val optimized = File("${zip}.opt")
    val cmd = exec {
        commandLine(aapt2, "optimize", "--collapse-resource-names", "-o", optimized, zip)
        isIgnoreExitValue = true
    }
    if (cmd.exitValue == 0) {
        delete(zip)
        optimized.renameTo(zip.toFile())
    }
}

tasks.whenTaskAdded {
    when (name) {
        "installDebug" -> finalizedBy(restartHost)
        "optimizeReleaseResources" -> finalizedBy(optimizeReleaseRes)
    }
}
