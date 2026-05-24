package io.integrityinspector.parser.reader.file;

import io.integrityinspector.config.AdditionalFileExtensionConfig;
import io.integrityinspector.config.AnalysisConfig;
import io.integrityinspector.config.AppConfig;
import io.integrityinspector.config.MultipleProjectCheckConfig;
import io.integrityinspector.config.MultithreadingConfig;
import io.integrityinspector.config.ParserConfig;
import io.integrityinspector.config.ProgrammingLangLineStartExclusion;
import io.integrityinspector.model.CodeFile;
import io.integrityinspector.model.CodeFileTree;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static io.integrityinspector.checker.TreeSimilarityCalculator.NOT_SUPPORT_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CodeReaderFactoryFactoryTreeTest {

    @Test
    public void createCodeReaderFactoryRegistersTreeReadersWhenParseTreeIsEnabled() {
        CodeReaderFactory<? extends CodeFile> factory = new CodeReaderFactoryFactory()
                .createCodeReaderFactory(config(true));

        CodeReader<? extends CodeFile> defaultReader = factory.findCodeReader("txt");
        CodeFileTree file = (CodeFileTree) defaultReader.createCodeFile("plain.txt", "first\nsecond");

        assertEquals("TEXT", file.getLanguage());
        assertEquals(NOT_SUPPORT_CODE, file.getCodeTree().getState().intValue());
        assertNotNull(factory.findCodeReader("java"));
        assertNotNull(factory.findCodeReader("py"));
        assertNotNull(factory.findCodeReader("ipynb"));
    }

    @Test
    public void createCodeReaderFactoryRegistersNonTreeLanguageReadersAndAdditionalExtensions() {
        CodeReaderFactory<? extends CodeFile> factory = new CodeReaderFactoryFactory()
                .createCodeReaderFactory(config(false));

        assertNotNull(factory.findCodeReader("c"));
        assertNotNull(factory.findCodeReader("cs"));
        assertNotNull(factory.findCodeReader("js"));
        assertNotNull(factory.findCodeReader("py"));
        assertTrue(factory.findCodeReader("custom") instanceof CodeFileReader);
    }

    private AppConfig config(boolean needParseTree) {
        HashSet<String> customExtensions = new HashSet<>();
        customExtensions.add("custom");
        AdditionalFileExtensionConfig extensionConfig = new AdditionalFileExtensionConfig(customExtensions, "java");
        AnalysisConfig analysisConfig = new AnalysisConfig(1, 1, 10, 1, 0.5, new MultithreadingConfig(1), "json");
        ParserConfig parserConfig = new ParserConfig(
                needParseTree,
                Collections.singletonList(extensionConfig),
                new ProgrammingLangLineStartExclusion(
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet()
                ),
                Collections.emptyList()
        );
        return new AppConfig(analysisConfig, parserConfig, new MultipleProjectCheckConfig(90));
    }
}
