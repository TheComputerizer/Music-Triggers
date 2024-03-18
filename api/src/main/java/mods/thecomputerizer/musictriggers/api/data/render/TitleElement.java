package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderableText;

import java.util.ArrayList;
import java.util.Map;

public class TitleElement extends CardAPI {

    public TitleElement(ChannelAPI channel) {
        super(channel);
    }

    @Override
    protected void run() {
        RenderHelper.addRenderable(new RenderableText(asValueMap()));
    }

    @Override
    protected String getTypeName() {
        return "Title Card";
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"subtitle_color",new ParameterString("white"));
        addParameter(map,"subtitle_scale",new ParameterFloat(0.75f));
        addParameter(map,"subtitles",new ParameterList<>(String.class,new ArrayList<>()));
        addParameter(map,"title_color",new ParameterString("red"));
        addParameter(map,"titles",new ParameterList<>(String.class,new ArrayList<>()));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return hasParameter("titles");
    }
}
