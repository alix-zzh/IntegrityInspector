package io.integrityinspector.antlr.core;

import io.integrityinspector.antlr.model.CodeTree;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DefaultCodeTreeParserTest {

    @Test
    public void parseCodeTreeReturnsConvertedTree() {
        CodeTree expected = new CodeTree(7);

        CodeTree actual = new DefaultCodeTreeParser()
                .parseCodeTree("file.java", "class A {}", content -> expected);

        assertSame(expected, actual);
    }

    @Test
    public void parseCodeTreeReturnsNullWhenConverterFails() {
        CodeTree actual = new DefaultCodeTreeParser()
                .parseCodeTree("bad.java", "bad", content -> {
                    throw new IllegalArgumentException("bad code");
                });

        assertNull(actual);
    }
}
