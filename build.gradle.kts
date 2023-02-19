// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(plugin = "com.github.ben-manes.versions")

buildscript {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:7.4.1")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
		classpath("com.github.ben-manes:gradle-versions-plugin:0.45.0")

		// NOTE: Do not place your application dependencies here; they belong
		// in the individual module build.gradle.kts files
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}

tasks.register("clean", Delete::class) {
	delete(rootProject.buildDir)
}
