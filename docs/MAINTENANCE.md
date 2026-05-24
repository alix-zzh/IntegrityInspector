# Maintenance Checklist

Use this checklist when changing CLI options, config fields, report output, readers, or build settings.

## Code Verification

Run:

```bash
mvn test
mvn package
```

Both commands should pass. `mvn test` enforces line and branch coverage gates. `mvn package` also runs PMD, CPD, and jar assembly.

CI/CD changes should also keep `.github/workflows/pr-quality.yml`, `.github/workflows/release.yml`, `.github/dependabot.yml`, and `docs/CI_CD.md` aligned.

## Demo Verification

Run the Unix-style demo command on Linux, macOS, or Windows with Git Bash/WSL:

```bash
bash demo/run-demo.sh
```

Windows:

```bat
demo\run-demo.bat
```

Expected output:

```text
report_ProjCheck.json
```

Remove generated demo reports before committing unless the report file is intentionally part of the change.

## Documentation Consistency Checks

Search for stale build artifact names:

```bash
rg --glob '!docs/MAINTENANCE.md' "jar-with-dependencies|integrity-inspector-0\\.9\\.0-jar"
```

Search for stale report format examples:

```bash
rg --glob '!docs/MAINTENANCE.md' "reportResultFormat.*JSON"
```

Search for known encoding artifacts:

```bash
rg --glob '!docs/MAINTENANCE.md' "РІР‚|вЂ"
```

When updating code, keep these files aligned:

- `README.md`
- `ARCHITECTURE.md`
- `PROJECT_SUMMARY.md`
- `docs/CONFIGURATION.md`
- `docs/REPORT_FORMAT.md`
- `docs/EXTENSIONS.md`
- `docs/CI_CD.md`
- `docs/TROUBLESHOOTING.md`
- `demo/config.sample.json`
- `demo/run-demo.sh`
- `demo/run-demo.bat`

## Source Of Truth

- CLI options: `AppParameters`
- Config shape: `AppConfig`, `AnalysisConfig`, `ParserConfig`, `MultipleProjectCheckConfig`
- Report shape: `Analysis`, `FileCheck`, `FileTreeCheck`, `CheckLine`, `LineInfo`, `TreeSimilarity`
- Reader keys and extension behavior: `CodeReaderFactoryFactory`, `CodeReaderFactory`, `DefaultProjectCodeParser`
- Writer behavior: `AnalysisWriterFactoryImpl`, `JsonWriter`, `ConsoleWriter`
