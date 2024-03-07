package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class ImageElement extends CardAPI {

    public ImageElement(ChannelAPI channel) {
        super(channel);
    }

    @Override
    protected String getTypeName() {
        return "Image Card";
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"animated",new ParameterBoolean(false));
        addParameter(map,"fps",new ParameterInt(20));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return false;
    }
}