plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.lunime.githubcollab.archanaberry.gachadesignstudio"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.lunime.githubcollab.archanaberry.gachadesignstudio"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "v1.0_alpha"
        manifestPlaceholders["MANAGE_EXTERNAL_STORAGE"] = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("commons-io:commons-io:2.11.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.annotation:annotation:1.3.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("commons-io:commons-io:2.8.0")
}
