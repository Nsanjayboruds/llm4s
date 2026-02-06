---
layout: page
title: Contributing to LLM4S
nav_order: 2
---

# Contributing to LLM4S

Thank you for your interest in contributing to LLM4S! This guide will help you get started.

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Code of Conduct

We are committed to providing a welcoming and inspiring community for all. Please read and respect our Code of Conduct. All contributors are expected to uphold this code.

---

## Getting Started

### Prerequisites

- Java 21+ (LTS)
- Scala 2.13.16 or 3.7.1+
- Git
- SBT 1.9.0+

### Setup

1. **Fork the Repository**
   - Click the "Fork" button on GitHub
   - Clone your fork:
   ```bash
   git clone https://github.com/YOUR-USERNAME/llm4s.git
   cd llm4s
   ```

2. **Add Upstream Remote**
   ```bash
   git remote add upstream https://github.com/llm4s/llm4s.git
   ```

3. **Install Pre-commit Hook**
   ```bash
   ./hooks/install.sh
   ```

4. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

---

## Before You Start

### Check Existing Issues

Before implementing, search for existing issues or discussions:
- Is this already being worked on?
- Have similar issues been closed?
- Would a discussion help clarify the approach?

### Open an Issue First

For significant work, open an issue first:

- **Bug Reports:** Use the [Bug Report](docs/reference/contribution-guidelines.md) template
- **Features:** Use the [Feature Request](docs/reference/contribution-guidelines.md) template
- **Improvements:** Use the [Enhancement](docs/reference/contribution-guidelines.md) template

Wait for maintainer feedback before coding. This saves time and ensures alignment.

---

## Making Changes

### Code Conventions

**Naming:**
- Types: `PascalCase` (e.g., `UserMessage`, `LLMError`)
- Values/Functions: `camelCase` (e.g., `sendMessage`, `retryConfig`)
- Constants: `SCREAMING_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`, `MAX_RETRIES`)

**Imports:**
- Group in order: scala, java, org.llm4s, other
- Use explicit imports (no wildcards in core code)
- Organize with `sbt scalafmtAll`

**Line Length:**
- 100 characters for Scala code
- 120 characters for documentation

### Code Patterns

**Error Handling:**
```scala
// ✅ GOOD: Use Result[A] for composable error handling
def connect(config: Config): Result[Client] = {
  for {
    validated <- validateConfig(config)
    client <- initializeClient(validated)
  } yield client
}

// ❌ BAD: Throwing exceptions
def connect(config: Config): Client = {
  val validated = validateConfig(config) match {
    case Success(c) => c
    case Failure(e) => throw e  // Don't do this!
  }
  initializeClient(validated)
}
```

**Configuration:**
```scala
// ✅ GOOD: Read config only at app edge
object Main extends App {
  val result = for {
    config <- Llm4sConfig.provider()
    client <- LLMConnect.getClient(config)
  } yield client
}

// ❌ BAD: Reading config in core modules
package org.llm4s.agent

object AgentImpl {
  val apiKey = sys.env.get("OPENAI_API_KEY")  // BANNED!
}
```

**Type Safety:**
```scala
// ✅ GOOD: Newtypes for domain values
case class ApiKey(value: String) extends AnyVal
case class ModelName(value: String) extends AnyVal

// ❌ BAD: Raw strings
def complete(model: String, apiKey: String): Result[Response]
// Can't distinguish between model and API key!
```

**Immutability:**
```scala
// ✅ GOOD: Immutable updates
state.copy(messages = state.messages :+ newMessage)

// ❌ BAD: Mutations
messages += newMessage  // Avoid mutable collections
```

---

## Testing Guidelines

### Test Organization

Tests mirror source structure:
```
modules/core/src/test/scala/org/llm4s/
├── agent/
│   ├── AgentSpec.scala
│   ├── HandoffSpec.scala
│   └── guardrails/
│       └── GuardrailsSpec.scala
├── llmconnect/
│   └── LLMConnectSpec.scala
└── toolapi/
    └── ToolRegistrySpec.scala
```

### Writing Tests

```scala
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MyComponentSpec extends AnyFlatSpec with Matchers {

  "MyComponent" should "handle valid input" in {
    val result = MyComponent.process("valid-input")
    result shouldBe Right(expectedOutput)
  }

  it should "return error for invalid input" in {
    val result = MyComponent.process("invalid")
    result.isLeft shouldBe true
  }

  it should "handle edge cases" in {
    val result = MyComponent.process("")
    result.isLeft shouldBe true
  }
}
```

### Mocking

