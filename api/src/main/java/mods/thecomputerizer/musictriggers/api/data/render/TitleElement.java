package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderableText;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.TITLE_CARD;

public class TitleElement extends CardAPI {

    public TitleElement(ChannelAPI channel) {
        super(channel,"title_card");
    }
    
    @Override protected String getSubTypeName() {
        return "Title_Card";
    }
    
    @Override public TableRef getReferenceData() {
        return TITLE_CARD;
    }
    
    @Override public void run() {
        super.run();
        RenderHelper.addRenderable(new RenderableText(asValueMap()));
    }

    @Override public boolean verifyRequiredParameters() {
        return hasParameter("titles");
    }
}
