package mods.thecomputerizer.musictriggers.api.client.gui;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;

import javax.annotation.Nullable;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;

@Getter
public class MTScreenInfo {
    
    public static MTScreenInfo get(String type) {
        return new MTScreenInfo(null,type);
    }
    
    public static MTScreenInfo get(@Nullable MTScreenInfo parent, String type) {
        return new MTScreenInfo(parent,type);
    }
    
    private final MTScreenInfo parent;
    private final String type;
    @Setter private DataLink link;
    private ChannelAPI channel;
    
    public MTScreenInfo(@Nullable MTScreenInfo parent, String type) {
        this.parent = parent;
        this.type = type;
        if(Objects.nonNull(this.parent)) this.channel = parent.channel;
    }
    
    public @Nullable DataLink findChannelLink() {
        if(Objects.isNull(this.channel)) return null;
        switch(this.type) {
            case "channel_info": return this.channel.getInfo().getLink(this);
            case "commands": return this.channel.getCommandsLink(this);
            case "jukebox": return this.channel.getRecordsLink(this);
            case "main": return this.channel.getMainLink(this);
            case "redirect": return this.channel.getRedirectLink(this);
            case "renders": return this.channel.getRendersLink(this);
            default: return null;
        }
    }
    
    public TextAPI<?> getDisplayName() {
        return TextHelper.getTranslated(String.format("gui.%1$s.screen.%2$s",MODID,this.type));
    }
    
    public ResourceLocationAPI<?> getIconTexture(boolean hover) {
        return getIconTexture(this.type,hover);
    }
    
    public ResourceLocationAPI<?> getIconTexture(String type, boolean hover) {
        return MTRef.res(String.format("textures/gui%1$sicon/%2$s.png", hover ? "/hover/" : "/",type));
    }
    
    public boolean is(String type) {
        return this.type.equals(type);
    }
    
    public MTScreenInfo next(String type) {
        return get(this,type);
    }
    
    public void setChannel(ChannelAPI channel, boolean force) {
        if(force || Objects.isNull(this.channel)) {
            this.channel = channel;
            if(Objects.nonNull(this.parent)) this.parent.setChannel(channel,force);
        }
    }
}
