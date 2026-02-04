plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.albertocavalcante"
// Version is set by CI from git tag, or defaults to SNAPSHOT for local builds
version = System.getenv("VERSION") ?: "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // Core dependencies (from Bazel's BUILD files)
    api("com.google.guava:guava:33.0.0-jre")
    api("com.google.code.findbugs:jsr305:3.0.2")

    // Annotation processing
    api("com.google.auto.value:auto-value-annotations:1.10.4")
    annotationProcessor("com.google.auto.value:auto-value:1.10.4")

    // Logging
    implementation("com.google.flogger:flogger:0.8")
    runtimeOnly("com.google.flogger:flogger-system-backend:0.8")

    // Error Prone annotations
    implementation("com.google.errorprone:error_prone_annotations:2.24.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Remove -Werror for synced code that may have warnings
    options.compilerArgs.add("-Xlint:all")
}

tasks.withType<Javadoc> {
    options {
        // Suppress warnings for missing javadoc (upstream code)
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Starlark Java")
                description.set("Standalone Starlark interpreter for Java — synced from Bazel")
                url.set("https://github.com/albertocavalcante/starlark-java")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("albertocavalcante")
                        name.set("Alberto Cavalcante")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/albertocavalcante/starlark-java.git")
                    developerConnection.set("scm:git:ssh://github.com/albertocavalcante/starlark-java.git")
                    url.set("https://github.com/albertocavalcante/starlark-java")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/albertocavalcante/starlark-java")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
