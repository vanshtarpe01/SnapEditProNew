plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.snapeditprovs"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.snapeditprovs"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation ("androidx.appcompat:appcompat:1.4.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.google.android.material:material:1.5.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.4.1")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.4.1")

    // Mobile FFmpeg for video processing
    implementation ("com.arthenica:mobile-ffmpeg-full:4.4")

    // ExoPlayer for video playback
    implementation ("com.google.android.exoplayer:exoplayer-core:2.16.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.16.1")

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")

    // TensorFlow Lite for AI features
    implementation ("org.tensorflow:tensorflow-lite:2.8.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.0")

    // For background tasks
    implementation ("androidx.work:work-runtime:2.7.1")

    // For ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Testing libraries
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")
}