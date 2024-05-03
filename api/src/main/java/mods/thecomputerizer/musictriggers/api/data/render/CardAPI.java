package mods.thecomputerizer.musictriggers.api.data.render;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventRunner;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public abstract class CardAPI extends ChannelEventRunner {

    private final List<TriggerAPI> triggers;

    protected CardAPI(ChannelAPI channel) {
        super(channel);
        this.triggers = new ArrayList<>();
    }

    @Override
    public void close() {
        this.triggers.clear();
    }

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return CardAPI.class;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"fade_in",new ParameterInt(20));
        addParameter(map,"fade_out",new ParameterInt(20));
        addParameter(map,"horizontal_alignment",new ParameterString("center"));
        addParameter(map,"opacity",new ParameterFloat(1f));
        addParameter(map,"play_once",new ParameterBoolean(false));
        addParameter(map,"scale_x",new ParameterFloat(1f));
        addParameter(map,"scale_y",new ParameterFloat(1f));
        addParameter(map,"time",new ParameterInt(100));
        addParameter(map,"vague",new ParameterBoolean(false));
        addParameter(map,"vertical_alignment",new ParameterString("center"));
        addParameter(map,"x",new ParameterInt(-1));
        addParameter(map,"y",new ParameterInt(-1));
    }

    @Override
    public boolean isResource() {
        return true;
    }

    public boolean parse(Toml table) {
        List<?> triggerRefs = table.getValueArray("triggers");
        if(!TriggerHelper.findTriggers(getChannel(),this.triggers,triggerRefs)) {
            logError("Failed to parse triggers for {}!",getTypeName());
            return false;
        }
        return parseParameters(table);
    }
}