package mods.thecomputerizer.musictriggers.legacy.v12.m2.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 Replaces the vanilla MusicTicker with a noninvasive override that can be turned off when a channel is playing audio
 */
public class MTMusicTicker1_12_2 extends MusicTicker {
    
    public MTMusicTicker1_12_2(Minecraft mc) {
        super(mc);
    }
    
    @Override
    public void update() {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.isNull(helper) || helper.canVanillaMusicPlay()) super.update();
    }
    
    @Override
    public void playMusic(@Nonnull MusicType requestedType) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.isNull(helper) || helper.canVanillaMusicPlay()) super.playMusic(requestedType);
    }
}
