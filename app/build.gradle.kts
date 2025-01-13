import com.android.build.gradle.BaseExtension


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    jacoco

}

android {
    signingConfigs {
        create("config") {
            storeFile = file(project.property("KEYSTORE_FILE")!!)
            storePassword = project.property("KEYSTORE_PASSWORD")?.toString()
            keyAlias = project.property("KEY_ALIAS")?.toString()
            keyPassword = project.property("KEY_PASSWORD")?.toString()
        }
    }
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
            signingConfig =  signingConfigs.getByName("config")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    // Add this block for unit test configuration
    testOptions {
        unitTests.all {
            // Configure JVM arguments for unit tests
            it.jvmArgs = listOf("-Dnet.bytebuddy.experimental=true")
            it.jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")

        }
    }
    testOptions.unitTests.isIncludeAndroidResources = true


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig fields

    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true

            buildConfigField("String", "MY_API_KEY", "\"${project.property("MY_API_KEY")}\"")

        }
        release {

            buildConfigField("String", "MY_API_KEY", "\"${project.property("MY_API_KEY")}\"")

        }
        getByName("release") {
            // Enables code shrinking, obfuscation, and optimization for only
            // your project's release build type. Make sure to use a build
            // variant with `isDebuggable=false`.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("config")

            proguardFiles(
                // Includes the default ProGuard rules files that are packaged with
                // the Android Gradle plugin. To learn more, go to the section about
                // R8 configuration files.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                // Includes a local, custom Proguard rules file
                "proguard-rules.pro"
            )
        }
    }
    defaultConfig {
        buildConfigField("String", "MY_API_KEY", "\"${project.property("MY_API_KEY")}\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/NOTICE.md"
        }
    }
}




dependencies {


    implementation(libs.core.ktx)
    implementation(libs.androidx.navigation.testing)


    // Core dependencies
    androidTestImplementation ("androidx.test:runner:1.6.2")
    androidTestImplementation ("androidx.test:rules:1.6.1")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")

    // Tests
    androidTestImplementation ("io.mockk:mockk-android:1.13.4")
    androidTestImplementation ("org.mockito:mockito-android:4.0.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation ("org.mockito:mockito-inline:5.2.0")
    implementation ("net.bytebuddy:byte-buddy:1.14.9")
    testImplementation ("org.slf4j:slf4j-simple:2.0.6")
    testImplementation ("io.mockk:mockk:1.13.4")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.mockito:mockito-core:4.2.0")
    testImplementation ("org.mockito:mockito-core:4.2.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

    implementation ("com.google.android.material:material:1.12.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation ("io.github.cdimascio:dotenv-kotlin:6.4.0")

    implementation ("androidx.work:work-runtime:2.10.0")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.firebase:firebase-messaging:24.1.0")


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
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


jacoco {
    toolVersion = "0.8.12"
    reportsDirectory = layout.buildDirectory.dir("customJacocoReportDir")
}

val exclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*"
)

tasks.withType(Test::class) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val androidExtension = extensions.getByType<BaseExtension>()

val jacocoTestReport by tasks.registering(JacocoReport::class) {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")
    group = "Reporting"
    description = "Generate Jacoco coverage reports"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug")
    val mainSrc = androidExtension.sourceSets.getByName("main").java.srcDirs

    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files(mainSrc))
    executionData.setFrom(fileTree(buildDir) {
        include("**/*.exec", "**/*.ec")
    })
}
