package io.integrityinspector.analysis;

import io.integrityinspector.config.AnalysisConfig;
import io.integrityinspector.config.AppConfig;
import io.integrityinspector.config.MultipleProjectCheckConfig;
import io.integrityinspector.config.MultithreadingConfig;
import io.integrityinspector.config.ParserConfig;
import io.integrityinspector.config.ProgrammingLangLineStartExclusion;
import io.integrityinspector.model.Analysis;
import io.integrityinspector.model.Project;
import io.integrityinspector.model.filecheker.FileTreeCheck;
import io.integrityinspector.parser.reader.project.ProjectParser;
import io.integrityinspector.parser.reader.project.ProjectParserFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreeAnalysisIntegrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void treeAnalysisProducesTreeSimilarityChecksForJavaProjects() throws Exception {
        AppConfig config = config();
        File checkProjectDir = createProject(temporaryFolder.getRoot(), "TreeCheck", "System.out.println(\"same\");");
        File baselineRoot = temporaryFolder.newFolder("tree-baseline");
        createProject(baselineRoot, "TreeBaseline", "System.out.println(\"same\");");

        ProjectParser parser = new ProjectParserFactory().createProjectParser(config);
        Project checkProject = parser.parseProject(checkProjectDir);
        List<Project> baselineProjects = parser.parseProjectListFromRootDir(baselineRoot.getAbsolutePath());
        AnalysisCreator analysisCreator = new AnalysisCreatorFactory().createAnalysisCreator(config);

        Analysis analysis = analysisCreator.create(checkProject, baselineProjects);

        assertTrue(analysis.getTotalUniquenessPercentage().compareTo(BigDecimal.ZERO) >= 0);
        assertFalse(analysis.getProjectChecks().isEmpty());
        FileTreeCheck fileTreeCheck = (FileTreeCheck) analysis.getProjectChecks().get(0);
        assertFalse(fileTreeCheck.getCodeTreeSimilarityList().isEmpty());
        assertTrue(fileTreeCheck.getCodeTreeSimilarityList().get(0).getSimilarityScore() >= 0);
    }

    private AppConfig config() {
        AnalysisConfig analysisConfig = new AnalysisConfig(10, 50, 3, 1, 0.8, new MultithreadingConfig(2), "json");
        ParserConfig parserConfig = new ParserConfig(
                true,
                Collections.emptyList(),
                new ProgrammingLangLineStartExclusion(
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet()
                ),
                Collections.singletonList("java")
        );
        return new AppConfig(analysisConfig, parserConfig, new MultipleProjectCheckConfig(100));
    }

    private File createProject(File root, String name, String statement) throws Exception {
        File project = new File(root, name);
        assertTrue(project.mkdirs());
        File code = new File(project, "Main.java");
        try (FileWriter writer = new FileWriter(code)) {
            writer.write("public class Main {\n");
            writer.write("    public static void main(String[] args) {\n");
            writer.write("        " + statement + "\n");
            writer.write("    }\n");
            writer.write("}\n");
        }
        return project;
    }
}
