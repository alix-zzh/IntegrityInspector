# IntegrityInspector — Project Summary

This document collects essential information for onboarding, exploration and task planning.

## Quick facts
- Language: Java (Maven project)
- Main class: io.integrityinspector.app.App
- Version: 0.9.0 (from pom.xml)
- Build: mvn clean package; produces jar-with-dependencies via maven-assembly-plugin
- Source layout: standard Maven: src/main/java, src/test/java, resources

## Architecture by Packages
- io.integrityinspector.app
  - Key classes: App, AppParameters, AppCoreComponents, AppCoreComponentsFactory
  - Purpose: CLI glue, wiring, and bootstrap
  - Detailed: App is the entry point; it parses CLI args, loads app config via AppConfigReader, constructs core components via AppCoreComponentsFactory, and selects the appropriate IntegrityInspector implementation (Single or Multiple project mode) to execute analysis.
- io.integrityinspector.config
  - Key classes: AppConfig, AppConfigReader, AdditionalFileExtensionConfig, ParserConfig, MultipleProjectCheckConfig
  - Purpose: configuration model and loader
  - Details: AppConfig aggregates configuration for analysis, parsing, and multi-project checks. AppConfigReader loads the JSON config (default from resources or a provided path) using Gson and logs its activity.
- io.integrityinspector.Inspector
  - Key: IntegrityInspector, SingleProjectIntegrityInspector, MultipleProjectIntegrityInspector
  - Purpose: orchestrate analysis per project/workspace
- io.integrityinspector.parser.reader.project
  - Key: DefaultProjectParser, ProjectParserFactory, DefaultProjectCodeParser
  - Purpose: parse a single project and produce CodeFile(s) or CodeFileTree
- io.integrityinspector.parser.reader.file
  - Key: CodeReaderFactory, CodeReaderFactoryFactory, CodeReader, CodeFileReader, IpynbReader
  - Purpose: read files using language-specific readers, with support for extra extensions
- io.integrityinspector.parser.cleaner.line
  - Key: DefaultLineCleaner, LineCleaner, PythonLineCommentCleaner
  - Purpose: normalize and clean code lines before analysis
- io.integrityinspector.parser.cleaner.comment
  - Key: CommonFileCommentCleaner, PythonFileCommentCleaner, CommentCleaner
- io.integrityinspector.antlr.core
  - Key: DefaultCodeTreeParser, CodeTreeParser, CodeTreeNodeConverter
  - Purpose: build code trees for structural comparison
- io.integrityinspector.antlr.java
- io.integrityinspector.antlr.python
- io.integrityinspector.antlr.model
- io.integrityinspector.model
  - Key: CodeFile, CodeFileTree, Project, Analysis
  - Purpose: data models used by parsing and analysis
- io.integrityinspector.analysis
  - Key: AnalysisCreator, AnalysisTask, various coefficient calculators
  - Purpose: compute similarity metrics and create Analysis objects
- io.integrityinspector.write
  - Key: AnalysisWriter, AnalysisWriterFactory, JsonWriter, JsonWriterFactory, ConsoleWriter
  - Purpose: persist and present results
- io.integrityinspector.config
  - (referred above)

### Algorithms and data flow
- Parsing: select appropriate CodeReader by extension; optionally parse code trees when configured
- Cleaning: normalize lines and remove language-specific noise
- Tree building: produce CodeTree/CodeFileTree via CodeTreeParser and converters
- Similarity: compute tree similarity using TreeSimilarityCalculator and related coefs
- Uniqueness: determine project-wise uniqueness using Zzh1 coefficients and percentage calculators
- Startup sequence (detailed)
- Bootstrapping: App.main creates AppParameters, initializes JCommander, parses CLI args and handles --help
- Configuration: AppConfigReader loads AppConfig (from default or provided path) via readBasedOnParameters(parameters)
- Core bootstrap: AppCoreComponentsFactory creates AppCoreComponents using the loaded AppConfig
- Inspector selection: if parameters include a checking directory, use MultipleProjectIntegrityInspector; otherwise use SingleProjectIntegrityInspector
- Execution: integrityInspector.process(parameters)

- SingleProjectInspector flow:
  1. Parse check project: parser.parseProject(checkFolder)
  2. Parse baselines: parser.parseProjectListFromRootDir(parameters.getBaseLineProjectDir())
  3. Create analysis: analysisCreator.create(checkProject, baselineProjects)
  4. Write report: analysisWriter.write(analysis, checkFolder.getName())

- MultipleProjectInspector flow:
  1. Parse projects: projects = parser.parseProjectListFromRootDir(parameters.getCheckingDirectory())
  2. For each project:
     - baselineProjects = all other projects
     - analysis = analysisCreator.create(checkProject, baselineProjects)
     - if analysis.totalUniquenessPercentage > maxUniquenessPercentageForCreatingReport -> log skip
     - else analysisWriter.write(analysis, projectName)

- Config loading details:
- AppConfigReader.read(): loads default /config.json from resources
- AppConfigReader.read(String filePath): loads config from given path
- AppConfigReader.readBasedOnParameters(parameters): chooses config path based on parameters.configFile

