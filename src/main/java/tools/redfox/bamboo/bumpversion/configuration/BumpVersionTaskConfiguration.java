package tools.redfox.bamboo.bumpversion.configuration;

import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementImpl;

import java.util.LinkedHashSet;
import java.util.Set;

public class BumpVersionTaskConfiguration extends AbstractTaskConfigurator implements TaskRequirementSupport {
    @Override
    public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition) {
        return new LinkedHashSet<Requirement>() {{
            add(new RequirementImpl("tools.redfox.bumpversion.executable", true, ".*"));
        }};
    }
}
