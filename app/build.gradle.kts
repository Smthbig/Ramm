import java.util.Properties

plugins {
    id("com.android.application")
}

val secretProps = Properties().apply {
    val secretPropsFile = rootProject.file("secret.properties")
    if (secretPropsFile.exists()) {
        load(secretPropsFile.inputStream())
    }
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

        vectorDrawables {
            useSupportLibrary = true
        }
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
            storeFile = file(secretProps["RELEASE_STORE_FILE"]?.toString() ?: "")
            storePassword = secretProps["RELEASE_STORE_PASSWORD"]?.toString()
            keyAlias = secretProps["RELEASE_KEY_ALIAS"]?.toString()
            keyPassword = secretProps["RELEASE_KEY_PASSWORD"]?.toString()
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