package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

public class TrackLoader extends ChannelElement {
    
    public TrackLoader(ChannelAPI channel) {
        super(channel,"");
    }
    
    protected AudioLoadResultHandler getResultHandler(AudioRef ref, String location) {
        return new FunctionalResultHandler(track -> itemLoaded(ref,track,"track",location),
                playlist -> itemLoaded(ref,playlist,"playlist",location),() -> noMatches(ref,location),
                ex -> loadFailed(ex,ref,location));
    }

    public void load(AudioPlayerManager manager, @Nullable AudioReference ref,
                        @Nullable AudioLoadResultHandler resultHandler, LoggableAPI logger) {
        if(Objects.nonNull(ref) && Objects.nonNull(resultHandler)) {
            manager.loadItem(ref,resultHandler);
        }
        else logger.logError("Unable to load missing reference or handler!");
    }

    public void load(AudioPlayerManager manager, @Nullable String id, @Nullable String title,
                        @Nullable AudioLoadResultHandler resultHandler, LoggableAPI logger) {
        load(manager,new AudioReference(id,title),resultHandler,logger);
    }

    public void loadLocal(AudioPlayerManager manager, AudioRef audio, File dir, @Nullable String path) {
        File file = Objects.nonNull(path) ? new File(dir,path) : null;
        if(Objects.nonNull(file) && file.exists()) {
            audio.setLoading();
            load(manager,file.getPath(),file.getName(),getResultHandler(audio,path),audio);
        }
        else audio.logError("Tried to load nonexistant or unreadable file {}!",path);
    }

    /**
     * Also works for resources
     */
    public void loadRemote(AudioPlayerManager manager, AudioRef audio, @Nullable String location) {
        if(Objects.nonNull(location)) {
            audio.setLoading();
            load(manager,location,null,getResultHandler(audio,location),audio);
        }
        else audio.logError("Tried to add null remote location!");
    }

    protected void loadFailed(FriendlyException ex, AudioRef audio, String location) {
        audio.logError("There was an error trying to load {}!",location,ex);
        audio.setItem(null);
    }

    protected void noMatches(AudioRef audio, String location) {
        audio.logError("No matches from {} were found",location);
        audio.setItem(null);
    }

    protected void itemLoaded(AudioRef audio, AudioItem item, String type, String location) {
        audio.logInfo("Successfully loaded {} from {}",type,location);
        audio.setItem(item);
    }
    
    @Override protected String getSubTypeName() {
        return "TrackLoader";
    }
    
    @Override public boolean isResource() {
        return false;
    }
    
    @Override public void close() {}
    
    @Override public TableRef getReferenceData() {
        return null;
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return TrackLoader.class;
    }
}