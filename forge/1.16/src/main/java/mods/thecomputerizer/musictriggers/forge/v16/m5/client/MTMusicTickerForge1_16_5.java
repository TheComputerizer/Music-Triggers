package mods.thecomputerizer.musictriggers.forge.v16.m5.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.BackgroundMusicSelector;
import net.minecraft.client.audio.MusicTicker;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 Replaces the vanilla MusicTicker with a noninvasive override that can be turned off when a channel is playing audio
 */
public class MTMusicTickerForge1_16_5 extends MusicTicker {
    
    public MTMusicTickerForge1_16_5(Minecraft mc) {
        super(mc);
    }
    
    @Override public void tick() {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.isNull(helper) || helper.canVanillaMusicPlay()) super.tick();
    }
    
    @Override public void startPlaying(@Nonnull BackgroundMusicSelector selector) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.isNull(helper) || helper.canVanillaMusicPlay()) super.startPlaying(selector);
    }
}
