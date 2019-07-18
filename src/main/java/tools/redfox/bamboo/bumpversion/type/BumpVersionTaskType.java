package tools.redfox.bamboo.bumpversion.type;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.utils.process.ExternalProcess;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BumpVersionTaskType implements TaskType {
    private ProcessService processService;
    private CapabilityContext capabilityContext;

    public BumpVersionTaskType(
            @ComponentImport final ProcessService processService,
            @ComponentImport CapabilityContext capabilityContext
    ) {
        this.processService = processService;
        this.capabilityContext = capabilityContext;
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

        if (builder.getTaskState() == TaskState.SUCCESS) {
            String finalFilter = filter;
            String[] bumpedVersion = taskContext.getBuildLogger()
                    .getLastNLogEntries(30)
                    .stream()
                    .map(LogEntry::getLog)
                    .filter(s -> s.startsWith(finalFilter))
                    .findFirst().orElse("=")
                    .split("=");

            if (bumpedVersion.length > 0) {
                taskContext.getBuildLogger().addBuildLogEntry("Failed to extract bumped version");
                builder.failed();
            } else {
                taskContext.getBuildContext().getVariableContext().addResultVariable("version.bumped", bumpedVersion[1]);
            }
        }

        return builder.build();
    }

    protected ExternalProcess execute(TaskContext taskContext, List<String> args) {
        ExternalProcess process;
        List<String> executable = new LinkedList<>();
        executable.add(capabilityContext.getCapabilityValue("tools.redfox.bumpversion.executable"));
        executable.addAll(args);
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
