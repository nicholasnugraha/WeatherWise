allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

subprojects {
    project.evaluationDependsOn(":app")
}

// Safety net: force compileSdkVersion and minSdkVersion for all Android library subprojects
// Some Flutter plugins (e.g., geolocator_android 4.6+) reference `flutter.compileSdkVersion`
// which is only available in Flutter 3.27+. This ensures the build doesn't fail on those
// plugins by setting concrete values for all library modules.
subprojects {
    afterEvaluate {
        if (project.hasProperty("android")) {
            val androidExt = project.extensions.findByName("android")
            if (androidExt is com.android.build.gradle.LibraryExtension) {
                if (androidExt.compileSdk == null) {
                    androidExt.compileSdk = 34
                }
                if (androidExt.defaultConfig.minSdk == null) {
                    androidExt.defaultConfig.minSdk = 21
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
