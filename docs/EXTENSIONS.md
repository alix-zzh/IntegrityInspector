# Extensions Guide

This guide explains how IntegrityInspector resolves file extensions and how to add support for more languages or extension aliases.

## Current Reader Registration

Reader registration happens in `io.integrityinspector.parser.reader.file.CodeReaderFactoryFactory`.

There are two modes.

### Line-Based Mode

Used when:

```json
"needParseTree": false
```

Registered reader keys:

| Key | Language label | Reader behavior |
| --- | --- | --- |
| `java` | `Java` | Common comment cleaner, default line cleaner. |
| `py` | `Python` | Python comment cleaner, Python line cleaner. |
| `ipynb` | `Python` | Extracts code cells, delegates to Python reader. |
| `c` | `C++` | Common comment cleaner, C/C++ line-start exclusions. |
| `cs` | `C#` | Common comment cleaner, C# line-start exclusions. |
| `js` | `JS` | Common comment cleaner, JS line-start exclusions. |
| default | `TEXT` | Generic text reader. |

### Tree-Based Mode

Used when:

```json
"needParseTree": true
```

Registered reader keys:

| Key | Language label | Tree behavior |
| --- | --- | --- |
| `java` | `Java` | Uses `JavaCodeTreeNodeConverter`. |
| `py` | `Python` | Uses `PythonCodeTreeNodeConverter`. |
| `ipynb` | `Python` | Extracts code cells, delegates to Python tree reader. |
| default | `TEXT` | Creates a not-supported tree marker. |

Tree similarity is meaningful only when both compared files are `CodeFileTree` instances with the same language and supported trees.

## Extension Filtering

`parseCodeConfig.listOfSupportedExtensions` is handled by `DefaultProjectCodeParser`.

- If the list is null or empty, all collected files are passed to the reader factory.
- If the list has values, only files with listed extensions are read.

Example:

```json
"listOfSupportedExtensions": ["java", "py", "ipynb"]
```

## Extension Aliases

`parseCodeConfig.additionalFileExtensions` maps extra file extensions to an existing reader key.

Example:

```json
"additionalFileExtensions": [
  {
    "extensions": ["kt", "kts", "ktm"],
    "existingExtension": "java"
  },
  {
    "extensions": ["ts"],
    "existingExtension": "js"
  }
]
```

With this config, `.kt` files use the Java reader and `.ts` files use the JS reader.

If `listOfSupportedExtensions` is enabled, include alias extensions there too:

```json
"listOfSupportedExtensions": ["java", "kt", "kts", "ktm", "js", "ts"]
```

The filter runs before alias resolution. If `kt` is not listed, a `.kt` file is skipped even when `kt -> java` is configured.

Important details:

- The mapping points to a reader key, not a language label.
- The key must exist in the active reader mode.
- In tree mode, only `java`, `py`, and `ipynb` have dedicated tree readers. `kt -> java` can reuse Java tree parsing; `ts -> js` falls back to the default tree reader because `js` has no tree reader.
- If `existingExtension` is unknown, `CodeReaderFactory` falls back to the default reader.

## Line-Start Exclusions

`programmingLangLineStartExclusion` configures normalized line prefixes to ignore.

Example:

```json
"programmingLangLineStartExclusion": {
  "java": ["import", "package"],
  "python": ["import", "from"],
  "cpp": ["include", "pragma"],
  "cSharp": ["using", "namespace"],
  "js": ["import", "package"]
}
```

`DefaultLineValidator` cleans a line first, then rejects it when the cleaned line is empty or starts with a configured prefix.

## Adding A New Line-Based Language

1. Decide the extension key, for example `rb`.
2. Add a language label constant in `CodeReaderFactoryFactory`.
3. Choose or implement:
   - a `CommentCleaner`
   - a `LineCleaner`
   - a `LineValidator`
4. Register a `CodeFileReader` in `createCodeFileCodeReaderFactory`.
5. Add default config entries for supported extensions and line-start exclusions if needed.
6. Add tests for:
   - reader registration
   - comment cleanup
   - line filtering
   - project parsing with the new extension

## Adding A Tree-Based Language

1. Add or generate an ANTLR grammar and parser classes.
2. Implement `CodeTreeNodeConverter`.
3. Register a `CodeFileTreeReader` in `createCodeFileCodeReaderTreeFactory`.
4. Add tests for:
   - converter behavior
   - `CodeFileTreeReader`
   - tree analysis integration
   - unsupported/fallback behavior

Do not edit generated ANTLR classes by hand. Regenerate them from grammar files when parser changes are needed.

## Adding Only An Alias

If a new extension can reuse an existing reader, no Java code is needed. Add an alias in config:

```json
{
  "extensions": ["tsx"],
  "existingExtension": "js"
}
```

Remember to include the alias extension in `listOfSupportedExtensions` if that filter is enabled.
