import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.Node

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "org.tpmobile.minghuidaily"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "org.tpmobile.minghuidaily"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = getVersionNameFromResources()//"V1.0.20260202"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            resValue("string", "app_name", "MHDaily")
            buildConfigField("boolean", "MY_DEBUG", "true")
            applicationIdSuffix = ".debug"
        }
        release {
            resValue("string", "app_name", "明慧新闻")
            buildConfigField("boolean", "MY_DEBUG", "false")

            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.zip4j)
    implementation(libs.jsoup)

    implementation(libs.subsampling.scale.image.view)
    implementation(libs.coil)

    implementation(libs.androidx.recyclerview)

}

fun getVersionNameFromResources(): String {
    val stringsFile = file("src/main/res/values/strings.xml")
    val xml = XmlSlurper().parse(stringsFile)
    var versionName: String? = null
    //println(xml.childNodes().forEach { node -> println((node as Node).attributes()) })
    xml.childNodes().forEach { node ->
        (node as Node).attributes().forEach { ss ->
            if (ss.value == "version_name") {
                versionName = node.text().toString()
            }
        }
    }
    if (versionName == null) {
        throw GradleException("String resource 'version_name' not found in strings.xml!")
    }
    println("versionName: $versionName")
    return versionName
}
