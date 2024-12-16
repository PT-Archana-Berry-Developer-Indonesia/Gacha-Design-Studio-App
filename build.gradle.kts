buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath(kotlin("gradle-plugin", version = "1.8.20"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}