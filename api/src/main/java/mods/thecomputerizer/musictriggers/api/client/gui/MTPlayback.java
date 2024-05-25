package mods.thecomputerizer.musictriggers.api.client.gui;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelClient;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelClientSpecial;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;

import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.GREEN;

@Getter
public class MTPlayback extends MTGUI {
    
    private ChannelAPI channel;
    
    public MTPlayback(ScreenAPI parent, MinecraftWindow window, int guiScale) {
        super(parent,"playback",window,guiScale);
        for(ChannelAPI channel : ChannelHelper.getClientHelper().getChannels().values()) {
            if(Objects.nonNull(channel) && channel.isClientChannel() && !(channel instanceof ChannelClientSpecial)) {
                this.channel = channel;
                break;
            }
        }
    }
    
    @Override public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        super.draw(ctx,center,mouseX,mouseY);
        if(Objects.nonNull(this.channel)) {
            RenderAPI renderer = ctx.getRenderer();
            double x = ctx.withScreenScaledX(0d);
            double y = ctx.withScreenScaledY(0.95d);
            renderer.drawCenteredString(ctx.getFont(),"Channel: "+this.channel.getName(),x,y,GREEN.getColorI());
            y = ctx.withScreenScaledY(0.75d+ctx.getScaledFontHeight()*1.5d);
            renderer.drawCenteredString(ctx.getFont(),"Song: "+this.channel.getPlayingSongName(),x,y,GREEN.getColorI());
            y = ctx.withScreenScaledY(-0.75d-ctx.getScaledFontHeight()*0.5d);
            renderer.drawCenteredString(ctx.getFont(),this.channel.getFormattedSongTime(),x,y,GREEN.getColorI());
        }
    }
    
    public void resetSong() {
        if(this.channel instanceof ChannelClient) {
            AudioTrack track = this.channel.getPlayer().getPlayingTrack();
            if(Objects.nonNull(track)) track.setPosition(0L);
        }
    }
    
    public void skipSong() {
        if(this.channel instanceof ChannelClient) this.channel.getPlayer().stopTrack();
    }
}