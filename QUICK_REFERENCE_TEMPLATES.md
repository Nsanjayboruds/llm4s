---
layout: page
title: Quick Reference - Templates
nav_order: 8
---

# Quick Reference: Issue & PR Templates

Fast lookup guide for using LLM4S issue and pull request templates.

## When to Use Each Template

### Bug Report ✅

**Use when:**
- You found a bug in LLM4S
- A feature doesn't work as documented
- You get an unexpected error

**What to include:**
- Clear description of the problem
- Steps to reproduce
- What you expected vs. what happened
- Environment (Scala version, JDK, OS, LLM provider)
- Error messages and logs

**Template:** [bug_report.yml](.github/ISSUE_TEMPLATE/bug_report.yml)

---

### Feature Request 💡

**Use when:**
- You want a new capability in LLM4S
- You have an idea for improvement
- You're requesting support for something new

**What to include:**
- Problem you're trying to solve
- Why this feature would be useful
- Proposed solution (if you have one)
- Alternative approaches you considered
- Whether you're willing to implement it

**Template:** [feature_request.yml](.github/ISSUE_TEMPLATE/feature_request.yml)

---

### Enhancement 🚀

**Use when:**
- You want to improve an existing feature
- You see room for optimization
- You want to refactor or modernize something

**What to include:**
- Current behavior and how it works now
- What improvement you're suggesting
- Benefits and impact
- What components this affects
- Whether it's a breaking change

**Template:** [enhancement.yml](.github/ISSUE_TEMPLATE/enhancement.yml)

---

### Pull Request 📝

**Use when:**
- You're submitting code changes
- You've fixed a bug
- You've implemented a feature
- You've updated documentation

**Checklist sections:**
1. **Type of Change** - What kind of change is this?
2. **Testing** - What testing did you do?
3. **Documentation** - What docs need updating?
4. **Code Quality** - Does code meet standards?
5. **Breaking Changes** - Is this a breaking change?
6. **Performance** - Any performance impact?

**Template:** [pull_request_template.md](.github/pull_request_template.md)

---

## Quick Checklists

### Before Opening an Issue

- [ ] Search existing issues (might already be reported)
- [ ] Check documentation (might answer your question)
- [ ] Provide steps to reproduce (if it's a bug)
- [ ] Include your environment details
- [ ] Use the appropriate template

### Before Opening a PR

- [ ] Branch from `main`
- [ ] Run `sbt scalafmtAll` (formatting)
- [ ] Run `sbt +test` (all tests)
- [ ] Verify tests pass on both Scala versions
- [ ] Add tests for new code
- [ ] Update docs if needed
- [ ] Fill out PR template completely
- [ ] Reference related issues

### Before Submitting an Issue

**Bug Report:**
- Is it reproducible every time?
- What's the exact error message?
- What version of LLM4S?
- What LLM provider are you using?

**Feature Request:**
- Is the use case clear?
- Would it benefit other users?
- Is the scope reasonable?

**Enhancement:**
- Is the improvement significant?
- Would it break existing code?
- Can it be done incrementally?

---

## Pro Tips by Template Type

### Bug Report Tips

✅ **DO:**
- Include complete error stack trace
- Show minimal code to reproduce
- List all environment details
- Be specific about version numbers

❌ **DON'T:**
- Say "it doesn't work" without details
- Hide error messages
- Skip environment information
- Mix multiple bugs in one report

### Feature Request Tips

✅ **DO:**
- Explain the problem first, solution second
- Consider user experience
- Think about all use cases
- Be open to alternative approaches

❌ **DON'T:**
- Demand implementation immediately
- Ignore existing similar features
- Skip explaining the "why"
- Be dismissive of concerns

### Enhancement Tips

✅ **DO:**
- Compare before/after performance if relevant
- Consider backward compatibility
- Think about future implications
- Suggest specific implementation approaches

❌ **DON'T:**
- Over-engineer for edge cases
- Make changes without discussion
- Ignore performance implications
- Skip documentation updates

### PR Tips

✅ **DO:**
- Keep PRs focused (one feature per PR)
- Write descriptive commit messages
- Check CI results before asking for review
- Be responsive to feedback
- Celebrate when merged! 🎉

❌ **DON'T:**
- Mix multiple features in one PR
- Skip writing tests
- Ignore failing CI checks
- Push changes without discussing
- Take feedback personally

---

## Template Features

### All Templates Include

- **Pre-submission checklist** - Verify before submitting
- **Auto-labeling** - Issues get labeled automatically (bug, feature, enhancement, triage)
- **Clear sections** - Organized fields guide you through
- **Examples** - Sample content to get you started
- **Links** - Quick access to relevant docs

### Issue Templates Provide

- Structured forms (no free-text confusion)
- Dropdown selections for consistency
- Required fields (prevents incomplete issues)
- Environment tracking (Scala version, JDK, etc.)
- Provider selection (for LLM provider specific issues)

### PR Template Provides

- **34 verification checkboxes**
  - 7 for type of change
  - 11 for testing approach
  - 9 for documentation
  - 7 for code quality
- Clear sections for breaking changes
- Performance impact tracking
- Final verification checklist

---

## Template Locations

```
llm4s/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml
│   │   ├── feature_request.yml
│   │   ├── enhancement.yml
│   │   └── config.yml
│   └── pull_request_template.md
├── CONTRIBUTING.md              # Detailed contributor guide
└── docs/reference/
    └── contribution-guidelines.md  # Advanced patterns
```

---

## Getting Help

**Need more detail?**
- [CONTRIBUTING.md](CONTRIBUTING.md) - Complete contribution guide
- [Contribution Guidelines](docs/reference/contribution-guidelines.md) - Advanced patterns
- [AGENTS.md](AGENTS.md) - Build commands and repo structure

**Questions or ideas?**
- [GitHub Discussions](https://github.com/llm4s/llm4s/discussions) - Ask questions
- [Discord](https://discord.gg/4uvTPn6qww) - Real-time chat
- [GitHub Issues](https://github.com/llm4s/llm4s/issues) - Bug reports and features

---

## Contact Links (In Template Config)

The issue template configuration (`config.yml`) includes quick links to:

1. **Getting Started Video** - Walk-through for new contributors
2. **Discord Community** - Real-time chat with maintainers
3. **Documentation** - Full guides and API reference
4. **CONTRIBUTING.md** - Complete contribution guidelines
5. **Configuration Reference** - Llm4sConfig documentation
6. **Discussions** - Q&A and ideas

---

## Quick Links

- [Open an issue](https://github.com/llm4s/llm4s/issues/new/choose) - Choose your template
- [Start a discussion](https://github.com/llm4s/llm4s/discussions/new) - Ask questions or share ideas
- [View open PRs](https://github.com/llm4s/llm4s/pulls) - See what's in progress
- [View recent changes](https://github.com/llm4s/llm4s/releases) - What's new in latest version

---

Last Updated: 2025-02-05
