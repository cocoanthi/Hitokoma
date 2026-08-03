import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
            implementation(compose.material3) // または compose.material
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.datetime)
            // RoomのランタイムとSQLiteドライバーを追加
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            // FileKit (Compose Multiplatform用ファイル/画像ピッカー)
            implementation("io.github.vinceglb:filekit-core:0.8.8")
            implementation("io.github.vinceglb:filekit-compose:0.8.8")
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        // Android 側のプレビュー表示を動作させるために以下も推奨
        androidMain.dependencies {
            implementation(compose.preview)
        }
    }
}

// Roomのコンパイラ（コード自動生成）を設定
dependencies {
    ksp(libs.androidx.room.compiler)
}

// Roomのスキーマ出力先を設定
room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "jp.pinolab.hitokoma"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
