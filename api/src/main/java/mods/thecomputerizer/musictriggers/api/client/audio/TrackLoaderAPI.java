package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TrackLoaderAPI {

    private final AtomicInteger queue;

    protected TrackLoaderAPI() {
        this.queue = new AtomicInteger();
    }

    protected void decrementQueue() {
        this.queue.set(this.queue.get()-1);
    }

    protected abstract @Nullable AudioReference getResourceReference(ResourceLocationAPI<?> res, LoggableAPI logger);

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

    public void load(AudioPlayerManager manager, AudioRef audio, String location) {

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

    public void loadFile(AudioPlayerManager manager, AudioRef audio, String path) {
        File file = new File(path);
        if(file.exists()) load(manager,file.getPath(),file.getName(),getResultHandler(audio,path),audio);
        else audio.logError("Tried to load nonexistant file `{}` for audio `{}`!",path,audio.getName());
    }

    public void loadRemote(AudioPlayerManager manager, AudioRef audio, @Nullable String url) {
        if(Objects.nonNull(url)) load(manager,url,null,getResultHandler(audio,url),audio);
        else audio.logError("Tried to null URL for audio `{}`!",audio.getName());
    }

    public void loadResource(AudioPlayerManager manager, AudioRef audio, String location) {
        ResourceLocationAPI<?> res = ResourceHelper.getResource(location);
        if(Objects.nonNull(res)) load(manager,getResourceReference(res,audio),getResultHandler(audio,location),audio);
        else audio.logError("Tried to null resource for audio `{}`!",audio.getName());
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