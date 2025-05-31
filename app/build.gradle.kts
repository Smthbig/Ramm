import java.util.Properties

plugins {
    id("com.android.application")
}

val isCi = System.getenv("CI") != null

val signingProps = if (isCi) {
    mapOf(
        "RELEASE_STORE_FILE" to "app/ramm-release-key.jks",
        "RELEASE_STORE_PASSWORD" to System.getenv("RELEASE_STORE_PASSWORD"),
        "RELEASE_KEY_ALIAS" to System.getenv("RELEASE_KEY_ALIAS"),
        "RELEASE_KEY_PASSWORD" to System.getenv("RELEASE_KEY_PASSWORD")
    )
} else {
    val propsFile = rootProject.file("secret.properties")
    val props = Properties().apply {
        if (propsFile.exists()) {
            load(propsFile.inputStream())
        }
    }
    mapOf(
        "RELEASE_STORE_FILE" to props["RELEASE_STORE_FILE"]?.toString(),
        "RELEASE_STORE_PASSWORD" to props["RELEASE_STORE_PASSWORD"]?.toString(),
        "RELEASE_KEY_ALIAS" to props["RELEASE_KEY_ALIAS"]?.toString(),
        "RELEASE_KEY_PASSWORD" to props["RELEASE_KEY_PASSWORD"]?.toString()
    )
}

android {
    namespace = "com.ramm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ramm"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    signingConfigs {
        create("release") {
            val storePath = signingProps["RELEASE_STORE_FILE"]
            if (!storePath.isNullOrBlank()) {
                storeFile = file(storePath)
            }
            storePassword = signingProps["RELEASE_STORE_PASSWORD"]
            keyAlias = signingProps["RELEASE_KEY_ALIAS"]
            keyPassword = signingProps["RELEASE_KEY_PASSWORD"]
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // UI & Material
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")

    // Animation
    implementation("com.airbnb.android:lottie:6.1.0")

    // Database
    implementation("androidx.sqlite:sqlite:2.3.1")

    // QR Code
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Annotations
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("androidx.annotation:annotation:1.7.1")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.8.10")
        }
    }
}