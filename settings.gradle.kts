pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.highcapable.gropify") version "1.0.1"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://api.xposed.info/")
    }
}

gropify {
    projects(":app") {
        buildscript {
            existsPropertyFiles(
                "local.properties",
                addDefault = true
            )
            permanentKeyValues(
                "KEYSTORE_PATH" to "",
                "KEYSTORE_PASSWORD" to "",
                "KEY_ALIAS" to "",
                "KEY_PASSWORD" to ""
            )
            locations(
                GropifyLocation.SystemEnv,
                GropifyLocation.RootProject,
                GropifyLocation.CurrentProject
            )
        }
    }
}

rootProject.name = "DouyinEnhancer"
include(":app")
