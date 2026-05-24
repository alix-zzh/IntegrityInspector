package io.integrityinspector.analysis;

import io.integrityinspector.model.CheckLine;
import io.integrityinspector.model.Line;
import io.integrityinspector.model.LineInfo;
import io.integrityinspector.model.filecheker.FileCheck;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class DefaultUniquenessPercentageCalculatorTest {

    private final DefaultUniquenessPercentageCalculator calculator = new DefaultUniquenessPercentageCalculator();

    @Test
    public void calculateTotalUniquenessPercentageReturnsZeroWhenNoLinesWereChecked() {
        assertEquals(
                BigDecimal.valueOf(0).setScale(2),
                calculator.calculateTotalUniquenessPercentage(Collections.singletonList(
                        new FileCheck("empty", Collections.emptyList(), BigDecimal.ZERO)
                ))
        );
    }

    @Test
    public void calculateTotalUniquenessPercentageReturnsZeroWhenAllLinesHaveMatches() {
        CheckLine matched = new CheckLine(
                new Line(1, "same", "same"),
                Collections.singletonList(new LineInfo("base", "Base.java", 0, 0, 0, new Line(1, "same", "same")))
        );

        assertEquals(
                BigDecimal.valueOf(0).setScale(2),
                calculator.calculateTotalUniquenessPercentage(Collections.singletonList(
                        new FileCheck("matched", Collections.singletonList(matched), BigDecimal.ZERO)
                ))
        );
    }

    @Test
    public void calculateTotalUniquenessPercentageCountsLinesWithoutMatchesAsUnique() {
        CheckLine unique = new CheckLine(new Line(1, "unique", "unique"), Collections.emptyList());
        CheckLine matched = new CheckLine(
                new Line(2, "same", "same"),
                Collections.singletonList(new LineInfo("base", "Base.java", 0, 0, 0, new Line(2, "same", "same")))
        );

        assertEquals(
                BigDecimal.valueOf(50).setScale(2),
                calculator.calculateTotalUniquenessPercentage(Collections.singletonList(
                        new FileCheck("mixed", Arrays.asList(unique, matched), BigDecimal.ZERO)
                ))
        );
    }
}
