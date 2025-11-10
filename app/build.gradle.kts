plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.miplata"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.miplata"
        minSdk = 26
        targetSdk = 36
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
    
    aaptOptions {
        noCompress += listOf("tflite")
    }
}

dependencies {
    implementation("androidx.core:core:1.13.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // --- CORRECCIÓN DE TENSORFLOW LITE ---
    // Versión más moderna de la librería de texto
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")
    // Librería "traductora" para operaciones modernas
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
