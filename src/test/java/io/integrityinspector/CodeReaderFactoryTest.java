package io.integrityinspector;

import io.integrityinspector.config.AnalysisConfig;
import io.integrityinspector.config.AppConfig;
import io.integrityinspector.config.MultithreadingConfig;
import io.integrityinspector.config.ParserConfig;
import io.integrityinspector.config.ProgrammingLangLineStartExclusion;
import io.integrityinspector.config.AdditionalFileExtensionConfig;
import io.integrityinspector.config.MultipleProjectCheckConfig;
import io.integrityinspector.model.CodeFile;
import io.integrityinspector.parser.reader.file.CodeReader;
import io.integrityinspector.parser.reader.file.CodeReaderFactory;
import io.integrityinspector.parser.reader.file.CodeReaderFactoryFactory;
import org.junit.Test;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class CodeReaderFactoryTest {

    @Test
    public void testCodeReaderFactoryRegistersReaders() {
        AnalysisConfig analysisConfig = new AnalysisConfig(
                100, // projectLimit
                50,  // lineSimilarLimit
                3,   // maxLineLengthDiff
                1,   // minLineLength
                0.8, // levenshteinSimilarityPercent
                new MultithreadingConfig(2),
                "JSON"
        );
        Set<String> emptySet = new HashSet<>();
        ProgrammingLangLineStartExclusion langExcl = new ProgrammingLangLineStartExclusion(emptySet, emptySet, emptySet, emptySet, emptySet);
        ParserConfig parserConfig = new ParserConfig(false, Collections.emptyList(), langExcl, Collections.emptyList());
        MultipleProjectCheckConfig multiCfg = new MultipleProjectCheckConfig(30);
        AppConfig appConfig = new AppConfig(analysisConfig, parserConfig, multiCfg);

        CodeReaderFactory<? extends CodeFile> factory = new CodeReaderFactoryFactory().createCodeReaderFactory(appConfig);

        CodeReader<? extends CodeFile> javaReader = factory.findCodeReader("java");
        assertNotNull("Java reader should be CodeFileReader or equivalent", javaReader);

        CodeReader<? extends CodeFile> pyReader = factory.findCodeReader("py");
        assertNotNull(pyReader);

        CodeReader<? extends CodeFile> ipynbReader = factory.findCodeReader("ipynb");
        assertNotNull(ipynbReader);

        CodeReader<? extends CodeFile> txtReader = factory.findCodeReader("txt");
        assertNotNull(txtReader);
    }
}
