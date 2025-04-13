plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.strack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.strack"
        minSdk = 22
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}



dependencies {

    // Versão do Compose
    implementation(libs.threetenabp)
    implementation(libs.material3)
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.material.v120)
    implementation(libs.google.material)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.xmlpull.v1131)
    implementation(libs.androidx.ui.v154)
    implementation(libs.androidx.foundation.v154)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.material3.v112)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.graphics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
