package tools.redfox.bamboo.bumpversion.task.capability;

import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.v2.build.agent.capability.AbstractMultipleExecutableCapabilityTypeModule;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.xwork2.TextProvider;

import java.util.*;

public class BumpversionCapability extends AbstractMultipleExecutableCapabilityTypeModule {
    public BumpversionCapability(@ComponentImport TemplateRenderer templateRenderer, @ComponentImport TextProvider textProvider) {
        setTemplateRenderer(templateRenderer);
        setTextProvider(textProvider);
    }

    @Override
    public Map<String, String> validate(Map<String, String[]> params) {
        return super.validate(params);
    }

    @Override
    public String getExecutableKindKey() {
        return "bumpversionExecutableKind";
    }

    @Override
    public List<String> getAdditionalCapabilityKeys() {
        return Collections.singletonList("system.bumpversion.executable.docker");
    }

    @Override
    public String getCapabilityKindUndefinedKey() {
        return "tools.redfox.bamboo.bumpversion.capability.type.system.bumpversion.error.undefinedExecutable";
    }

    @Override
    public String getMandatoryCapabilityKey() {
        return "system.bumpversion.executable";
    }

    @Override
    public String getCapabilityUndefinedKey() {
        return "tools.redfox.bamboo.bumpversion.capability.type.system.bumpversion.error.undefinedExecutableKind";
    }

    @Override
    public List<String> getDefaultWindowPaths() {
        return new ArrayList<>();
    }

    @Override
    public String getExecutableFilename() {
        return "bumpversion";
    }

    public String getExecutableDescription(String key) {
        return this.getText("tools.redfox.bamboo.bumpversion.capability.type." + key + ".description", new String[]{"/usr/bin/ssh"});
    }
}
