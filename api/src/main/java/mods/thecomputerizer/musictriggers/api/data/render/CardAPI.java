package mods.thecomputerizer.musictriggers.api.data.render;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class CardAPI extends ParameterWrapper {

    private final List<TriggerAPI> triggers;

    protected CardAPI(ChannelAPI channel) {
        super(channel);
        this.triggers = new ArrayList<>();
    }

    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
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
        initExtraParameters(map);
        return map;
    }

    public boolean parse(Table table) {
        List<String> triggerRefs = table.getValOrDefault("triggers",new ArrayList<>());
        if(!TriggerHelper.findTriggers(getChannel(),this.triggers,triggerRefs)) {
            logError("Failed to parse {}!",getTypeName());
            return false;
        }
        return parseParameters(table);
    }
}