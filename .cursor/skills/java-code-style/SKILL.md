---
name: java-code-style
description: Enforce Java coding standards for implementation, review, and refactoring. Use when writing Java code, reviewing Java changes, or updating Java classes, records, tests, and exception handling.
---

# Java Code Style

## Trigger
Use when writing, reviewing, or refactoring Java code.

## Rules

### General
- No `var` keyword.
- Do not keep unused variables, classes, or methods.
- Use records for plain data holders; never add constructors to records with more than 2 fields - use `@Builder` instead.
- No `@With` on records in mappers - use `@Builder=true`.
- Use `final` only on classes and class fields, not inside methods.

### Optionals
- Only use `Optional` as a method return type.
- Never use `Optional` as a record/constructor parameter.
- Never create `Optional` only to avoid null checks.
