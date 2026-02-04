# Upstream Dependency Version Synchronization

## Problem

This repo syncs Java source files from `bazelbuild/bazel` via Copybara. However, dependency versions (Guava, JSR305, Auto Value, Flogger, Error Prone) are defined separately:

- **Upstream**: `bazel/third_party/` BUILD files and MODULE.bazel
- **This repo**: `gradle/libs.versions.toml` (Gradle) and `MODULE.bazel` (Bazel)

When upstream bumps a dependency version, we don't automatically know or update.

## Options Considered

### Option 1: Manual monitoring (current)

Keep versions frozen. Rely on CI build failures after sync to detect incompatibilities.

**Pros:**
- Simple, no infrastructure
- Deps are stable APIs, rarely break

**Cons:**
- Could drift from upstream
- No visibility into version differences

### Option 2: CI workflow to check upstream

Add a scheduled workflow that:
1. Fetches `bazelbuild/bazel/third_party/` and `MODULE.bazel`
2. Parses dependency versions
3. Compares with `gradle/libs.versions.toml`
4. Creates issue if versions differ

```yaml
# Example: .github/workflows/check-upstream-deps.yml
name: Check Upstream Dependencies
on:
  schedule:
    - cron: '0 0 * * 1'  # Weekly
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Fetch upstream deps
        run: |
          # Fetch and parse bazel/third_party/BUILD
          # Compare with libs.versions.toml
          # Create issue if different
```

**Pros:**
- Automated visibility
- Can create PRs automatically

**Cons:**
- Parsing Bazel BUILD files is fragile
- Bazel's dep definitions are scattered across files
- Maintenance overhead

### Option 3: Copybara transformation

Add a Copybara transformation that extracts upstream versions during sync:

```python
# In copy.bara.sky
core.transform(
    # Extract versions from third_party and write to UPSTREAM_DEPS.json
)
```

**Pros:**
- Versions captured at sync time
- Single source of truth

**Cons:**
- Complex transformation logic
- Copybara runs infrequently (weekly)
- Still need separate process to update our deps

### Option 4: Dependabot/Renovate (independent updates)

Let Dependabot bump our deps independently of upstream.

**Pros:**
- Zero maintenance
- Always on recent versions

**Cons:**
- Versions won't match upstream
- Could introduce incompatibilities

## Current Decision

**Option 1** - Manual monitoring with frozen deps.

Rationale:
- The Starlark code is explicitly designed to avoid Bazel-specific dependencies
- Core deps (Guava, JSR305, etc.) have stable APIs
- Breaking changes are rare and would surface as CI failures
- Overhead of automated sync doesn't justify the benefit yet

## Future Work

- [ ] Re-evaluate after 6 months of operation
- [ ] If CI failures become frequent due to dep mismatches, implement Option 2
- [ ] Consider Option 3 if Copybara transformations prove reliable

## References

- Upstream source: `bazelbuild/bazel/src/main/java/net/starlark/java/`
- Upstream deps: `bazelbuild/bazel/third_party/`
- Copybara config: `.copybara/copy.bara.sky`
- Version catalog: `gradle/libs.versions.toml`
