import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    defaultConfig {
        namespace = "com.keyrico.keyrisdk"
        minSdk = 23
        compileSdk = 34

        val version =
            loadProperties("${rootDir}/keyrisdk/keyri.properties").getProperty("keyri.version")

        buildConfigField("String", "VERSION", "\"${version ?: ""}\"")
    }

    buildTypes.all {
        isMinifyEnabled = false
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

    buildFeatures {
        buildConfig = true
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs.plus("-opt-in=kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)

    // Material
    implementation(libs.google.material)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Networking
    implementation(libs.squareup.okhttp3.okhttp)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.squareup.retrofit2.retrofit)

    // Gson
    implementation(libs.google.gson)
}

tasks.register<WriteProperties>("writeVersion") {
    description = "Write Keyri version property"
    destinationFile.set(file("keyri.properties"))

    property("keyri.version", System.getenv("RELEASE_VERSION"))
}
