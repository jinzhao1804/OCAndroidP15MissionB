// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.sonarqube") version "6.0.1.5171"
}

sonar {
    properties {
        property("sonar.projectKey", "jinzhao1804_OCAndroidP15MissionB")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "jinzhao1804")
        property("sonar.login", "7e4e7a96ed7f0a73481d4ab154ccfb3bb522edbb")
        property("sonar.token", "7e4e7a96ed7f0a73481d4ab154ccfb3bb522edbb")

    }
}