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
    private final File file;
    private final ResourceLocation resource;
    private final boolean isFromDataPack;

    public ConfigJukebox(File file) {
        this.resource = null;
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.recordMap = new HashMap<>();
        this.isFromDataPack = false;
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

    public ConfigJukebox(@Nullable ResourceManager manager, ResourceLocation resource, Object channel) throws IOException {
        this.file = null;
        this.resource = resource;
        this.recordMap = new HashMap<>();
        this.isFromDataPack = Objects.nonNull(manager);
        if(this.isFromDataPack) {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(manager.getResource(resource).getInputStream()))) {
                parse(reader,channel,true);
            }
        }
    }

    private void parse(BufferedReader reader, Object channel, boolean isDataPack) throws IOException {
        String line = reader.readLine();
        while(Objects.nonNull(line)) {
            if(!line.contains("Format") && line.contains("=") && !line.contains("==")) {
                String[] broken = MusicTriggers.stringBreaker(line,"=");
                tryAddTrack(channel,broken[0].trim(),broken[1].trim(),isDataPack);
            }
            line = reader.readLine();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private Reader makeReader() throws IOException {
        return Objects.nonNull(this.file) ? new FileReader(this.file) : new InputStreamReader(Minecraft.getInstance()
                .getResourceManager().getResource(this.resource).getInputStream());
    }

    @OnlyIn(Dist.CLIENT)
    public void parse(Channel channel) {
        if(this.isFromDataPack) return;
        try(BufferedReader reader = new BufferedReader(makeReader())) {
            parse(reader,channel,false);
        } catch(Exception e) {
            Constants.MAIN_LOG.error("Channel[{}] - Failed to parse jukebox config!",channel.getChannelName(),e);
        }
    }

    private void tryAddTrack(Object channel, String track, String lang, boolean isDataPack) {
        if(isDataPack || checkIsLoaded(channel,track)) {
            String hasTranslation = isDataPack ? "translation key" : "translation";
            if(!this.recordMap.containsKey(track)) {
                this.recordMap.put(track, lang);
                String logThis = isDataPack ? lang : getTranslation(lang);
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track with id {} is now playable by a " +
                        "custom record and has a {} of {}",channel,track,hasTranslation,logThis);
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Track with id {} has already been " +
                            "loaded to a custom record with a {} of {}!",channel,hasTranslation,
                    isDataPack ? this.recordMap.get(track) : getTranslation(this.recordMap.get(track)));
        } else MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Unable to load unknown track id {} to "+
                "a custom record!",channel,track);
    }

    /**
     * Client Only but the annotation is left off just in case it tries being loaded.
     */
    private boolean checkIsLoaded(Object channel, String track) {
        return ((Channel)channel).isTrackLoaded(track);
    }

    /**
     * Client Only but the annotation is left off just in case it tries being loaded.
     */
    private String getTranslation(String lang) {
        return AssetUtil.genericLang(Constants.MODID,"record",lang,false).getString();
    }

    @OnlyIn(Dist.CLIENT)
    public ConfigJukebox(FriendlyByteBuf buf) {
        this.file = null;
        this.resource = null;
        this.recordMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
        this.isFromDataPack = true;
    }
}
