package io.integrityinspector;

import io.integrityinspector.app.App;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class StartupFlowTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final List<File> reportsToDelete = new ArrayList<>();

    @After
    public void after() {
        for (File report : reportsToDelete) {
            if (report.exists()) {
                report.delete();
            }
        }
    }

    @Test
    public void singleProjectStartupWritesJsonReport() throws Exception {
        File checkProject = createProject("StartupSingleCheck", "System.out.println(\"same\");");
        File baselineRoot = temporaryFolder.newFolder("baseline");
        createProject(baselineRoot, "StartupSingleBaseline", "System.out.println(\"same\");");
        File config = createConfig(false, 100);
        File report = reportFile(checkProject.getName());

        App.main(new String[]{
                "-cf", config.getAbsolutePath(),
                "-b", baselineRoot.getAbsolutePath(),
                "-ch-prj", checkProject.getAbsolutePath()
        });

        assertTrue(report.exists());
        String json = Files.readString(report.toPath());
        assertTrue(json.contains("\"totalUniquenessPercentage\""));
        assertTrue(json.contains("\"projectChecks\""));
    }

    @Test
    public void multiProjectStartupWritesJsonReportForProjectBelowThreshold() throws Exception {
        File checkingRoot = temporaryFolder.newFolder("checking");
        File firstProject = createProject(checkingRoot, "StartupMultiFirst", "System.out.println(\"same\");");
        File secondProject = createProject(checkingRoot, "StartupMultiSecond", "System.out.println(\"same\");");
        File config = createConfig(false, 100);
        File report = reportFile(firstProject.getName());
        reportFile(secondProject.getName());

        App.main(new String[]{
                "-cf", config.getAbsolutePath(),
                "-ch-dir", checkingRoot.getAbsolutePath()
        });

        assertTrue(report.exists());
        String json = Files.readString(report.toPath());
        assertTrue(json.contains("\"countPerProject\""));
    }

    private File createProject(String name, String statement) throws Exception {
        return createProject(temporaryFolder.getRoot(), name, statement);
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

    private File createConfig(boolean needParseTree, int maxUniquenessPercentageForCreatingReport) throws Exception {
        File config = temporaryFolder.newFile("config.json");
        try (FileWriter writer = new FileWriter(config)) {
            writer.write("{\n");
            writer.write("  \"analysisConfig\": {\n");
            writer.write("    \"projectLimit\": 10,\n");
            writer.write("    \"lineSimilarLimit\": 50,\n");
            writer.write("    \"maxLineLengthDiff\": 3,\n");
            writer.write("    \"minLineLength\": 1,\n");
            writer.write("    \"levenshteinSimilarityPercent\": 0.8,\n");
            writer.write("    \"multithreadingConfig\": {\"threadsCount\": 2},\n");
            writer.write("    \"reportResultFormat\": \"json\"\n");
            writer.write("  },\n");
            writer.write("  \"parseCodeConfig\": {\n");
            writer.write("    \"needParseTree\": " + needParseTree + ",\n");
            writer.write("    \"additionalFileExtensions\": [],\n");
            writer.write("    \"programmingLangLineStartExclusion\": {\n");
            writer.write("      \"cpp\": [], \"cSharp\": [], \"java\": [], \"python\": [], \"js\": []\n");
            writer.write("    },\n");
            writer.write("    \"listOfSupportedExtensions\": [\"java\"]\n");
            writer.write("  },\n");
            writer.write("  \"multipleProjectCheckConfig\": {\n");
            writer.write("    \"maxUniquenessPercentageForCreatingReport\": " + maxUniquenessPercentageForCreatingReport + "\n");
            writer.write("  }\n");
            writer.write("}\n");
        }
        return config;
    }

    private File reportFile(String projectName) {
        File report = new File("report_" + projectName + ".json");
        reportsToDelete.add(report);
        if (report.exists()) {
            report.delete();
        }
        return report;
    }
}
