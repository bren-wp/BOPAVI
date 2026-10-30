plugins { id("com.android.application") }

// GitHub always produces an unsigned release AAB. Publisher-only signing happens
// outside this repository; no environment variable can enable upload signing here.
android {
    namespace = "com.brendigo.bopavi"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.brendigo.bopavi"
        minSdk = 26
        targetSdk = 36
        versionCode = 170
        versionName = "1.0.40"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
// AGP 9 supplies Kotlin support directly, without a second Android plugin.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
