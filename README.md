# IntegrityInspector

IntegrityInspector is a Java CLI tool for source-code similarity analysis. It compares one project against a baseline set, or compares all projects inside a directory against each other, and writes an analysis report.

The project is closest in purpose to tools such as JPlag, Moss, Simian, Codequiry, and other plagiarism/similarity checkers for source code. IntegrityInspector combines line-level checks with an optional ANTLR-based tree comparison path for Java and Python.

## Current Capabilities

- Single-project mode: compare one project with a directory of baseline projects.
- Multi-project mode: compare every project in a directory with the other projects in that directory.
- Line-level similarity checks with configurable thresholds.
- Optional code-tree parsing for Java, Python, and `.ipynb` notebooks that are read as Python code.
- Configurable extension aliases, for example `kt` can be treated as `java`.
- JSON report writer and console writer fallback.
- HTML report reader in `view/report_reader.html`.

## Requirements

- JDK 21
- Maven

## Build And Test

```bash
mvn test
mvn package
```

`mvn test` runs the test suite and JaCoCo checks. Generated ANTLR classes are excluded from coverage. The current coverage gate requires:

- line coverage >= 90%
- branch coverage >= 90%

`mvn package` also runs PMD and CPD checks and builds:

```text
target/integrity-inspector-0.9.0.jar
```

## CLI Usage

Show CLI help:

```bash
java -jar target/integrity-inspector-0.9.0.jar --help
```

Run a single project against baselines:

```bash
java -jar target/integrity-inspector-0.9.0.jar \
  --config path/to/config.json \
  --checking-project path/to/project-under-check \
  --baseline-projects path/to/baseline-projects
```

Run all projects in one directory against each other:

```bash
java -jar target/integrity-inspector-0.9.0.jar \
  --config path/to/config.json \
  --checking-directory path/to/projects
```

CLI options:

| Short | Long | Purpose |
| --- | --- | --- |
| `-h` | `--help` | Print help. |
| `-cf` | `--config` | Use a JSON config file instead of the default classpath config. |
| `-ch-prj` | `--checking-project` | Project to compare against baselines. |
| `-b` | `--baseline-projects` | Directory containing baseline projects. |
| `-ch-dir` | `--checking-directory` | Directory containing projects to compare with each other. |

If `--checking-directory` is provided, multi-project mode is used. Otherwise the app uses single-project mode.

Valid argument combinations:

| Mode | Required options | Directory shape |
| --- | --- | --- |
| Single-project | `--checking-project`, `--baseline-projects` | `--checking-project` points to one project directory; `--baseline-projects` points to a root containing project subdirectories. |
| Multi-project | `--checking-directory` | `--checking-directory` points to a root containing project subdirectories. |

Files directly under a baseline root or checking-directory root are ignored in list mode; immediate subdirectories are treated as projects.

## Reports

Set `analysisConfig.reportResultFormat` to exactly `json` to write JSON files:

```json
"reportResultFormat": "json"
```

The value is currently case-sensitive. Any other value falls back to the console writer.

JSON reports are written to the process working directory with this name:

```text
report_<project-name>.json
```

There is no CLI flag for changing the output directory yet. The `JsonWriter` class supports an output directory constructor for programmatic use and tests.

Open `view/report_reader.html` in a browser to load a JSON report as an HTML document with summary metrics, per-file uniqueness, expandable similar-line details, and tree similarity entries. A complete sample report is available at `view/report_example.json`.

See `docs/REPORT_FORMAT.md` for the JSON schema, metric definitions, tree similarity interpretation, and HTML reader usage.

## Configuration

The default config is `src/main/resources/config.json`. Use `--config` to pass a custom JSON file.

Important fields:

- `projectLimit`: how many top matching projects/tree matches to keep in selected parts of analysis.
- `lineSimilarLimit`: maximum similar lines stored for a checked line.
- `maxLineLengthDiff`: maximum allowed length difference before two normalized lines are compared.
- `minLineLength`: minimum normalized line length required for comparison.
- `levenshteinSimilarityPercent`: maximum Levenshtein distance ratio for a line match.
- `needParseTree`: enables tree-backed readers and tree similarity analysis.
- `listOfSupportedExtensions`: optional extension filter. If omitted or empty, all files are read through the reader factory.
- `additionalFileExtensions`: maps extra extensions to an existing reader key.
- `maxUniquenessPercentageForCreatingReport`: in multi-project mode, reports are skipped when total uniqueness is above this threshold.

See `docs/CONFIGURATION.md` for minimal and full examples, field-by-field behavior, and extension alias rules.

## Demo

The repository includes a small demo:

On Linux, macOS, or Windows with Git Bash/WSL:

```bash
bash demo/run-demo.sh
```

On Windows:

```bat
demo\run-demo.bat
```

Demo inputs live in:

- `demo_projects/check/ProjCheck`
- `demo_projects/baseline/ProjA`
- `demo_projects/baseline/ProjB`

Expected demo output:

```text
report_ProjCheck.json
```

## Documentation

- `ARCHITECTURE.md`: package-level architecture and data flow.
- `PROJECT_SUMMARY.md`: quick onboarding snapshot.
- `docs/CONFIGURATION.md`: config schema and examples.
- `docs/REPORT_FORMAT.md`: report schema and metrics.
- `docs/EXTENSIONS.md`: how to add language readers or extension aliases.
- `docs/TROUBLESHOOTING.md`: common issues and fixes.
- `docs/MAINTENANCE.md`: verification checklist for future code and docs changes.

## Known Caveats

- Tree parsing is implemented for Java and Python. Other languages fall back to a not-supported tree marker in tree mode.
- Generated ANTLR sources are committed under `src/main/java/io/integrityinspector/antlr/*/gen` and should not be edited by hand.
- The generated ANTLR sources may print a tool/runtime version warning during tests; the generated sources are intentionally excluded from coverage gates.
