package io.integrityinspector.antlr.core;

import io.integrityinspector.antlr.model.CodeTree;
import io.integrityinspector.antlr.python.gen.Python3Lexer;
import io.integrityinspector.antlr.python.gen.Python3Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class DefaultCodeTreeConverterTest {
    private static final String code =
            "def main():\n" +
                    "        print(\"Test\")\n" +
                    "        operation = input()";


    @Test
    public void convertCodeTreeNodePositiveTest() {
        CodeTreeConverter converter = new DefaultCodeTreeConverter();
        CharStream charStream = CharStreams.fromString(code);
        Python3Lexer lexer = new Python3Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        ParseTree tree = parser.file_input();

        CodeTree actual = converter.convertCodeTreeNode(tree);

        assertEquals(Integer.valueOf(-1), actual.getState());
        assertFalse(actual.getChildren().isEmpty());
        assertTreeParentsAreLinked(actual);
    }

    private void assertTreeParentsAreLinked(CodeTree node) {
        for (CodeTree child : node.getChildren()) {
            assertSame(node, child.getParent());
            assertTreeParentsAreLinked(child);
        }
    }
}
