import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

fun generateVersionName(versionCode: Int): String {
    val versionFormat = "$versionCode.0"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val date = Date()
    return "$versionFormat ${dateFormat.format(date)}"
}

android {
    namespace = "csit.puet"
    compileSdk = 34

    defaultConfig {
        applicationId = "csit.puet"
        minSdk = 26
        targetSdk = 34
        versionCode = 55
        versionName = generateVersionName(versionCode!!)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_TOKEN", "\"token: yc9diebszhke5piqg9gc\"")
        buildFeatures {
            buildConfig = true
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
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

    packaging {
        resources.excludes += setOf("META-INF/INDEX.LIST", "META-INF/DEPENDENCIES")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.annotation:annotation:1.8.2")
    implementation ("androidx.work:work-runtime:2.9.1")
    implementation ("androidx.work:work-runtime-ktx:2.9.1")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Google Account Credential
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:1.32.2")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20240705-2.0.0")
    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation ("com.google.android.play:integrity:1.4.0")

    // Jackson
    implementation("com.google.http-client:google-http-client-jackson2:1.39.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.3")
}