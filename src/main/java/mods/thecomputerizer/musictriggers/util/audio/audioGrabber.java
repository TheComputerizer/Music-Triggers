package mods.thecomputerizer.musictriggers.util.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import javax.sound.sampled.AudioInputStream;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;

public class audioGrabber {

    public static void dl(String url) {
        try {
            AudioPlayerManager manager = new DefaultAudioPlayerManager();
            AudioSourceManagers.registerRemoteSources(manager);
            manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);

            AudioPlayer player = manager.createPlayer();

            manager.loadItem(url, new FunctionalResultHandler(null, playlist -> player.playTrack(playlist.getTracks().get(0)), null, null));

            AudioDataFormat format = manager.getConfiguration().getOutputFormat();
            AudioInputStream stream = AudioPlayerInputStream.createStream(player, format, 10000L, false);
            VorbisEncoder.encode(stream, "./config/MusicTriggers/songs/assets/musictriggers/sounds/music/output.ogg");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
