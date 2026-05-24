package io.integrityinspector.parser.reader.file;

import io.integrityinspector.antlr.model.CodeTree;
import io.integrityinspector.model.CodeFileTree;
import io.integrityinspector.model.Line;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CodeFileTreeReaderTest {

    @Test
    public void readBuildsCodeFileTreeFromCleanedContent() throws Exception {
        CodeTree parsedTree = new CodeTree(42);
        CodeFileTreeReader reader = new CodeFileTreeReader(
                "Java",
                code -> code.replace("// comment", ""),
                line -> "clean:" + line.trim(),
                file -> "class A {}\n// comment",
                (content, validator, cleaner) -> Collections.singletonList(new Line(1, content, cleaner.apply(content))),
                line -> line.contains("class"),
                (content) -> new CodeTree(1),
                (fileName, content, converter) -> {
                    assertEquals("Sample.java", fileName);
                    assertEquals("class A {}\n", content);
                    return parsedTree;
                }
        );

        CodeFileTree actual = reader.read("Sample.java");

        assertEquals("Sample.java", actual.getSourceFile());
        assertEquals("Java", actual.getLanguage());
        assertEquals("class A {}\n// comment".length(), actual.getFileLineCount());
        assertEquals(1, actual.getCode().size());
        assertEquals("clean:class A {}", actual.getCode().get(0).getContentFiltered());
        assertSame(parsedTree, actual.getCodeTree());
    }
}
