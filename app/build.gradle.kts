import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystorePath = System.getenv("KADRAJ_KEYSTORE")?.takeIf { it.isNotBlank() }
val storePassword = System.getenv("KADRAJ_STORE_PASSWORD")
val keyPassword = System.getenv("KADRAJ_KEY_PASSWORD") ?: storePassword

android {
    namespace = "com.omersusin.kadraj"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.omersusin.kadraj"
        minSdk = 29
        targetSdk = 35
        versionCode = 5
        versionName = "1.4.0"
    }

    signingConfigs {
        if (keystorePath != null && file(keystorePath).exists()) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KADRAJ_STORE_PASSWORD")
                keyAlias = System.getenv("KADRAJ_KEY_ALIAS") ?: "kadraj"
                keyPassword = System.getenv("KADRAJ_KEY_PASSWORD") ?: System.getenv("KADRAJ_STORE_PASSWORD")
                storeType = System.getenv("KADRAJ_STORE_TYPE") ?: "PKCS12"
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    testImplementation("junit:junit:4.13.2")
}
