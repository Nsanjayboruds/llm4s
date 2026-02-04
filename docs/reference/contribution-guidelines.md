---
layout: page
title: Contribution Guidelines
parent: Reference
nav_order: 7
---

# Contribution Guidelines

Detailed guidelines for contributing to LLM4S with emphasis on code quality, testing, and documentation.

{: .fs-6 .fw-300 }

## Table of Contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## What Can You Contribute?

### Code Contributions
- **Features** - New functionality aligned with roadmap
- **Bug Fixes** - Reported issues and edge cases
- **Enhancements** - Improvements to existing features
- **Refactoring** - Code quality and maintainability improvements
- **Tests** - Additional test coverage and edge case handling
- **Performance** - Optimizations with benchmarks

### Non-Code Contributions
- **Documentation** - Guides, examples, API docs
- **Tutorials** - Step-by-step examples
- **Blog Posts** - Educational content about LLM4S
- **Issue Triage** - Help categorize and organize issues
- **Community** - Answering questions on Discord/GitHub

### Don't Contribute
- **Closed PRs** - Don't reopen without discussion
- **Abandoned Issues** - Check status before starting
- **External Tools** - LLM4S is focused and intentional about scope

---

## The Contribution Workflow

### 1. Find or Create an Issue

**Search first:**
```bash
# GitHub Issues search
https://github.com/llm4s/llm4s/issues?q=is:issue+label:good-first-issue
```

**Good first issues** are marked with `good-first-issue` label.

**Create if not found:**
- For bugs: Use [Bug Report](../.github/ISSUE_TEMPLATE/bug_report.yml) template
- For features: Use [Feature Request](../.github/ISSUE_TEMPLATE/feature_request.yml) template
- For improvements: Use [Enhancement](../.github/ISSUE_TEMPLATE/enhancement.yml) template

**Wait for response:**
- Maintainers will provide guidance
- Confirm approach before coding

### 2. Fork and Setup

```bash
# Fork on GitHub (click button)

# Clone your fork
git clone https://github.com/YOUR-USERNAME/llm4s.git
cd llm4s

# Add upstream
git remote add upstream https://github.com/llm4s/llm4s.git

# Install pre-commit hook
./hooks/install.sh

# Create feature branch
git checkout -b feature/description
```

### 3. Make Your Changes

**Follow conventions:**
- Use `Result[A]` for errors
- Configure at app edge only
- Keep code immutable
- Use type-safe newtypes
- Run `sbt scalafmtAll`

**Write tests:**
- Unit tests for new code
- Edge case coverage
- Cross-version tests if needed

**Update docs:**
- Scaladoc for public APIs
- User guides if needed
- Examples if appropriate

### 4. Test Thoroughly

```bash
# Format code
sbt scalafmtAll

# Compile
sbt +compile

# Run all tests
sbt +test

# Check coverage
sbt coverage test coverageReport

# Cross-compile everything
sbt buildAll
```

**All must pass before submitting PR.**

### 5. Submit Pull Request

**Title format:**
```
[TYPE] Brief description

[FEATURE] Add support for streaming with timeout
[BUG FIX] Fix token counting edge case
[DOCS] Update configuration guide
```

**Use PR template** (auto-populated from `.github/pull_request_template.md`):
- Clear description
- Issue references
- Testing approach
- Documentation updates
- Checklist verification

### 6. Respond to Feedback

- Be responsive to comments
- Ask for clarification if needed
- Push updates to same branch
- Conversation closes PR when satisfied
- Maintainers merge when ready

---

## Code Standards

### Error Handling Pattern

```scala
// GOOD: Use Result[A]
def processData(data: String): Result[ProcessedData] = {
  for {
    parsed <- parseJson(data)
    validated <- validate(parsed)
  } yield ProcessedData(validated)
}

// BAD: Throwing exceptions
def processData(data: String): ProcessedData = {
  val parsed = parseJson(data) match {
    case Success(p) => p
    case Failure(e) => throw e  // DON'T DO THIS
  }
  validate(parsed)
}
```

