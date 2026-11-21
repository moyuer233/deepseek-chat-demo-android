import java.util.Properties

/** 签名配置：release 构建必须有它，缺失时显式失败而不是产出未签名包。 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
/** 只有显式请求 release 任务时才要求签名，debug 构建不受影响。 */
val wantsReleaseBuild = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.moyuer233.deepseeknative"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.moyuer233.deepseeknative"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val props = Properties().apply { keystorePropertiesFile.inputStream().use { load(it) } }
                storeFile = rootProject.file(
                    requireNotNull(props.getProperty("storeFile")) { "keystore.properties 缺少 storeFile" }
                )
                storePassword = requireNotNull(props.getProperty("storePassword")) { "keystore.properties 缺少 storePassword" }
                keyAlias = requireNotNull(props.getProperty("keyAlias")) { "keystore.properties 缺少 keyAlias" }
                keyPassword = requireNotNull(props.getProperty("keyPassword")) { "keystore.properties 缺少 keyPassword" }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            } else if (wantsReleaseBuild) {
                throw GradleException(
                    "release 构建需要签名配置：缺少 " + keystorePropertiesFile.absolutePath + "（debug 构建不受影响）"
                )
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

/** release 构建前的签名校验：覆盖 build/assemble 这类会顺带打出 release 的任务。 */
val checkReleaseSigning = tasks.register("checkReleaseSigning") {
    group = "verification"
    description = "release 构建前校验签名配置是否存在"
    doFirst {
        if (!keystorePropertiesFile.exists()) {
            throw GradleException(
                "release 构建需要签名配置：缺少 " + keystorePropertiesFile.absolutePath + "（debug 构建不受影响）"
            )
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(checkReleaseSigning) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
}
