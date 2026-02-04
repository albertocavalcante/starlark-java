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

# ==============================================================================
# Development / Experimentation
# ==============================================================================
# These recipes are for iterating on sync configuration before stabilizing.
# Once releases are published, history should be immutable.

# Reset repo to pre-sync state (DESTRUCTIVE - requires confirmation)
[confirm("This will DELETE all commits after pre-sync tag. Continue?")]
reset-to-presync:
    git fetch origin --tags
    git reset --hard pre-sync
    @echo "Reset to pre-sync. Use 'git push --force-with-lease' to update remote."

# Reset and force push (DESTRUCTIVE)
[confirm("This will FORCE PUSH and rewrite remote history. Continue?")]
nuke-sync:
    git fetch origin --tags
    git reset --hard pre-sync
    git push --force-with-lease origin main
    @echo "Remote reset to pre-sync tag."

# Run Copybara sync locally (dry-run)
sync-dry-run: ensure-copybara
    java -jar "{{copybara_jar}}" migrate .copybara/copy.bara.sky sync-starlark-java --dry-run

# Run Copybara sync locally (first time with history)
sync-init: ensure-copybara
    java -jar "{{copybara_jar}}" migrate .copybara/copy.bara.sky sync-starlark-java --init-history

# ==============================================================================
# Tag Management
# ==============================================================================

# Create/update pre-sync tag at current commit
tag-presync:
    @if git rev-parse pre-sync >/dev/null 2>&1; then \
        git tag -d pre-sync; \
        git push origin :refs/tags/pre-sync 2>/dev/null || true; \
    fi
    git tag -a pre-sync -m "Repository bootstrap before initial Copybara sync"
    git push origin pre-sync
    @echo "pre-sync tag set at $(git rev-parse --short HEAD)"
