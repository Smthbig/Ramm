plugins {
    id("com.android.application")
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

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Material Design & UI
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")

    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.1.0")

    // SQLite (if not using Room)
    implementation("androidx.sqlite:sqlite:2.3.1")
    // ZXing core for QR code generation
    implementation("com.google.zxing:core:3.5.2")
    // Optional: ZXing Android Integration (only if you're using it)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
     // This resolves javax.annotation.Nullable during R8 shrink
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("androidx.annotation:annotation:1.7.1")

    // LeakCanary (Debugging Tool)
   // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
   

}

// ✅ Fix for Kotlin version conflict
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.8.10")
        }
    }
}