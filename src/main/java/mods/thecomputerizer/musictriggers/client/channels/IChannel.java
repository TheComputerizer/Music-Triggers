package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.minecraft.sounds.SoundSource;

public interface IChannel {

    String getChannelName();
    AudioPlayer getPlayer();
    void tickFast();
    void onSetSound(SoundSource category, float volume);
    void onTrackStop(AudioTrackEndReason endReason);
    void initCache();
}
