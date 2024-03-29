package io.integrityinspector.checker;

import io.integrityinspector.model.*;
import io.integrityinspector.model.filecheker.FileCheck;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FileStringChecker implements FileChecker<FileCheck> {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;
    private final PlagiarismLineChecker lineChecker;
    private final int lineSimilarLimit;


    public FileCheck checkFile(CodeFile codeFile, List<Project> baselineProjects) {
        double plagiarismLineCount = 0.0;
        List<CheckLine> checkedLines = new ArrayList<>();
        for (Line checkedLine : codeFile.getCode()) {
            List<Check> checks = compareLineWithBaselineProjects(checkedLine, baselineProjects);
            List<LineInfo> similarLines = extractSimilarLines(checks, lineSimilarLimit);
            if (!similarLines.isEmpty()) {
                plagiarismLineCount++;
            }
            checkedLines.add(new CheckLine(checkedLine, similarLines));
        }

        BigDecimal uniqueStringPresent = calculateUniqueStringPresent(codeFile, plagiarismLineCount);

        return new FileCheck(codeFile.getSourceFile(), checkedLines, uniqueStringPresent);
    }

    private BigDecimal calculateUniqueStringPresent(CodeFile codeFile, double plagiarismLineCount) {
        if (codeFile.getCode().isEmpty()) {
            return ONE_HUNDRED;
        }
        BigDecimal totalLineCount = BigDecimal.valueOf(codeFile.getCode().size());
        BigDecimal nonUniqueStringPresent = BigDecimal
                .valueOf(plagiarismLineCount)
                .multiply(ONE_HUNDRED)
                .divide(totalLineCount, SCALE, RoundingMode.HALF_DOWN);

        return ONE_HUNDRED.subtract(nonUniqueStringPresent);

    }

    private List<LineInfo> extractSimilarLines(List<Check> list, int lineSimilarLimit) {
        return list
                .stream()
                .filter(distinctByKey(x -> x.getBaseline().getContent().trim() + x.getBaselineProject()))
                .sorted(Check.CHECK_COMPARATOR)
                .limit(lineSimilarLimit)
                .map(x -> new LineInfo(x.getBaselineProject(), x.getBaselineCodeFile(), x.getLevenshteinDistance(), x.getCosineDistance(), x.getJaccardDistance(), x.getBaseline()))
                .sorted(LineInfo.LINE_INFO_COMPARATOR)
                .collect(Collectors.toList());
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private List<Check> compareLineWithBaselineProjects(Line checkedLine, List<Project> baselineProjects) {
        List<Check> list = new ArrayList<>();
        for (Project baselineProject : baselineProjects) {
            for (CodeFile baselineCodeFile : baselineProject.getCodeFileList()) {
                list.addAll(lineChecker.check(checkedLine, baselineCodeFile, baselineProject.getName(), baselineCodeFile.getSourceFile()));
            }
        }
        return list;
    }
}
