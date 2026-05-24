package io.integrityinspector.analysis;

import io.integrityinspector.checker.ProjectChecker;
import io.integrityinspector.model.Analysis;
import io.integrityinspector.model.CodeFile;
import io.integrityinspector.model.Project;
import io.integrityinspector.model.ProjectCount;
import io.integrityinspector.model.TreeCheckList;
import io.integrityinspector.model.TreeSimilarity;
import io.integrityinspector.model.filecheker.FileCheck;
import io.integrityinspector.model.filecheker.FileTreeCheck;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TreeAnalysisCreatorTest {

    @Test
    public void createCombinesStringAndTreeChecksIntoAnalysis() {
        CodeFile checkFile = new CodeFile("Main.java", Collections.emptyList(), 10, "Java");
        Project checkProject = new Project("check", Collections.singletonList(checkFile), 10);
        List<Project> baselineProjects = Collections.singletonList(new Project("base", Collections.emptyList(), 0));
        List<ProjectCount> counts = Collections.singletonList(new ProjectCount("base", 2));
        List<TreeSimilarity> treeSimilarities = Collections.singletonList(new TreeSimilarity("base", "Base.java", 4));

        TreeAnalysisCreator creator = new TreeAnalysisCreator(
                (project, baselines) -> Collections.singletonList(
                        new FileTreeCheck("Main.java", Collections.emptyList(), BigDecimal.ZERO, treeSimilarities)
                ),
                new CountsExtractor() {
                    @Override
                    public <T extends FileCheck> List<ProjectCount> extractCountsPerProject(List<T> fileChecks) {
                        return counts;
                    }
                },
                (projectCounts, baselines) -> {
                    assertSame(counts, projectCounts);
                    assertSame(baselineProjects, baselines);
                    return baselineProjects;
                },
                new ProjectChecker<>((file, baselines) -> new FileCheck(file.getSourceFile(), Collections.emptyList(), BigDecimal.valueOf(60))),
                fileChecks -> BigDecimal.valueOf(60),
                fileTreeChecks -> Collections.singletonList(new TreeCheckList("Main.java", treeSimilarities)),
                (totalUniquenessPercentage, projectLineCount) -> {
                    assertEquals(BigDecimal.valueOf(60), totalUniquenessPercentage);
                    assertEquals(10, projectLineCount);
                    return BigDecimal.valueOf(0.6);
                }
        );

        Analysis actual = creator.create(checkProject, baselineProjects);

        assertEquals(BigDecimal.valueOf(60), actual.getTotalUniquenessPercentage());
        assertEquals(BigDecimal.valueOf(0.6), actual.getZzh1UniquenessCoefficient());
        assertSame(counts, actual.getCountPerProject());
        assertEquals(1, actual.getProjectChecks().size());

        FileTreeCheck fileCheck = (FileTreeCheck) actual.getProjectChecks().get(0);
        assertEquals("Main.java", fileCheck.getCodeFileName());
        assertEquals(BigDecimal.valueOf(60), fileCheck.getUniqueStringPresent());
        assertSame(treeSimilarities, fileCheck.getCodeTreeSimilarityList());
    }

    @Test
    public void createDropsStringChecksWithoutMatchingTreeChecks() {
        CodeFile first = new CodeFile("First.java", Collections.emptyList(), 1, "Java");
        CodeFile second = new CodeFile("Second.java", Collections.emptyList(), 1, "Java");
        Project checkProject = new Project("check", Arrays.asList(first, second), 2);
        List<TreeSimilarity> firstTreeSimilarities = Collections.singletonList(new TreeSimilarity("base", "Base.java", 1));

        TreeAnalysisCreator creator = new TreeAnalysisCreator(
                (project, baselines) -> Collections.emptyList(),
                new CountsExtractor() {
                    @Override
                    public <T extends FileCheck> List<ProjectCount> extractCountsPerProject(List<T> fileChecks) {
                        return Collections.emptyList();
                    }
                },
                (projectCounts, baselines) -> baselines,
                new ProjectChecker<>((file, baselines) -> new FileCheck(file.getSourceFile(), Collections.emptyList(), BigDecimal.TEN)),
                fileChecks -> BigDecimal.TEN,
                fileTreeChecks -> Collections.singletonList(new TreeCheckList("First.java", firstTreeSimilarities)),
                (totalUniquenessPercentage, projectLineCount) -> BigDecimal.ONE
        );

        Analysis actual = creator.create(checkProject, Collections.emptyList());

        assertEquals(1, actual.getProjectChecks().size());
        FileTreeCheck fileCheck = (FileTreeCheck) actual.getProjectChecks().get(0);
        assertEquals("First.java", fileCheck.getCodeFileName());
        assertSame(firstTreeSimilarities, fileCheck.getCodeTreeSimilarityList());
    }
}
