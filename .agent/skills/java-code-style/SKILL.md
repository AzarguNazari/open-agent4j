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
- Use guard-clause pattern instead of if-else or ternary operators.
- Do not use comment unless its VERY important. If you write code, keep it as as simple as possible
- Dont use the short variable names (one letter or two letters).
- For using Java libraries, do the import and use only the Object directly. (not java.util.List rather use List)

### Optionals
- Only use `Optional` as a method return type.
- Never use `Optional` as a record/constructor parameter.
- Never create `Optional` only to avoid null checks.
