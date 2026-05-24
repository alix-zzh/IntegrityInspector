# Architecture Overview

This document describes the current IntegrityInspector code structure and runtime flow.

## Runtime Entry Point

The CLI starts in `io.integrityinspector.app.App`.

Startup sequence:

1. Create `AppParameters`.
2. Parse CLI options with JCommander.
3. Exit after printing usage when `--help` is provided.
4. Load configuration with `AppConfigReader.readBasedOnParameters`.
5. Build core collaborators with `AppCoreComponentsFactory`.
6. Select an inspector:
   - `MultipleProjectIntegrityInspector` when `checkingDirectory` is present.
   - `SingleProjectIntegrityInspector` otherwise.
7. Run `integrityInspector.process(parameters)`.

## Main Workflows

### Single-Project Mode

`SingleProjectIntegrityInspector` performs:

1. Parse the checking project with `ProjectParser.parseProject`.
2. Parse baseline projects with `ProjectParser.parseProjectListFromRootDir`.
3. Build an `Analysis` through `AnalysisCreator.create`.
4. Write the report through `AnalysisWriter.write`.

The output report name is based on the checked project directory name.

### Multi-Project Mode

`MultipleProjectIntegrityInspector` performs:

1. Parse every subdirectory in `checkingDirectory` as a project.
2. For each project, use all other projects as baselines.
3. Build an `Analysis`.
4. Skip report generation when `analysis.totalUniquenessPercentage` is greater than `multipleProjectCheckConfig.maxUniquenessPercentageForCreatingReport`.
5. Otherwise write a report for that project.

## Package Map

### `io.integrityinspector.app`

- `App`: CLI entry point.
- `AppParameters`: JCommander CLI options.
- `AppCoreComponents`: holder for parser, analysis creator, writer, and config.
- `AppCoreComponentsFactory`: wires parser, analysis, and writer factories.

### `io.integrityinspector.config`

Configuration model and JSON loading.

- `AppConfig`: root config object.
- `AnalysisConfig`: thresholds, limits, thread count, and report format.
- `ParserConfig`: tree parsing flag, extension filters, extension aliases, line-start exclusions.
- `MultipleProjectCheckConfig`: report skip threshold for multi-project mode.
- `AppConfigReader`: loads default `/config.json` or a provided file path.

### `io.integrityinspector.parser.reader.project`

Project-level parsing.

- `ProjectParserFactory`: creates a configured `ProjectParser`.
- `DefaultProjectParser`: turns a directory into a `Project`.
- `DefaultProjectCodeParser`: recursively collects files, filters by `listOfSupportedExtensions`, and delegates file reading to `CodeReaderFactory`.

If `listOfSupportedExtensions` is null or empty, all collected files are read through the configured reader factory.

### `io.integrityinspector.parser.reader.file`

File-level reading.

- `CodeReaderFactoryFactory`: registers file readers based on `ParserConfig.needParseTree`.
- `CodeReaderFactory`: resolves a reader by extension and `additionalFileExtensions`.
- `CodeFileReader`: reads line-based `CodeFile` instances.
- `CodeFileTreeReader`: reads `CodeFileTree` instances with parsed `CodeTree`.
- `IpynbReader`: extracts code cells from `.ipynb` JSON and delegates to the Python reader.
- `DefaultCodeFileContextReader`: reads raw file content.
- `DefaultLineForCheckExtractor`: extracts normalized `Line` instances for analysis.
- `DefaultLineValidator`: filters blank lines and configured language prefixes.

Non-tree reader keys:

- `java`
- `py`
- `ipynb`
- `c`
- `cs`
- `js`
- default `TEXT`

Tree reader keys:

- `java`
- `py`
- `ipynb`
- default `TEXT` with a not-supported tree marker

### `io.integrityinspector.parser.cleaner`

Line and comment cleanup.

- `CommonFileCommentCleaner`: removes C/Java-style comments.
- `PythonFileCommentCleaner`: removes Python line comments and triple-quoted blocks.
- `DefaultLineCleaner`: normalizes line content.
- `PythonLineCommentCleaner`: Python-specific line cleanup.

### `io.integrityinspector.antlr`

ANTLR-backed structural parsing.

- `antlr.java` and `antlr.python`: language-specific converters.
- `antlr.core.DefaultCodeTreeParser`: calls a `CodeTreeNodeConverter` and returns null on parser/converter failure.
- `antlr.core.DefaultCodeTreeConverter`: converts ANTLR parse trees into `CodeTree`.
- `antlr.model.CodeTree`: tree model used by the Zhang-Shasha distance library.
- `antlr.*.gen`: generated parser/lexer classes. These are excluded from coverage and should not be edited manually.

### `io.integrityinspector.checker`

Similarity primitives.

- `PlagiarismLineChecker`: compares one checked line with every line in a baseline file.
- `FileStringChecker`: builds per-file line matches and file uniqueness.
- `FileTreeChecker`: adds tree similarity results when both files are `CodeFileTree` and have the same language.
- `TreeSimilarityCalculator`: wraps Zhang-Shasha tree distance and returns `Integer.MAX_VALUE` for null or unsupported trees.
- `ProjectChecker`: applies a file checker to every file in a project.

### `io.integrityinspector.analysis`

Analysis orchestration and aggregation.

- `AnalysisCreatorFactory`: chooses string analysis or tree analysis.
- `StringAnalysisCreator`: line-level analysis flow.
- `TreeAnalysisCreator`: tree-assisted flow. It first analyzes tree similarities, limits baselines by counts, then computes line-level uniqueness against the limited baseline set.
- `MultithreadingProjectAnalyzer`: splits baseline projects into chunks and runs analysis tasks in a thread pool.
- `DefaultCountsExtractor`: counts matches per baseline project.
- `DefaultBaseLineProjectLimiter`: selects baseline projects by count.
- `DefaultUniquenessPercentageCalculator`: calculates total uniqueness as unique checked lines divided by all checked lines.
- `DefualtZzh1UniquenessCoefficientCalculator`: calculates the ZZH1 coefficient from uniqueness and project line count.
- `DefaultCodeTreeAnalysisExtractor`: groups, sorts, filters, and limits tree similarity results.

### `io.integrityinspector.write`

Report output.

- `AnalysisWriterFactoryImpl`: returns `JsonWriterFactory` only for the exact `json` format. Other values use `ConsoleWriterFactory`.
- `JsonWriter`: writes `report_<name>.json`.
- `ConsoleWriter`: logs the `Analysis`.

## Data Flow

```text
CLI args
  -> AppParameters
  -> AppConfig
  -> ProjectParser
  -> Project / CodeFile / CodeFileTree
  -> AnalysisCreator
  -> Analysis
  -> AnalysisWriter
```

String analysis flow:

```text
Project
  -> FileStringChecker
  -> CheckLine / LineInfo
  -> counts per baseline project
  -> total uniqueness percentage
  -> ZZH1 coefficient
  -> Analysis
```

Tree analysis flow:

```text
Project
  -> FileTreeChecker
  -> TreeSimilarity
  -> baseline limiting
  -> line-level analysis on limited baselines
  -> filtered tree similarity lists
  -> Analysis
```

## Build And Quality Gates

The project is built with Maven and Java 21.

`mvn test` runs JUnit 4 tests and JaCoCo. Generated ANTLR classes are excluded from coverage. The enforced coverage gates are:

- line coverage >= 90%
- branch coverage >= 90%

`mvn package` also runs PMD and CPD checks and builds the runnable jar.
