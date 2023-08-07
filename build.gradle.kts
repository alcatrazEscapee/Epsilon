plugins {
    java
    id("maven-publish")
}

group = "com.alcatrazescapee"
version = System.getenv("VERSION") ?: "indev"

repositories {
    mavenCentral()
}

dependencies {

    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.jetbrains:annotations:23.0.0")

    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.alcatrazescapee"
            artifactId = "epsilon"
            version = "${project.version}"
        }
    }
    repositories {
        maven {
            url = uri(System.getenv("MAVEN_URL") ?: "")
            credentials {
                username = System.getenv("MAVEN_USERNAME") ?: "<username>"
                password = System.getenv("MAVEN_PASSWORD") ?: "<password>"
            }
        }
    }
}