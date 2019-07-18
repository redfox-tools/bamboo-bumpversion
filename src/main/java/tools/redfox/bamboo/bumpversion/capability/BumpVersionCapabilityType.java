package tools.redfox.bamboo.bumpversion.capability;

import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.v2.build.agent.capability.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Streams;
import com.opensymphony.xwork2.TextProvider;

import java.util.*;

public class BumpVersionCapabilityType extends AbstractCapabilityTypeModule {
    public BumpVersionCapabilityType(@ComponentImport TemplateRenderer templateRenderer, @ComponentImport TextProvider textProvider) {
        setTemplateRenderer(templateRenderer);
    }

    @Override
    public Map<String, String> validate(Map<String, String[]> map) {
        return Collections.emptyMap();
    }

    @Override
    public Capability getCapability(Map<String, String[]> map) {
        return new CapabilityImpl(
                "tools.redfox.bumpversion.executable",
                Arrays.stream(map.get("tools.redfox.bumpversion.executable")).filter(c-> !c.isEmpty()).findFirst().orElse("")
        );
    }

    @Override
    public String getLabel(String s) {
        return "bumpversion";
    }
}
