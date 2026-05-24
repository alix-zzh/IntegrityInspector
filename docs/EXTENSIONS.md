# Extensions Guide

This document describes how to extend IntegrityInspector with new language readers and file extensions.

Overview
- The system uses CodeReaderFactoryFactory to create a CodeReaderFactory based on AppConfig.ParserConfig.
- Each language may have a dedicated CodeReader (e.g., Java, Python) or a generic Text reader for unknown types.
- To add support for a new language, you typically:
  - Implement a new CodeReader<T> for your CodeFile type
  - Extend CodeReaderFactoryFactory.createCodeReaderFactory to register your reader for a new language key
  - Update AppConfig with new extensions mapping and (if needed) a new CodeReader to handle them

Step-by-step example (pseudo-steps)
- Create a new CodeFile implementation for your language (if necessary) and a reader that parses files into that type.
- Implement a new CodeReader<T> that reads a file and returns the CodeFile instance.
- Register in CodeReaderFactoryFactory:
  - Add a new language constant (e.g., MYLANG = "MyLang")
  - Create a new CodeFileReader for that language and put it into readerMap
- Update parser config to include the new file extensions (e.g., .mlg) and any mapping rules.

Best practices
- Keep the reader small and focused; reuse existing helpers (LineCleaner, CommentCleaner) where possible.
- Add tests for the new reader to cover simple cases.

Appendix: references
- See io.integrityinspector.parser.reader.file.* for existing readers as reference
