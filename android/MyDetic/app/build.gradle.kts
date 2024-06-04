plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "net.ghosttrails.www.mydetic"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "net.ghosttrails.www.mydetic"
}

dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    implementation("com.google.firebase:firebase-firestore")
    testImplementation("junit:junit:4.13.2")
    testImplementation ("org.json:json:20230618")
    testImplementation ("org.robolectric:robolectric:4.10.3")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("androidx.work:work-runtime:2.9.0")
    implementation("com.google.guava:guava:31.1-jre")
}
