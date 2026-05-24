package io.integrityinspector.checker;

import io.integrityinspector.antlr.model.CodeTree;
import io.integrityinspector.model.CodeFile;
import io.integrityinspector.model.CodeFileTree;
import io.integrityinspector.model.Project;
import io.integrityinspector.model.TreeSimilarity;
import io.integrityinspector.model.filecheker.FileCheck;
import io.integrityinspector.model.filecheker.FileTreeCheck;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileTreeCheckerTest {

    @Test
    public void checkFileReturnsStringCheckWhenFileIsNotTreeBacked() {
        CodeFile codeFile = new CodeFile("plain.txt", Collections.emptyList(), 1, "TEXT");
        FileTreeChecker checker = new FileTreeChecker(
                new TreeSimilarityCalculator(),
                (file, projects) -> new FileCheck(file.getSourceFile(), Collections.emptyList(), BigDecimal.valueOf(75))
        );

        FileTreeCheck actual = checker.checkFile(codeFile, Collections.emptyList());

        assertEquals("plain.txt", actual.getCodeFileName());
        assertEquals(BigDecimal.valueOf(75), actual.getUniqueStringPresent());
        assertTrue(actual.getCodeTreeSimilarityList().isEmpty());
    }

    @Test
    public void checkFileCalculatesTreeSimilarityOnlyForMatchingTreeLanguages() {
        CodeFileTree checkFile = new CodeFileTree("check.java", Collections.emptyList(), 1, new CodeTree(1), "Java");
        Project sameLanguage = new Project(
                "same",
                Collections.singletonList(new CodeFileTree("same.java", Collections.emptyList(), 1, new CodeTree(1), "Java")),
                1
        );
        Project differentLanguage = new Project(
                "different",
                Collections.singletonList(new CodeFileTree("different.py", Collections.emptyList(), 1, new CodeTree(1), "Python")),
                1
        );
        Project nonTree = new Project(
                "nonTree",
                Collections.singletonList(new CodeFile("plain.java", Collections.emptyList(), 1, "Java")),
                1
        );
        FileTreeChecker checker = new FileTreeChecker(
                new TreeSimilarityCalculator(),
                (file, projects) -> new FileCheck(file.getSourceFile(), Collections.emptyList(), BigDecimal.TEN)
        );

        FileTreeCheck actual = checker.checkFile(checkFile, Arrays.asList(sameLanguage, differentLanguage, nonTree));

        List<TreeSimilarity> similarities = actual.getCodeTreeSimilarityList();
        assertEquals(1, similarities.size());
        assertEquals("same", similarities.get(0).getProject());
        assertEquals("same.java", similarities.get(0).getCodeFileName());
        assertEquals(0, similarities.get(0).getSimilarityScore());
    }
}
