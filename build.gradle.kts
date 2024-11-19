// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath(kotlin("gradle-plugin", version = "1.8.22"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.8.22" apply false
    kotlin("kapt") version "1.8.22" apply false
}
