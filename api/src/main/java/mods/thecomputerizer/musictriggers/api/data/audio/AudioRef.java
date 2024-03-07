package mods.thecomputerizer.musictriggers.api.data.audio;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class AudioRef extends ParameterWrapper {

    private final String name;

    protected AudioRef(ChannelAPI channel, String name) {
        super(channel);
        this.name = name;
    }

    @Override
    protected String getTypeName() {
        return "Audio `"+getName()+"`";
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"chance",new ParameterInt(100));
        addParameter(map,"must_finish",new ParameterBoolean(false));
        addParameter(map,"pitch",new ParameterFloat(1f));
        addParameter(map,"play_once",new ParameterInt(0));
        addParameter(map,"play_x",new ParameterInt(1));
        addParameter(map,"resume_on_play",new ParameterBoolean(false));
        addParameter(map,"start_at",new ParameterInt(0));
        addParameter(map,"volume",new ParameterFloat(1f));
        initExtraParameters(map);
        return map;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {

    }

    public boolean parse(Table table) {
        return parseParameters(table);
    }

    @Override
    public boolean verifyRequiredParameters() {
        return false;
    }
}