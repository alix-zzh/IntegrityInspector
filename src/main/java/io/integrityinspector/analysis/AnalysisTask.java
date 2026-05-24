package io.integrityinspector.analysis;

import io.integrityinspector.checker.FileChecker;
import io.integrityinspector.checker.ProjectChecker;
import io.integrityinspector.model.Project;
import io.integrityinspector.model.filecheker.FileCheck;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

@AllArgsConstructor
public class AnalysisTask<T extends FileCheck, K extends FileChecker<T>> implements Callable<List<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(AnalysisTask.class);
    private final ProjectChecker<T, K> fullProjectChecker;
    private final List<Project> baselineList;
    private final Project check;

    @Override
    public List<T> call() {
        LOG.info("Processing analysis in multi-thread, batch: {}",
                baselineList.stream().map(Project::getName).reduce((x, y) -> x + ", " + y).orElse(" - "));

        return fullProjectChecker.checkProject(check, baselineList);
    }
}