```scala
import org.scalamock.scalatest.MockFactory

class MyComponentSpec extends AnyFlatSpec with MockFactory {

  "MyComponent" should "call dependencies correctly" in {
    val mockClient = mock[LLMClient]
    
    (mockClient.complete _)
      .expects(*, *)
      .returning(Right(testResponse))
      .once()
    
    val component = new MyComponent(mockClient)
    val result = component.run()
    
    result shouldBe Right(expectedResult)
  }
}
```

### Testing Requirements

- ✅ **Write tests for new code** - Minimum 80% coverage
- ✅ **Test both happy path and error cases**
- ✅ **Test edge cases** (empty inputs, timeouts, network errors)
- ✅ **Test cross-version compatibility** if code touches Scala version specifics
- ✅ **Run `sbt +test`** before submitting PR
- ✅ **Check coverage** with `sbt coverage test coverageReport`

Coverage exclusions:
- Samples and examples
- Workspace runner code
- Main() methods

---

## Submission Process

### 1. Run Tests Locally

```bash
# Format code
sbt scalafmtAll

# Run all tests
sbt +test

# Check coverage
sbt coverage test coverageReport

# Build everything
sbt buildAll
```

**All must pass before submitting PR.**

### 2. Commit Guidelines

**Format:**
```
[TYPE] Brief description (50 chars max)

Optional detailed explanation (72 chars per line)
- Bullet points for multiple changes
- Reference related issues

Fixes #123
Relates to #456
```

**Examples:**
```
[FEATURE] Add streaming support with timeout handling
[BUG FIX] Fix token counting for unicode characters
[DOCS] Update RAG evaluation guide with new metrics
[REFACTOR] Simplify error handling in agent framework
```

Keep commits:
- **Focused** - One logical change per commit
- **Atomic** - Each commit should compile and pass tests
- **Clean** - No debugging statements or temporary code

### 3. Push and Create PR

```bash
# Push to your fork
git push origin feature/your-feature-name

# Create PR on GitHub
# Provide a clear description and reference related issues
```

### 4. PR Checklist

- [ ] Describe the change clearly
- [ ] Reference related issues
- [ ] Note type of change (feature, bug fix, etc.)
- [ ] Confirm testing done
- [ ] Verify documentation updated
- [ ] Run quality checks
- [ ] Sign off on contribution agreement

### 5. Review Process

