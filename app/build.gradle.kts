import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    // 作为 Xposed 模块使用务必添加，其它情况可选
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yst.mkga.hook.dy"
    compileSdk {
        version = release(37) {
        }
    }
    defaultConfig {
        applicationId = "com.yst.mkga.hook.dy"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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