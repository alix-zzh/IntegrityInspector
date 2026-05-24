package io.integrityinspector.parser.reader.file;

import io.integrityinspector.model.CodeFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IpynbReaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readIpynbCodeFileConcatenatesOnlyCodeCellSources() throws IOException {
        File notebook = createNotebook();
        IpynbReader<CodeFile> reader = new IpynbReader<>(new CapturingReader());

        assertEquals("print(1)\nprint(2)\n", reader.readIpynbCodeFile(notebook.getAbsolutePath()));
    }

    @Test
    public void readDelegatesNotebookCodeToPythonReader() throws IOException {
        File notebook = createNotebook();
        CapturingReader pythonReader = new CapturingReader();
        IpynbReader<CodeFile> reader = new IpynbReader<>(pythonReader);

        CodeFile actual = reader.read(notebook.getAbsolutePath());

        assertEquals(notebook.getAbsolutePath(), pythonReader.file);
        assertEquals("print(1)\nprint(2)\n", pythonReader.context);
        assertEquals("Python", actual.getLanguage());
        assertEquals("print(1)\nprint(2)\n".length(), actual.getFileLineCount());
    }

    private File createNotebook() throws IOException {
        File notebook = temporaryFolder.newFile("notebook.ipynb");
        try (FileWriter writer = new FileWriter(notebook)) {
            writer.write("{\"cells\":["
                    + "{\"cell_type\":\"markdown\",\"source\":[\"# title\\n\"]},"
                    + "{\"cell_type\":\"code\",\"source\":[\"print(1)\\n\",\"print(2)\\n\"]}"
                    + "]}");
        }
        return notebook;
    }

    private static class CapturingReader implements CodeReader<CodeFile> {
        private String file;
        private String context;

        @Override
        public CodeFile read(String file) {
            return createCodeFile(file, "");
        }

        @Override
        public CodeFile createCodeFile(String file, String fileContext) {
            this.file = file;
            this.context = fileContext;
            return new CodeFile(file, Collections.emptyList(), fileContext.length(), "Python");
        }
    }
}
