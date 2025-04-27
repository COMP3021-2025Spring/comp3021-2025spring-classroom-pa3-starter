plugins {
    java
    application
    checkstyle
}

group = "hk.ust.cse.comp3021"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.json:json:20240303")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.jline:jline:3.25.1")
    implementation("org.jline:jline-terminal-jansi:3.25.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed")
        showStandardStreams = true
    }
    // parallel request may be banned by the server
    maxParallelForks = 1
    maxHeapSize = "1g"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err
    mainClass = "hk.ust.cse.comp3021.Main"
}