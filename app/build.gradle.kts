plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventorias"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eventorias"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {


    implementation ("io.coil-kt:coil-compose:2.6.0")

    implementation ("com.google.accompanist:accompanist-permissions:0.34.0")

    implementation("androidx.navigation:navigation-compose:2.8.5")


    implementation ("androidx.compose.ui:ui:1.7.6")
    implementation ("androidx.compose.material3:material3:1.3.1")
    implementation ("androidx.navigation:navigation-compose:2.8.5")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    //firebase
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    // Firebase UI for Authentication
    implementation ("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation ("com.google.firebase:firebase-core:21.1.1")

   // implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}