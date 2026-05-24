# Architecture Overview

This document enumerates the main packages, their responsibilities, and the key classes used to realize the IntegrityInspector application.

## Startup flow
- Entry point: App.main
- Steps: create AppParameters, build JCommander, parse args, handle --help
- Load config: AppConfigReader.readBasedOnParameters(parameters)
- Build core components: AppCoreComponentsFactory.createAppCoreComponents(config)
- Choose inspector: if parameters.checkingDirectory is provided, use MultipleProjectIntegrityInspector; otherwise use SingleProjectIntegrityInspector
- Run analyzer: integrityInspector.process(parameters)

## Package map
- io.integrityinspector.app
  - App: entry point, wires CLI and core components
  - AppParameters: CLI options
  - AppCoreComponents: holds core collaborators (ProjectParser, AnalysisCreator, AnalysisWriter, AppConfig)
  - AppCoreComponentsFactory: constructs components based on AppConfig
- io.integrityinspector.config
  - AppConfig: composite config object (AnalysisConfig, ParserConfig, MultipleProjectCheckConfig)
  - AppConfigReader: loads AppConfig from JSON (default on classpath or provided path)
  - AdditionalFileExtensionConfig, ParserConfig, MultipleProjectCheckConfig: policy data for parsing and analysis
- io.integrityinspector.Inspector
  - IntegrityInspector: interface
  - SingleProjectIntegrityInspector: runs a check for a single project against baselines
  - MultipleProjectIntegrityInspector: runs checks for multiple projects against each other
- io.integrityinspector.parser.reader.project
  - ProjectParserFactory: builds a ProjectParser based on AppConfig
  - DefaultProjectParser: reads a project directory and returns a Project with CodeFiles
  - DefaultProjectCodeParser: uses CodeReaderFactory to parse code files inside a project
  - ProjectParser: interface for parsing projects
  - ProjectCodeParser: parses code files into CodeFile/CodeFileTree
- io.integrityinspector.parser.reader.file
  - CodeReaderFactoryFactory: creates CodeReaderFactory instances for different code readers
  - CodeReaderFactory: selects a CodeReader by file extension
  - CodeReader: base interface for file readers
  - CodeFileReader: implementation for CodeFile reading
  - IpynbReader: wrapper for Python notebooks
  - DefaultCodeFileContextReader: reads file lines into code context
  - LineForCheckExtractor: extracts lines to check for plagiarism
- io.integrityinspector.parser.cleaner.line
  - LineCleaner: interface for cleaning lines
  - DefaultLineCleaner: default line cleaning strategy
  - PythonLineCommentCleaner: Python-specific comment cleaning
- io.integrityinspector.parser.cleaner.comment
  - CommentCleaner: interface
  - CommonFileCommentCleaner: generic file comment cleaner
  - PythonFileCommentCleaner: Python-specific cleaner
- io.integrityinspector.antlr.core
  - CodeTreeParser: parses code into CodeTree
  - CodeTreeNodeConverter: converts parse nodes to CodeTree nodes
  - DefaultCodeTreeParser: default parser implementation for code trees
- io.integrityinspector.antlr.java
- io.integrityinspector.antlr.python
- io.integrityinspector.antlr.model
- io.integrityinspector.model
  - CodeFile, CodeFileTree, Project, Analysis: domain models used by parsing and analysis
- io.integrityinspector.analysis
  - AnalysisCreator: creates Analysis objects from a check and baselines
  - Zzh1UniquenessCoefficientCalculator / DefualtZzh1UniquenessCoefficientCalculator: tree-distance based coefficients
  - UniquenessPercentageCalculator: aggregates coefficients to a final percentage
  - MultithreadingProjectAnalyzer: parallel processing support
  - AnalysisTaskFactory, AnalysisTask, AnalysisCreatorFactory: task orchestration and factory wiring
- io.integrityinspector.write
  - AnalysisWriter / AnalysisWriterFactory: write results
  - JsonWriter / JsonWriterFactory: JSON output implementation
  - ConsoleWriter / ConsoleWriterFactory: console reporting

## Algorithms and data flow
1) Parsing
  - CodeReaderFactoryFactory selects readers by extension; Ipynb supported; default reader for TEXT
