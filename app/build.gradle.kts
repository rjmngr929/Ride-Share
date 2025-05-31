plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.devtools.ksp")

    id("com.google.dagger.hilt.android")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.my.raido" //raido
    compileSdk = 34

    defaultConfig {
        applicationId = "com.my.raido"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.core)
    implementation(libs.transport.runtime)
    implementation(libs.androidx.core.animation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.kotlin.permission)

    implementation(kotlin("script-runtime"))

    // design dynamic
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)

    // lottie animation
    implementation (libs.lottie)

    implementation (libs.glide)
    ksp (libs.compiler)

    //    Hilt-Dagger
    implementation (libs.hilt.android)
    ksp (libs.hilt.compiler)

//    Lifecycles
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

//    Room Database
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    ksp (libs.androidx.room.compiler)

//    Coroutines
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)




    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)



    // Image Loading Library
    implementation (libs.picasso)

    implementation (libs.squareup.okhttp)

// Navigation
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

//    google map api
    implementation (libs.play.services.maps)

    // Places Library (required for search places)
    implementation (libs.places)

//    Firebase
    implementation(libs.firebase.bom)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    // cloud messaging
    implementation (libs.firebase.messaging)

//    Google maps
    implementation (libs.android.maps.utils)

    implementation (libs.androidx.viewpager2)

    implementation (libs.shimmer)

    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-auth:23.1.0")

    implementation ("com.firebase:geofire-android:3.1.0")

//    ************************************************************

    //OlaMap SDK
//    implementation(files("libs/OlaMapSdk-1.6.0.aar"))

    //place SDK
    implementation(files("libs/Places-sdk-2.3.9.jar"))



    //Maplibre
    implementation ("org.maplibre.gl:android-sdk:10.0.2")
    implementation ("org.maplibre.gl:android-plugin-annotation-v9:1.0.0")
    implementation ("org.maplibre.gl:android-plugin-markerview-v9:1.0.0")


//    ************************************
    //OlaMap SDK
    implementation(files("libs/maps-navigation-sdk-1.0.116.aar"))
//    implementation(files("libs/maps-1.0.68.aar"))
    //Required for OlaMap SDK
    implementation ("org.maplibre.gl:android-sdk:10.2.0")
    implementation ("org.maplibre.gl:android-sdk-directions-models:5.9.0")
    implementation ("org.maplibre.gl:android-sdk-services:5.9.0")
    implementation ("org.maplibre.gl:android-sdk-turf:5.9.0")
    implementation ("org.maplibre.gl:android-plugin-markerview-v9:1.0.0")
    implementation ("org.maplibre.gl:android-plugin-annotation-v9:1.0.0")
    implementation ("com.moengage:moe-android-sdk:12.6.01")

    implementation ("androidx.lifecycle:lifecycle-extensions:2.0.0")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.2.1")

    //CashFree
    implementation ("com.cashfree.pg:api:2.1.25")

    //Socket
    implementation ("io.socket:socket.io-client:2.0.1") {
        exclude(
            group = "org.json",
            module = "json"
        )
    }


//    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.12")

}