plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.cs_project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cs_project"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.excludes += listOf("META-INF/DEPENDENCIES")
        resources.pickFirsts += listOf(
            "**/*.xsd",
            "META-INF/*.md",
            "**/LICENSE.txt",
            "**/Messages.properties",
            "**/Log4j2Plugins.dat"
        )
    }
}

dependencies {
    configurations.all {
        resolutionStrategy {
            force("com.google.guava:guava:32.0.1-jre")
        }
    }
    implementation(libs.matheclipse.core) {
        exclude(group="org.apache.logging.log4j", module="log4j-core")
    }
    implementation("in.hourglass.mathrender:katexmathview:1.0.3")
    implementation("tech.units:indriya:2.2.2")
    implementation(libs.androidx.runtime.livedata)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    runtimeOnly(libs.log4j2.android)

}