- Cleaning: LineCleaner and CommentCleaner variants normalize and tidy input
- Tree construction: CodeTreeParser + CodeTreeNodeConverter -> CodeTree/CodeFileTree structures
- Similarity: TreeSimilarityCalculator computes tree-based similarity; NOT-SUPPORTED codes mapped to special placeholder
- Coefficients: Zzh1UniquenessCoefficientCalculator and DefualtZzh1UniquenessCoefficientCalculator derive distance-based coefficients; UniquenessPercentageCalculator yields overall percentage
- Analysis and reporting: AnalysisCreator builds analyses; AnalysisWriter writes JSON/console reports
- Multithreading: MultithreadingProjectAnalyzer enables parallel processing of projects when configured

## Class Hierarchy (High-Level)
This section provides a compact view of key interfaces and their implementations across the codebase.
IntegrityInspector (interface)
- SingleProjectIntegrityInspector (implements IntegrityInspector)
- MultipleProjectIntegrityInspector (implements IntegrityInspector)

io.integrityinspector.app
- App (entry point)
- AppParameters (CLI options)
- AppCoreComponents (containers for core collaborators)
- AppCoreComponentsFactory (builder for core components)

io.integrityinspector.config
- AppConfig (config object)
- AppConfigReader (loads AppConfig from JSON)
- AdditionalFileExtensionConfig, ParserConfig, MultipleProjectCheckConfig (policy/config data)

io.integrityinspector.Inspector
- IntegrityInspector (interface, above)

io.integrityinspector.parser.reader.project
- ProjectParser (interface)
- DefaultProjectParser (implements ProjectParser)
- ProjectCodeParser (interface)
- DefaultProjectCodeParser (implements ProjectCodeParser)
- ProjectParserFactory (builder/creator using AppConfig)

io.integrityinspector.parser.reader.file
- CodeReader (interface)
- CodeReaderFactory (maps extensions to CodeReader)
- CodeReaderFactoryFactory (creates CodeReaderFactory)
- CodeFileReader (implements CodeReader<CodeFile>)
- IpynbReader (implements CodeReader<CodeFile> via Python reader)
- DefaultCodeFileContextReader, LineForCheckExtractor (supporting utilities)

io.integrityinspector.parser.cleaner.line
- LineCleaner (interface)
- DefaultLineCleaner (implements LineCleaner)
- PythonLineCommentCleaner (specialized cleaner)

io.integrityinspector.parser.cleaner.comment
- CommentCleaner (interface)
- CommonFileCommentCleaner (implements CommentCleaner)
- PythonFileCommentCleaner (implements CommentCleaner)

io.integrityinspector.antlr.core
- CodeTreeParser (interface)
- DefaultCodeTreeParser (implements CodeTreeParser)
- CodeTreeConverter (interface)
- CodeTreeConverter implementations (DefaultCodeTreeConverter)
- CodeTreeNodeConverter (interface)
- CodeTree (model)

io.integrityinspector.antlr.java
- JavaCodeTreeNodeConverter (implements CodeTreeNodeConverter)

io.integrityinspector.antlr.python
- PythonCodeTreeNodeConverter (implements CodeTreeNodeConverter)

io.integrityinspector.antlr.model
- CodeTree (model)

io.integrityinspector.model
- CodeFile, CodeFileTree (models for file representations)
- Project, Analysis (domain models for parsing and analysis)

io.integrityinspector.analysis
- AnalysisCreator (creates Analysis)
- Zzh1UniquenessCoefficientCalculator
- DefualtZzh1UniquenessCoefficientCalculator
- UniquenessPercentageCalculator
- MultithreadingProjectAnalyzer
- AnalysisTask, AnalysisTaskFactory
- AnalysisWriter dependencies (within write package)

io.integrityinspector.write
- AnalysisWriter, AnalysisWriterFactory
- JsonWriter, JsonWriterFactory
- ConsoleWriter, ConsoleWriterFactory

io.integrityinspector.checker
- TreeSimilarityCalculator
- FileTreeChecker
- FileChecker
- PlagiarismLineChecker
