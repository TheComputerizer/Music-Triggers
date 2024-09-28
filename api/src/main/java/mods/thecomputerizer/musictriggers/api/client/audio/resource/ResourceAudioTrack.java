package mods.thecomputerizer.musictriggers.api.client.audio.resource;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import lombok.Getter;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;

public class ResourceAudioTrack extends DelegatedAudioTrack {

    private final ResourceLocationAPI<?> location;
    @Getter private final MediaContainerDescriptor trackFactory;
    private final ResourceAudioSourceManager manager;

    public ResourceAudioTrack(AudioTrackInfo info, MediaContainerDescriptor trackFactory,
                              ResourceAudioSourceManager manager) {
        super(info);
        this.location = ResourceHelper.getResource(info.identifier);
        this.trackFactory = trackFactory;
        this.manager = manager;
    }

    @Override public AudioSourceManager getSourceManager() {
        return manager;
    }

    @Override protected AudioTrack makeShallowClone() {
        return new ResourceAudioTrack(this.trackInfo,this.trackFactory,this.manager);
    }

    @Override public void process(LocalAudioTrackExecutor executor) throws Exception {
        try(ResourceSeekableInputStream stream = ResourceSeekableInputStream.get(this.manager.getChannel(),this.location)) {
            processDelegate((InternalAudioTrack)this.trackFactory.createTrack(this.trackInfo,stream),executor);
        }
    }
}
