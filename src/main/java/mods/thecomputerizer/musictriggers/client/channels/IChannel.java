package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.minecraft.util.SoundCategory;

public interface IChannel {
    String getChannelName();
    AudioPlayer getPlayer();
    void tickFast();
    void onSetSound(SoundCategory category, float volume);
    void onTrackStop(AudioTrackEndReason endReason);
    void initCache();
}
