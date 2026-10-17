import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val signingProps = Properties().apply {
    val localFile = rootProject.file("keystore.properties")
    if (localFile.exists()) localFile.inputStream().use { load(it) }
}
fun signingValue(name: String): String? =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: signingProps.getProperty(name)?.takeIf { it.isNotBlank() }

android {
    namespace = "com.snakeid.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.snakeid.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    val storeFilePath = signingValue("OPHIA_STORE_FILE")
    val storePassword = signingValue("OPHIA_STORE_PASSWORD")
    val keyAlias = signingValue("OPHIA_KEY_ALIAS")
    val keyPassword = signingValue("OPHIA_KEY_PASSWORD")
    val canSign = !storeFilePath.isNullOrBlank() &&
        !storePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() &&
        !keyPassword.isNullOrBlank() &&
        file(storeFilePath).exists()
    if (canSign) {
        signingConfigs {
            create("release") {
                storeFile = file(storeFilePath!!)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (canSign) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    androidResources {
        noCompress += "tflite"
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
dependencies {
    implementation(project(":composeApp"))
    implementation(libs.activity.compose)
    implementation("androidx.lifecycle:lifecycle-runtime-android:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-android:2.8.7")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}
