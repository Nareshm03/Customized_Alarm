plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.example.teacherscheduler"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.teacherscheduler"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        resValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

// Workaround for KSP NoSuchFileException: build/generated/ksp/debug/resources
// This ensures the directory exists before the ASM transformation and JavaRes tasks run.
androidComponents {
    onVariants { variant ->
        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val kspResDir = layout.buildDirectory.dir("generated/ksp/${variant.name}/resources")
        val createKspResDirTask = tasks.register("createKspResDirFor$variantName") {
            outputs.dir(kspResDir)
            doLast {
                val dir = kspResDir.get().asFile
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
        }
        
        tasks.matching { 
            it.name == "transform${variantName}ClassesWithAsm" ||
            it.name == "process${variantName}JavaRes"
        }.configureEach {
            dependsOn(createKspResDirTask)
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Lottie Animations
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Fragment and ViewPager
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Navigation component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // SQLCipher for database encryption
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // Firebase BOM (latest version)
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    
    // Firebase Auth, Firestore, and Storage
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-common-ktx")
    
    // Coroutine support for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    
    // For Google Sign-In (updated 2025 approach)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    
    // Google Play Services (legacy - still needed for some features)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Coroutines with Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Image loading
    implementation("io.coil-kt:coil:2.5.0")
    
    // Calendar view (Updated group ID)
    implementation("com.kizitonwose.calendar:view:2.4.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    // Memory leak detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}