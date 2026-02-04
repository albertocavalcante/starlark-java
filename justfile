# Justfile for starlark-java

set shell := ["bash", "-cu"]

copybara_version := "20251215"
copybara_jar := "/tmp/copybara_deploy.jar"

# List available recipes
default:
    @just --list

# Download Copybara JAR if not present
[private]
ensure-copybara:
    @if [ ! -f "{{copybara_jar}}" ]; then \
        echo "Downloading Copybara {{copybara_version}}..."; \
        curl -fsSL -o "{{copybara_jar}}" \
            "https://github.com/google/copybara/releases/download/v{{copybara_version}}/copybara_deploy.jar"; \
    fi

# Validate Copybara config
validate-copybara: ensure-copybara
    java -jar "{{copybara_jar}}" validate .copybara/copy.bara.sky

# Build with Gradle
build:
    ./gradlew build

# Clean build artifacts
clean:
    ./gradlew clean

# Run tests
test:
    ./gradlew test

# Check (build + test)
check:
    ./gradlew check

# Build with Bazel (after BUILD files are generated)
bazel-build:
    bazel build //...

# Run all validations (used by CI and pre-commit)
validate: validate-copybara check
