// Top-level build file where you can add configuration options common to all sub-projects/modules.

tasks.register<Delete>("clean") {
    group = "build"
    rootProject.allprojects.forEach {
        delete(it.projectDir.resolve(".gradle"))
        delete(it.projectDir.resolve(".cxx"))
        delete(it.buildDir)
    }
}
