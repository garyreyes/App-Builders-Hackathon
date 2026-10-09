// AGP's built-in Kotlin (2.2.10) compiles the app. The Compose compiler plugin must match it exactly.
plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
