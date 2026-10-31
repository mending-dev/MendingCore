---
description: Parse, safe string parsing without exceptions in the normal flow.
---

# Parse

`dev.mending.core.api.util.Parse` converts strings to values and returns an empty result instead of throwing. Every method accepts `null`.

```java
OptionalInt amount = Parse.integer(args[0]);
if (amount.isEmpty()) {
    sender.sendMessage(Component.text("Please enter a number."));
    return;
}
give(player, amount.getAsInt());
```

## Methods

| Signature | Returns | Description |
| --- | --- | --- |
| `bool(String)` | `Optional<Boolean>` | `true`/`false`, `yes`/`no` (case-insensitive) and `1`/`0`. |
| `isBoolean(String)` | `boolean` | Whether `bool` would succeed. |
| `integer(String)` | `OptionalInt` | An int with optional `+` or `-`. |
| `isInteger(String)` | `boolean` | Whether `integer` would succeed. |
| `longValue(String)` | `OptionalLong` | A long with optional sign. |
| `doubleValue(String)` | `OptionalDouble` | A finite decimal number. |
| `enumValue(Class<E>, String)` | `Optional<E>` | Case-insensitive enum lookup. |

## Rules

### Integers and longs

* Only ASCII digits `0-9` are accepted. Other scripts' digits are rejected.
* An optional leading `+` or `-` is allowed. `+5` is `5`.
* Overflow gives an empty result. `"2147483648"` is not an int.
* Whitespace is **not** trimmed, `" 5"` is invalid. Trim input yourself if you want that.
* Parsing does not use exceptions for ordinary invalid input, so it is cheap.

### Doubles

* Accepts forms like `12`, `-0.5`, `.5`, `1e3`.
* Rejects `NaN`, `Infinity`, Java suffixes such as `1d` or `2f`, hex floats and anything longer than 64 characters.
* The result is always finite.

### Booleans

Only `true`, `false`, `yes`, `no`, `1`, `0` (case-insensitive for the words). `on`/`off` are **not** accepted.

### Enums

```java
Optional<Material> material = Parse.enumValue(Material.class, args[0]);
```

The match ignores case and uses no locale. It scans the constants linearly. For hot paths with big enums such as `Material`, build a lookup map once instead.

## Why not `Integer.parseInt` with try/catch?

* Exceptions are slow when invalid input is common (command arguments, user input).
* `Character.isDigit` and `parseInt` accept digits from other scripts, which usually is not what you want.
* `toLowerCase()` without a locale breaks in some languages (Turkish dotted/dotless I). `Parse` compares with `equalsIgnoreCase`.

## Typical use

```java
int page = Parse.integer(args.length > 1 ? args[1] : null).orElse(1);
boolean silent = Parse.bool(args[2]).orElse(false);
```
