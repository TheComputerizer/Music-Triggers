package mods.thecomputerizer.musictriggers.config;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.*;

public class ConfigRedirect {

    public final Map<String,String> urlMap;
    public final Map<String, ResourceLocation> resourceLocationMap;
    private final boolean isServerSide;
    private final File file;
    private final ResourceLocation resource;
    private final IResourceManager manager;
    private final String channelName;

    public ConfigRedirect(boolean isServerSide, File file, String channelName) {
        this.urlMap = new HashMap<>();
        this.resourceLocationMap = new HashMap<>();
        this.isServerSide = isServerSide;
        if(this.isServerSide) this.file = file;
        else {
            boolean exists = file.exists();
            this.file = exists ? file : FileUtil.generateNestedFile(file,false);
            if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        }
        this.resource = null;
        this.manager = Minecraft.getMinecraft().getResourceManager();
        this.channelName = channelName;
        if(this.isServerSide) {
            try(BufferedReader reader = new BufferedReader(makeReader())) {
                parse(reader);
            } catch(Exception e) {
                Constants.MAIN_LOG.error("Channel[{}] - Failed to parse jukebox config!",this.channelName,e);
            }
        }
    }

    private List<String> headerLines() {
        return Arrays.asList("Format this like name = url",
                "If you are trying to redirect to an already registered resource location Format it like name == location instead",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "Here are 2 examples:",
                "thx = https://youtu.be/z3Q4WBpCXhs",
                "title == minecraft:sounds/music/menu/menu1.ogg");
    }

    @SideOnly(Side.CLIENT)
    public ConfigRedirect(ResourceLocation resource, String channelName) {
        this.urlMap = new HashMap<>();
        this.resourceLocationMap = new HashMap<>();
        this.isServerSide = false;
        this.file = null;
        this.resource = resource;
        this.manager = Minecraft.getMinecraft().getResourceManager();
        this.channelName = channelName;
    }

    private void parse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while(Objects.nonNull(line)) {
            if(!line.contains("Format") && line.contains(" = ") && !line.contains(" == ")) {
                this.urlMap.put(line.substring(0,line.indexOf('=')-1).trim(),line.substring(line.indexOf('=')+1).trim());
            } else if (!line.contains("Format") && line.contains("==")) {
                String resource = line.substring(line.indexOf('=') + 2).trim();
                try {
                    this.resourceLocationMap.put(line.substring(0,line.indexOf('=')-1), new ResourceLocation(resource));
                } catch (Exception ignored) {
                    MusicTriggers.logExternally(Level.ERROR,"Channel[{}] - Resource location {} was invalid!",
                            this.channelName,resource);
                }
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
    public void parse() {
        if(this.isServerSide) return;
        try(BufferedReader reader = new BufferedReader(makeReader())) {
            parse(reader);
        } catch(Exception e) {
            Constants.MAIN_LOG.error("Channel[{}] - Failed to parse redirect config!",this.channelName,e);
        }
    }

    @SideOnly(Side.CLIENT)
    public ConfigRedirect(ByteBuf buf, String channelName) {
        this.urlMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
        this.resourceLocationMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> new ResourceLocation(NetworkUtil.readString(buf1)));
        this.isServerSide = true;
        this.file = null;
        this.resource = null;
        this.manager = Minecraft.getMinecraft().getResourceManager();
        this.channelName = channelName;
    }
}
