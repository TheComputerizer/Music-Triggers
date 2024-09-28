package mods.thecomputerizer.musictriggers.api.client.audio.resource;

import com.sedmelluq.discord.lavaplayer.container.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

@Getter
public class ResourceAudioSourceManager extends ProbingAudioSourceManager {

    private final ChannelAPI channel;

    public ResourceAudioSourceManager(ChannelAPI channel) {
        this(MediaContainerRegistry.DEFAULT_REGISTRY,channel);
    }

    public ResourceAudioSourceManager(MediaContainerRegistry registry, ChannelAPI channel) {
        super(registry);
        this.channel = channel;
    }

    @Override protected AudioTrack createTrack(AudioTrackInfo info, MediaContainerDescriptor trackFactory) {
        return new ResourceAudioTrack(info,trackFactory,this);
    }

    @Override public AudioTrack decodeTrack(AudioTrackInfo info, DataInput input) throws IOException {
        MediaContainerDescriptor trackFactory = decodeTrackFactory(input);
        return Objects.nonNull(trackFactory) ? new ResourceAudioTrack(info,trackFactory,this) : null;
    }

    private MediaContainerDetectionResult detectContainerForResource(
            AudioReference reference, ResourceLocationAPI<?> location) {
        try(ResourceSeekableInputStream stream = ResourceSeekableInputStream.get(this.channel,location)) {
            int dotIndex = location.getPath().lastIndexOf('.');
            String ext = dotIndex>=0 ? location.getPath().substring(dotIndex+1) : null;
            return new MediaContainerDetection(this.containerRegistry,reference,stream,
                    MediaContainerHints.from(null,ext)).detectContainer();
        } catch(IOException ex) {
            throw new FriendlyException("Failed to open file for reading.",SUSPICIOUS,ex);
        }
    }

    @Override public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        encodeTrackFactory(((ResourceAudioTrack)track).getTrackFactory(),output);
    }

    @Override public String getSourceName() {
        return "resource";
    }

    @Override public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        ResourceLocationAPI<?> location = ResourceHelper.getResource(reference.identifier);
        return Objects.nonNull(location) ? handleLoadResult(detectContainerForResource(reference,location)) : null;
    }

    @Override public void shutdown() {}
}
