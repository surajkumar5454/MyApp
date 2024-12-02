plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.details"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.details2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.cardview)

    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.3")

}