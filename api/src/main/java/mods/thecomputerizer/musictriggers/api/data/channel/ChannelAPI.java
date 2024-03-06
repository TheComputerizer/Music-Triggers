package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper.ModLogger;

public abstract class ChannelAPI {

    public static ModLogger LOGGER = LogHelper.create(MTRef.MODID);

    public abstract String getName();
    public abstract AudioPlayer getPlayer();
    public abstract boolean isEnabled();
    public abstract void onTrackStop(AudioTrackEndReason endReason);
    public abstract void tickFast();
    public abstract void tickSlow();
}