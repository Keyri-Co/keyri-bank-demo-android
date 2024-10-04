plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    defaultConfig {
        namespace = "com.keyrico.scanner"
        minSdk = 23
        compileSdk = 34
    }

    buildTypes.all {
        isMinifyEnabled = true
        proguardFile("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    viewBinding.isEnabled = true
}

dependencies {
    // Keyri
    api(projects.keyrisdk)

    // Core
    implementation(libs.androidx.core.ktx)

    // UI
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.activity.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Camera and analysis
    implementation(libs.google.mlkit.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.view)
}
