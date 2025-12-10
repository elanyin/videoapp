plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.bytedance.videoapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.bytedance.videoapp"
        minSdk = 28
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:1.10.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ExoPlayer (核心 + ui)
    implementation(libs.media3.exoplayer)
    implementation("androidx.media3:media3-ui:1.3.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // RecyclerView / ViewPager2 / ConstraintLayout 都通常已经有
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lottie 用于点赞动画
    implementation("com.airbnb.android:lottie:6.1.0")
    // Paging3（如果想接入分页）
    implementation("androidx.paging:paging-runtime:3.2.0")

    // 下拉刷新
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}