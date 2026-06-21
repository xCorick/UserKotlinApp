plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.proyecto"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.proyecto"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Material Design (para TextInputLayout, CardView)
    implementation("com.google.android.material:material:1.12.0")
    // CardView (si no viene incluido)
    implementation("androidx.cardview:cardview:1.0.0")

// Retrofit - Para consumir la API REST
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Corrutinas - Para llamadas asíncronas sin bloquear la interfaz
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Lifecycle - Para manejar el ciclo de vida
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Componentes de interfaz
    implementation("androidx.cardview:cardview:1.0.0")       // CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2") // RecyclerView
}