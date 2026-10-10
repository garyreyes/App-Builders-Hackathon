plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ph.appbuilders.offlinehealth"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ph.appbuilders.offlinehealth"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        ndk {
            // Phones are arm64; x86_64 lets the same APK run the on-phone AI path on the emulator.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true // VERSION_NAME for the Settings footer
    }
}

// Every version is pinned. Compose artifacts take theirs from the BOM.
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.8.0")
    // Same lifecycle version Compose already resolves (2.8.7), so this adds no version bump.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.10.1")

    // On-phone AI runtime (Google LiteRT-LM), same pin as spikes/android-llm: 0.18.0 was days old on Oct 9.
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.17.1")
    implementation(project(":llama-runtime"))

    testImplementation("junit:junit:4.13.2")
    // Android's org.json is a stub in JVM unit tests; this is the real one.
    testImplementation("org.json:json:20240303")
    // Same coroutines version the app already resolves (1.9.0).
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
