import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    // 作为 Xposed 模块使用务必添加，其它情况可选
    alias(libs.plugins.ksp)
}

android {
    namespace = gropify.project.namespace
    compileSdk {
        version = release(gropify.project.compileSdk) {
        }
    }

    defaultConfig {
        applicationId = gropify.project.applicationId
        minSdk = gropify.project.minSdk
        targetSdk = gropify.project.targetSdk
        versionCode = gropify.project.versionCode
        versionName = gropify.project.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /*
     * Referenced from [RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen/blob/master/app/build.gradle.kts)
     * Thanks to [GSWXXN](https://github.com/GSWXXN)
     */
    val isKeyStoreAvailable = try {
        gropify.keystore.path.isNotBlank() && gropify.keystore.password.isNotBlank() && gropify.key.alias.isNotBlank() && gropify.key.password.isNotBlank()
    } catch (_: Exception) {
        false
    }
    if (isKeyStoreAvailable) {
        signingConfigs {
            create("universal") {
                storeFile = file(gropify.keystore.path)
                storePassword = gropify.keystore.password
                keyAlias = gropify.key.alias
                keyPassword = gropify.key.password
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    flavorDimensions += "tier"
    productFlavors {
        create("CI") {
            dimension = "tier"
            versionCode = defaultConfig.versionCode?.plus(1)
            versionName = "${defaultConfig.versionName?.split(Regex("\\s+-\\s+"))?.get(0)}-CI.${
                getGitHeadRefsSuffix(rootProject)
            }"
        }
        create("App") {
            dimension = "tier"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        all {
            signingConfig =
                signingConfigs.findByName("universal") ?: run {
                    println("WARN: Keystore not available, using debug signingConfig")
                    println("NOTE: To set up custom signing, configure the following environment variables:")
                    println("  KEYSTORE_PATH       - Path to the keystore file")
                    println("  KEYSTORE_PASSWORD   - Keystore password")
                    println("  KEY_ALIAS           - Key alias name")
                    println("  KEY_PASSWORD        - Key password")
                    signingConfigs.getByName("debug")
                }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs {
            keepDebugSymbols += "**/libdexkit.so"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

}

androidComponents {
    onVariants { variant ->
        val flavorVN = android.productFlavors.findByName(variant.flavorName ?: "")?.versionName
        val vn: String = flavorVN ?: android.defaultConfig.versionName ?: ""
        val buildTypeSuffix = if (variant.buildType == "debug") "-debug" else ""
        variant.outputs.forEach { output ->
            output.outputFileName.set("DouyinEnhancer_${vn}${buildTypeSuffix}.apk")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // ------------------ 底层与工具库 ------------------
    implementation(libs.luckypray.dexkit)

    // ---------------------- HOOK ----------------------
    // 基础依赖
    implementation(libs.yukihookapi.api)
    // 推荐使用 KavaRef 作为核心反射 API
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
    // 作为 Xposed 模块使用务必添加，其它情况可选
    compileOnly(libs.xposed.api)
    // 作为 Xposed 模块使用务必添加，其它情况可选
    ksp(libs.yukihookapi.ksp.xposed)
}

/**
 * from [MiuiHomeR](https://github.com/qqlittleice/MiuiHome_R/blob/main/app/build.gradle.kts)
 * 用于获取 git commit id
 */
fun getGitHeadRefsSuffix(project: Project): String {
    // .git/HEAD描述当前目录所指向的分支信息，内容示例："ref: refs/heads/master\n"
    val headFile = File(project.rootProject.projectDir, ".git" + File.separator + "HEAD")
    if (headFile.exists()) {
        val string: String = headFile.readText(Charsets.UTF_8)
        val string1 = string.replace(Regex("""ref:|\s"""), "")
        val result = if (string1.isNotBlank() && string1.contains('/')) {
            val refFilePath = ".git" + File.separator + string1
            // 根据HEAD读取当前指向的hash值，路径示例为：".git/refs/heads/master"
            val refFile = File(project.rootProject.projectDir, refFilePath)
            // 索引文件内容为hash值+"\n"，
            // 示例："90312cd9157587d11779ed7be776e3220050b308\n"
            refFile.readText(Charsets.UTF_8).replace(Regex("""\s"""), "").subSequence(0, 7)
        } else {
            string.take(7)
        }
        println("commit_id: $result")
        return result.toString()
    } else {
        println("WARN: .git/HEAD does NOT exist")
        return ""
    }
}