package tools.redfox.bamboo.bumpversion.task.configuration;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementImpl;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class BumpversionTaskConfiguration extends AbstractTaskConfigurator implements TaskRequirementSupport {
    @Override
    public Map<String, String> generateTaskConfigMap(ActionParametersMap params, TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put("executionMode", ((String[]) params.get("executionMode"))[0]);

        return config;
    }

    @Override
    public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        context.put("executionModes", new LinkedList<String>() {{
            add("local");
            add("docker");
        }});
        context.put("executionMode", taskDefinition.getConfiguration().get("executionMode"));
    }

    @Override
    public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition) {
        return new LinkedHashSet<Requirement>() {{
            if (taskDefinition.getConfiguration().getOrDefault("executionMode", "local").equals("local")) {
                add(new RequirementImpl("system.bumpversion.executable", true, ".*"));
            } else {
                add(new RequirementImpl("system.docker.executable", true, ".*"));
                add(new RequirementImpl("system.bumpversion.executable.docker", true, ".*"));
            }
        }};
    }
}
