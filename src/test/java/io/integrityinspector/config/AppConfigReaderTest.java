package io.integrityinspector.config;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class AppConfigReaderTest {
    private static final AppConfigReader configReader = new AppConfigReader();
    private static final AppConfig expected;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File configFile;

    static {
        Set<String> set6 = new HashSet<>();
        Set<String> set7 = new HashSet<>();
        Set<String> set8 = new HashSet<>();

        set6.add("extensions 6 1");
        set6.add("extensions 6 2");
        set6.add("extensions 6 3");

        set7.add("extensions 7 1");
        set7.add("extensions 7 2");

        set8.add("extensions 8 1");

        AdditionalFileExtensionConfig extension6 = new AdditionalFileExtensionConfig(set6, "6");
        AdditionalFileExtensionConfig extension7 = new AdditionalFileExtensionConfig(set7, "7");
        AdditionalFileExtensionConfig extension8 = new AdditionalFileExtensionConfig(set8, "8");

        List<AdditionalFileExtensionConfig> additionalFileExtensions = new ArrayList<>();
        additionalFileExtensions.add(extension6);
        additionalFileExtensions.add(extension7);
        additionalFileExtensions.add(extension8);

        AnalysisConfig analysisConfig = new AnalysisConfig(1, 2, 3, 4, 5.0, new MultithreadingConfig(6), "json");

        ProgrammingLangLineStartExclusion programmingLangLineStartExclusion = new ProgrammingLangLineStartExclusion();
        programmingLangLineStartExclusion.setCpp(new HashSet<>(Arrays.asList("1", "2", "3")));
        programmingLangLineStartExclusion.setCSharp(new HashSet<>(Arrays.asList("4", "5")));
        programmingLangLineStartExclusion.setJava(new HashSet<>(Arrays.asList("6", "7")));
        programmingLangLineStartExclusion.setPython(new HashSet<>(Arrays.asList("8", "9")));
        programmingLangLineStartExclusion.setJs(new HashSet<>(Arrays.asList("10", "11")));
        ParserConfig parseCodeConfig = new ParserConfig(false, additionalFileExtensions, programmingLangLineStartExclusion, Collections.singletonList("java"));
        MultipleProjectCheckConfig multipleProjectCheckConfig = new MultipleProjectCheckConfig(12);
        expected = new AppConfig(analysisConfig, parseCodeConfig, multipleProjectCheckConfig);
    }

    @Before
    public void before() throws IOException {
        configFile = temporaryFolder.newFile("config.json");
        Gson gson = new Gson();
        String str = gson.toJson(expected);
        BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
        writer.write(str);
        writer.close();
    }

    @Test
    public void readFromPathTest() throws IOException {
        AppConfig actual = configReader.read(configFile.getAbsolutePath());
        assertEquals(expected, actual);
    }

    @Test
    public void readWithoutResourcesTest() {
        AppConfig actual = configReader.read();
        assertEquals(expected, actual);
    }
}
