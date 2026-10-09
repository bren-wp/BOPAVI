plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

// Play Console requires an upload-key signature. Never commit private keys or
// credential values: supply all four variables only in the protected signing job.
// Normal PR builds intentionally produce an UNSIGNED release bundle.
val playKeystore = System.getenv("BOPAVI_UPLOAD_KEYSTORE_PATH")
val playStorePassword = System.getenv("BOPAVI_UPLOAD_STORE_PASSWORD")
val playKeyAlias = System.getenv("BOPAVI_UPLOAD_KEY_ALIAS")
val playKeyPassword = System.getenv("BOPAVI_UPLOAD_KEY_PASSWORD")
val playValues = listOf(playKeystore, playStorePassword, playKeyAlias, playKeyPassword)
require(playValues.all { it.isNullOrBlank() } || playValues.all { !it.isNullOrBlank() }) {
    "Incomplete Play upload signing environment: provide all four BOPAVI_UPLOAD_* values"
}
val playSigningEnabled = playValues.all { !it.isNullOrBlank() }
android {
    namespace = "com.brendigo.bopavi"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.brendigo.bopavi"
        minSdk = 26
        targetSdk = 36
        versionCode = 31
        versionName = "0.1.30"
    }
    if (playSigningEnabled) {
        signingConfigs {
            create("playUpload") {
                val keystoreFile = file(playKeystore!!)
                require(keystoreFile.isFile) { "Play upload keystore missing at supplied path" }
                storeFile = keystoreFile
                storePassword = playStorePassword!!
                keyAlias = playKeyAlias!!
                keyPassword = playKeyPassword!!
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (playSigningEnabled) signingConfig = signingConfigs.getByName("playUpload")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
