package tools.redfox.bamboo.bumpversion.builders;

import com.atlassian.bamboo.specs.api.builders.task.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.redfox.bamboo.bumpversion.model.BumpVersionTaskProperties;

public class BumpVersionTask extends Task<BumpVersionTask, BumpVersionTaskProperties> {
    @Nullable
    protected String options;

    public BumpVersionTask options(@Nullable String options) {
        this.options = options;
        return this;
    }

    @NotNull
    @Override
    protected BumpVersionTaskProperties build() {
        return new BumpVersionTaskProperties(
                description,
                taskEnabled,
                options,
                requirements,
                conditions
        );
    }
}
