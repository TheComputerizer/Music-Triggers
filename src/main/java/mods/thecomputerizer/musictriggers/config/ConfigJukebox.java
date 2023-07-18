package mods.thecomputerizer.musictriggers.config;


import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.*;

public class ConfigJukebox {

    private File file;
    private ResourceLocation fileSource;
    public final Map<String, String> recordMap;

    public ConfigJukebox(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.recordMap = new HashMap<>();
    }

    public ConfigJukebox(ResourceLocation fileSource) {
        this.fileSource = fileSource;
        this.recordMap = new HashMap<>();
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
    private Reader makeReader() throws IOException {
        return Objects.nonNull(this.file) ? new FileReader(this.file) : new InputStreamReader(Minecraft.getMinecraft()
                .getResourceManager().getResource(this.fileSource).getInputStream());
    }

    @SideOnly(Side.CLIENT)
    public void parse(Channel channel) {
        if(Objects.isNull(this.file) && Objects.isNull(this.fileSource)) return;
        try {
            BufferedReader br = new BufferedReader(makeReader());
            String line = br.readLine();
            while(Objects.nonNull(line)) {
                if(!line.contains("Format") && line.contains("=") && !line.contains("==")) {
                    String[] broken = MusicTriggers.stringBreaker(line,"=");
                    tryAddTrack(channel,broken[0].trim(),broken[1].trim());
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    private void tryAddTrack(Channel channel, String track, String lang) {
        if(channel.isTrackLoaded(track)) {
            if(!this.recordMap.containsKey(track)) {
                this.recordMap.put(track, lang);
                MusicTriggers.logExternally(Level.INFO, "Channel[{}] - Track with id {} is now playable by a " +
                        "custom record and has a translation of {}",channel.getChannelName(),track,
                        AssetUtil.genericLang(Constants.MODID,"record",lang,false));
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Track with id {} has already been " +
                            "loaded to a custom record with a translation of {}!",channel.getChannelName(),
                    AssetUtil.genericLang(Constants.MODID,"record",this.recordMap.get(track),false));
        } else MusicTriggers.logExternally(Level.ERROR, "Channel[{}] - Unable to load unknown track id {} to "+
                "a custom record!",channel.getChannelName(),track);
    }

    @SideOnly(Side.CLIENT)
    public ConfigJukebox(ByteBuf buf) {
        this.recordMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
    }
}
