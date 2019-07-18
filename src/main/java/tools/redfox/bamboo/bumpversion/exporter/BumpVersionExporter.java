package tools.redfox.bamboo.bumpversion.exporter;

import com.atlassian.bamboo.specs.api.builders.task.Task;
import com.atlassian.bamboo.specs.api.model.task.TaskProperties;
import com.atlassian.bamboo.specs.api.validators.common.ValidationProblem;
import com.atlassian.bamboo.task.TaskContainer;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.export.TaskDefinitionExporter;
import com.atlassian.bamboo.task.export.TaskValidationContext;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import tools.redfox.bamboo.bumpversion.builders.BumpVersionTask;
import tools.redfox.bamboo.bumpversion.model.BumpVersionTaskProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BumpVersionExporter implements TaskDefinitionExporter {
    @NotNull
    @Override
    public Map<String, String> toTaskConfiguration(@NotNull TaskContainer taskContainer, @NotNull TaskProperties taskProperties) {
        BumpVersionTaskProperties typedTaskProperties = new BumpVersionTaskProperties();
        Preconditions.checkState(typedTaskProperties != null, "Don't know how to import task properties of type: " + taskProperties.getClass().getName());
        Map<String, String> config = new HashMap<>();
        config.put("options", typedTaskProperties.getOptions());
        return config;
    }

    @NotNull
    @Override
    public Task toSpecsEntity(@NotNull TaskDefinition taskDefinition) {
        Map<String, String> config = taskDefinition.getConfiguration();
        return new BumpVersionTask()
                .options(config.get("options"));
    }

    @NotNull
    @Override
    public List<ValidationProblem> validate(@NotNull TaskValidationContext taskValidationContext, @NotNull TaskProperties taskProperties) {
        return Collections.emptyList();
    }
}
