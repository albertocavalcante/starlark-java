plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.albertocavalcante"
// Version is set by CI from git tag, or defaults to SNAPSHOT for local builds
version = System.getenv("VERSION") ?: "0.1.0-SNAPSHOT"

java {
    // Upstream uses Java 16+ features (pattern matching in instanceof)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // Core dependencies (from Bazel's BUILD files)
    api(libs.guava)
    api(libs.jsr305)

    // Annotation processing
    api(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    // Logging
    implementation(libs.flogger)
    runtimeOnly(libs.flogger.backend)

    // Error Prone annotations
    implementation(libs.errorprone.annotations)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
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
