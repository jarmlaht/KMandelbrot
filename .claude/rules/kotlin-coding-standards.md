---
description: Kotlin coding conventions and best practices
paths:
  - "**/*.kt"
  - "**/*.kts"
---

# Kotlin Coding Standards

Follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) and the [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide). The rules below are the ones that matter most in this project.

## Naming
- Classes, objects and interfaces: `PascalCase`. Functions, properties and local variables: `camelCase`.
- Constants (`const val` and top-level or object `val`s holding immutable data): `SCREAMING_SNAKE_CASE`.
- `@Composable` functions that emit UI are nouns in `PascalCase` (`MandelbrotScreen`).
- Test classes are named after the class under test with a `Test` suffix. Test function names describe the behaviour (`fitWholeSetIsCentredOnTheSet`).
- Name things for what they mean, not for their type. Avoid abbreviations other than well-known ones (`re`, `im`, `id`, `url`).
- A file with a single class is named after the class. A file with only top-level declarations is named for what it contains, in `PascalCase`.

## Formatting
- Four spaces for indentation, no tabs. Maximum line length is 120 characters.
- Use trailing commas in multi-line parameter and argument lists.
- No wildcard imports. Remove unused imports.
- One statement per line; no semicolons.
- Always use braces for multi-line `if`, `for` and `while` bodies.

## Immutability and state
- Prefer `val` to `var`. Use `var` only when the value really changes.
- Prefer read-only collection types (`List`, `Set`, `Map`) in signatures; keep mutable collections private.
- Use `data class` for types that only hold values.
- Expose state as read-only; keep the setter private (`var x by mutableStateOf(...)` with `private set`).
- Avoid global mutable state. Top-level `var`s are not allowed.

## Null safety
- Prefer non-null types. Make a type nullable only when "no value" is a real state.
- Never use `!!`. Use `?.`, `?:`, `requireNotNull`, `checkNotNull`, or restructure the code.
- Use `lateinit` only for values set by a framework lifecycle before first use.
- Do not use platform types implicitly: declare the type when taking a value from Java code.

## Functions
- Keep functions short and doing one thing.
- Use an expression body when the function is a single expression.
- Prefer default arguments to overloads. Use named arguments when a call has several arguments of the same type or boolean flags.
- Declare the return type of every public function, even with an expression body.
- Prefer pure functions for logic: take inputs as parameters, return a result, no side effects.
- Use top-level functions for stateless helpers; do not create `Utils` objects or classes.
- Use extension functions to add behaviour to a type you do not own, not to hide dependencies.

## Classes
- Use the most restrictive visibility that works: `private` first, then `internal`, then public.
- Prefer composition to inheritance. Classes stay `final` unless designed for extension.
- Use `sealed` classes or interfaces for closed sets of variants and `enum class` for fixed sets of constants.
- Use `object` for singletons without state, and `companion object` for factory functions and constants tied to a class.
- Order class contents: properties, `init` blocks, secondary constructors, functions, companion object.

## Idioms
- Use `when` instead of long `if`/`else if` chains. Make `when` on sealed types and enums exhaustive without an `else` branch.
- Use `if`, `when` and `try` as expressions instead of assigning in each branch.
- Use string templates instead of concatenation.
- Use ranges and `until`/`..<` for loops; use `indices` and `withIndex()` instead of manual index arithmetic.
- Use the standard collection operations (`map`, `filter`, `any`, `sumOf`) for clarity. In hot loops, such as per-pixel calculations, prefer plain `for` loops and primitive arrays (`IntArray`, `DoubleArray`) to avoid boxing and allocation.
- Use scope functions sparingly and do not nest them: `let` for null checks, `apply` for configuring an object, `also` for side effects.
- No magic numbers: give constants a name, with the unit in the name or a comment when it is not obvious.

## Error handling
- Validate arguments with `require` and state with `check`; both throw with a clear message.
- Throw specific exception types. Do not catch `Exception` or `Throwable` unless rethrowing.
- Never swallow an exception silently.
- Do not use exceptions for normal control flow; return `null` or a sealed result type when failure is expected.

## Coroutines
- Do not use `GlobalScope`. Launch from a scope tied to a lifecycle (`viewModelScope`, `lifecycleScope`) or from `coroutineScope { }`.
- Never block a thread in a coroutine (`Thread.sleep`, `runBlocking` outside tests and `main`).
- CPU-bound work runs on `Dispatchers.Default`, blocking I/O on `Dispatchers.IO`, UI updates on `Dispatchers.Main`.
- A `suspend` function must be safe to call from the main thread: it moves its own work to the right dispatcher.
- Long-running loops must cooperate with cancellation (`ensureActive()` or a suspending call).
- Never catch `CancellationException` without rethrowing it.

## Comments and documentation
- Write KDoc (`/** ... */`) for public classes and functions whose purpose or contract is not obvious from the name.
- Comments explain why, not what. Do not leave commented-out code.
- A `TODO` comment must reference an issue number.

## Tests
- Pure Kotlin logic has unit tests under `app/src/test`.
- One behaviour per test, arranged as given / when / then.
- Tests do not depend on each other or on execution order.
- Use fixed inputs; no randomness and no real time delays.