- **Respond promptly** to reviewer comments
- **Ask for clarification** if feedback is unclear
- **Push updates** to the same branch (don't force push)
- **Mark conversations** as resolved when addressed
- **Be patient** - Maintainers volunteer their time

---

## Adding New Features

### Design Phase

1. **Discuss Design**
   - Open an issue or discussion
   - Explain problem and proposed solution
   - Get feedback before coding

2. **Check Compatibility**
   - Does it work with all LLM providers?
   - Does it work with Scala 2.13 and 3.x?
   - Are there binary compatibility concerns?

3. **Plan Implementation**
   - What modules are affected?
   - Are new dependencies needed?
   - Will it require documentation?

### Implementation Phase

```scala
// 1. Define types
case class MyFeature(setting: String)

// 2. Implement core logic
trait MyFeatureImpl {
  def execute(): Result[Output]
}

// 3. Add public API
trait MyFeatureApi {
  def feature: MyFeature => Result[Output]
}

// 4. Document with Scaladoc
/**
 * Executes MyFeature with given configuration.
 * 
 * @param feature Configuration for the feature
 * @return Result with output or error
 * @example
 * {{{
 * val result = client.executeFeature(MyFeature("config"))
 * }}}
 */
```

### Testing Phase

- Unit tests for core logic
- Integration tests with each LLM provider
- Edge case coverage (timeouts, failures, etc.)
- Performance tests if applicable
- Cross-version tests

### Documentation Phase

- **Scaladoc** - Full API documentation with examples
- **User Guide** - How and when to use the feature (in `docs/guide/`)
- **Configuration** - If needed, update configuration docs
- **Examples** - Add sample in `modules/samples/`
- **Migration** - If breaking change, add migration notes

---

## Documentation Standards

### Scaladoc

```scala
/**
 * Processes an LLM request and returns structured response.
 *
 * The method handles authentication, message formatting,
 * token counting, and result parsing.
 *
 * @param messages Conversation messages with proper roles
 * @param model Optional model override; uses configured default if None
 * @return Right(Response) on success, Left(LLMError) on failure
 *
 * @throws Nothing - All errors are in Result type
 *
 * @example
 * {{{
 * val response = client.complete(
 *   messages = List(
 *     SystemMessage("You are helpful"),
 *     UserMessage("What is Scala?")
 *   ),
 *   model = Some(ModelName("gpt-4o"))
 * )
 * }}}
 *
 * @see [[Message]] for message types
 * @see [[Response]] for response structure
 */
def complete(
  messages: Seq[Message],
  model: Option[ModelName]
): Result[Response]
```

### User Guides

Place in `docs/guide/` with this structure:

```markdown
# Feature Name

## Overview
What this feature does and why it matters.

## When to Use
When should developers use this?

## Basic Usage

```scala
// Minimal working example
```

## Advanced Usage

```scala
// More complex examples
```

## Configuration

Environment variables and settings.

## Common Patterns

Patterns and recipes for this feature.

## Troubleshooting

Common issues and solutions.
```

### Code Comments

```scala
// Comment WHAT and WHY, not HOW

// ✅ GOOD - Explains decision and reason
// Use Result[A] here because this is a fallible operation
// and we want to compose errors with for-comprehensions
def connect(): Result[Connection] = ...

// ❌ BAD - Just repeats code
// Create a connection to the server
def connect(): Connection = ...
```

---

## Release Process

The LLM4S team manages releases. Your contributions will be included automatically.

### Version Numbering

**Pre-release (0.x.y):**
- API may change between versions
- Current: 0.1.0-SNAPSHOT

**Stable (1.0.0+):**
- Follows semantic versioning
- MiMa checks for binary compatibility
- Breaking changes require major version bump

---

## Commit Messages

### Why Good Messages Matter

- Helps others understand changes
- Makes bisecting easier for bug hunting
- Documents decision reasoning
- Appears in release notes

### Format

**Subject line (50 chars max):**
```
[TYPE] Brief imperative description
```

**Types:**
- `[FEATURE]` - New functionality
- `[BUG FIX]` - Bug fix
- `[ENHANCEMENT]` - Improvement to existing feature
- `[REFACTOR]` - Code restructuring
- `[DOCS]` - Documentation only
- `[TEST]` - Test-only changes
- `[PERF]` - Performance improvement

**Examples:**
```
[FEATURE] Add streaming support with configurable timeout
[BUG FIX] Fix token counting for emoji and unicode
[ENHANCEMENT] Improve error messages for configuration errors
[REFACTOR] Simplify agent state management
[DOCS] Update configuration guide with new options
```

**Body (optional, 72 chars per line):**
```
Explain what changed and why, not how.

- Multiple changes as bullets
- Reference issues: Fixes #123
- Mention breaking changes if any

Fixes #123
Relates to #456, #789
BREAKING CHANGE: Config.oldField removed in favor of newField
```

---

## Troubleshooting

### Compilation Issues

```bash
# Clean rebuild
sbt clean compile

# Check Java version
java -version  # Should be 21+

# Check Scala version
sbt scalaVersion  # Should be 3.7.1 or 2.13.16

# Check for common issues
sbt compile -- -deprecation
```

### Test Failures

```bash
# Run specific test
sbt "core/testOnly org.llm4s.agent.AgentSpec"

# Run with verbose output
sbt "core/test -- -oD"

# Run tests for specific Scala version
sbt "+test"  # All versions
sbt "++3.7.1!; test"  # Specific version
```

### Formatting Issues

```bash
# Apply formatting
sbt scalafmtAll

# Check without modifying
sbt scalafmtCheck
```

### Dependency Conflicts

```bash
# View dependency tree
sbt dependencyTree

# Check for updates
sbt dependencyUpdates

# Exclude conflicting versions
libraryDependencies += "org" %% "lib" % "1.0.0" excludeAll(
  ExclusionRule("conflicting-org", "conflicting-lib")
)
```

### Build Cache Issues

```bash
# Clear build cache
rm -rf ~/.sbt
rm -rf ~/.ivy2/cache
sbt update
```

---

## Getting Help

**Documentation:**
- [README.md](README.md) - Project overview
- [docs/guide/](docs/guide/) - User guides
- [docs/getting-started/](docs/getting-started/) - Setup guides
- [Contribution Guidelines](docs/reference/contribution-guidelines.md) - Advanced patterns

**Community:**
- [GitHub Issues](https://github.com/llm4s/llm4s/issues) - Bug reports and features
- [GitHub Discussions](https://github.com/llm4s/llm4s/discussions) - Questions and ideas
- [Discord](https://discord.gg/4uvTPn6qww) - Real-time chat

**Development:**
- [AGENTS.md](AGENTS.md) - Repository structure
- [CLAUDE.md](CLAUDE.md) - Developer patterns
- [API Reference](docs/api/) - Generated from code

---

## Acknowledgments

Thank you for contributing to LLM4S! 🎉

Your contributions - whether code, documentation, bug reports, or community support - help make LLM4S better for everyone. We truly appreciate your effort and enthusiasm.
