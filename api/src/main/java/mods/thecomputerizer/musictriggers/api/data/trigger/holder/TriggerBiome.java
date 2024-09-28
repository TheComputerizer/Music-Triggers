package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

@Getter
public class TriggerBiome extends HolderTrigger {

    private ResourceContext tagCtx;

    public TriggerBiome(ChannelAPI channel) {
        super(channel,"biome");
    }
    
    @Override public boolean imply(String id) {
        setExistingParameterValue("resource_name", Collections.singletonList(id));
        return super.imply(id);
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveBiome(this);
    }

    @Override public boolean isServer() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override public void successfullyParsed() {
        super.successfullyParsed();
        List<String> resourceName = (List<String>)getParameterAsList("biome_tag");
        List<String> displayeName = Collections.emptyList();
        String resourceMatcher = getParameterAsString("biome_tag_matcher");
        String displayMatcher = "EXACT";
        this.tagCtx = new ResourceContext(resourceName,displayeName,resourceMatcher,displayMatcher);
    }

    @Override public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"biome_rainfall","biome_tag","biome_temperature","display_name",
                    "rain_type","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
