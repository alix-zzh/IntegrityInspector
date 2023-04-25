package org.integrityinspector.parser.reader.file;

import lombok.EqualsAndHashCode;
import org.integrityinspector.antlr.model.CodeTree;
import org.integrityinspector.model.CodeFile;
import org.integrityinspector.model.Line;
import org.integrityinspector.parser.cleaner.line.DefaultLineCleaner;
import org.integrityinspector.parser.cleaner.line.LineCleaner;

import java.io.IOException;
import java.util.List;

@EqualsAndHashCode
public class DefaultReader implements CodeReader {

    private static final LineCleaner LINE_CLEANER = new DefaultLineCleaner();
    private static final String PYTHON_FILE_LINE_DELIMITER = "\n";
    private static final DefaultCodeFileReader FILER_READER = new DefaultCodeFileReader(PYTHON_FILE_LINE_DELIMITER);
    private static final LineForCheckExtractor LINE_FOR_CHECK_EXTRACTOR = new LineForCheckExtractor();
    private static final String LANGUAGE = "TEXT";


    @Override
    public CodeFile read(String file) throws IOException {
        String fileContext = FILER_READER.readFileFullContext(file);
        List<Line> lineForCheck = LINE_FOR_CHECK_EXTRACTOR.extractLinesForCheck(
                fileContext,
                PYTHON_FILE_LINE_DELIMITER,
                x -> true,
                LINE_CLEANER::cleanLine
        );
        return new CodeFile(file, lineForCheck, fileContext.length(), new CodeTree(-1), LANGUAGE);
    }

}