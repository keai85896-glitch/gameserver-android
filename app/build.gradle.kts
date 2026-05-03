plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tools.gameserver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tools.gameserver"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Fix 4.2: Release 签名配置（替换 debug 签名）
    signingConfigs {
        getByName("debug") {
            // debug 签名保持默认
        }
        create("release") {
            storeFile = file("release-key.jks")
            storePassword = "GameTool2024"
            keyAlias = "gameserver"
            keyPassword = "GameTool2024"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Force use of ARM64 binaries for AAPT2 in Proot environment
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.android.tools.build" && requested.name == "aapt2") {
            useTarget("com.android.tools.build:aapt2:${requested.version}:linux-aarch64")
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // OkHttp — HTTP 客户端（直连目标服务器）
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Kotlin Serialization — JSON 序列化/反序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Security Crypto — EncryptedSharedPreferences（敏感数据加密存储）
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // DataStore Preferences — 替代 SharedPreferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Material Icons Extended — 更多图标
    implementation("androidx.compose.material:material-icons-extended")

    // Material (for pullRefresh)
    implementation("androidx.compose.material:material")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
