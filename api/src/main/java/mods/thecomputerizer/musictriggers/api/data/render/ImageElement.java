package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceAPI;

import java.util.Map;

public class ImageElement extends CardAPI {

    public ImageElement(ChannelAPI channel) {
        super(channel);
    }

    @Override
    protected void run() {
        ResourceAPI api = TILRef.getCommonSubAPI("ResourceAPI",CommonAPI::getResource);
        RenderHelper.addRenderable(RenderHelper.initPNG(api.getLocation(getParameterAsString("name")),asValueMap()));
    }

    @Override
    protected String getTypeName() {
        return "Image Card";
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"animated",new ParameterBoolean(false));
        addParameter(map,"fps",new ParameterInt(20));
        addParameter(map,"name",new ParameterString(""));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return hasParameter("name");
    }
}