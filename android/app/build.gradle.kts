plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

// GitHub always produces an unsigned release AAB. Publisher-only signing happens
// outside this repository; no environment variable can enable upload signing here.
android {
    namespace = "com.brendigo.bopavi"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.brendigo.bopavi"
        minSdk = 26
        targetSdk = 36
        versionCode = 158
        versionName = "1.0.28"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
