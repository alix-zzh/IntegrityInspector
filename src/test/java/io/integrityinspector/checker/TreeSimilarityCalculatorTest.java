package io.integrityinspector.checker;

import io.integrityinspector.antlr.model.CodeTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeSimilarityCalculatorTest {

    private final TreeSimilarityCalculator calculator = new TreeSimilarityCalculator();

    @Test
    public void calculateTreeSimilarityReturnsMaxValueForNullTrees() {
        assertEquals(Integer.MAX_VALUE, calculator.calculateTreeSimilarity(null, new CodeTree(1)));
        assertEquals(Integer.MAX_VALUE, calculator.calculateTreeSimilarity(new CodeTree(1), null));
    }

    @Test
    public void calculateTreeSimilarityReturnsMaxValueForUnsupportedTrees() {
        assertEquals(
                Integer.MAX_VALUE,
                calculator.calculateTreeSimilarity(new CodeTree(TreeSimilarityCalculator.NOT_SUPPORT_CODE), new CodeTree(1))
        );
        assertEquals(
                Integer.MAX_VALUE,
                calculator.calculateTreeSimilarity(new CodeTree(1), new CodeTree(TreeSimilarityCalculator.NOT_SUPPORT_CODE))
        );
    }

    @Test
    public void calculateTreeSimilarityUsesZhangShashaDistanceForSupportedTrees() {
        assertEquals(0, calculator.calculateTreeSimilarity(new CodeTree(1), new CodeTree(1)));
        assertEquals(1, calculator.calculateTreeSimilarity(new CodeTree(1), new CodeTree(2)));
    }
}
