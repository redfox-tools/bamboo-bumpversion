package tools.redfox.bamboo.bumpversion.task;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.utils.BambooUrl;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.utils.process.ExternalProcess;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BumpversionTask implements TaskType {
    private ProcessService processService;
    private CapabilityContext capabilityContext;
    private AdministrationConfigurationAccessor configurationAccessor;
    private CustomVariableContext customVariableContext;

    public BumpversionTask(
            @ComponentImport final ProcessService processService,
            @ComponentImport CapabilityContext capabilityContext,
            @ComponentImport AdministrationConfigurationAccessor configurationAccessor,
            @ComponentImport CustomVariableContext customVariableContext
    ) {
        this.processService = processService;
        this.capabilityContext = capabilityContext;
        this.configurationAccessor = configurationAccessor;
        this.customVariableContext = customVariableContext;
    }

    @Override
    public TaskResult execute(TaskContext taskContext) throws TaskException {
        final TaskResultBuilder builder = TaskResultBuilder.create(taskContext);
        List<String> args = new LinkedList<String>() {{
            add("--list");
            add(bumpType(taskContext));
        }};

        String filter = "new_version=";
        if (taskContext.getCommonContext().getTriggerReason().getKey().equals("com.atlassian.bamboo.plugin.system.triggerReason:RerunBuildTriggerReason")) {
            taskContext.getBuildLogger().addBuildLogEntry("Ignore version bump for task rebuild. Return current version");
            args.add(0, "--dry-run");
            filter = "current_version=";
        }

        ExternalProcess process = execute(taskContext, args);
        builder.checkReturnCode(process, 0);

        String finalFilter = filter;
        String bumpedVersion = taskContext.getBuildLogger()
                .getLastNLogEntries(30)
                .stream()
                .map(LogEntry::getLog)
                .filter(s -> s.startsWith(finalFilter))
                .findFirst().orElse("=")
                .split("=")[1];

        if (bumpedVersion.isEmpty()) {
            taskContext.getBuildLogger().addBuildLogEntry("Failed to extract bumped version");
            builder.failed();
        } else {
            taskContext.getBuildContext().getVariableContext().addResultVariable("version.bumped", bumpedVersion);
        }

        return builder.build();
    }

    protected ExternalProcess execute(TaskContext taskContext, List<String> args) {
        ExternalProcess process;
        List<String> executable = new LinkedList<>();
        if (taskContext.getConfigurationMap().get("executionMode").equals("local")) {
            executable.add(capabilityContext.getCapabilityValue("system.bumpversion.executable"));
            executable.addAll(args);
        } else {
            String domain = "localhost";
            try {
                domain = (new URL(new BambooUrl(configurationAccessor).getBaseUrl(UrlMode.ABSOLUTE))).getHost();
            } catch (MalformedURLException e) {
            }
            executable.addAll(Arrays.asList(
                    capabilityContext.getCapabilityValue("system.docker.executable"), "run",
                    "-v", taskContext.getWorkingDirectory() + ":/home/guest/host",
                    "-v", customVariableContext.getVariableContexts().get("agentWorkingDirectory").getValue() + ":" + customVariableContext.getVariableContexts().get("agentWorkingDirectory").getValue(),
                    "-e", "GIT_AUTHOR_NAME=Bamboo", "-e", "GIT_AUTHOR_EMAIL=<bamboo@" + domain + ">",
                    "-e", "GIT_COMMITTER_NAME=Bamboo", "-e", "GIT_COMMITTER_EMAIL=<bamboo@" + domain + ">",
                    capabilityContext.getCapabilityValue("system.bumpversion.executable.docker")
            ));
            executable.addAll(args);
        }
        process = processService.createExternalProcess(taskContext,
                new ExternalProcessBuilder()
                        .command(executable)
                        .workingDirectory(taskContext.getWorkingDirectory()));

        taskContext.getBuildLogger().addBuildLogEntry("Executing bumpversion with: " + process.getCommandLine());
        process.execute();

        return process;
    }

    protected String bumpType(TaskContext taskContext) {
        Pattern pattern = Pattern.compile("bumpversion::(major|minor|patch)", Pattern.MULTILINE);

        return taskContext.getBuildContext()
                .getBuildChanges()
                .getChanges()
                .stream()
                .map(commitContext -> {
                    Matcher matcher = pattern.matcher(commitContext.getComment());
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                    return null;
                }).filter(Objects::nonNull)
                .findFirst()
                .orElse("patch");
    }
}