### Configuration Pattern

```scala
// GOOD: Configuration at app edge
object Main extends App {
  val result = for {
    config <- Llm4sConfig.provider()
    client <- LLMConnect.getClient(config)
  } yield client
}

// BAD: Reading config in core code
package org.llm4s.agent

object AgentImpl {
  val apiKey = sys.env.get("OPENAI_API_KEY")  // BANNED!
}
```

### Type Safety Pattern

```scala
// GOOD: Newtypes for domain values
case class ApiKey(value: String) extends AnyVal
case class ModelName(value: String) extends AnyVal

def complete(
  model: ModelName,
  apiKey: ApiKey
): Result[Response] = ...

// BAD: Raw strings
def complete(
  model: String,    // Could be anything!
  apiKey: String
): Result[Response] = ...
```

### Immutability Pattern

```scala
// GOOD: Immutable updates
case class AgentState(
  messages: Seq[Message],
  toolResults: Seq[ToolResult]
) {
  def addMessage(msg: Message): AgentState =
    this.copy(messages = messages :+ msg)
  
  def recordToolResult(result: ToolResult): AgentState =
    this.copy(toolResults = toolResults :+ result)
}

// BAD: Mutable modifications
class AgentStateMutable {
  var messages = scala.collection.mutable.Buffer[Message]()
  def addMessage(msg: Message): Unit = {
    messages += msg  // Mutation!
  }
}
```

---

## Testing Standards

### Test File Location

```
modules/core/src/test/scala/org/llm4s/
└── [mirror source package structure]
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

### Minimal Test Example

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

### Testing with Mocks

```scala
import org.scalamock.scalatest.MockFactory

class MyComponentSpec extends AnyFlatSpec with MockFactory {

