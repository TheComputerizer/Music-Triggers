package mods.thecomputerizer.musictriggers.config;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class ConfigJukebox {

    public final Map<String, String> recordMap;
    private final boolean isServerSide;
    private final File file;
    private final ResourceLocation resource;
    private final ResourceManager manager;
    private Object channel;

    public ConfigJukebox(File file) {
        this.recordMap = new HashMap<>();
        this.isServerSide = false;
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.resource = null;
        this.manager = Minecraft.getInstance().getResourceManager();
    }

    private List<String> headerLines() {
        return Arrays.asList("Format this like name = key",
                "The key refers to a lang key in the format of record.musictriggers.key which ",
                "determines the description of the registered disc",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "Here is an example",
                "song1 = dragon");
    }

    public void setChannel(Object channel) {
        this.channel = channel;
    }

    public ConfigJukebox(boolean isServerSide, ResourceLocation resource, ResourceManager manager, @Nullable Object channel) throws IOException {
        this.recordMap = new HashMap<>();
        this.isServerSide = isServerSide;
        this.file = null;
        this.resource = resource;
        this.manager = manager;
        this.channel = channel;
        if(this.isServerSide) {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(this.manager.getResource(resource).getInputStream()))) {
                parse(reader);
            }
        }
    }

    private void parse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while(Objects.nonNull(line)) {
            if(!line.contains("Format") && line.contains("=") && !line.contains("==")) {
                String[] broken = MusicTriggers.stringBreaker(line,"=");
                tryAddTrack(broken[0].trim(),broken[1].trim());
            }
            line = reader.readLine();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private Reader makeReader() throws IOException {
        return Objects.nonNull(this.file) ? new FileReader(this.file) :
                new InputStreamReader(this.manager.getResource(this.resource).getInputStream());
    }

    @OnlyIn(Dist.CLIENT)
    public void parse(Channel channel) {
        if(this.isServerSide) return;
        try(BufferedReader reader = new BufferedReader(makeReader())) {
            parse(reader);
        } catch(Exception e) {
            Constants.MAIN_LOG.error("Channel[{}] - Failed to parse jukebox config!",channel.getChannelName(),e);
        }
    }

    private void tryAddTrack(String track, String lang) {
        if(this.isServerSide || checkIsLoaded(track)) {
            String hasTranslation = this.isServerSide ? "translation key" : "translation";
            if(!this.recordMap.containsKey(track)) {
                this.recordMap.put(track, lang);
                String logThis = this.isServerSide ? lang : getTranslation(lang);
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track with id {} is now playable by a " +
                        "custom record and has a {} of {}",this.channel,track,hasTranslation,logThis);
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Track with id {} has already been " +
                            "loaded to a custom record with a {} of {}!",this.channel,hasTranslation,
                    this.isServerSide ? this.recordMap.get(track) : getTranslation(this.recordMap.get(track)));
        } else MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Unable to load unknown track id {} to "+
                "a custom record!",this.channel,track);
    }

    /**
     * Client Only but the annotation is left off just in case it tries being loaded.
     */
    private boolean checkIsLoaded(String track) {
        return ((Channel)this.channel).isTrackLoaded(track);
    }

    /**
     * Client Only but the annotation is left off just in case it tries being loaded.
     */
    private String getTranslation(String lang) {
        return AssetUtil.genericLang(Constants.MODID,"record",lang,false).getString();
    }

    @OnlyIn(Dist.CLIENT)
    public ConfigJukebox(FriendlyByteBuf buf) {
        this.recordMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
        this.isServerSide = true;
        this.file = null;
        this.resource = null;
        this.manager = null;
    }
}
