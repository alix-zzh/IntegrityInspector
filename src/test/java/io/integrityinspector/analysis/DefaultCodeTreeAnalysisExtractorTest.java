package io.integrityinspector.analysis;

import io.integrityinspector.model.TreeCheckList;
import io.integrityinspector.model.TreeSimilarity;
import io.integrityinspector.model.filecheker.FileTreeCheck;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefaultCodeTreeAnalysisExtractorTest {

    @Test
    public void codeTreeCheckGroupsSortsFiltersAndLimitsResults() {
        FileTreeCheck firstFile = new FileTreeCheck(
                "First.java",
                Collections.emptyList(),
                BigDecimal.TEN,
                Arrays.asList(
                        new TreeSimilarity("slow", "Slow.java", 30),
                        new TreeSimilarity("best", "Best.java", 3),
                        new TreeSimilarity("unsupported", "Unsupported.java", Integer.MAX_VALUE),
                        new TreeSimilarity("second", "Second.java", 7)
                )
        );
        FileTreeCheck secondFile = new FileTreeCheck(
                "Second.java",
                Collections.emptyList(),
                BigDecimal.ONE,
                Collections.singletonList(new TreeSimilarity("only", "Only.java", 5))
        );

        List<TreeCheckList> actual = new DefaultCodeTreeAnalysisExtractor(2)
                .codeTreeCheck(Arrays.asList(firstFile, secondFile));

        TreeCheckList first = findByFileName(actual, "First.java");
        assertEquals(2, first.getTreeSimilarityList().size());
        assertEquals("best", first.getTreeSimilarityList().get(0).getProject());
        assertEquals(3, first.getTreeSimilarityList().get(0).getSimilarityScore());
        assertEquals("second", first.getTreeSimilarityList().get(1).getProject());
        assertEquals(7, first.getTreeSimilarityList().get(1).getSimilarityScore());

        TreeCheckList second = findByFileName(actual, "Second.java");
        assertEquals(1, second.getTreeSimilarityList().size());
        assertEquals("only", second.getTreeSimilarityList().get(0).getProject());
    }

    private TreeCheckList findByFileName(List<TreeCheckList> checks, String fileName) {
        return checks.stream()
                .filter(check -> check.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(AssertionError::new);
    }
}
