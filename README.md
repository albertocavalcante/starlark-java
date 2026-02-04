# starlark-java

[![CI](https://github.com/albertocavalcante/starlark-java/actions/workflows/ci.yml/badge.svg)](https://github.com/albertocavalcante/starlark-java/actions/workflows/ci.yml)
[![Sync](https://github.com/albertocavalcante/starlark-java/actions/workflows/sync.yml/badge.svg)](https://github.com/albertocavalcante/starlark-java/actions/workflows/sync.yml)
[![License](https://img.shields.io/github/license/albertocavalcante/starlark-java)](LICENSE)

Standalone Starlark interpreter for Java — automatically synced from [Bazel](https://github.com/bazelbuild/bazel).

**Full interpreter included.** Unlike syntax-only libraries, this package provides the complete Starlark runtime: lexer, parser, AST, resolver, and evaluator.

## Why This Package?

The official Starlark Java implementation lives inside Bazel's monorepo, making it difficult to use standalone. However, the Bazel developers explicitly designed it to be independent — BUILD files contain comments like _"Do not add Bazel or Google dependencies here!"_

This package extracts the `net.starlark.java` packages for standalone use, staying current via automated [Copybara](https://github.com/google/copybara) syncs.

## Installation

### Gradle (GitHub Packages)

Add the GitHub Packages repository and dependency:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/albertocavalcante/starlark-java")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}

// build.gradle.kts
dependencies {
    implementation("io.github.albertocavalcante:starlark-java:0.1.0")
}
```

### Maven (GitHub Packages)

Add to your `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>
```

Then in your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/albertocavalcante/starlark-java</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.albertocavalcante</groupId>
    <artifactId>starlark-java</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

> **Note:** GitHub Packages requires authentication even for public packages. You need a GitHub token with `read:packages` scope.

### Bazel

BUILD files are synced from upstream. Add this repository as a dependency and use:

```python
deps = ["@starlark_java//src/main/java/net/starlark/java/syntax"]
```

## Usage

### Parsing (Syntax Only)

```java
import net.starlark.java.syntax.*;

String code = "def hello(name): return 'Hello, ' + name";
ParserInput input = ParserInput.fromString(code, "<example>");
StarlarkFile file = StarlarkFile.parse(input);

if (!file.ok()) {
    for (SyntaxError error : file.errors()) {
        System.err.println(error);
    }
}
```

### Full Interpretation

```java
import net.starlark.java.eval.*;
import net.starlark.java.syntax.*;

String code = """
    def factorial(n):
        if n <= 1:
            return 1
        return n * factorial(n - 1)

    result = factorial(5)
    """;

Module module = Module.create();
try (Mutability mu = Mutability.create("example")) {
    StarlarkThread thread = StarlarkThread.createTransient(mu, StarlarkSemantics.DEFAULT);

    ParserInput input = ParserInput.fromString(code, "<example>");
    Starlark.execFile(input, FileOptions.DEFAULT, module, thread);

    Object result = module.getGlobal("result");
    System.out.println("factorial(5) = " + result);  // 120
}
```

## Packages

| Package | Description |
|---------|-------------|
| `net.starlark.java.syntax` | Lexer, parser, AST nodes, resolver |
| `net.starlark.java.eval` | Interpreter, built-in functions, types |
| `net.starlark.java.annot` | Annotations for exposing Java to Starlark |
| `net.starlark.java.spelling` | Spell checker for error suggestions |

## What's NOT Included

- **lib/json** — Has one Bazel-specific import, excluded to keep the package pure
- **cmd** — Standalone REPL, can be added later
- **Native profiler** — JNI code for CPU profiling, optional

## Build

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Versioning

Versions follow: `v0.YYYYMMDD.N`

See `VERSION.json` for exact upstream commit references (generated after first sync).

## What You Need To Do

To use this package from GitHub Packages, you need:

1. **GitHub Token** with `read:packages` scope
2. Configure your build tool to authenticate with GitHub Packages (see Installation above)

To publish releases (maintainers only):

1. Push a tag: `git tag v0.20260204.0 && git push --tags`
2. The release workflow will automatically build and publish to GitHub Packages

## Acknowledgements

This package automatically syncs code from:

- **[bazelbuild/bazel](https://github.com/bazelbuild/bazel)** — Apache 2.0, Copyright The Bazel Authors

The Starlark Java implementation was designed by the Bazel team to be Bazel-independent.

See `NOTICE` and `LICENSE-bazel` for full attribution.

## License

Apache 2.0 — See [LICENSE](LICENSE)
