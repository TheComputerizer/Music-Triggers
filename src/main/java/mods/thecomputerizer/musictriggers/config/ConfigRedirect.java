package mods.thecomputerizer.musictriggers.config;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.*;

public class ConfigRedirect {

    private File file;
    private ResourceLocation fileSource;
    public final Map<String,String> urlMap;
    public final Map<String, ResourceLocation> resourceLocationMap;

    public ConfigRedirect(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.urlMap = new HashMap<>();
        this.resourceLocationMap = new HashMap<>();
    }

    public ConfigRedirect(ResourceLocation fileSource) {
        this.fileSource = fileSource;
        this.urlMap = new HashMap<>();
        this.resourceLocationMap = new HashMap<>();
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

    private Reader makeReader() throws IOException {
        return Objects.nonNull(this.file) ? new FileReader(this.file) : new InputStreamReader(Minecraft.getMinecraft()
                        .getResourceManager().getResource(this.fileSource).getInputStream());
    }

    public void parse() {
        if(Objects.isNull(this.file) && Objects.isNull(this.fileSource)) return;
        try {
            BufferedReader br = new BufferedReader(makeReader());
            String line = br.readLine();
            while(Objects.nonNull(line)) {
                if(!line.contains("Format") && line.contains(" = ") && !line.contains(" == ")) {
                    urlMap.put(line.substring(0,line.indexOf('=')-1).trim(),line.substring(line.indexOf('=')+1).trim());
                } else if (!line.contains("Format") && line.contains("==")) {
                    String resource = line.substring(line.indexOf('=') + 2).trim();
                    try {
                        resourceLocationMap.put(line.substring(0,line.indexOf('=')-1), new ResourceLocation(resource));
                    } catch (Exception ignored) {
                        MusicTriggers.logExternally(Level.ERROR,"Resource location {} was invalid!", resource);
                    }
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    public ConfigRedirect(ByteBuf buf) {
        this.urlMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
        this.resourceLocationMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                buf1 -> new ResourceLocation(NetworkUtil.readString(buf1)));
    }
}
