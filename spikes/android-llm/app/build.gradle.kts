plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ph.hackathon.spike"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ph.hackathon.spike"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1-spike"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // On-device LLM runtime (Google LiteRT-LM). 0.17.1 pinned: 0.18.0 is only days old.
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.17.1")

    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.core:core-ktx:1.10.1")
}
