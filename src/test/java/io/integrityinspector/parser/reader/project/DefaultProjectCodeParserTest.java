package io.integrityinspector.parser.reader.project;

import io.integrityinspector.config.AdditionalFileExtensionConfig;
import io.integrityinspector.model.CodeFile;
import io.integrityinspector.parser.reader.file.CodeReader;
import io.integrityinspector.parser.reader.file.CodeReaderFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefaultProjectCodeParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parseCodeReadsNestedFilesWhenSupportedExtensionsAreNull() throws Exception {
        File root = temporaryFolder.newFolder("project");
        File nested = new File(root, "nested");
        nested.mkdir();
        writeFile(new File(nested, "Main.custom"));

        DefaultProjectCodeParser parser = new DefaultProjectCodeParser(factory(), null);

        List<CodeFile> files = parser.parseCode(root);

        assertEquals(1, files.size());
        assertEquals("Main.custom", new File(files.get(0).getSourceFile()).getName());
    }

    @Test
    public void parseCodeSkipsFilesWithUnsupportedExtensions() throws Exception {
        File root = temporaryFolder.newFolder("filtered-project");
        writeFile(new File(root, "Main.java"));
        writeFile(new File(root, "notes.txt"));

        DefaultProjectCodeParser parser = new DefaultProjectCodeParser(factory(), Collections.singletonList("java"));

        List<CodeFile> files = parser.parseCode(root);

        assertEquals(1, files.size());
        assertEquals("Main.java", new File(files.get(0).getSourceFile()).getName());
    }

    private CodeReaderFactory<CodeFile> factory() {
        CodeReader<CodeFile> reader = new CodeReader<CodeFile>() {
            @Override
            public CodeFile read(String file) {
                return new CodeFile(file, Collections.emptyList(), 1, "TEXT");
            }

            @Override
            public CodeFile createCodeFile(String file, String fileContext) {
                return new CodeFile(file, Collections.emptyList(), fileContext.length(), "TEXT");
            }
        };
        return new CodeReaderFactory<>(reader, new HashMap<>(), Collections.<AdditionalFileExtensionConfig>emptyList());
    }

    private void writeFile(File file) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("content");
        }
    }
}