## Architecture overview
- Core modules
  - app
    - App: entry point, wires CLI and core components
    - AppParameters: CLI options (help, config, baseline, checking project, checking directory)
    - AppCoreComponents: holds ProjectParser, AnalysisCreator, AnalysisWriter, AppConfig
    - AppCoreComponentsFactory: constructs components based on AppConfig
  - config
    - AppConfig: aggregates AnalysisConfig, ParserConfig, MultipleProjectCheckConfig
    - AppConfigReader: reads config.json from classpath or file system
    - AdditionalFileExtensionConfig, ParserConfig, MultipleProjectCheckConfig
  - parser.reader
    - project
      - DefaultProjectParser, ProjectParserFactory, DefaultProjectCodeParser
    - file
      - CodeReaderFactory, CodeReaderFactoryFactory, CodeReader, CodeReaderContext readers, IpynbReader
  - model
    - CodeFile, CodeFileTree, Project, Analysis, etc.
  - antlr
    - Java, Python parsers and converters, DefaultCodeTreeParser
  - Inspector
    - IntegrityInspector (interface), SingleProjectIntegrityInspector, MultipleProjectIntegrityInspector
  - write
    - core: AnalysisWriter, AnalysisWriterFactory
    - json: JsonWriter, JsonWriterFactory
    - console: ConsoleWriter, ConsoleWriterFactory
- Build-time and dependencies
  - Lombok, Gson, Commons Lang/Text, SLF4J, JCommander, ANTLR4 runtime, Zhang-Shasha (tree distance)
  - JUnit for tests
- Config and inputs
  - Default config path: /config.json (located in resources)
  - AppConfig comprises analysisConfig, parseCodeConfig, multipleProjectCheckConfig

## Notable usage patterns
- Two user workflows:
  - SingleProjectIntegrityInspector: compares one project against baseline set
  - MultipleProjectIntegrityInspector: compares multiple projects against each other
- End-to-end pipeline: parse input projects -> analyze similarities -> write reports

## Test Coverage
- Current status: We have smoke/integration tests for startup flow and unit tests for core components (cleaners, converters) in src/test/java. Coverage of parsing, analysis, and reporting paths is partial.
- Gaps:
  1) Parser pipeline: DefaultProjectParser, ProjectCodeParser, CodeFileReader interactions across languages
  2) Terminal/code-tree path: parsing trees via DefaultCodeTreeParser and CodeTreeNodeConverter
  3) Tree-based analysis: TreeAnalysisCreator, TreeSimilarityCalculator, and CodeTree-based analysis
 4) JSON/Console reporting: JsonWriter behavior and console writer integration with Analysis objects
  5) Config loading robustness: various combinations of AppConfigReader, ParserConfig, and MultipleProjectCheckConfig
- Proposed plan:
  - Add focused unit tests for CodeReaderFactoryFactory mapping and for DefaultProjectParser/ProjectCodeParser with small in-memory directories
  - Add tests for IpynbReader and Python/Java code tree converters with small synthetic inputs
  - Add integration tests that mock analysis components to test the end-to-end pipeline without heavy IO
  - Ensure Jacoco reports are generated in CI and locally to visualize coverage gaps

## How to run
- Build: mvn clean package
- Run example: java -jar target/integrity-inspector-0.9.0-jar-with-dependencies.jar --help
- CLI options (from AppParameters):
  - -ch-prj / --checking-project: path to a single project
  - -b / --baseline-projects: path to baseline dir
  - -ch-dir / --checking-directory: path to directory with multiple projects
  - -cf / --config: path to config.json
  - -h / --help: show usage

## Next steps
- Add sample config.json and small sample datasets
- Consider CI workflow for build and tests
- Expand docs with API/architecture diagrams

## Notes and questions
- Do you want me to add a minimal sample config and demo data to the repo now?
- Should I set up a CI workflow (GitHub Actions) to run mvn test and package on push?

- Demo Environment
- This repository now includes a minimal end-to-end demo setup:
- demo/config.sample.json
- demo_projects/check/ProjCheck/Main.java
- demo_projects/baseline/ProjA/Main.java
- demo_projects/baseline/ProjB/Main.java
- demo/run-demo.sh
- demo/run-demo.bat

- How to run in demo:
- Build: mvn clean package
- Run (UNIX): bash demo/run-demo.sh
- Run (Windows): demo/run-demo.bat

- Parallel processing and architecture notes


### Selected Classes Map
- io.integrityinspector.app: App, AppParameters, AppCoreComponents, AppCoreComponentsFactory
- io.integrityinspector.config: AppConfig, AppConfigReader, AdditionalFileExtensionConfig, ParserConfig, MultipleProjectCheckConfig
- io.integrityinspector.Inspector: IntegrityInspector, SingleProjectIntegrityInspector, MultipleProjectIntegrityInspector
- io.integrityinspector.parser.reader.project: ProjectParserFactory, DefaultProjectParser, DefaultProjectCodeParser
- io.integrityinspector.parser.reader.file: CodeReaderFactoryFactory, CodeReaderFactory, CodeReader, CodeFileReader, IpynbReader
- io.integrityinspector.parser.cleaner.line: LineCleaner, DefaultLineCleaner, PythonLineCommentCleaner
- io.integrityinspector.parser.cleaner.comment: CommentCleaner, CommonFileCommentCleaner, PythonFileCommentCleaner
- io.integrityinspector.antlr.core: CodeTreeParser, CodeTreeNodeConverter, DefaultCodeTreeParser
- io.integrityinspector.antlr.java: JavaCodeTreeNodeConverter
- io.integrityinspector.antlr.python: PythonCodeTreeNodeConverter
- io.integrityinspector.antlr.model: CodeTree
- io.integrityinspector.model: CodeFile, CodeFileTree, Project, Analysis
- io.integrityinspector.analysis: AnalysisCreator, Zzh1UniquenessCoefficientCalculator, DefualtZzh1UniquenessCoefficientCalculator, UniquenessPercentageCalculator
- io.integrityinspector.write: AnalysisWriter, JsonWriter, ConsoleWriter
- io.integrityinspector.checker: TreeSimilarityCalculator, FileTreeChecker, FileChecker, PlagiarismLineChecker
- io.integrityinspector.app: App, AppParameters
