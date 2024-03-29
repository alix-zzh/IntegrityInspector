package io.integrityinspector.analysis;

import io.integrityinspector.model.Project;
import io.integrityinspector.model.ProjectCount;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultBaseLineProjectLimiter implements BaseLineProjectLimiter {

    @Override
    public List<Project> limitBaselineProjectList(List<ProjectCount> countsPerProject, List<Project> baselineProjects) {
        Set<String> topNSimilarProjects = countsPerProject
                .stream()
                .map(ProjectCount::getName)
                .collect(Collectors.toSet());
        return baselineProjects
                .stream()
                .filter(x -> topNSimilarProjects.contains(x.getName()))
                .collect(Collectors.toList());
    }
}