package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceAPI;

public class ImageElement extends CardAPI {

    public ImageElement(ChannelAPI channel) {
        super(channel,"image_card");
    }
    
    @Override
    public String getName() {
        return getParameterAsString("name");
    }

    @Override
    protected String getSubTypeName() {
        return "Image_Card";
    }
    
    @Override public TableRef getReferenceData() {
        return MTDataRef.IMAGE_CARD;
    }
    
    @Override
    public void run() {
        super.run();
        ResourceAPI api = TILRef.getCommonSubAPI(CommonAPI::getResource);
        RenderHelper.addRenderable(RenderHelper.initPNG(api.getLocation(getParameterAsString("name")),asValueMap()));
    }
    
    @Override
    public boolean verifyRequiredParameters() {
        return hasParameter("name");
    }
}