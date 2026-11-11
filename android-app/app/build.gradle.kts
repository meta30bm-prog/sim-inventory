plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.siminventory.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.siminventory.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
