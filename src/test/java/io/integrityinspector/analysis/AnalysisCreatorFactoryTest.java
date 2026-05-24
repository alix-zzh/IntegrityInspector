package io.integrityinspector.analysis;

import io.integrityinspector.config.AnalysisConfig;
import io.integrityinspector.config.AppConfig;
import io.integrityinspector.config.MultipleProjectCheckConfig;
import io.integrityinspector.config.MultithreadingConfig;
import io.integrityinspector.config.ParserConfig;
import io.integrityinspector.config.ProgrammingLangLineStartExclusion;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class AnalysisCreatorFactoryTest {

    @Test
    public void createAnalysisCreatorReturnsStringCreatorWhenTreeParsingIsDisabled() {
        AnalysisCreator actual = new AnalysisCreatorFactory().createAnalysisCreator(config(false, new MultithreadingConfig(2)));

        assertTrue(actual instanceof StringAnalysisCreator);
    }

    @Test
    public void createAnalysisCreatorReturnsTreeCreatorWhenTreeParsingIsEnabled() {
        AnalysisCreator actual = new AnalysisCreatorFactory().createAnalysisCreator(config(true, new MultithreadingConfig(2)));

        assertTrue(actual instanceof TreeAnalysisCreator);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createAnalysisCreatorRejectsTreeParsingWithoutMultithreadingConfig() {
        new AnalysisCreatorFactory().createAnalysisCreator(config(true, null));
    }

    private AppConfig config(boolean needParseTree, MultithreadingConfig multithreadingConfig) {
        AnalysisConfig analysisConfig = new AnalysisConfig(2, 3, 10, 1, 0.5, multithreadingConfig, "json");
        ParserConfig parserConfig = new ParserConfig(
                needParseTree,
                Collections.emptyList(),
                new ProgrammingLangLineStartExclusion(
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet()
                ),
                Collections.emptyList()
        );
        return new AppConfig(analysisConfig, parserConfig, new MultipleProjectCheckConfig(80));
    }
}
