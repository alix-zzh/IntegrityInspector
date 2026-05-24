# Troubleshooting

## No JSON Report Was Created

Check `analysisConfig.reportResultFormat`.

JSON output is selected only when the value is exactly:

```json
"reportResultFormat": "json"
```

The value is case-sensitive. `JSON`, `Json`, or any other value uses the console writer.

## Report Was Written In An Unexpected Directory

`JsonWriter` writes reports to the process working directory. There is currently no CLI option for changing the output directory.

Run the CLI from the directory where you want `report_<project-name>.json` to appear.

## Multi-Project Mode Skips Reports

In multi-project mode, `MultipleProjectIntegrityInspector` skips report generation when:

```text
analysis.totalUniquenessPercentage > maxUniquenessPercentageForCreatingReport
```

Increase `multipleProjectCheckConfig.maxUniquenessPercentageForCreatingReport` if you want reports for more unique projects.

## Baseline Files Are Ignored

For `--baseline-projects` and `--checking-directory`, the root directory is expected to contain project subdirectories.

Example:

```text
baseline-root/
  ProjectA/
    Main.java
  ProjectB/
    Main.java
```

Files directly under `baseline-root` are ignored by `DefaultProjectParser.parseProjectListFromRootDir`.

## Alias Extensions Are Not Parsed

If `listOfSupportedExtensions` is configured, alias extensions must be listed there too.

Example:

```json
"additionalFileExtensions": [
  {
    "extensions": ["kt"],
    "existingExtension": "java"
  }
],
"listOfSupportedExtensions": ["java", "kt"]
```

Without `kt` in `listOfSupportedExtensions`, `.kt` files are skipped before reader alias resolution happens.

## Tree Mode Produces No Tree Similarity

Tree similarity is only produced when both files:

- are read as `CodeFileTree`;
- have the same language label;
- have supported, non-null trees.

Dedicated tree readers currently exist for `java`, `py`, and `ipynb`.

## ANTLR Version Warning During Tests

You may see warnings like:

```text
ANTLR Tool version ... used for code generation does not match the current runtime version ...
```

The generated ANTLR classes are committed and intentionally excluded from coverage gates. Do not edit generated classes by hand.

## `mvn test` Fails On Coverage

Generated ANTLR classes are excluded, but all other code is checked by JaCoCo.

Current gates:

- line coverage >= 90%
- branch coverage >= 90%

Add focused tests or adjust the code before lowering these thresholds.
