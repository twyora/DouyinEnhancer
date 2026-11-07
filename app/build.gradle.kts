@file:Suppress("ktlint:standard:no-wildcard-imports")

import com.google.protobuf.gradle.*
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
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

        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /*
     * Referenced from [RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen/blob/master/app/build.gradle.kts)
     * Thanks to [GSWXXN](https://github.com/GSWXXN)
     */
    val isKeyStoreAvailable = try {
        gropify.keystore.path.isNotBlank() &&
            gropify.keystore.password.isNotBlank() &&
            gropify.key.alias.isNotBlank() &&
            gropify.key.password.isNotBlank()
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
                resolveGitCommitSuffix(rootProject)
            }"
        }
        create("App") {
            dimension = "tier"
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
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

    androidResources {
        additionalParameters += listOf("--package-id", "0x7E", "--allow-reserved-package-id")
    }
}

androidComponents {
    onVariants { variant ->
        val flavorVN = android.productFlavors.findByName(variant.flavorName ?: "")?.versionName
        val vn: String = flavorVN ?: android.defaultConfig.versionName ?: ""
        val buildTypeSuffix = if (variant.buildType == "debug") "-debug" else ""
        variant.outputs.forEach { output ->
            output.outputFileName.set("DouyinEnhancer_${vn}$buildTypeSuffix.apk")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("java")
                id("kotlin")
            }
        }
    }
}

buildscript {
    dependencies {
        classpath(libs.jgit)
    }
}

dependencies {
    // local modules
    implementation(project(":annotation"))
    ksp(project(":processor"))

    // androidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // hooking
    platform(libs.yukihook.bom).apply {
        implementation(this)
        ksp(this)
    }
    implementation(libs.yukihook.core)
    ksp(libs.yukihook.compiler)
    implementation(libs.yukihook.extension.kavaref)
    implementation(libs.yukihook.runtime.libxposed)
    compileOnly(libs.libxposed.api)

    implementation(platform(libs.kavaref.bom))
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.android)
    implementation(libs.kavaref.extension)

    // dex tooling
    implementation(libs.luckypray.dexkit)

    // serialization
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.java.util)
    implementation(libs.protobuf.kotlin)
    implementation(libs.json.canonicalization)

    // storage
    implementation(libs.fastkv)

    // media & utilities
    implementation(libs.gifkt)
    implementation(libs.commons.collections4)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // navigation & lifecycle
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

fun resolveGitCommitSuffix(project: Project) = runCatching {
    val builder = FileRepositoryBuilder().readEnvironment().findGitDir(
        project.projectDir
    ) ?: error(".git directory not found in ${project.projectDir}")

    builder.build().use { repo ->
        val objId = repo.resolve("HEAD^{commit}") ?: error(
            "cannot resolve HEAD commit"
        )

        objId.name.take(7)
    }
}.onFailure {
    println("failed to get git commit id: ${it.message}")
}.getOrDefault("")
