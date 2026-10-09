plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.brendigo.bopavi"
    compileSdk = 35
    defaultConfig { applicationId = "com.brendigo.bopavi"; minSdk = 26; targetSdk = 35; versionCode = 26; versionName = "0.1.25" }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
