package mods.thecomputerizer.musictriggers.fabric.v16.m5.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 Replaces the vanilla MusicTicker with a noninvasive override that can be turned off when a channel is playing audio
 */
public class MTMusicTickerFabric1_16_5 extends MusicManager {
    
    public MTMusicTickerFabric1_16_5(Minecraft mc) {
        super(mc);
    }
    
    @Override public void tick() {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.isNull(helper) || helper.canVanillaMusicPlay()) super.tick();
    }
    
    @Override public void startPlaying(@Nonnull Music selector) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.isNull(helper) || helper.canVanillaMusicPlay()) super.startPlaying(selector);
    }
}
