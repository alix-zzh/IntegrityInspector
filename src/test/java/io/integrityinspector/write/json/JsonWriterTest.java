package io.integrityinspector.write.json;

import io.integrityinspector.model.Analysis;
import io.integrityinspector.model.ProjectCount;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class JsonWriterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void writeCreatesJsonReportFile() throws Exception {
        File outputDirectory = temporaryFolder.newFolder("reports");
        Analysis analysis = new Analysis(
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(0.42),
                Collections.emptyList(),
                Collections.singletonList(new ProjectCount("baseline", 2))
        );

        new JsonWriter(outputDirectory.toPath()).write(analysis, "unit_json_writer");

        File report = new File(outputDirectory, "report_unit_json_writer.json");
        String json = Files.readString(report.toPath());
        assertTrue(json.contains("\"totalUniquenessPercentage\":88"));
        assertTrue(json.contains("\"zzh1UniquenessCoefficient\":0.42"));
        assertTrue(json.contains("\"name\":\"baseline\""));
    }
}
