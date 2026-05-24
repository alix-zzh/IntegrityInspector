package io.integrityinspector.write.json;

import com.google.gson.Gson;
import io.integrityinspector.model.Analysis;
import io.integrityinspector.write.core.AnalysisWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonWriter implements AnalysisWriter {
    private static final Logger LOG = LoggerFactory.getLogger(JsonWriter.class);
    private final Path outputDirectory;

    public JsonWriter() {
        this(Path.of("."));
    }

    public JsonWriter(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void write(Analysis analysis, String name) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(analysis);
        String fileName = "report_" + name + ".json";
        Files.createDirectories(outputDirectory);
        Path reportPath = outputDirectory.resolve(fileName);
        BufferedWriter writer = Files.newBufferedWriter(reportPath);
        try {
            writer.write(json);
            writer.flush();
        } finally {
            writer.close();
        }
        LOG.info("Report generated : {}", fileName);
    }
}
