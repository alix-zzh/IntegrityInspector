package io.integrityinspector.parser.reader.file;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultLineValidatorTest {

    @Test
    public void isLineNeedAddToCheckListRejectsLinesThatCleanToEmpty() {
        DefaultLineValidator validator = new DefaultLineValidator(Collections.emptySet(), line -> "");

        assertFalse(validator.isLineNeedAddToCheckList("   "));
    }

    @Test
    public void isLineNeedAddToCheckListRejectsConfiguredPrefixes() {
        DefaultLineValidator validator = new DefaultLineValidator(
                Collections.singleton("import"),
                String::trim
        );

        assertFalse(validator.isLineNeedAddToCheckList(" import java.util.List;"));
    }

    @Test
    public void isLineNeedAddToCheckListAcceptsNonEmptyLineWithoutMatchingPrefix() {
        DefaultLineValidator validator = new DefaultLineValidator(
                Collections.singleton("import"),
                String::trim
        );

        assertTrue(validator.isLineNeedAddToCheckList("class Main {}"));
    }

    @Test
    public void isLineNeedAddToCheckListAcceptsWhenNoPrefixesAreConfigured() {
        DefaultLineValidator validator = new DefaultLineValidator(Collections.emptySet(), String::trim);

        assertTrue(validator.isLineNeedAddToCheckList("class Main {}"));
    }

    @Test
    public void isLineNeedAddToCheckListChecksAllConfiguredPrefixes() {
        DefaultLineValidator validator = new DefaultLineValidator(
                Arrays.asList("package", "import").stream().collect(java.util.stream.Collectors.toSet()),
                String::trim
        );

        assertFalse(validator.isLineNeedAddToCheckList("import java.util.List;"));
        assertTrue(validator.isLineNeedAddToCheckList("class Main {}"));
    }
}
