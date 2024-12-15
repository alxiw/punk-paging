// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
	dependencies {
		classpath(libs.bundles.project.classpath)
	}
}

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.jetbrains.kotlin.android) apply false
	alias(libs.plugins.google.ksp) apply false
}

allprojects {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
		maven(url = "https://jitpack.io")
	}
}

task<Delete>("clean") {
	delete = setOf(rootProject.layout.buildDirectory.asFile.get().path)
}
