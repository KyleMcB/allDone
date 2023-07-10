plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}



dependencies {
    implementation(project(":entities"))
    testImplementation(project(":entities-test"))
}