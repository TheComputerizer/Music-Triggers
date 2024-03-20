package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TrackLoader {

    private final AtomicInteger queue;

    public TrackLoader() {
        this.queue = new AtomicInteger();
    }

    protected void decrementQueue() {
        this.queue.set(this.queue.get()-1);
    }

    protected AudioLoadResultHandler getResultHandler(AudioRef ref, String location) {
        return new FunctionalResultHandler(track -> itemLoaded(ref,track,"track",location),
                playlist -> itemLoaded(ref,playlist,"playlist",location),() -> noMatches(ref,location),
                ex -> loadFailed(ex,ref,location));
    }

    protected void incrementQueue() {
        this.queue.set(this.queue.get()+1);
    }

    public boolean isQueued() {
        return this.queue.get()>0;
    }

    public void load(AudioPlayerManager manager, @Nullable AudioReference ref,
                        @Nullable AudioLoadResultHandler resultHandler, LoggableAPI logger) {
        if(Objects.nonNull(ref) && Objects.nonNull(resultHandler)) {
            incrementQueue();
            manager.loadItem(ref,resultHandler);
        }
        else logger.logError("Unable to load missing audio reference or load handler!");
    }

    public void load(AudioPlayerManager manager, @Nullable String id, @Nullable String title,
                        @Nullable AudioLoadResultHandler resultHandler, LoggableAPI logger) {
        load(manager,new AudioReference(id,title),resultHandler,logger);
    }

    public void loadLocal(AudioPlayerManager manager, AudioRef audio, @Nullable String path) {
        File file = Objects.nonNull(path) ? new File(path) : null;
        if(Objects.nonNull(path) && file.exists() && file.canRead())
            load(manager,file.getPath(),file.getName(),getResultHandler(audio,path),audio);
        else audio.logError("Tried to load nonexistant or unreadable file `{}` for audio `{}`!",path,audio.getName());
    }

    /**
     * Also works for resources
     */
    public void loadRemote(AudioPlayerManager manager, AudioRef audio, @Nullable String location) {
        if(Objects.nonNull(location)) load(manager,location,null,getResultHandler(audio,location),audio);
        else audio.logError("Tried to null remote location for audio `{}`!",audio.getName());
    }

    protected void loadFailed(FriendlyException ex, AudioRef audio, String location) {
        audio.logError("There was an error trying to load `{}` for audio `{}`!",audio.getName(),location,ex);
        decrementQueue();
    }

    protected void noMatches(AudioRef audio, String location) {
        audio.logError("No matches from `{}` were found for audio `{}`.",audio.getName(),location);
        decrementQueue();
    }

    protected void itemLoaded(AudioRef audio, AudioItem item, String type, String location) {
        audio.logInfo("Successfully loaded {} from `{}` for audio `{}`.",type,location,audio.getName());
        audio.setItem(item);
        decrementQueue();
    }
}