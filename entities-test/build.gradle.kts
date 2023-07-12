plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    kotlin("plugin.serialization")
}

kotlin {
    android()
    jvm("desktop")



    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "business log primitive"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "entities-test"
            isStatic = true
        }
        extraSpecAttributes["resources"] =
            "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val kotlin_version = extra["kotlin.version"] as String
        val kotestVersion = extra["kotest.property.version"] as String
        val coroutinesVersion = extra["kotlinx.coroutines.version"] as String
        val commonMain by getting {
            dependencies {
                api(kotlin("test"))
                implementation(project(":entities"))
                api("io.kotest:kotest-property:$kotestVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                api("io.kotest:kotest-assertions-core:$kotestVersion")
            }
        }
        val commonTest by getting {
            dependencies {
            }
        }

        val androidMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val desktopMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
            }
        }

    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.xingpeds.alldone.entities.test"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)

    }

}