  "MyComponent" should "use dependencies correctly" in {
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

### Coverage Requirements

```bash
# Run with coverage
sbt coverage test coverageReport

# Must maintain 80%+ statement coverage
```

**Exclude from coverage:**
- Samples and examples
- Workspace runner code
- Integration tests (optional)
- Main() methods

---

## Documentation Standards

### Scaladoc Format

```scala
/**
 * Processes an LLM request and returns structured response.
 *
 * The method handles authentication, message formatting,
 * token counting, and result parsing.
 *
 * @param messages Sequence of conversation messages
 *                 (system, user, assistant roles supported)
 * @param model    Optional model override; uses configured model if None
 * @return Right(CompletionResponse) on success
 *         Left(LLMError) if API call fails or validation fails
 *
 * @throws Nothing - All errors are represented in Result type
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
 * @see [[CompletionResponse]] for response structure
 * @see [[LLMError]] for error types
 */
def complete(
  messages: Seq[Message],
  model: Option[ModelName]
): Result[CompletionResponse]
```

### User Guide Format

```markdown
# Feature Name

## Overview
Brief description of what this feature does and why it matters.

## When to Use
When should developers use this feature?

## Basic Usage

```scala
// Minimal working example
val result = Feature.use(...)
```

## Advanced Usage

```scala
// More complex example with options
val result = Feature.useWithOptions(...)
  .withTimeout(30.seconds)
  .withRetry(maxAttempts = 3)
```

## Configuration

Environment variables:
- `FEATURE_SETTING`: What this controls

## Common Patterns

Example patterns for this feature.

## Troubleshooting

Common issues and solutions.

## See Also
- [Related Guide](./related.md)
- [API Reference](../api/feature.md)
```

### Example Code Format

```scala
// Place in modules/samples/src/main/scala/org/llm4s/samples/<category>/

import org.llm4s.config.Llm4sConfig
import org.llm4s.llmconnect.LLMConnect
import org.llm4s.llmconnect.model._

object MyFeatureExample extends App {
  val result = for {
    providerConfig <- Llm4sConfig.provider()
    client <- LLMConnect.getClient(providerConfig)
    response <- client.complete(
      messages = List(UserMessage("Question")),
      model = None
    )
  } yield response

  result match {
    case Right(response) => println(s"Success: ${response.content}")
    case Left(error) => println(s"Error: $error")
  }
}
```

---

## Release Process

The LLM4S team manages releases. Your contributions will be included automatically.

### Version Numbering

**Pre-release (0.x.y):**
- API may change between versions
- Current stable: 0.1.0-SNAPSHOT

**Stable (1.0.0+):**
- Follows semantic versioning
- MiMa checks for binary compatibility
- Breaking changes require major version bump

---

## Performance Considerations

### JVM Optimization

```scala
// Prefer immutable collections
val list: Seq[Message] = messages :+ newMessage

// Avoid unnecessary object creation
val result = messages.map(_.role)  // Good
val result = messages.map(m => m.role)  // Also good

// Prefer lazy evaluation where appropriate
lazy val config = Llm4sConfig.provider()
```

### Benchmarking

If your change affects performance:

```bash
# Run with JMH (planned for future)
# sbt "core/jmh:run"
```

Document benchmarks in your PR.

---

## Troubleshooting Common Issues

### Compilation Fails

```bash
# Clean rebuild
sbt clean compile

# Check Java version
java -version  # Should be 21+

# Check Scala version
sbt scalaVersion  # Should be 3.7.1 or 2.13.16
```

### Tests Fail

```bash
# Run specific failing test
sbt "core/testOnly org.llm4s.agent.AgentSpec -- -oF"

# With verbose output
sbt test -- -oD
```

### Formatting Issues

```bash
# Apply formatting
sbt scalafmtAll

# Verify no changes needed
sbt scalafmtCheck
```

### Dependencies Conflict

```bash
# Check dependency tree
sbt dependencyTree

# Check for updates
sbt dependencyUpdates
```

---

## Best Practices Summary

### DO

✅ Ask questions in issues before coding  
✅ Write tests for new code  
✅ Keep commits focused and atomic  
✅ Use descriptive commit messages  
✅ Reference related issues  
✅ Test cross-version compatibility  
✅ Update documentation  
✅ Run `sbt buildAll` before PR  
✅ Keep PRs reasonably sized  
✅ Be respectful in discussions  

### DON'T

❌ Don't commit without running tests  
❌ Don't skip formatting (`sbt scalafmtAll`)  
❌ Don't add unnecessary dependencies  
❌ Don't mix features in one PR  
❌ Don't update version numbers  
❌ Don't commit secrets or API keys  
❌ Don't use `sys.env` in core code  
❌ Don't throw exceptions in core code  
❌ Don't ignore merge conflicts  
❌ Don't be dismissive of feedback  

---

## Frequently Asked Questions

### Can I work on multiple issues at once?

Yes, but use separate branches:
```bash
git checkout -b fix/issue-1
# Work and commit
git checkout main
git pull upstream main

git checkout -b fix/issue-2
# Work and commit
```

### Should I squash my commits?

Not unless asked. Maintainers prefer to see the history.

### What if my PR gets stale?

We may close very old PRs if there's no activity. You can reopen or create a new one if your changes are still relevant.

### How long does review take?

Varies by complexity:
- Simple (docs, tests): 1-2 days
- Medium (bug fixes, enhancements): 3-5 days
- Large (new features): 1-2 weeks

### Can I suggest major changes?

Yes! Open an issue with `[DISCUSSION]` and use GitHub Discussions. Major changes benefit from early design feedback.

---

## Getting Help

- **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Contributing guide
- **[AGENTS.md](../AGENTS.md)** - Repository structure and commands
- **[CLAUDE.md](../CLAUDE.md)** - Developer patterns
- **Discord**: https://discord.gg/4uvTPn6qww
- **GitHub Discussions**: https://github.com/llm4s/llm4s/discussions

---

## Acknowledgments

Thank you for contributing to LLM4S! Every contribution - whether code, documentation, or community support - helps make LLM4S better.

We're grateful for your interest in building reliable, type-safe LLM applications. 🎉
