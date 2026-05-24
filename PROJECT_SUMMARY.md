# IntegrityInspector Project Summary

This summary is a compact onboarding reference. See `README.md` for user-facing usage and `ARCHITECTURE.md` for deeper design details.

## Quick Facts

- Language: Java
- Build tool: Maven
- Java target: 21
- Main class: `io.integrityinspector.app.App`
- Version: `0.9.0`
- Runnable jar after packaging: `target/integrity-inspector-0.9.0.jar`
- Default config: `src/main/resources/config.json`
- Test framework: JUnit 4
- Coverage: JaCoCo with generated ANTLR classes excluded

## What The Tool Does

IntegrityInspector compares source-code projects and reports likely similarity/plagiarism signals.

Supported workflows:

- Compare one checked project against a directory of baseline projects.
- Compare every project in a directory against the other projects in the same directory.

The analysis is primarily line-based. When `parseCodeConfig.needParseTree` is enabled, Java and Python files also get code-tree similarity data.

## Inputs

The CLI accepts:

- `--checking-project` / `-ch-prj`: one project under check.
- `--baseline-projects` / `-b`: directory of baseline projects.
- `--checking-directory` / `-ch-dir`: directory where each subdirectory is a project to compare with the others.
- `--config` / `-cf`: JSON config file.
- `--help` / `-h`: print CLI help.

If `--checking-directory` is present, the app uses multi-project mode. Otherwise it uses single-project mode.

## Outputs

When `analysisConfig.reportResultFormat` is exactly `json`, reports are written as:

```text
report_<project-name>.json
```

The report is written to the process working directory. Any other report format value falls back to console logging.

## Configuration Notes

Important config sections:

- `analysisConfig`: similarity thresholds, limits, threading, report format.
- `parseCodeConfig`: parser mode, supported extension filter, extension aliases, ignored line prefixes.
- `multipleProjectCheckConfig`: multi-project report skip threshold.

Important behavior:

- `listOfSupportedExtensions` is optional. Null or empty means no extension filter.
- `additionalFileExtensions` maps extra extensions to existing reader keys.
- `reportResultFormat` is case-sensitive.
- `needParseTree=true` enables tree-backed readers for Java, Python, and `.ipynb`. Other extensions use a not-supported tree marker unless mapped to an existing tree reader.
- See `docs/CONFIGURATION.md` for field-level config behavior.
- See `docs/REPORT_FORMAT.md` for report fields and metric interpretation.

## Current Package Responsibilities

- `app`: CLI and bootstrap.
- `config`: config objects and JSON loading.
- `parser.reader.project`: project directory traversal and file collection.
- `parser.reader.file`: file readers, extension mapping, notebook extraction, line extraction.
- `parser.cleaner`: comment and line normalization.
- `antlr`: Java/Python parsing and `CodeTree` conversion.
- `checker`: line and tree similarity checks.
- `analysis`: aggregation, baseline limiting, uniqueness, ZZH1 coefficient, multithreaded execution.
- `write`: JSON and console report output.
- `model`: domain data objects.

## Build Commands

```bash
mvn test
mvn package
```

`mvn test` enforces:

- line coverage >= 90%
- branch coverage >= 90%

`mvn package` runs tests, JaCoCo checks, PMD, CPD, and jar assembly.

## Demo

Demo files:

- `demo/config.sample.json`
- `demo_projects/check/ProjCheck/Main.java`
- `demo_projects/baseline/ProjA/Main.java`
- `demo_projects/baseline/ProjB/Main.java`
- `demo/run-demo.sh`
- `demo/run-demo.bat`
- expected output: `report_ProjCheck.json`

Run:

Linux, macOS, or Windows with Git Bash/WSL:

```bash
bash demo/run-demo.sh
```

Windows:

```bat
demo\run-demo.bat
```

## Known Technical Notes

- Generated ANTLR parser/lexer classes are committed under `src/main/java/io/integrityinspector/antlr/*/gen`.
- Do not edit generated ANTLR classes manually.
- The generated ANTLR code may emit a tool/runtime version warning during tests.
- The class name `DefualtZzh1UniquenessCoefficientCalculator` is misspelled in code and kept as-is for compatibility.
- `docs/TROUBLESHOOTING.md` lists common report, config, and tree-mode issues.
- `docs/MAINTENANCE.md` lists checks to keep docs, demo, and code aligned.
