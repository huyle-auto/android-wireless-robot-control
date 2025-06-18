plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myrobotapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myrobotapp.test2"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.github.evrencoskun:TableView:0.8.9.4")
    implementation("com.maltaisn:calcdialog:2.2.2")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("org.mindrot:jbcrypt:0.4")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}