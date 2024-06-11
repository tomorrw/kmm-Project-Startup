plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("maven-publish")
    id("kover")
    id("com.android.library")
    id("kotlinx-serialization")
}

group = commonLibs.versions.library.group.get()
version = commonLibs.versions.library.version.get()
var androidTarget: String = ""

kotlin {
    android {
        publishLibraryVariants("release")
        publishLibraryVariantsGroupedByFlavor = true
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        val iosDefinitions = commonLibs.versions.ios
        name = iosDefinitions.baseName.get()
        summary = iosDefinitions.summary.get()
        homepage = iosDefinitions.homepage.get()
        authors = iosDefinitions.authors.get()
        version = commonLibs.versions.library.version.get()
        ios.deploymentTarget = iosDefinitions.deployment.target.get()
        publishDir = rootProject.file("./")
        framework {
            baseName = "shared"
            isStatic = true
            transitiveExport = true
            embedBitcode(org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.BITCODE)
        }
        pod(name = "libPhoneNumber-iOS") {
            moduleName = "libPhoneNumber_iOS"
            source = git("https://github.com/iziz/libPhoneNumber-iOS")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(commonLibs.kotlin.stdlib)

                with(commonLibs.kotlin) {
                    implementation(dateTime)
                    implementation(coroutinesCore)
                    implementation(serializationJson)
                }

                with(commonLibs.ktor) {
                    implementation(clientCore)
                    implementation(clientJson)
                    implementation(json)
                    implementation(clientLogging)
                    implementation(auth)
                    implementation(contentNegotiation)
                    implementation(clientWebsocket)
                }
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("com.googlecode.libphonenumber:libphonenumber:8.12.32")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(commonLibs.ktor.clientDarwin)
            }
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

val javadocJar = tasks.register("javadocJar", Jar::class.java) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("shared")
                description.set("Shared code between Android and iOS")
                url.set("https://github.com/tomorrw/kmm-Project-Startup")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("Marc-Jalkh")
                        name.set("Marc Jalkh")
                        email.set("marc@tomorrow.services")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/tomorrw/kmm-Project-Startup")
                    url.set("https://github.com/tomorrw/kmm-Project-Startup")
                }

            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            setUrl("https://github.com/tomorrw/kmm-Project-Startup")
            credentials {
                username = "Marc-Jalkh"
            }
        }
    }
}

android {
    namespace = commonLibs.versions.library.group.get()
    compileSdk = commonLibs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = commonLibs.versions.android.minSdk.get().toInt()
        targetSdk = commonLibs.versions.android.compileSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    beforeEvaluate {
        libraryVariants.all {
            compileOptions {
                // Flag to enable support for the new language APIs
                isCoreLibraryDesugaringEnabled = true
            }
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
