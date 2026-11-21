import java.util.Properties

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
            val propsFile = rootProject.file("keystore.properties")
            if (propsFile.exists()) {
                val props = Properties().apply { propsFile.inputStream().use { load(it) } }
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
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = if (rootProject.file("keystore.properties").exists()) signingConfigs.getByName("release") else null
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
