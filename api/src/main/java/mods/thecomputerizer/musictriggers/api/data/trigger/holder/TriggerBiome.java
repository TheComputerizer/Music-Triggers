package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class TriggerBiome extends HolderTrigger {

    private ResourceContext tagCtx;

    public TriggerBiome(ChannelAPI channel) {
        super(channel,"biome");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"biome_rainfall",new ParameterFloat(Float.MIN_VALUE));
        addParameter(map,"biome_tag",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"biome_tag_matcher",new ParameterString("EXACT"));
        addParameter(map,"biome_temperature",new ParameterFloat(Float.MIN_VALUE));
        addParameter(map,"display_matcher",new ParameterString("EXACT"));
        addParameter(map,"display_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"rain_type",new ParameterString("ANY"));
        addParameter(map,"rainfall_greater_than",new ParameterBoolean(true));
        addParameter(map,"resource_matcher",new ParameterString("PARTIAL"));
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"temperature_greater_than",new ParameterBoolean(true));
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveBiome(this);
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void successfullyParsed() {
        super.successfullyParsed();
        List<String> resourceName = (List<String>)getParameterAsList("biome_tag");
        List<String> displayeName = Collections.emptyList();
        String resourceMatcher = getParameterAsString("biome_tag_matcher");
        String displayMatcher = "EXACT";
        this.tagCtx = new ResourceContext(resourceName,displayeName,resourceMatcher,displayMatcher);
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"biome_rainfall","biome_tag","biome_temperature","display_name",
                    "rain_type","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
