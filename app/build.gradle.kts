plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.snackstream"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.snackstream"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.cardview)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // splash screen
    implementation("androidx.core:core-splashscreen:1.0.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.10.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.9.7")
    implementation("androidx.navigation:navigation-ui:2.9.7")

    // WorkManager
    implementation("androidx.work:work-runtime:2.11.2")

    //RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // paging for infinite scroll
    implementation("androidx.paging:paging-common-android:3.4.2")
    implementation("androidx.paging:paging-runtime:3.4.2")

    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    annotationProcessor("androidx.room:room-compiler:2.6.0")

    // MDC
    implementation("com.google.android.material:material:1.11.0")

    // For credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Import the BoM for the Firebase platform
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:5.0.7")

    // Cloudinary cloud storage
    implementation("com.cloudinary:cloudinary-android:3.1.2")
    // Download + Preprocess:
    implementation("com.cloudinary:cloudinary-android-download:3.1.2")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.1.2")

    // for loading reels
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

}