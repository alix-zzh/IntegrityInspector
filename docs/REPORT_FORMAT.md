# Report Format

When `analysisConfig.reportResultFormat` is exactly `json`, IntegrityInspector writes:

```text
report_<project-name>.json
```

The report is written to the process working directory.

## Top-Level Object

The JSON report maps to `io.integrityinspector.model.Analysis`.

| Field | Type | Meaning |
| --- | --- | --- |
| `totalUniquenessPercentage` | decimal | Project-level uniqueness percentage. Higher means fewer matched checked lines. |
| `zzh1UniquenessCoefficient` | decimal | Internal coefficient derived from uniqueness and project line count. |
| `projectChecks` | array | Per-file checks. Contains `FileCheck` objects in line mode and `FileTreeCheck` objects in tree mode. |
| `countPerProject` | array | Match counts grouped by baseline project. |

## Uniqueness Percentage

`DefaultUniquenessPercentageCalculator` calculates:

```text
unique checked lines / all checked lines * 100
```

Implementation detail: the current code names the variable `matchedLineCount`, but it counts checked lines with an empty `similarLines` list. In other words, it counts lines that did not match a baseline line.

If there are no checked lines, or no unique checked lines, the result is `0.00`.

## ZZH1 Coefficient

`zzh1UniquenessCoefficient` is calculated by `DefualtZzh1UniquenessCoefficientCalculator`.

The current implementation:

- normalizes project size by `projectLineCount / 1000`, capped at `1.0`;
- applies a sigmoid-like coefficient;
- combines that coefficient with `totalUniquenessPercentage`.

The exact business meaning of “ZZH1” is not documented in code. Treat it as an internal score rather than a standalone plagiarism verdict.

## `projectChecks`

Line-based analysis writes `FileCheck` objects:

| Field | Type | Meaning |
| --- | --- | --- |
| `codeFileName` | string | Checked file path. |
| `checkedLines` | array | Checked lines and their similar baseline lines. |
| `uniqueStringPresent` | decimal | Per-file percentage of checked lines without matches. |

Tree-based analysis writes `FileTreeCheck` objects. They include all `FileCheck` fields plus:

| Field | Type | Meaning |
| --- | --- | --- |
| `codeTreeSimilarityList` | array | Tree distance results for similar baseline files/projects. |

## `checkedLines`

Each item maps to `CheckLine`:

| Field | Type | Meaning |
| --- | --- | --- |
| `line` | object | The checked `Line`. |
| `similarLines` | array | Baseline lines considered similar to the checked line. Empty means the checked line is currently treated as unique. |

`Line` fields:

| Field | Type | Meaning |
| --- | --- | --- |
| `lineIndex` | integer | 1-based line index from extraction. |
| `content` | string | Original extracted line content. |
| `contentFiltered` | string | Normalized line content used for comparison. |

`LineInfo` fields:

| Field | Type | Meaning |
| --- | --- | --- |
| `project` | string | Baseline project name. |
| `file` | string | Baseline file path. |
| `levenshteinDistance` | number | Levenshtein distance ratio used by line matching. Lower is more similar. |
| `cosineDistance` | number | Cosine distance from Apache Commons Text. Lower is more similar. |
| `jaccardDistance` | number | Jaccard distance from Apache Commons Text. Lower is more similar. |
| `line` | object | Baseline `Line`. |

## `countPerProject`

Each item maps to `ProjectCount`:

| Field | Type | Meaning |
| --- | --- | --- |
| `name` | string | Baseline project name. |
| `count` | integer | Number of retained matches associated with that project. |

## Tree Similarity

`TreeSimilarity` fields:

| Field | Type | Meaning |
| --- | --- | --- |
| `project` | string | Baseline project name. |
| `codeFileName` | string | Baseline file path. |
| `similarityScore` | integer | Zhang-Shasha tree edit distance. Lower means more structurally similar. |

Unsupported tree comparisons are represented internally as `Integer.MAX_VALUE` and filtered out by `DefaultCodeTreeAnalysisExtractor`.

## HTML Report Reader

`view/report_reader.html` is a lightweight local HTML report reader for JSON reports. It renders:

- top-level uniqueness and ZZH-1 metrics;
- match counts per baseline project;
- per-file uniqueness;
- checked source lines, colored by whether similar baseline lines were found;
- expandable similar-line details with project, file, line text, and distance metrics;
- tree similarity entries when the JSON report contains `codeTreeSimilarityList`;
- a collapsible raw JSON view for debugging.

Usage:

1. Open `view/report_reader.html` in a browser.
2. Drop a `report_<project-name>.json` file onto the page, or click the drop zone and select a report.
3. Inspect the rendered report. Matched lines can be expanded to show similar baseline lines.

For a complete sample that exercises line matches, unique lines, baseline counts, empty file checks, and tree similarity, load `view/report_example.json`.

Because it uses the browser `FileReader` API, it can read local files selected by the user without a local web server.
