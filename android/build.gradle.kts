// LiteRT-LM 0.17.1 (on-phone AI) is compiled with Kotlin 2.4, so AGP's built-in Kotlin is bumped to match,
// as in spikes/android-llm. The Compose compiler plugin must match the Kotlin version exactly.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    }
}

plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
}
