package mods.thecomputerizer.musictriggers.config;


import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.*;

public class ConfigJukebox {

    public final Map<String, String> recordMap;
    private final boolean isServerSide;
    private final File file;
    private final ResourceLocation resource;
    private final IResourceManager manager;
    private Object channel;

    public ConfigJukebox(boolean isServerSide, File file, Object channel) {
        this.recordMap = new HashMap<>();
        this.isServerSide = isServerSide;
        if(this.isServerSide) this.file = file;
        else {
            boolean exists = file.exists();
            this.file = exists ? file : FileUtil.generateNestedFile(file,false);
            if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        }
        this.resource = null;
        this.manager = Minecraft.getMinecraft().getResourceManager();
        this.channel = channel;
        if(this.isServerSide) {
            try(BufferedReader reader = new BufferedReader(makeReader())) {
                parse(reader);
            } catch(Exception e) {
                Constants.MAIN_LOG.error("Channel[{}] - Failed to parse jukebox config!",this.channel,e);
            }
        }
    }

    public void setChannel(Object channel) {
        this.channel = channel;
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

    @SideOnly(Side.CLIENT)
    public ConfigJukebox(ResourceLocation resource, Object channel) {
        this.recordMap = new HashMap<>();
        this.isServerSide = false;
        this.file = null;
        this.resource = resource;
        this.manager = Minecraft.getMinecraft().getResourceManager();
        this.channel = channel;
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

    @SideOnly(Side.CLIENT)
    private Reader makeReader() throws IOException {
        return Objects.nonNull(this.file) ? new FileReader(this.file) : new InputStreamReader(this.manager
                .getResource(this.resource).getInputStream());
    }

    @SideOnly(Side.CLIENT)
    public void parse(Object channel) {
        if(this.isServerSide) return;
        if(Objects.isNull(this.channel)) this.channel = channel;
        try(BufferedReader reader = new BufferedReader(makeReader())) {
            parse(reader);
        } catch(Exception e) {
            Constants.MAIN_LOG.error("Channel[{}] - Failed to parse jukebox config!",this.channel,e);
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
        return AssetUtil.genericLang(Constants.MODID,"record",lang,false);
    }

    @SideOnly(Side.CLIENT)
    public ConfigJukebox(ByteBuf buf) {
        this.recordMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
        this.isServerSide = true;
        this.file = null;
        this.resource = null;
        this.manager = Minecraft.getMinecraft().getResourceManager();
        this.channel = null;
    }
}
