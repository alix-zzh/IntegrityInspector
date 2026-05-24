# Configuration Reference

IntegrityInspector loads configuration from `/config.json` on the classpath by default. Use `--config` or `-cf` to provide a file-system path.

The root object maps to `io.integrityinspector.config.AppConfig`:

- `analysisConfig`
- `parseCodeConfig`
- `multipleProjectCheckConfig`

## Minimal Config

This minimal config writes JSON reports and reads all files through the reader factory:

```json
{
  "analysisConfig": {
    "projectLimit": 1,
    "lineSimilarLimit": 4,
    "maxLineLengthDiff": 15,
    "minLineLength": 5,
    "levenshteinSimilarityPercent": 0.333,
    "multithreadingConfig": {
      "threadsCount": 4
    },
    "reportResultFormat": "json"
  },
  "parseCodeConfig": {
    "needParseTree": false,
    "additionalFileExtensions": [],
    "programmingLangLineStartExclusion": {
      "cpp": [],
      "cSharp": [],
      "java": [],
      "python": [],
      "js": []
    },
    "listOfSupportedExtensions": []
  },
  "multipleProjectCheckConfig": {
    "maxUniquenessPercentageForCreatingReport": 75
  }
}
```

## Full Example

```json
{
  "analysisConfig": {
    "projectLimit": 1,
    "lineSimilarLimit": 4,
    "maxLineLengthDiff": 15,
    "minLineLength": 5,
    "levenshteinSimilarityPercent": 0.333,
    "multithreadingConfig": {
      "threadsCount": 8
    },
    "reportResultFormat": "json"
  },
  "multipleProjectCheckConfig": {
    "maxUniquenessPercentageForCreatingReport": 75
  },
  "parseCodeConfig": {
    "needParseTree": false,
    "programmingLangLineStartExclusion": {
      "cpp": ["include", "pragma", "import"],
      "cSharp": ["using", "namespace"],
      "java": ["import", "package"],
      "python": ["import", "from"],
      "js": ["import", "package"]
    },
    "additionalFileExtensions": [
      {
        "extensions": ["kt", "kts", "ktm"],
        "existingExtension": "java"
      },
      {
        "extensions": ["cpp", "h"],
        "existingExtension": "c"
      },
      {
        "extensions": ["ts"],
        "existingExtension": "js"
      }
    ],
    "listOfSupportedExtensions": ["java", "py", "ipynb", "kt", "kts", "ktm"]
  }
}
```

## `analysisConfig`

| Field | Type | Used by | Behavior |
| --- | --- | --- | --- |
| `projectLimit` | integer | `DefaultCountsExtractor`, `DefaultCodeTreeAnalysisExtractor` | Keeps the top N baseline projects/tree matches in selected aggregation steps. |
| `lineSimilarLimit` | integer | `FileStringChecker` | Maximum similar lines retained for each checked line. |
| `maxLineLengthDiff` | integer | `PlagiarismLineChecker` | Skips line comparison when normalized line length difference is greater than or equal to this value. |
| `minLineLength` | integer | `PlagiarismLineChecker` | Skips line comparison unless the shorter normalized line is longer than this value. |
| `levenshteinSimilarityPercent` | number | `PlagiarismLineChecker` | A line pair is considered similar when Levenshtein distance divided by max line length is lower than this value. |
| `multithreadingConfig.threadsCount` | integer | `MultithreadingProjectAnalyzer` | Number of worker threads used for baseline chunks. Required for tree analysis in the current factory. |
| `reportResultFormat` | string | `AnalysisWriterFactoryImpl` | Exactly `json` selects `JsonWriter`; every other value uses `ConsoleWriter`. This value is case-sensitive. |

## `parseCodeConfig`

| Field | Type | Used by | Behavior |
| --- | --- | --- | --- |
| `needParseTree` | boolean | `CodeReaderFactoryFactory`, `AnalysisCreatorFactory` | `false` uses line-based readers and `StringAnalysisCreator`; `true` uses tree-backed readers and `TreeAnalysisCreator`. |
| `additionalFileExtensions` | array | `CodeReaderFactory` | Maps extra extensions to an existing reader key. |
| `programmingLangLineStartExclusion` | object | `DefaultLineValidator` | Prefixes rejected after line normalization. |
| `listOfSupportedExtensions` | array or null | `DefaultProjectCodeParser` | If null or empty, no extension filter is applied. Otherwise only listed extensions are read. |

## Extension Alias Rules

Aliases point to reader keys, not language labels:

```json
{
  "extensions": ["kt"],
  "existingExtension": "java"
}
```

If `listOfSupportedExtensions` is enabled, include both native and alias extensions you want to parse:

```json
"listOfSupportedExtensions": ["java", "kt"]
```

In tree mode, dedicated tree readers exist for `java`, `py`, and `ipynb`. An alias such as `kt -> java` can reuse Java tree parsing. An alias such as `ts -> js` does not get JS tree parsing because a JS tree reader is not registered.

## `multipleProjectCheckConfig`

| Field | Type | Used by | Behavior |
| --- | --- | --- | --- |
| `maxUniquenessPercentageForCreatingReport` | integer | `MultipleProjectIntegrityInspector` | In multi-project mode, reports are skipped when total uniqueness is greater than this threshold. |

## Default Config

The repository default is `src/main/resources/config.json`. Keep this file aligned with the examples above when changing reader keys, defaults, or report behavior.
