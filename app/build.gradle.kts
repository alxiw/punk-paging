plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "io.github.alxiw.punkpaging"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("room.incremental" to "true"))
            }
        }
    }

    buildTypes {
        getByName("release") {
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
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            kotlinOptions.freeCompilerArgs = listOf(*kotlinOptions.freeCompilerArgs.toTypedArray(), "-Xopt-in=kotlin.RequiresOptIn")
        }
    }

    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")

    // di
    implementation("com.google.dagger:dagger:${Versions.dagger}")
    kapt("com.google.dagger:dagger-compiler:${Versions.dagger}")

    // sup
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("com.google.android.material:material:1.4.0")

    // arch
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.paging:paging-runtime-ktx:${Versions.paging}")

    // rx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")

    // net
    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}")
    implementation("com.squareup.picasso:picasso:2.71828")

    // db
    implementation("androidx.room:room-runtime:${Versions.room}")
    implementation("androidx.room:room-ktx:${Versions.room}")
    implementation("androidx.room:room-paging:${Versions.room}")
    kapt("androidx.room:room-compiler:${Versions.room}")

    // util
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")

    // log
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // debug
    debugImplementation("com.facebook.stetho:stetho-okhttp3:1.6.0")
    debugImplementation("com.readystatesoftware.chuck:library:1.1.0")
}
