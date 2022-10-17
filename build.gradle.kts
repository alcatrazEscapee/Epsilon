plugins {
    java
    id("maven-publish")
    id("com.jfrog.artifactory") version "4.25.4"
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

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
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
        mavenLocal()
    }
}


artifactory {
    setContextUrl("https://alcatrazescapee.jfrog.io/artifactory")
    publish {
        repository {
            setRepoKey("mods")
            setUsername(System.getenv("ARTIFACTORY_USERNAME"))
            setPassword(System.getenv("ARTIFACTORY_PASSWORD"))
        }
        defaults {
            publications("mavenJava")
            setPublishArtifacts(true)
            setPublishPom(true)
        }
    }
}