package io.integrityinspector.app;

import io.integrityinspector.analysis.AnalysisCreator;
import io.integrityinspector.analysis.AnalysisCreatorFactory;
import io.integrityinspector.config.AppConfig;
import io.integrityinspector.parser.reader.project.ProjectParser;
import io.integrityinspector.parser.reader.project.ProjectParserFactory;
import io.integrityinspector.write.core.AnalysisWriter;
import io.integrityinspector.write.jtwig.JtwigTemplateNameFactory;
import io.integrityinspector.write.jtwig.JtwigWriter;

public class AppCoreComponentsFactory {


    public AppCoreComponents createAppCoreComponents(AppConfig config) {
        AnalysisCreatorFactory analysisCreatorFactory = new AnalysisCreatorFactory();
        AnalysisCreator analysisCreator = analysisCreatorFactory.createAnalysisCreator(config);

        JtwigTemplateNameFactory jtwigTemplateNameFactory = new JtwigTemplateNameFactory();
        String templateName = jtwigTemplateNameFactory.createTemplateName(config);

        ProjectParserFactory projectParserFactory = new ProjectParserFactory();
        ProjectParser projectParser = projectParserFactory.createProjectParser(config);

        AnalysisWriter analysisWriter = new JtwigWriter();

        return new AppCoreComponents(
                projectParser,
                analysisCreator,
                templateName,
                analysisWriter,
                config
        );
    }
}
