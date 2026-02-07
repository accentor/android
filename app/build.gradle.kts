import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "me.vanpetegem.accentor"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.vanpetegem.accentor"
        minSdk = 26
        targetSdk = 36
        versionCode = 47
        versionName = "0.20.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            optIn.add("androidx.compose.foundation.ExperimentalFoundationApi")
            optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
            optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
            optIn.add("coil.annotation.ExperimentalCoilApi")
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    lint {
        quiet = true
        disable.addAll(
            arrayOf(
                "AndroidGradlePluginVersion",
                "GradleDependency",
                "MemberExtensionConflict",
                "NewerVersionAvailable",
                "ObsoleteLintCustomCheck",
                "OldTargetApi",
                "SyntheticAccessor",
                "UnsafeOptInUsageError",
                "UnusedAttribute",
                "VectorPath",
            ),
        )
        checkAllWarnings = true
        ignoreWarnings = false
        warningsAsErrors = true
        textReport = true
        explainIssues = !project.hasProperty("isCI")
    }
}

tasks.lint {
    dependsOn(tasks.ktlintCheck)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(true)
}

dependencies {
    implementation(libs.acra.dialog)
    implementation(libs.acra.mail)
    implementation(libs.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.appcompat)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.core.ktx)
    implementation(libs.fuel)
    implementation(libs.fuel.android)
    implementation(libs.gson)
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.media)
    implementation(libs.media3.common)
    implementation(libs.media3.database)
    implementation(libs.media3.datasource)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(platform(libs.compose.bom))
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
